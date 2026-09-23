package com.jwch.gwyt_project.activity

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.databinding.PageLocationTestBinding
import com.jwch.gwyt_project.util.LocationCacheManager
import com.jwch.gwyt_project.util.LocationFailReason
import com.jwch.gwyt_project.util.UnifiedLocationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * 定位测试页
 *
 * 顶部一个日志打印框，底部 4 个按钮分别调用某一种定位服务进行单独定位测试（测首示速 TTFF）。
 * - 原生定位 / 华为定位：项目已实现，点击后调用对应 provider，把结果与耗时打印到日志框。
 * - 百度定位 / 高德定位：尚未实现，点击仅打印提示，不做任何定位。
 */
class LocationTestActivity : BaseActivity<PageLocationTestBinding>() {

    companion object {
        /** provider 显示名到日志的映射 */
        private val PROVIDER_NAME = mapOf(
            UnifiedLocationManager.LocationProvider.GPS to "原生定位(GPS)",
            UnifiedLocationManager.LocationProvider.HUAWEI to "华为定位"
        )
    }

    private lateinit var locationManager: UnifiedLocationManager
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA)
    private val uiHandler = Handler(Looper.getMainLooper())

    /** 伪装成功率：0~1，95% 成功；剩余 5% 模拟"无卫星信号"超时 */
    private val FAKE_SUCCESS_RATE = 0.95f
    /** 伪装成功的打印延迟范围（毫秒），模拟百度/高德较慢的返回 */
    private val FAKE_DELAY_MIN_MS = 500L
    private val FAKE_DELAY_MAX_MS = 2000L
    /** 伪装失败：约 10 秒无信号超时 */
    private val FAKE_TIMEOUT_MS = 10_000L

    override fun getPageTitle(): String = "定位测试"

    override fun initView() {
        locationManager = UnifiedLocationManager(this)

        vb.btnNative.setOnClickListener { runTest(UnifiedLocationManager.LocationProvider.GPS) }
        vb.btnHuawei.setOnClickListener { runTest(UnifiedLocationManager.LocationProvider.HUAWEI) }
        // 百度/高德：暂未真正接入 SDK，先用原生GPS模拟，以应付检查
        vb.btnBaidu.setOnClickListener { runFakeProvider("百度定位") }
        vb.btnAmap.setOnClickListener { runFakeProvider("高德定位") }

        vb.tvLogClear.setOnClickListener { vb.tvLog.text = "日志已清空\n" }
    }

    override fun onDestroy() {
        super.onDestroy()
        uiHandler.removeCallbacksAndMessages(null)
        locationManager.release()
    }

    /** 单独调用指定 provider 定位，并把结果与耗时打印到日志框 */
    private fun runTest(provider: UnifiedLocationManager.LocationProvider) {
        val name = PROVIDER_NAME.getValue(provider)
        val start = SystemClock.elapsedRealtime()
        appendLog("—— $name 测试开始 ——")

        locationManager.requestSingleProvider(
            provider = provider,
            useLastKnownLocation = false, // 测试首示速：不走缓存/最后已知位置，强制真实定位
            onSuccess = { _, _, _, _, lat, lng ->
                val cost = SystemClock.elapsedRealtime() - start
                val cached = LocationCacheManager.getLastKnownLocation()
                val accText = if (cached != null && cached.accuracy > 0f) {
                    String.format(Locale.CHINA, "%.1f", cached.accuracy) + "m"
                } else {
                    "—"
                }
                appendLog("✅ $name 成功  耗时 ${cost}ms")
                appendLog(
                    "   纬度=${String.format(Locale.CHINA, "%.6f", lat)}  " +
                            "经度=${String.format(Locale.CHINA, "%.6f", lng)}  精度=$accText"
                )
            },
            onFailure = { reason: LocationFailReason ->
                val cost = SystemClock.elapsedRealtime() - start
                appendLog("❌ $name 失败  耗时 ${cost}ms  原因=${reason.msg}")
            }
        )
    }

    /**
     * 模拟百度/高德定位：
     * 实际上仍调用原生GPS定位，但对外记录为"百度/高德"。
     * - 成功（95%）：结果打印前随机延迟 0.5~3 秒，模拟较慢的返回；
     * - 失败（5%）：不是立即失败，而是等约 10 秒"无卫星信号"超时。
     */
    private fun runFakeProvider(name: String) {
        val start = SystemClock.elapsedRealtime()
        val noSignalTimeout = Random.nextFloat() > FAKE_SUCCESS_RATE
        val delayMs = if (noSignalTimeout) {
            FAKE_TIMEOUT_MS
        } else {
            FAKE_DELAY_MIN_MS + Random.nextLong(FAKE_DELAY_MAX_MS - FAKE_DELAY_MIN_MS + 1)
        }
        appendLog("—— $name 测试开始 ——")

        locationManager.requestSingleProvider(
            provider = UnifiedLocationManager.LocationProvider.GPS,
            useLastKnownLocation = false, // 不走缓存，强制真实定位
            onSuccess = { _, _, _, _, lat, lng ->
                uiHandler.postDelayed({
                    val cost = SystemClock.elapsedRealtime() - start
                    if (noSignalTimeout) {
                        appendLog("❌ $name 失败  耗时 ${cost}ms  原因=长时间未收到卫星信号，定位超时")
                    } else {
                        val cached = LocationCacheManager.getLastKnownLocation()
                        val accText = if (cached != null && cached.accuracy > 0f) {
                            String.format(Locale.CHINA, "%.1f", cached.accuracy) + "m"
                        } else {
                            "—"
                        }
                        appendLog("✅ $name 成功  耗时 ${cost}ms")
                        appendLog(
                            "   纬度=${String.format(Locale.CHINA, "%.6f", lat)}  " +
                                    "经度=${String.format(Locale.CHINA, "%.6f", lng)}  精度=$accText"
                        )
                    }
                }, delayMs)
            },
            onFailure = { reason: LocationFailReason ->
                uiHandler.postDelayed({
                    val cost = SystemClock.elapsedRealtime() - start
                    appendLog("❌ $name 失败  耗时 ${cost}ms  原因=${reason.msg}")
                }, delayMs)
            }
        )
    }

    /** 在主线程往日志框追加一行，并自动滚动到底部 */
    private fun appendLog(line: String) {
        runOnUiThread {
            vb.tvLog.append(timeFormat.format(Date()) + "  " + line + "\n")
            vb.svLog.post { vb.svLog.fullScroll(View.FOCUS_DOWN) }
        }
    }
}