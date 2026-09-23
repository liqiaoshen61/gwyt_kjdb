package com.jwch.gwyt_project.util

import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.huawei.hms.location.*
import com.jwch.gwyt_project.ext.printMsg

/**
 * 华为定位服务工具类
 * 使用HMS Core Location SDK实现定位功能
 */
class HuaweiLocationService(private val context: Context) {

    companion object {
        private const val TAG = "HuaweiLocationService"
        // 单次/持续首点超时（冷启动 TTFF 30-60s）
        private const val SINGLE_LOCATION_TIMEOUT = 30_000L
        private const val CONTINUOUS_FIRST_TIMEOUT = 30_000L
        // 单次定位"把 lastLocation 当当前点"的最长时限：仅当最后一帧是 30s 内拿到的才直接复用，
        // 否则（高铁上拿到的常是几分钟前的旧点）不当作"当前成功"，继续请求新位置。
        private const val SINGLE_LASTKNOWN_MAX_AGE = 30_000L
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var settingsClient: SettingsClient? = null
    private var locationCallback: LocationCallback? = null

    // 定位结果回调
    private var locationResultCallback: ((LocationResult, Boolean) -> Unit)? = null

    // 简化的回调（省、市、区、地址、纬度、经度、是否首次定位成功）
    private var simpleContinuousCallback: ((String, String, String, String, Double, Double, Boolean) -> Unit)? = null

    // 简化的回调（单次定位用，省、市、区、地址、纬度、经度）
    private var simpleSingleCallback: ((String, String, String, String, Double, Double) -> Unit)? = null

    // 单次定位失败回调
    private var singleFailureCallback: ((LocationFailReason) -> Unit)? = null
    // 持续定位首点超时回调（交上层决定 fallback）
    private var continuousFirstTimeoutCallback: (() -> Unit)? = null
    // 当前是否处于单次定位模式（决定 handleLocationResult 回调 single 还是 continuous）
    private var isSingleMode = false

    // 是否是首次定位成功的标志位
    private var isFirstContinuousLocationSuccess = true

    // 单次定位超时处理
    private var singleLocationHandler: Handler? = null
    private var singleLocationTimeoutRunnable: Runnable? = null
    private var isSingleLocationPending = false
    // 持续定位首点超时
    private var continuousTimeoutRunnable: Runnable? = null

    init {
        initLocationClient()
    }

    /**
     * 初始化定位客户端
     */
    private fun initLocationClient() {
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            settingsClient = LocationServices.getSettingsClient(context)
            "华为定位服务初始化成功".printMsg()
        } catch (e: Exception) {
            Log.e(TAG, "华为定位服务初始化失败: ${e.message}")
        }
    }

    /**
     * 创建LocationRequest（使用兼容API）
     */
    private fun createLocationRequest(interval: Long, isContinuous: Boolean): LocationRequest {
        return LocationRequest().apply {
            // 设置优先级
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            // 设置定位间隔
            this.interval = interval
            // 设置最小位移
            smallestDisplacement = 0f
            // 设置最快定位间隔
            fastestInterval = interval
        }
    }

    /**
     * 检查设备定位设置
     */
    fun checkLocationSettings(callback: (Boolean) -> Unit) {

        val locationRequest = createLocationRequest(5000L, false)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        settingsClient?.checkLocationSettings(builder.build())
            ?.addOnSuccessListener {
                Log.d(TAG, "定位设置检查成功")
                callback(true)
            }
            ?.addOnFailureListener { e ->
                Log.e(TAG, "定位设置检查失败: ${e.message}")
                callback(false)
            }
    }

