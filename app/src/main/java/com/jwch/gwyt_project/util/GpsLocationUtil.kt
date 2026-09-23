package com.jwch.gwyt_project.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.jwch.gwyt_project.ext.printMsg

class GpsLocationUtil(private val context: Context) : SensorEventListener {

    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var continuousLocationListener: LocationListener? = null
    private var singleLocationListener: LocationListener? = null

    private var continuousLocationBlock: ((Location, Float, Boolean) -> Unit)? = null
    // 单次成功回调 (Location, rotation, isWeak)：isWeak=true 表示"本次请求内只拿到弱信号/兜底点"（点位是当前、但精度差）
    private var singleLocationBlock: ((Location, Float, Boolean) -> Unit)? = null

    // 是否是首次定位成功的标志位
    private var isFirstContinuousLocationSuccess = true

    private val handler = Handler(Looper.getMainLooper())
    private var continuousLocationTimeoutRunnable: Runnable? = null
    private var singleLocationTimeoutRunnable: Runnable? = null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private lateinit var sensorManager: SensorManager
    var rotation = 0f

    // 位置缓存
    private var lastKnownLocation: Location? = null
    // 定位提供者优先级列表
    private val locationProviders = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    )
    
    // 检查设备是否支持北斗定位
    fun isBeidouSupported(): Boolean {
        val packageManager = context.packageManager
        return packageManager.hasSystemFeature("android.hardware.location.gps")
    }
    
    // 获取当前使用的卫星系统信息
    fun getSatelliteInfo(): String {
        return "当前设备支持的卫星系统：GPS、北斗、GLONASS、Galileo等"
    }
    // ===== 定位有效性阈值（分级）=====
    // 首次定位准入阈值（弱信号容忍，落地优先）：野外/楼宇间首点常 50-200m，原 50m 直接丢弃导致"定位不上"
    private val FIRST_MIN_ACCURACY = 200f
    private val FIRST_MAX_LOCATION_AGE = 15 * 60 * 1000L // 15分钟
    // 收敛判优阈值（首点落地后，用于判断是否为更优解并重绘）
    private val STRICT_MIN_ACCURACY = 50f
    private val STRICT_MAX_LOCATION_AGE = 5 * 60 * 1000L // 5分钟
    // 单次/持续定位首点超时（冷启动 TTFF 30-60s，原 8s/10s 必然误判失败）
    private val SINGLE_LOCATION_TIMEOUT = 30_000L
    private val CONTINUOUS_FIRST_TIMEOUT = 30_000L
    // 单次定位"把旧点当当前点"的最长时限：仅当最后一帧定位是 30s 内拿到的，才允许被当作
    // "当前位置"快速返回/兜底。否则在高速移动（高铁）GPS 拿不到新点时，会把几分钟前的旧点
    // 当"定位成功"交出去 —— 明明在 B 点却回停留在 A 点。
    private val SINGLE_LASTKNOWN_MAX_AGE = 30_000L
    // 持续定位重绘最小位移阈值（米），小于此值不重绘，防抖
    // 原 5m 在步行(≈1m/s)下需走 3~5 秒才重绘一次，造成点位"3秒动一下"的卡顿感；降为 1m 提高灵敏度
    private val MIN_REDRAW_DISTANCE = 1f

    // 持续定位收敛过程跟踪的最佳点（用于 isBetterLocation 防抖）
    private var continuousBestLocation: Location? = null
    // 单次定位过程跟踪的最佳弱信号点（超时兜底）
    private var singleBestLocation: Location? = null
    // 失败/超时回调
    private var singleFailureBlock: ((LocationFailReason) -> Unit)? = null
    private var continuousFirstTimeoutBlock: (() -> Unit)? = null

    init {
        initSensor()
        initLastKnownLocation()
    }

    fun initSensor(){
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    // 初始化最后已知位置
    private fun initLastKnownLocation() {
        if (checkLocationPermission()) {
            for (provider in locationProviders) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val location = locationManager.getLastKnownLocation(provider)
                        if (isBetterLocation(location, lastKnownLocation)) {
                            lastKnownLocation = location
                        }
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 启动持续定位
    // onLocation: 持续收到的有效定位点 (location, rotation, isFirstSuccess)
    // onFirstTimeout: 首点等待超时，交上层决定是否 fallback 到其他 provider
    fun startContinuousLocationUpdates(
        onLocation: (Location, Float, Boolean) -> Unit,
        onFirstTimeout: () -> Unit = {}
    ) {
        if (!checkLocationPermission()) {
            // 无权限直接触发首点超时，让上层 fallback 或报错
            onFirstTimeout.invoke()
            return
        }
        continuousLocationBlock = onLocation
        continuousFirstTimeoutBlock = onFirstTimeout
        // 重置首次定位标志与收敛跟踪
        isFirstContinuousLocationSuccess = true
        continuousBestLocation = null

        // 先尝试使用最后已知位置（用首次准入阈值，落地优先）
        lastKnownLocation?.let {
            if (isLocationValid(it, isStrict = false)) {
                continuousBestLocation = it
                onLocation(it, rotation, isFirstContinuousLocationSuccess)
                isFirstContinuousLocationSuccess = false
            }
        }

        continuousLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                handleContinuousLocation(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // 可以添加状态变化处理
            }
            override fun onProviderEnabled(provider: String) {
                // 可以添加提供者启用处理
            }
            override fun onProviderDisabled(provider: String) {
                // 可以添加提供者禁用处理
            }
        }

        // 注册多个定位提供者
        registerLocationListeners(continuousLocationListener as LocationListener, true)

        // 首点等待超时：首点未到则触发 onFirstTimeout 并停止当前 listener（交上层 fallback）
        continuousLocationTimeoutRunnable = Runnable {
            // 首点已到（已回至少一次有效点），仅日志记录，不打断持续定位
            if (!isFirstContinuousLocationSuccess) {
                "持续定位：本轮暂无新点，保持监听".printMsg()
                return@Runnable
            }
            // 首点未到，尝试弱信号兜底
            lastKnownLocation?.let {
                if (isLocationValid(it, isStrict = false)) {
                    "持续定位首点超时，使用弱信号最后已知位置兜底".printMsg()
                    continuousBestLocation = it
                    continuousLocationBlock?.invoke(it, rotation, isFirstContinuousLocationSuccess)
                    isFirstContinuousLocationSuccess = false
                    return@Runnable
                }
            }
            // 完全无点，通知上层 fallback
            "持续定位首点超时，无可用点，通知上层切换".printMsg()
            stopContinuousLocationUpdates()
            continuousFirstTimeoutBlock?.invoke()
        }
        handler.postDelayed(continuousLocationTimeoutRunnable as Runnable, CONTINUOUS_FIRST_TIMEOUT)
    }

    // 处理持续定位收到的点：首点用 FIRST 准入，后续用 STRICT + isBetterLocation 防抖收敛
    private fun handleContinuousLocation(location: Location) {
        val isFirst = isFirstContinuousLocationSuccess
        // 首点用首次准入阈值，后续用严格阈值
        if (!isLocationValid(location, isStrict = !isFirst)) {
            // 即便不达严格阈值，也作为弱信号候选跟踪（供超时兜底）
            if (isBetterLocation(location, singleBestLocation)) {
                singleBestLocation = location
            }
            return
        }
        // 防抖：首点直接落地；后续点需比当前最佳更优且位移超过 MIN_REDRAW_DISTANCE
        if (isFirst || isBetterLocation(location, continuousBestLocation)) {
            if (!isFirst && continuousBestLocation != null) {
                val dist = location.distanceTo(continuousBestLocation!!)
                if (dist < MIN_REDRAW_DISTANCE) return
            }
            continuousBestLocation = location
            lastKnownLocation = location

            // 保存位置到缓存
            LocationCacheManager.saveLocation(location)

            continuousLocationBlock?.invoke(location, rotation, isFirstContinuousLocationSuccess)
            isFirstContinuousLocationSuccess = false
        }
    }

    // 启动单次定位（双回调，供 UnifiedLocationManager fallback 调度）
    // onSuccess: 成功 (location, rotation)；onFailure: 失败原因
    // @param useLastKnownLocation false 时跳过 lastKnownLocation 快速返回，强制请求真实定位（测试首示速用）
    fun requestSingleLocation(
        onSuccess: (Location, Float, Boolean) -> Unit,
        onFailure: (LocationFailReason) -> Unit,
        useLastKnownLocation: Boolean = true
    ) {
        singleBestLocation = null
        if (!checkLocationPermission()) {
            onFailure.invoke(LocationFailReason.PERMISSION_DENIED)
            return
        }
        // GPS/NETWORK 均不可用，直接报 GPS 未开启
        if (!isAnyProviderEnabled()) {
            onFailure.invoke(LocationFailReason.GPS_DISABLED)
            return
        }

        singleLocationBlock = onSuccess
        singleFailureBlock = onFailure

        // 尝试最后已知位置：仅当它是"30s 内刚拿到"的才当"当前点"秒回（严格阈值，缓存点通常较准）；
        // 相对更久的旧点（高铁上常是几分钟前）不作为当前成功返回，走实时定位拿新点。
        if (useLastKnownLocation && isLastKnownFreshForSingle()) {
            val lk = lastKnownLocation!!
            if (isLocationValid(lk, isStrict = true)) {
                onSuccess(lk, rotation, false)
                stopSingleLocationUpdates()
                return
            }
        }

        singleLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                handleSingleLocation(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // 注册多个定位提供者
        registerLocationListeners(singleLocationListener as LocationListener, false)

        // 超时处理：30s
        singleLocationTimeoutRunnable = Runnable {
            if (singleLocationBlock == null) return@Runnable
            // 兜底：只接受"当前位置"。候选顺序=本次请求内跟踪到的弱信号点（本请求收到、≤30s），
            // 否则"刚更新过(≤30s)的最后已知位置"。两者都是当前帧，只是精度差，标记 isWeak=true。
            // 关键：不再拿几分钟前的旧 lastKnownLocation 冒充当前点 —— 高铁上那正是"报成功却还在 A 点"的根因。
            val weak = singleBestLocation
            val fallback = when {
                weak != null && isLocationValid(weak, isStrict = false) -> weak
                useLastKnownLocation && isLastKnownFreshForSingle() ->
                    lastKnownLocation?.takeIf { isLocationValid(it, isStrict = false) }
                else -> null
            }
            if (fallback != null) {
                "单次定位超时，返回弱/兜底点 精度=${fallback.accuracy}m（位置是当前帧，信号较弱）".printMsg()
                singleLocationBlock?.invoke(fallback, rotation, true)
                stopSingleLocationUpdates()
                return@Runnable
            }
            // 完全无新鲜点 → 如实判定失败，而不是用旧点冒充。
            // 到这步时 weak 必为 null（有效即已在上面返回）；若 lastKnown 仍在 30s 内但精度超阈，算"信号弱"，否则"冷启动超时"
            val reason = if (isLastKnownFreshForSingle())
                LocationFailReason.ACCURACY_TOO_LOW else LocationFailReason.TTFF_TIMEOUT
            "单次定位超时，无新鲜可用点 reason=$reason".printMsg()
            val cb = singleFailureBlock
            stopSingleLocationUpdates()
            cb?.invoke(reason)
        }
        handler.postDelayed(singleLocationTimeoutRunnable as Runnable, SINGLE_LOCATION_TIMEOUT)
    }

    // 处理单次定位收到的点：严格阈值直接成功（新鲜=非弱）；弱信号点跟踪供超时兜底（弱）
    private fun handleSingleLocation(location: Location) {
        if (isLocationValid(location, isStrict = true)) {
            lastKnownLocation = location

            // 保存位置到缓存
            LocationCacheManager.saveLocation(location)

            singleLocationBlock?.invoke(location, rotation, false)
            stopSingleLocationUpdates() // 单次定位完成后停止
            return
        }
        // 不达严格阈值，作为弱信号候选跟踪
        if (isLocationValid(location, isStrict = false) && isBetterLocation(location, singleBestLocation)) {
            singleBestLocation = location
        }
    }

    /** 最后已知位置是否"新鲜到可当当前点"：仅 30s 内视为当前，更久视为"可能已移动" */
    private fun isLastKnownFreshForSingle(): Boolean {
        val lk = lastKnownLocation ?: return false
        return (System.currentTimeMillis() - lk.time) <= SINGLE_LASTKNOWN_MAX_AGE
    }

    // 是否有任意定位 provider 可用
    private fun isAnyProviderEnabled(): Boolean {
        return locationProviders.any {
            try { locationManager.isProviderEnabled(it) } catch (e: Exception) { false }
        }
    }

    // 停止持续定位
    fun stopContinuousLocationUpdates() {
        continuousLocationListener?.let {
            locationManager.removeUpdates(it)
        }
        continuousLocationListener = null
        continuousLocationBlock = null
        continuousFirstTimeoutBlock = null
        continuousLocationTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
        continuousLocationTimeoutRunnable = null
        // 重置首次定位标志
        isFirstContinuousLocationSuccess = true
        continuousBestLocation = null
    }

    // 停止单次定位
    fun stopSingleLocationUpdates() {
        singleLocationListener?.let {
            locationManager.removeUpdates(it)
        }
        singleLocationListener = null
        singleLocationBlock = null
        singleFailureBlock = null
        singleLocationTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
        singleLocationTimeoutRunnable = null
        singleBestLocation = null
    }

    // 注册多个定位提供者
    private fun registerLocationListeners(listener: LocationListener, isContinuous: Boolean) {
        val minTime = 1000L // 持续/单次定位均 1 秒一格
        val minDistance = 1f

        for (provider in locationProviders) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                        provider,
                        minTime,
                        minDistance,
                        listener
                    )
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // 检查定位权限
    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 检查位置是否有效
    // isStrict=true 用收敛阈值（首点落地后判优），false 用首次准入阈值（首点落地优先）
    private fun isLocationValid(location: Location?, isStrict: Boolean = true): Boolean {
        if (location == null) return false

        val minAccuracy = if (isStrict) STRICT_MIN_ACCURACY else FIRST_MIN_ACCURACY
        val maxAge = if (isStrict) STRICT_MAX_LOCATION_AGE else FIRST_MAX_LOCATION_AGE

        // 检查位置年龄
        val locationAge = System.currentTimeMillis() - location.time
        if (locationAge > maxAge) return false

        // 检查位置精度
        if (location.accuracy > minAccuracy) return false

        // 检查位置是否有有效的坐标
        if (location.latitude == 0.0 && location.longitude == 0.0) return false

        return true
    }

    // 比较两个位置，返回更好的那个
    private fun isBetterLocation(location: Location?, currentBestLocation: Location?): Boolean {
        if (location == null) return false
        if (currentBestLocation == null) return true

        // 检查位置年龄
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > STRICT_MAX_LOCATION_AGE
        val isSignificantlyOlder = timeDelta < -STRICT_MAX_LOCATION_AGE
        val isNewer = timeDelta > 0
        
        if (isSignificantlyNewer) return true
        if (isSignificantlyOlder) return false
        
        // 检查位置精度
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200
        val isEquallyAccurate = accuracyDelta == 0
        
        if (isMoreAccurate) return true
        if (isNewer && !isSignificantlyLessAccurate) return true
        
        return false
    }

    // 释放资源
    fun release() {
        stopContinuousLocationUpdates()
        stopSingleLocationUpdates()
        
        // 取消传感器监听
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(
                event.values, 0,
                accelerometerReading, 0, accelerometerReading.size
            )
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(
                event.values, 0,
                magnetometerReading, 0, magnetometerReading.size
            )
        }

        SensorManager.getRotationMatrix(
            rotationMatrix, null,
            accelerometerReading, magnetometerReading
        )

        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val azimuthInRadians = orientationAngles[0]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

        rotation = -azimuthInDegrees
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


}