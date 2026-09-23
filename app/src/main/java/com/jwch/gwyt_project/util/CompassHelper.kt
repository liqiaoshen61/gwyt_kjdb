package com.jwch.gwyt_project.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Surface
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 设备朝向（罗盘）提供器，带「来源自动降级」回退链。
 *
 * 与定位接口的 bearing 无关 —— 那个字段只在设备移动（约 speed > 0.5m/s）时才有值，
 * 野外站着打点恒为 0，不能用于此场景。
 *
 * 姿态来源按优先级依次尝试，前面的失效才降级到后面的，保证普通安卓设备的磁力行为
 * 与历史版本一致（不因加了回退而变差）：
 *  1. MAG（加速度计 + 磁力计）：有绝对北向，与历史实现完全一致，磁力正常时永不降级。
 *  2. ROTATION_VECTOR（标准旋转向量）：陀螺 + 磁力融合。磁力同源的 ROM 上多半与 MAG 同进退。
 *  3. GAME_ROTATION_VECTOR（游戏旋转向量）：纯陀螺、无磁力，**无绝对北向、会漂移**，
 *     仅作为鸿蒙等设备磁力失效时的兜底。用 [syncHeading]（定位 bearing）做周期校正。
 *
 * 失效判定：
 *  - MAG、ROTATION_VECTOR 这类依赖磁力的：在 [FALLBACK_GRACE_MS] 内一直算不出有效姿态 → 降级；
 *  - ROTATION_VECTOR / GAME 这类"总能返回向量值"的：即使有值，若**设备在动但方位长期不变**
 *    （磁力失效冻结的典型特征）→ 也判定失效降级。
 *    为此用陀螺仪做运动检测（[isMoving]）。
 *
 * 输出约定：heading ∈ [0, 360)，正北为 0°，顺时针递增。
 * 回调与 [start]/[stop] 均运行在主线程。
 */
class CompassHelper(context: Context) : SensorEventListener {

    companion object {
        /** 回调限频（毫秒）。地图符号改角度不算贵，但也不值得按传感器频率刷 */
        private const val EMIT_INTERVAL_MS = 100L

        /** 低通滤波系数：越小越平滑、越滞后 */
        private const val FILTER_ALPHA = 0.18f

        /** 当前来源持续失效多久后降级到下一个可用来源（毫秒） */
        private const val FALLBACK_GRACE_MS = 5000L

        /** 角速度高于此值视为"设备在动"（rad/s），用于识别向量类来源"不响应转动" */
        private const val MOTION_THRESHOLD_RAD_S = 0.4f
    }

    /** 姿态来源优先级 */
    private enum class Source(val label: String) {
        MAG("磁力+加速度"),
        ROTATION_VECTOR("旋转向量"),
        GAME_ROTATION_VECTOR("游戏旋转向量")
    }

    private val appContext: Context = context.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationVector: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gameRotation: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val gyroscope: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // NaN 表示"尚未收到事件"，区别于"收到但值为 0"
    private val accelerometerReading = FloatArray(3) { Float.NaN }
    private val magnetometerReading = FloatArray(3) { Float.NaN }
    private val rotationVecReading = FloatArray(3) { Float.NaN }
    private val gameVecReading = FloatArray(3) { Float.NaN }
    private val gyroReading = FloatArray(3)

    private var running = false
    private var lastEmit = 0L

    /** 当前生效的姿态来源 */
    private var current = firstAvailableSource()

    /** 每个来源最后一次"成功算出姿态"的时刻，用于 MAG 类超时降级 */
    private val lastValidBySource = HashMap<Source, Long>()

    /** 当前来源最后一次"方位产生实质变化"的时刻，用于向量类不响应转动的降级 */
    private var lastAngleChange = 0L

    /** 是否正在转动（由陀螺仪判定） */
    private var isMoving = false

    /** 游戏旋转向量相对基准偏移：输出 = 原始 - offset，用以把漂移扳回绝对方位 */
    private var gameOffset = 0f

    /** 最近一次由游戏旋转向量算出的原始方位 */
    private var latestGameRaw = 0f

    /** 平滑后的朝向，[0, 360)，正北=0，顺时针。尚未算出有效值时是 null */
    var heading: Float? = null
        private set

    /** 朝向变化回调，运行在主线程 */
    var onHeadingChanged: ((Float) -> Unit)? = null

    /** 当前生效的姿态来源（诊断/测试面板展示用） */
    val currentSourceLabel: String
        get() = current.name