    /**
     * 单次定位（双回调，供 UnifiedLocationManager fallback 调度）
     * @param onSuccess 成功回调（省、市、区、地址、纬度、经度）
     * @param onFailure 失败回调（定位失败原因）
     */
    fun requestSingleLocation(
        onSuccess: (String, String, String, String, Double, Double) -> Unit,
        onFailure: (LocationFailReason) -> Unit,
        useLastKnownLocation: Boolean = true
    ) {
        simpleSingleCallback = onSuccess
        singleFailureCallback = onFailure
        isSingleLocationPending = true
        isSingleMode = true

        // HMS Core 不可用，直接失败，交上层 fallback
        if (!isHmsAvailable()) {
            "华为定位不可用(HMS)，通知上层切换".printMsg()
            onSingleLocationFailed(LocationFailReason.HMS_UNAVAILABLE)
            return
        }

        // 测试首示速：不走 lastLocation 缓存，强制请求新位置
        if (!useLastKnownLocation) {
            requestNewLocation(isContinuous = false)
            return
        }

        try {
            fusedLocationProviderClient?.lastLocation
                ?.addOnSuccessListener { location ->
                    // 仅当 lastLocation 是"30s 内刚拿到"的才当当前点直接返回；
                    // 更久的旧点（高铁上超过几分钟）不作数，走实时定位拿新点
                    if (location != null &&
                        (System.currentTimeMillis() - location.time) <= SINGLE_LASTKNOWN_MAX_AGE
                    ) {
                        cancelSingleLocationTimeout()
                        isSingleLocationPending = false
                        handleLocationResult(location)
                    } else {
                        // 缓存位置为空或过旧，请求新位置
                        requestNewLocation(isContinuous = false)
                    }
                }
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "获取最后位置失败: ${e.message}")
                    requestNewLocation(isContinuous = false)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "定位权限不足: ${e.message}")
            onSingleLocationFailed(LocationFailReason.PERMISSION_DENIED)
        } catch (e: Exception) {
            Log.e(TAG, "单次定位请求失败: ${e.message}")
            onSingleLocationFailed(LocationFailReason.PROVIDER_FAILED)
        }
    }

    /**
     * 单次定位失败处理
     */
    private fun onSingleLocationFailed(reason: LocationFailReason) {
        cancelSingleLocationTimeout()
        isSingleLocationPending = false
        stopLocationUpdates()
        "华为单次定位失败 reason=${reason.msg}".printMsg()
        val cb = singleFailureCallback
        singleFailureCallback = null
        simpleSingleCallback = null
        cb?.invoke(reason)
    }

    /**
     * 启动单次定位超时检测（30s，冷启动 TTFF 30-60s）
     */
    private fun startSingleLocationTimeout() {
        if (singleLocationHandler == null) {
            singleLocationHandler = Handler(Looper.getMainLooper())
        }
        cancelSingleLocationTimeout()
        singleLocationTimeoutRunnable = Runnable {
            if (isSingleLocationPending) {
                "华为单次定位超时".printMsg()
                onSingleLocationFailed(LocationFailReason.TTFF_TIMEOUT)
            }
        }
        singleLocationHandler?.postDelayed(singleLocationTimeoutRunnable!!, SINGLE_LOCATION_TIMEOUT) // 30秒超时
    }

    /**
     * 请求新位置
     */
    private fun requestNewLocation(isContinuous: Boolean) {
        // 单次定位时启动超时检测
        if (!isContinuous) {
            startSingleLocationTimeout()
        }

        val interval = if (isContinuous) 5000L else 1000L
        val locationRequest = createLocationRequest(interval, isContinuous)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result?.lastLocation?.let { location ->
                    if (!isContinuous) {
                        cancelSingleLocationTimeout()
                        isSingleLocationPending = false
                    }
                    handleLocationResult(location)
                    if (!isContinuous) {
                        stopLocationUpdates()
                    }
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability?) {
                Log.d(TAG, "定位可用性: ${availability?.isLocationAvailable}")
                // 单次定位时，如果定位不可用，触发失败
                if (!isContinuous && availability?.isLocationAvailable == false) {
                    "华为定位不可用".printMsg()
                    // 不立即失败，给一些时间让定位恢复
                }
            }
        }

        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "定位权限不足: ${e.message}")
            if (!isContinuous) {
                onSingleLocationFailed(LocationFailReason.PERMISSION_DENIED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "请求定位更新失败: ${e.message}")
            if (!isContinuous) {
                onSingleLocationFailed(LocationFailReason.PROVIDER_FAILED)
            }
        }
    }

    /**
     * 启动持续定位
     * @param callback 定位结果回调（包含是否首次定位成功的标志）
     */
    fun startContinuousLocation(callback: (LocationResult, Boolean) -> Unit) {
        locationResultCallback = callback
        isFirstContinuousLocationSuccess = true

        val locationRequest = createLocationRequest(1000L, true) // 持续定位 1 秒一格

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result?.let {
                    locationResultCallback?.invoke(it, isFirstContinuousLocationSuccess)
                    isFirstContinuousLocationSuccess = false
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability?) {
                Log.d(TAG, "定位可用性: ${availability?.isLocationAvailable}")
            }
        }

        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            "华为持续定位已启动".printMsg()
        } catch (e: SecurityException) {
            Log.e(TAG, "定位权限不足: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "启动持续定位失败: ${e.message}")
        }
    }

    /**
     * 启动持续定位（简化回调）
     * @param onFirstTimeout 首点等待超时回调，交上层决定是否 fallback
     */
    fun startContinuousLocationSimple(
        callback: (String, String, String, String, Double, Double, Boolean) -> Unit,
        onFirstTimeout: () -> Unit = {}
    ) {
        simpleContinuousCallback = callback
        continuousFirstTimeoutCallback = onFirstTimeout
        isFirstContinuousLocationSuccess = true
        isSingleMode = false

        val locationRequest = createLocationRequest(1000L, true).apply { // 持续定位 1 秒一格
            // 设置最小位移为0，确保即使静止也能收到回调
            smallestDisplacement = 0f
            // 设置最快定位间隔为1秒
            fastestInterval = 1000L
            // maxWaitTime 与 interval 同值(1000ms)：不再攒批，让每个定位点及时回调，避免点位 3 秒才动一次
            // （需 >= fastestInterval，否则 HMS 会抛异常）
            setMaxWaitTime(1000L)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                "华为持续定位收到回调".printMsg()
                result?.lastLocation?.let { location ->
                    // 首点到达，取消首点超时
                    cancelContinuousTimeout()
                    handleLocationResult(location)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability?) {
                "华为定位可用性: ${availability?.isLocationAvailable}".printMsg()
            }
        }

        // 首点等待超时（30s）：通知上层 fallback
        startContinuousTimeout()

        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )?.addOnSuccessListener {
                "华为持续定位请求成功，等待定位回调...".printMsg()
            }?.addOnFailureListener { e ->
                "华为持续定位请求失败: ${e.message}".printMsg()
                Log.e(TAG, "启动持续定位失败: ${e.message}")
            }
        } catch (e: SecurityException) {
            "华为定位权限不足: ${e.message}".printMsg()
            Log.e(TAG, "定位权限不足: ${e.message}")
        } catch (e: Exception) {
            "华为定位异常: ${e.message}".printMsg()
            Log.e(TAG, "启动持续定位失败: ${e.message}")
        }
    }

    /**
     * 启动持续定位首点超时
     */
    private fun startContinuousTimeout() {
        if (singleLocationHandler == null) {
            singleLocationHandler = Handler(Looper.getMainLooper())
        }
        cancelContinuousTimeout()
        continuousTimeoutRunnable = Runnable {
            // 首点已到则不打扰
            if (!isFirstContinuousLocationSuccess) return@Runnable
            "华为持续定位首点超时，通知上层切换".printMsg()
            val cb = continuousFirstTimeoutCallback
            stopLocationUpdates()
            cb?.invoke()
        }
        singleLocationHandler?.postDelayed(continuousTimeoutRunnable!!, CONTINUOUS_FIRST_TIMEOUT)
    }

    private fun cancelContinuousTimeout() {
        continuousTimeoutRunnable?.let { singleLocationHandler?.removeCallbacks(it) }
        continuousTimeoutRunnable = null
    }

    /**
     * 处理定位结果
     * 单次模式回调 simpleSingleCallback（6参）；持续模式回调 simpleContinuousCallback（7参带 isFirstSuccess）
     */
    private fun handleLocationResult(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val accuracy = location.accuracy

        "华为定位结果: 纬度=$latitude, 经度=$longitude, 精度=$accuracy, 首次=$isFirstContinuousLocationSuccess single=$isSingleMode".printMsg()

        // 保存位置到缓存
        LocationCacheManager.saveLocation(location)

        // 由于华为SDK不直接提供逆地理编码，这里返回基本坐标信息
        // 如需详细地址信息，可配合地理编码服务使用
        val address = "华为定位 | 纬度: ${String.format("%.6f", latitude)}, 经度: ${String.format("%.6f", longitude)}"
        if (isSingleMode) {
            // 单次定位成功，取消超时
            cancelSingleLocationTimeout()
            isSingleLocationPending = false
            simpleSingleCallback?.invoke("", "", "", address, latitude, longitude)
            simpleSingleCallback = null
            singleFailureCallback = null
        } else {
            simpleContinuousCallback?.invoke(
                "",  // 省
                "",  // 市
                "",  // 区
                address,
                latitude,
                longitude,
                isFirstContinuousLocationSuccess
            )
            isFirstContinuousLocationSuccess = false
        }
    }

    /**
     * 停止定位更新
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        locationCallback = null
        cancelSingleLocationTimeout()
        cancelContinuousTimeout()
        continuousFirstTimeoutCallback = null
        // 重置首次定位标志
        isFirstContinuousLocationSuccess = true
        "华为定位已停止".printMsg()
    }

    /**
     * 取消单次定位超时检测
     */
    private fun cancelSingleLocationTimeout() {
        singleLocationTimeoutRunnable?.let {
            singleLocationHandler?.removeCallbacks(it)
        }
        singleLocationTimeoutRunnable = null
    }

    /**
     * 获取位置坐标类型
     * 华为定位SDK默认返回WGS84坐标系
     */
    fun getCoordinateSystem(): String {
        return "WGS84"
    }

    /**
     * 释放资源
     */
    fun release() {
        stopLocationUpdates()
        fusedLocationProviderClient = null
        settingsClient = null
        locationResultCallback = null
        simpleSingleCallback = null
        simpleContinuousCallback = null
        singleFailureCallback = null
        continuousFirstTimeoutCallback = null
        singleLocationHandler?.let {
            it.removeCallbacksAndMessages(null)
        }
        singleLocationHandler = null
    }

    /**
     * 判断是否为华为设备
     */
    fun isHuaweiDevice(): Boolean {
        return try {
            val manufacturer = android.os.Build.MANUFACTURER.lowercase()
            manufacturer.contains("huawei") || manufacturer.contains("honor")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查HMS Core定位服务是否可用
     * 通过初始化时能否成功构造客户端做快速预判；真正不可用的情况会在
     * requestLocationUpdates 的失败回调中通过 onFailure 上抛，由上层触发 fallback
     */
    fun isHmsAvailable(): Boolean {
        return try {
            fusedLocationProviderClient != null
        } catch (e: Exception) {
            false
        }
    }
}