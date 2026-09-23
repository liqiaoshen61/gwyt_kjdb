package com.jwch.gwyt_project.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.view.Surface
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.databinding.PageLocationDetailBinding
import com.jwch.gwyt_project.util.CompassHelper
import com.jwch.gwyt_project.util.DeviceInfoUtil
import com.jwch.gwyt_project.util.LocationCacheManager
import com.jwch.gwyt_project.util.LocationFailReason
import com.jwch.gwyt_project.util.UnifiedLocationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 定位详细信息页
 * 展示最近一次定位的水平误差、海拔、经纬度、速度、方位角、定位提供者与时间，
 * 并提供「获取最新定位」按钮触发一次真实修复并刷新。
 * 同时通过加速度计+磁力计实时展示设备姿态（倾斜角度 / 旋转角度）。
 */
class LocationDetailActivity : BaseActivity<PageLocationDetailBinding>(), SensorEventListener {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private lateinit var locationManager: UnifiedLocationManager

    private lateinit var sensorManager: SensorManager
    // 用 NaN 标记"尚未收到事件"，区别于"收到了但值为 0"（后者常用于判断磁力计坏/未校准）
    private val accelerometerReading = FloatArray(3) { Float.NaN }
    private val magnetometerReading = FloatArray(3) { Float.NaN }
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // 姿态 UI 刷新限频（毫秒）：避免磁力计噪声引起数字快速抖动
    private val poseUpdateIntervalMs = 500L
    private var lastPoseUpdate = 0L

    /**
     * 姿态传感器兼容性测试：候选的"单一融合传感器"通道，各自能独立算出设备姿态三角度。
     * 用于排查鸿蒙5+ 等设备上磁力计拿不到数据的场景——哪条通道能出数值，就用哪条。
     */
    private class PoseChannel(val label: String, val type: Int) {
        /** 设备是否暴露了该传感器 */
        var has = false
        /** 实时算出的 [方位角, 俯仰, 侧倾]，NaN 表示暂无有效值 */
        val pose = FloatArray(3) { Float.NaN }
    }

    private val poseChannels = listOf(
        PoseChannel("旋转向量", Sensor.TYPE_ROTATION_VECTOR),
        PoseChannel("磁力旋转向量", Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR),
        PoseChannel("游戏旋转向量", Sensor.TYPE_GAME_ROTATION_VECTOR)
    )

    /** 罗盘探针：观察主地图 CompassHelper 的回退链在本机上会落到哪个姿态来源 */
    private val compassProbe: CompassHelper by lazy { CompassHelper(this) }

    override fun getPageTitle(): String = "定位详细信息"

    override fun initView() {
        locationManager = UnifiedLocationManager(this)
        render()
        vb.tvLocateNow.setOnClickListener {
            locateNow()
        }
        vb.tvDeviceInfo.text = DeviceInfoUtil.summary()
        initSensor()
    }