    /** 是否有至少一条姿态来源可用 */
    val isAvailable: Boolean
        get() = sources().any { sourceHasSensors(it) }

    fun start() {
        if (running || sensorManager == null) return
        if (!isAvailable) return
        running = true
        register(accelerometer)
        register(magnetometer)
        register(rotationVector)
        register(gameRotation)
        register(gyroscope) // 运动检测辅助
        resetCounters()
    }

    fun stop() {
        if (!running) return
        running = false
        sensorManager?.unregisterListener(this)
    }

    /**
     * 用外部绝对方位校正当前朝向（如定位 bearing），纠正游戏旋转向量的漂移。
     *
     * 只对游戏旋转向量（无绝对北、会累积偏移）生效；磁力 / 旋转向量自身有绝对北向，
     * 不应被 GPS"行进方向"覆盖——那通常比磁力方位更吵、低速时还是陈旧值，
     * 硬覆盖正是"定位后箭头猛转 / 连续几秒顶在同一错误角度"的根源。
     *
     * 校正方式：只更新 GAME 的基准偏移，让 [updateHeading] 的低通滤波在约百毫秒内
     * 平滑趋近目标，避免绕过滤波一步到位（那样每次 fix 都会造成朝向跳变）。
     */
    fun syncHeading(absoluteDegrees: Float) {
        if (!running) return
        if (current != Source.GAME_ROTATION_VECTOR) return
        val abs = normalize(absoluteDegrees)
        gameOffset = normalize(latestGameRaw - abs)
        // 刷新"最后一次方位变化"时刻，避免持续在动的 GAME 被误判为失效而降级
        lastAngleChange = SystemClock.elapsedRealtime()
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER ->
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)

