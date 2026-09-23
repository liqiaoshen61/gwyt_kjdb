package com.jwch.gwyt_project.util

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.jwch.gwyt_project.ext.printMsg

/**
 * 统一定位管理类
 * 自动根据设备类型选择最优定位服务：
 * - 华为设备优先使用华为定位SDK
 * - 其他设备使用原生GPS定位
 *
 * 定位失败时在可用 provider 间自动 fallback（串行调度，去重回调）：
 * - 华为定位失败 → 原生GPS
 * - 原生GPS失败 → 华为定位（仅 HMS 可用时）
 */
class UnifiedLocationManager(private val context: Context) {

    companion object {
        private const val TAG = "UnifiedLocationManager"
    }

    // 定位服务类型
    enum class LocationProvider {
        HUAWEI,     // 华为定位SDK
        GPS         // 原生GPS定位
    }

    // 当前使用的定位服务
    private var currentProvider: LocationProvider? = null

    // 各定位服务实例
    private var huaweiLocationService: HuaweiLocationService? = null
    private var gpsLocationUtil: GpsLocationUtil? = null

    // ===== 单次定位请求级状态（防并发重复回调）=====
    private var singleDone = false
    private var singleInProgress = false
    private var singleSuccessCallback: ((String, String, String, String, Double, Double) -> Unit)? = null
    // 弱信号成功回调（可选）：单次定位只拿到当前帧但精度差/兜底点时回调它，让 UI 提示"信号较弱"
    private var singleSuccessWeakCallback: ((String, String, String, String, Double, Double) -> Unit)? = null
    private var singleFailureCallback: ((LocationFailReason) -> Unit)? = null

    // 持续定位首点状态（用于 fallback）
    private var continuousDone = false
    private var continuousActiveProvider: LocationProvider? = null

    init {
        initLocationService()
    }

    /**
     * 初始化定位服务
     */
    private fun initLocationService() {
        // 判断是否为华为设备
        if (isHuaweiDevice()) {
            try {
                val svc = HuaweiLocationService(context)
                if (svc.isHmsAvailable()) {
                    huaweiLocationService = svc
                    currentProvider = LocationProvider.HUAWEI
                    "检测到华为设备且HMS可用，使用华为定位服务".printMsg()
                    return
                } else {
                    "华为设备但HMS不可用，降级原生GPS定位".printMsg()
                    Log.w(TAG, "HMS不可用，降级GPS")
                }
            } catch (e: Exception) {
                Log.e(TAG, "华为定位服务初始化失败: ${e.message}")
            }
        }

        // 使用原生GPS定位
        try {
            gpsLocationUtil = GpsLocationUtil(context)
            currentProvider = LocationProvider.GPS
            "使用原生GPS定位服务".printMsg()
        } catch (e: Exception) {
            Log.e(TAG, "GPS定位服务初始化失败: ${e.message}")
        }
    }

    private fun ensureHuawei(): HuaweiLocationService? {
        if (huaweiLocationService == null) {
            try {
                val svc = HuaweiLocationService(context)
                if (svc.isHmsAvailable()) huaweiLocationService = svc
            } catch (e: Exception) {
                Log.e(TAG, "按需创建华为定位失败: ${e.message}")
            }
        }
        return huaweiLocationService
    }

    private fun ensureGps(): GpsLocationUtil? {
        if (gpsLocationUtil == null) {
            try {
                gpsLocationUtil = GpsLocationUtil(context)
            } catch (e: Exception) {
                Log.e(TAG, "按需创建GPS定位失败: ${e.message}")
            }
        }
        return gpsLocationUtil
    }