    override fun onResume() {
        super.onResume()
        registerSensor()
        if (compassProbe.isAvailable) {
            compassProbe.start()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterSensor()
        compassProbe.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSensor()
    }

    private fun initSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun registerSensor() {
        if (!::sensorManager.isInitialized) return

        // 现有姿态：加速度 + 磁力
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // 测试面板：候选融合传感器通道（找不到的不注册，面板对应行会显示 ×）
        for (ch in poseChannels) {
            sensorManager.getDefaultSensor(ch.type)?.let {
                ch.has = true
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private fun unregisterSensor() {
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(
                event.values, 0, accelerometerReading, 0, accelerometerReading.size
            )
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(
                event.values, 0, magnetometerReading, 0, magnetometerReading.size
            )
            else -> {
                // 测试面板的候选融合传感器通道
                poseChannels.firstOrNull { it.type == event.sensor.type }?.let { ch ->
                    // 都对原始方位统一做鸿蒙补偿（游戏中鸿蒙补偿由 DeviceInfoUtil.absoluteHeading 内部按需决定）
                    computePoseFromRotationVector(event.values, ch.pose)
                }
            }
        }
        updatePose()
    }

    /** 限频后计算并刷新设备姿态（倾斜角度 / 旋转角度）+ 兼容性测试面板 */
    private fun updatePose() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPoseUpdate < poseUpdateIntervalMs) return
        lastPoseUpdate = now

        // 现有通道：加速度 + 磁力
        var accMagOk = false
        // 两路读数都必须「已收到且为有限值」才可求姿态。
        // 注意 getRotationMatrix 不会拒绝 NaN 输入，会把 NaN 一路带到 getOrientation 的角度里，
        // 直接 roundToInt 会崩，所以先做有限性判断。
        val haveBoth = isFinite(accelerometerReading) && isFinite(magnetometerReading)
        if (haveBoth && SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            accMagOk = true
            val axes = remapAxes()
            val remappedMatrix = FloatArray(9)
            if (!SensorManager.remapCoordinateSystem(rotationMatrix, axes[0], axes[1], remappedMatrix)) {
                System.arraycopy(rotationMatrix, 0, remappedMatrix, 0, rotationMatrix.size)
            }
            SensorManager.getOrientation(remappedMatrix, orientationAngles)

            // [0]=方位角，[1]=俯仰/倾斜，[2]=侧倾/旋转
            // 纯血鸿蒙传感器参考轴与安卓差 90°，绝对方位做统一补偿（与地图 CompassHelper 口径一致）
            val headingDeg = DeviceInfoUtil.absoluteHeading(
                normDegree(Math.toDegrees(orientationAngles[0].toDouble()).toFloat())
            )
            // pitch 按设备习惯取反：镜头/屏幕上扬为正倾角
            val tiltDeg = -Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val rollDeg = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            // 二次兜底：万一角度仍是 NaN/无穷，显示 -- 而非崩溃
            vb.tvHeading.text = angleOrDash(headingDeg)
            vb.tvTilt.text = angleOrDash(tiltDeg)
            vb.tvRoll.text = angleOrDash(rollDeg)
        }

        refreshTestPanel(accMagOk)
    }

    /** 数组内是否全部为有限值（无 NaN / 无穷） */
    private fun isFinite(f: FloatArray): Boolean {
        for (v in f) if (v.isNaN() || v.isInfinite()) return false
        return true
    }

    /** 角度安全转字符串：NaN/无穷显示 --，否则四舍五入加 ° */
    private fun angleOrDash(v: Float): String =
        if (v.isNaN() || v.isInfinite()) "--" else "${v.roundToInt()}°"

    /**
     * 候选融合传感器（旋转向量等）算出姿态三角度。
     * 输出与 [updatePose] 同一约定：方位 [0,360) 正北0 顺时针，俯仰取反（上扬为正），侧倾顺时针。
     * 方位统一走 [DeviceInfoUtil.absoluteHeading] 做鸿蒙 +90° 补偿（非鸿蒙为无操作）。
     */
    private fun computePoseFromRotationVector(values: FloatArray, out: FloatArray) {
        out[0] = Float.NaN
        out[1] = Float.NaN
        out[2] = Float.NaN

        val m = FloatArray(9)
        // 注意：getRotationMatrixFromVector 在现代 SDK 里返回 void，数值非法时抛 IllegalArgumentException，
        // 而非像 getRotationMatrix 那样返回 boolean，所以用 try/catch 兜底。
        if (values.size < 3) return
        try {
            SensorManager.getRotationMatrixFromVector(m, values)
        } catch (e: Exception) {
            return
        }
        val axes = remapAxes()
        val mr = FloatArray(9)
        if (!SensorManager.remapCoordinateSystem(m, axes[0], axes[1], mr)) {
            System.arraycopy(m, 0, mr, 0, m.size)
        }
        val o = FloatArray(3)
        SensorManager.getOrientation(mr, o)
        out[0] = normDegree(Math.toDegrees(o[0].toDouble()).toFloat())
        out[1] = -Math.toDegrees(o[1].toDouble()).toFloat()
        out[2] = Math.toDegrees(o[2].toDouble()).toFloat()
        // 鸿蒙上传感器偏 90°，补偿成与 updatePose/CompassHelper 一致的真实方位（非鸿蒙为无操作）
        out[0] = DeviceInfoUtil.absoluteHeading(out[0])
    }

    /** 按当前屏幕旋转重映射参考轴（横/竖屏都正确） */
    private fun remapAxes(): IntArray = when (displayRotation) {
        Surface.ROTATION_90 -> intArrayOf(
            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_Z
        )
        Surface.ROTATION_180 -> intArrayOf(
            SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_Z
        )
        Surface.ROTATION_270 -> intArrayOf(
            SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, SensorManager.AXIS_Z
        )
        else -> intArrayOf(
            SensorManager.AXIS_X, SensorManager.AXIS_Y, SensorManager.AXIS_Z
        )
    }

    private fun normDegree(degree: Float): Float {
        var d = degree % 360f
        if (d < 0f) d += 360f
        return d
    }

    /** 刷新测试面板：逐条展示各通道是否可用 + 实时三角度，并附带磁力计原始值帮助定位"磁力全 0"问题 */
    private fun refreshTestPanel(accMagOk: Boolean) {
        val sb = StringBuilder(128)
        sb.append("现有 磁力+加速度 ：").append(if (accMagOk) "√" else "×").append('\n')
        for (ch in poseChannels) {
            sb.append(ch.label)
                .append('(').append(if (ch.has) "√" else "×").append(")：")
                .append(degText(ch.pose[0])).append("  ")
                .append(degText(ch.pose[1])).append("  ")
                .append(degText(ch.pose[2])).append('\n')
        }
        // 磁力计原始值：鸿蒙5上常见"有传感器但恒定 0"，因此也展示出来
        val magExists = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
        sb.append("磁力原始(").append(if (magExists) "√" else "×").append(")：x ")
            .append(rawText(magnetometerReading[0])).append("  y ")
            .append(rawText(magnetometerReading[1])).append("  z ")
            .append(rawText(magnetometerReading[2])).append('\n')
        // 罗盘回退链当前落点（实时），一个空行隔开
        sb.append('\n')
            .append("主地图 CompassHelper 来源：").append(compassProbe.currentSourceLabel)
            .append('\n').append("（数值顺序：方位 / 俯仰 / 侧倾；√=检测到传感器，--=暂无有效值）")
        vb.tvSensorTest.text = sb.toString()
    }

    private fun degText(v: Float): String = if (v.isNaN()) "--" else "${v.roundToInt()}°"

    private fun rawText(v: Float): String =
        if (v.isNaN()) "--" else String.format(Locale.CHINA, "%.1f", v)

    /** 当前显示屏旋转（用于重映射传感器参考轴，兼容横/竖屏） */
    private val displayRotation: Int
        get() = try {
            @Suppress("DEPRECATION")
            (getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay.rotation
        } catch (e: Exception) {
            Surface.ROTATION_0
        }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /** 读缓存渲染当前定位详情 */
    private fun render() {
        val cached = LocationCacheManager.getLastKnownLocation()
        if (cached == null) {
            clearValues()
            vb.tvEmpty.text = "暂无定位数据\n请点击下方「获取最新定位」开始定位"
            return
        }

        vb.tvEmpty.text = ""
        vb.tvProvider.text = friendlyProvider(cached.provider)
        vb.tvTime.text = timeFormat.format(Date(cached.timestamp))
        vb.tvAccuracy.text = if (cached.accuracy > 0f) "${formatDecimal(cached.accuracy)} m" else "——"
        vb.tvAltitude.text = if (cached.altitude != 0.0) "${formatDecimal(cached.altitude)} m" else "——"
        vb.tvCoord.text = coordName(cached.provider)
        vb.tvLongitude.text = String.format(Locale.CHINA, "%.6f", cached.longitude)
        vb.tvLatitude.text = String.format(Locale.CHINA, "%.6f", cached.latitude)
    }

    private fun clearValues() {
        vb.tvProvider.text = "暂无定位"
        vb.tvTime.text = ""
        vb.tvAccuracy.text = "——"
        vb.tvAltitude.text = "——"
        vb.tvCoord.text = "——"
        vb.tvLongitude.text = "——"
        vb.tvLatitude.text = "——"
    }

    /** 触发一次真实定位（不走缓存）并回填页面 */
    private fun locateNow() {
        showLoading()
        locationManager.requestSingleLocation(
            showTip = false,
            useCache = false,
            onSuccess = { _, _, _, _, _, _ ->
                runOnUiThread {
                    hideLoading()
                    render()
                }
            },
            onFailure = { reason: LocationFailReason ->
                runOnUiThread {
                    hideLoading()
                    tip("定位失败：${reason.msg}")
                    render()
                }
            }
        )
    }

    private fun friendlyProvider(provider: String): String = when (provider.uppercase()) {
        "HUAWEI", "FUSED" -> "华为定位"
        "BAIDU" -> "百度定位"
        "GPS" -> "原生GPS"
        "NETWORK" -> "网络定位"
        "PASSIVE" -> "被动定位"
        else -> provider
    }

    private fun coordName(provider: String): String = when (provider.uppercase()) {
        "BAIDU" -> "BD09LL"
        "HUAWEI" -> "WGS84"
        else -> "WGS84"
    }

    private fun formatDecimal(value: Float): String = String.format(Locale.CHINA, "%.1f", value)
    private fun formatDecimal(value: Double): String = String.format(Locale.CHINA, "%.1f", value)
}