            Sensor.TYPE_MAGNETIC_FIELD ->
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)

            Sensor.TYPE_ROTATION_VECTOR ->
                System.arraycopy(event.values, 0, rotationVecReading, 0, rotationVecReading.size)

            Sensor.TYPE_GAME_ROTATION_VECTOR ->
                System.arraycopy(event.values, 0, gameVecReading, 0, gameVecReading.size)

            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyroReading, 0, gyroReading.size)
                updateMotion()
            }

            else -> return
        }
        // 先判定当前来源是否失效、需要降级（每次事件都查，事件频率够高）
        ensureSourceValid()
        // 再限频计算并发射 heading
        updateHeading()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 磁力计精度变化无需特殊处理：滤波能吃掉大部分噪声
    }

    /** 判据失效则顺位降级到下一个可用来源 */
    private fun ensureSourceValid() {
        val now = SystemClock.elapsedRealtime()
        val invalid = when (current) {
            Source.MAG -> now - (lastValidBySource[current] ?: 0L) > FALLBACK_GRACE_MS
            // 向量类总能返回"一个值"，用"在动但方位长期不变"判它是否失效
            Source.ROTATION_VECTOR,
            Source.GAME_ROTATION_VECTOR -> isMoving && now - lastAngleChange > FALLBACK_GRACE_MS
        }
        if (!invalid) return

        val ordered = sources()
        val next = ordered.dropWhile { it != current }.drop(1).firstOrNull { sourceHasSensors(it) }
        if (next == null) return // 已是最后一个可用来源

        current = next
        lastAngleChange = now // 给新来源独立的宽限窗口
        lastValidBySource[current] = now
    }

    private fun updateHeading() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastEmit < EMIT_INTERVAL_MS) return
        lastEmit = now

        val euler = computeSourceEuler(current) ?: return
        // 纯血鸿蒙上传感器参考轴与安卓差 90°，对"原始读数"统一校正（所有来源都补，含游戏旋转向量）。
        // 这里校正的是 raw；syncHeading 的 bearing 是地理真北、不经过 absoluteHeading，
        // 因此 GAME 的 gameOffset（= 校正后 raw - bearing）恰好把 +90 抵消掉，运动中不会重复补偿。
        val raw = DeviceInfoUtil.absoluteHeading(euler[0])

        val effective = if (current == Source.GAME_ROTATION_VECTOR) {
            latestGameRaw = raw
            normalize(raw - gameOffset)
        } else {
            raw
        }

        val prev = heading
        val next = if (prev == null) effective else normalize(prev + shortestDelta(prev, effective) * FILTER_ALPHA)
        heading = next

        // 有实质方位变化则刷新"最后一次变化"时刻（否在动但方位不变的判据会触发）
        if (prev == null || abs(shortestDelta(prev, next)) >= 1f) lastAngleChange = now

        onHeadingChanged?.invoke(next)
    }

    /** 计算某来源的姿态欧拉角，成功返回 [head, pitch, roll]，失败返回 null */
    private fun computeSourceEuler(s: Source): FloatArray? {
        val m = FloatArray(9)
        when (s) {
            Source.MAG -> {
                if (accelerometer == null || magnetometer == null) return null
                if (!finite(accelerometerReading) || !finite(magnetometerReading)) return null
                if (!SensorManager.getRotationMatrix(m, null, accelerometerReading, magnetometerReading)) return null
            }

            Source.ROTATION_VECTOR -> {
                if (rotationVector == null) return null
                if (rotationVecReading.size < 3 || !finite(rotationVecReading)) return null
                if (!vecToMatrix(rotationVecReading, m)) return null
            }

            Source.GAME_ROTATION_VECTOR -> {
                if (gameRotation == null) return null
                if (gameVecReading.size < 3 || !finite(gameVecReading)) return null
                if (!vecToMatrix(gameVecReading, m)) return null
            }
        }

        val axes = remapAxes()
        val mr = FloatArray(9)
        if (!SensorManager.remapCoordinateSystem(m, axes[0], axes[1], mr)) {
            System.arraycopy(m, 0, mr, 0, m.size)
        }
        val o = FloatArray(3)
        SensorManager.getOrientation(mr, o)
        if (!finite(o)) return null

        lastValidBySource[s] = SystemClock.elapsedRealtime()
        return floatArrayOf(
            normalize(Math.toDegrees(o[0].toDouble()).toFloat()),
            -Math.toDegrees(o[1].toDouble()).toFloat(),
            Math.toDegrees(o[2].toDouble()).toFloat()
        )
    }

    /** 旋转向量 → 旋转矩阵。注意该方法返回 void、非法向量会抛异常，故用 try/catch */
    private fun vecToMatrix(v: FloatArray, m: FloatArray): Boolean =
        try {
            SensorManager.getRotationMatrixFromVector(m, v)
            true
        } catch (e: Exception) {
            false
        }

    /** 陀螺仪判"设备是否在动" */
    private fun updateMotion() {
        val t = sqrt(
            gyroReading[0] * gyroReading[0] +
                gyroReading[1] * gyroReading[1] +
                gyroReading[2] * gyroReading[2]
        )
        isMoving = t >= MOTION_THRESHOLD_RAD_S
    }

    /**
     * 按当前屏幕旋转重映射参考轴。
     * getOrientation 的轴固定在竖屏自然方向，主界面是 landscape 锁定，必需重映射，否则整体偏 90°。
     */
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

    /** 计算箭头在屏幕上应旋转到的角度（减去地图旋转补偿） */
    fun screenAngle(mapRotation: Float): Float {
        val h = heading ?: return 0f
        return normalize(h - mapRotation)
    }

    private fun register(s: Sensor?) {
        if (s != null) {
            sensorManager?.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME, mainHandler)
        }
    }

    private fun resetCounters() {
        current = firstAvailableSource()
        lastValidBySource.clear()
        for (s in sources()) if (sourceHasSensors(s)) lastValidBySource[s] = SystemClock.elapsedRealtime()
        lastAngleChange = SystemClock.elapsedRealtime()
        isMoving = false
        gameOffset = 0f
        latestGameRaw = 0f
        lastEmit = 0L
        heading = null
    }

    private fun sources(): List<Source> = listOf(
        Source.MAG, Source.ROTATION_VECTOR, Source.GAME_ROTATION_VECTOR
    )

    private fun sourceHasSensors(s: Source): Boolean = when (s) {
        Source.MAG -> accelerometer != null && magnetometer != null
        Source.ROTATION_VECTOR -> rotationVector != null
        Source.GAME_ROTATION_VECTOR -> gameRotation != null
    }

    private fun firstAvailableSource(): Source =
        sources().firstOrNull { sourceHasSensors(it) } ?: Source.MAG

    private val displayRotation: Int
        get() = try {
            @Suppress("DEPRECATION")
            (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        } catch (e: Exception) {
            Surface.ROTATION_0
        }

    private fun normalize(degree: Float): Float {
        var d = degree % 360f
        if (d < 0f) d += 360f
        return d
    }

    private fun shortestDelta(from: Float, to: Float): Float {
        var delta = (to - from) % 360f
        if (delta > 180f) delta -= 360f
        if (delta <= -180f) delta += 360f
        return delta
    }

    private fun finite(f: FloatArray): Boolean {
        for (v in f) if (v.isNaN() || v.isInfinite()) return false
        return true
    }
}