    /**
     * 判断是否为华为设备
     */
    private fun isHuaweiDevice(): Boolean {
        return try {
            val manufacturer = android.os.Build.MANUFACTURER.lowercase()
            manufacturer.contains("huawei") || manufacturer.contains("honor")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取当前使用的定位服务类型
     */
    fun getCurrentProvider(): LocationProvider? = currentProvider

    /**
     * 系统"位置信息"总开关是否开启（GPS / 网络 provider 至少一个可用）。
     * 只关心系统定位开关，与定位权限无关（权限由 provider 内部单独校验）。
     */
    fun isSystemLocationEnabled(): Boolean = try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    } catch (e: Exception) {
        false
    }

    /**
     * 获取缓存的位置
     * @param maxAgeMs 最大有效期（毫秒），默认30分钟
     * @param maxAccuracy 最大精度（米），默认300米
     * @return 缓存的位置信息，如果无有效缓存则返回null
     */
    fun getCachedLocation(
        maxAgeMs: Long = 30 * 60 * 1000L,
        maxAccuracy: Float = 300f
    ): LocationCacheManager.CachedLocation? {
        return LocationCacheManager.getCachedLocation(maxAgeMs, maxAccuracy)
    }

    /**
     * 检查是否有有效的缓存位置
     */
    fun hasValidCachedLocation(): Boolean {
        return LocationCacheManager.hasValidCache()
    }

    /**
     * 获取缓存位置的年龄描述
     */
    fun getCacheAgeDescription(): String {
        return LocationCacheManager.getCacheAgeDescription()
    }

    /**
     * 单次定位（含 provider fallback 与失败原因上抛）
     *
     * 优先级：
     * 1. 后台服务的最新位置（如果服务正在运行且有近期位置，10秒内）
     * 2. 缓存位置（如果有效期在30秒内）
     * 3. 触发实时GPS定位
     *
     * @param showTip 保留兼容参数（提示由 provider 内部处理）
     * @param onSuccess 成功回调（省、市、区、地址、纬度、经度）
     * @param onFailure 失败回调（聚合后的失败原因）
     */
    fun requestSingleLocation(
        showTip: Boolean = true,
        useCache: Boolean = true,
        maxCacheAgeMs: Long = 30 * 1000L, // 30秒，用于备用缓存
        onSuccess: (String, String, String, String, Double, Double) -> Unit,
        onFailure: (LocationFailReason) -> Unit,
        onSuccessWeak: ((String, String, String, String, Double, Double) -> Unit)? = null
    ) {
        // 单次/持续互斥：开始单次前若持续定位在跑，先停掉，避免共享状态（isFirst 标志等）互相干扰
        stopLocation()

        // 优先级1：检查后台服务是否有最新的位置
        val backgroundService = com.jwch.gwyt_project.service.BackgroundLocationService.getInstance()
        if (backgroundService != null) {
            val serviceLocation = backgroundService.getBestLocation()
            if (serviceLocation != null) {
                val age = System.currentTimeMillis() - serviceLocation.time
                val accuracy = if (serviceLocation.hasAccuracy()) serviceLocation.accuracy else 1000f

                // 如果后台服务的位置是10秒内的，直接使用
                if (age <= 10_000L && accuracy <= 200f) {
                    val lat = serviceLocation.latitude
                    val lng = serviceLocation.longitude
                    "使用后台服务最新位置: lat=$lat, lng=$lng, accuracy=${accuracy}m, age=${age}ms".printMsg()
                    OperationLogger.logOperation(context, "使用后台服务位置 精度=${String.format("%.1f", accuracy)}m")
                    onSuccess("", "", "", "后台服务位置", lat, lng)
                    return
                }
            }
        }

        // 优先级2：检查缓存位置（作为备用）
        if (useCache) {
            val cached = LocationCacheManager.getCachedLocation(maxCacheAgeMs, 200f)
            if (cached != null) {
                "使用缓存位置: lat=${cached.latitude}, lng=${cached.longitude}, accuracy=${cached.accuracy}m, age=${LocationCacheManager.getCacheAgeDescription()}".printMsg()
                OperationLogger.logOperation(context, "使用缓存位置 精度=${String.format("%.1f", cached.accuracy)}m ${LocationCacheManager.getCacheAgeDescription()}")
                onSuccess("", "", "", "缓存位置", cached.latitude, cached.longitude)
                return
            }
        }

        // 优先级3：触发实时GPS定位
        "无近期位置，开始实时定位...".printMsg()
        singleDone = false
        singleInProgress = true
        singleSuccessCallback = onSuccess
        singleSuccessWeakCallback = onSuccessWeak
        singleFailureCallback = onFailure

        val queue = buildSingleProviderQueue()
        runSingleProviderQueue(queue, 0, null)
    }

    /**
     * 兼容旧签名：不关心失败原因时使用（统一收口为成功/全空回调）
     */
    fun requestSingleLocation(
        showTip: Boolean = true,
        callback: (String, String, String, String, Double, Double) -> Unit
    ) {
        requestSingleLocation(
            showTip = showTip,
            useCache = true,
            maxCacheAgeMs = 30 * 1000L,
            onSuccess = callback,
            onFailure = { reason ->
                "单次定位最终失败: ${reason.msg}".printMsg()
                callback("", "", "", "", 0.0, 0.0)
            }
        )
    }

    /**
     * 构建单次定位 provider 尝试队列（primary 在前，secondary 在后）
     */
    private fun buildSingleProviderQueue(): List<LocationProvider> {
        val primary = currentProvider ?: LocationProvider.GPS
        val secondary = when (primary) {
            LocationProvider.HUAWEI -> LocationProvider.GPS
            LocationProvider.GPS -> if (ensureHuawei()?.isHmsAvailable() == true) LocationProvider.HUAWEI else null
        }
        return listOfNotNull(primary, secondary)
    }

    /**
     * 串行执行 provider 队列：当前 provider 失败则切下一个，成功或全部失败即终止
     */
    private fun runSingleProviderQueue(
        queue: List<LocationProvider>,
        index: Int,
        prevReason: LocationFailReason?
    ) {
        // 已完成（成功或被 cancel）→ 终止
        if (singleDone || !singleInProgress) {
            prevReason?.let { "队列终止 reason=${it.msg} done=$singleDone".printMsg() }
            return
        }
        if (index >= queue.size) {
            // 全部失败
            singleDone = true
            singleInProgress = false
            val finalReason = prevReason ?: LocationFailReason.ALL_PROVIDERS_FAILED
            OperationLogger.logOperation(context, "单次定位最终失败: ${finalReason.msg}")
            singleFailureCallback?.invoke(finalReason)
            singleSuccessCallback = null
            singleFailureCallback = null
            return
        }
        val provider = queue[index]
        "尝试定位 provider=$provider".printMsg()
        prevReason?.let {
            OperationLogger.logOperation(context, "定位切换：${queue[index - 1]}→$provider(原因${it.msg})")
        }

        when (provider) {
            LocationProvider.HUAWEI -> {
                ensureHuawei()?.requestSingleLocation(
                    onSuccess = { province, city, district, address, lat, lng ->
                        onSingleSuccess(provider, province, city, district, address, lat, lng, 0f)
                    },
                    onFailure = { reason ->
                        runSingleProviderQueue(queue, index + 1, reason)
                    }
                ) ?: runSingleProviderQueue(queue, index + 1, LocationFailReason.HMS_UNAVAILABLE)
            }
            LocationProvider.GPS -> {
                ensureGps()?.requestSingleLocation(
                    onSuccess = { location, _, isWeak ->
                        val lat = location.latitude
                        val lng = location.longitude
                        val acc = if (location.hasAccuracy()) location.accuracy else 0f
                        onSingleSuccess(
                            provider, "", "", "",
                            "GPS定位 | 纬度: ${String.format("%.6f", lat)}, 经度: ${String.format("%.6f", lng)}",
                            lat, lng, acc, isWeak
                        )
                    },
                    onFailure = { reason ->
                        runSingleProviderQueue(queue, index + 1, reason)
                    }
                ) ?: runSingleProviderQueue(queue, index + 1, LocationFailReason.PROVIDER_FAILED)
            }
        }
    }

    private fun onSingleSuccess(
        provider: LocationProvider,
        province: String, city: String, district: String, address: String,
        lat: Double, lng: Double, accuracy: Float,
        isWeak: Boolean = false
    ) {
        if (singleDone) {
            "重复成功回调被丢弃 provider=$provider".printMsg()
            return
        }
        singleDone = true
        singleInProgress = false

        // 保存位置到缓存
        LocationCacheManager.saveLocation(lat, lng, accuracy, provider.name)

        OperationLogger.logOperation(context, "定位成功[$provider] 精度=${String.format("%.1f", accuracy)}m 弱信号=$isWeak")
        currentProvider = provider
        // 弱信号/兜底点交给独立的弱回调（UI 提示"信号较弱"）；未提供弱回调时降级到普通成功回调
        if (isWeak && singleSuccessWeakCallback != null) {
            singleSuccessWeakCallback?.invoke(province, city, district, address, lat, lng)
        } else {
            singleSuccessCallback?.invoke(province, city, district, address, lat, lng)
        }
        singleSuccessCallback = null
        singleSuccessWeakCallback = null
        singleFailureCallback = null
    }

    /**
     * 单次定位（仅调用指定 provider，不做 fallback）
     * 用于定位测试页：单独调用某一种定位服务以对比各 provider 的首示速（TTFF）。
     *
     * @param provider 指定定位服务（HUAWEI / GPS）
     * @param onSuccess 成功回调（省、市、区、地址、纬度、经度）
     * @param onFailure 失败回调（该 provider 的失败原因）
     */
    fun requestSingleProvider(
        provider: LocationProvider,
        onSuccess: (String, String, String, String, Double, Double) -> Unit,
        onFailure: (LocationFailReason) -> Unit,
        useLastKnownLocation: Boolean = true
    ) {
        // 单次/持续互斥：开始单次前若持续定位在跑，先停掉
        stopLocation()

        singleDone = false
        singleInProgress = true
        singleSuccessCallback = onSuccess
        singleSuccessWeakCallback = null
        singleFailureCallback = onFailure

        fun fail(reason: LocationFailReason) {
            singleDone = true
            singleInProgress = false
            "单次定位[${provider}]失败: ${reason.msg}".printMsg()
            singleFailureCallback?.invoke(reason)
            singleSuccessCallback = null
            singleSuccessWeakCallback = null
            singleFailureCallback = null
        }

        when (provider) {
            LocationProvider.HUAWEI -> {
                ensureHuawei()?.requestSingleLocation(
                    onSuccess = { province, city, district, address, lat, lng ->
                        onSingleSuccess(provider, province, city, district, address, lat, lng, 0f)
                    },
                    onFailure = { reason -> fail(reason) },
                    useLastKnownLocation = useLastKnownLocation
                ) ?: fail(LocationFailReason.HMS_UNAVAILABLE)
            }
            LocationProvider.GPS -> {
                ensureGps()?.requestSingleLocation(
                    onSuccess = { location, _, isWeak ->
                        val lat = location.latitude
                        val lng = location.longitude
                        val acc = if (location.hasAccuracy()) location.accuracy else 0f
                        onSingleSuccess(
                            provider, "", "", "",
                            "GPS定位 | 纬度: ${String.format("%.6f", lat)}, 经度: ${String.format("%.6f", lng)}",
                            lat, lng, acc, isWeak
                        )
                    },
                    onFailure = { reason -> fail(reason) },
                    useLastKnownLocation = useLastKnownLocation
                ) ?: fail(LocationFailReason.PROVIDER_FAILED)
            }
        }
    }

    /**
     * 取消当前单次定位（用户主动放弃或页面销毁时调用）
     */
    fun cancelSingleLocation() {
        if (!singleInProgress) return
        singleDone = true
        singleInProgress = false
        OperationLogger.logOperation(context, "定位被取消")
        huaweiLocationService?.stopLocationUpdates()
        gpsLocationUtil?.stopSingleLocationUpdates()
        singleSuccessCallback = null
        singleSuccessWeakCallback = null
        singleFailureCallback = null
    }

    /**
     * 启动持续定位（含首点 fallback）
     * @param onSuccess 持续回调（省、市、区、地址、纬度、经度、是否首次定位成功）
     * @param onFirstFailure 首点完全失败回调（所有 provider 均无法给出首点）
     */
    fun startContinuousLocation(
        onSuccess: (String, String, String, String, Double, Double, Boolean) -> Unit,
        onFirstFailure: (LocationFailReason) -> Unit = {}
    ) {
        // 单次/持续互斥：开始持续前先清掉可能未完成的单次定位
        cancelSingleLocation()

        continuousDone = false
        continuousActiveProvider = null
        val queue = buildSingleProviderQueue()
        runContinuousProviderQueue(queue, 0, onSuccess, onFirstFailure, null)
    }

    private fun runContinuousProviderQueue(
        queue: List<LocationProvider>,
        index: Int,
        onSuccess: (String, String, String, String, Double, Double, Boolean) -> Unit,
        onFirstFailure: (LocationFailReason) -> Unit,
        prevReason: LocationFailReason?
    ) {
        if (continuousDone) return
        if (index >= queue.size) {
            continuousDone = true
            val finalReason = prevReason ?: LocationFailReason.ALL_PROVIDERS_FAILED
            OperationLogger.logOperation(context, "持续定位首点最终失败: ${finalReason.msg}")
            onFirstFailure.invoke(finalReason)
            return
        }
        val provider = queue[index]
        "尝试持续定位 provider=$provider".printMsg()
        prevReason?.let {
            OperationLogger.logOperation(context, "持续定位切换：${queue[index - 1]}→$provider(原因${it.msg})")
        }

        when (provider) {
            LocationProvider.HUAWEI -> {
                ensureHuawei()?.startContinuousLocationSimple(
                    callback = { province, city, district, address, lat, lng, isFirstSuccess ->
                        if (continuousDone) return@startContinuousLocationSimple
                        if (continuousActiveProvider == null) {
                            continuousActiveProvider = provider
                            currentProvider = provider
                            OperationLogger.logOperation(context, "持续定位首点成功[$provider]")
                        }
                        onSuccess.invoke(province, city, district, address, lat, lng, isFirstSuccess)
                    },
                    onFirstTimeout = {
                        runContinuousProviderQueue(queue, index + 1, onSuccess, onFirstFailure, LocationFailReason.TTFF_TIMEOUT)
                    }
                ) ?: runContinuousProviderQueue(queue, index + 1, onSuccess, onFirstFailure, LocationFailReason.HMS_UNAVAILABLE)
            }
            LocationProvider.GPS -> {
                ensureGps()?.startContinuousLocationUpdates(
                    onLocation = { location, _, isFirstSuccess ->
                        if (continuousDone) return@startContinuousLocationUpdates
                        if (continuousActiveProvider == null) {
                            continuousActiveProvider = provider
                            currentProvider = provider
                            OperationLogger.logOperation(context, "持续定位首点成功[$provider]")
                        }
                        val lat = location.latitude
                        val lng = location.longitude
                        onSuccess.invoke(
                            "", "", "",
                            "GPS定位 | 纬度: ${String.format("%.6f", lat)}, 经度: ${String.format("%.6f", lng)}",
                            lat, lng, isFirstSuccess
                        )
                    },
                    onFirstTimeout = {
                        runContinuousProviderQueue(queue, index + 1, onSuccess, onFirstFailure, LocationFailReason.TTFF_TIMEOUT)
                    }
                ) ?: runContinuousProviderQueue(queue, index + 1, onSuccess, onFirstFailure, LocationFailReason.PROVIDER_FAILED)
            }
        }
    }

    /**
     * 兼容旧签名：仅关心成功回调
     */
    fun startContinuousLocation(
        callback: (String, String, String, String, Double, Double, Boolean) -> Unit
    ) {
        startContinuousLocation(callback) { reason ->
            "持续定位首点失败: ${reason.msg}".printMsg()
        }
    }

    /**
     * 停止定位
     */
    fun stopLocation() {
        when (continuousActiveProvider ?: currentProvider) {
            LocationProvider.HUAWEI -> {
                huaweiLocationService?.stopLocationUpdates()
            }
            LocationProvider.GPS -> {
                gpsLocationUtil?.stopContinuousLocationUpdates()
            }
            null -> {}
        }
        continuousDone = true
        continuousActiveProvider = null
        "定位已停止".printMsg()
    }

    /**
     * 释放资源
     */
    fun release() {
        cancelSingleLocation()
        stopLocation()
        huaweiLocationService?.release()
        huaweiLocationService = null

        gpsLocationUtil?.release()
        gpsLocationUtil = null

        currentProvider = null
    }

    /**
     * 强制使用指定的定位服务
     */
    fun forceUseProvider(provider: LocationProvider) {
        // 先停止当前定位
        stopLocation()
        cancelSingleLocation()

        // 切换到指定的定位服务
        currentProvider = provider
        when (provider) {
            LocationProvider.HUAWEI -> {
                ensureHuawei()
                "强制使用华为定位服务".printMsg()
            }
            LocationProvider.GPS -> {
                ensureGps()
                "强制使用GPS定位服务".printMsg()
            }
        }
    }
}
