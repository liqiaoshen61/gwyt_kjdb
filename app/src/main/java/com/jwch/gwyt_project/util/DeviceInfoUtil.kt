package com.jwch.gwyt_project.util

import android.os.Build

/**
 * 设备型号与系统信息判断。
 *
 * 用途：定位详情页展示当前设备信息，以及排查鸿蒙设备的行为差异（如磁力计/姿态传感器失效）。
 *
 * 注意鸿蒙的判定语义：本 app 能跑的鸿蒙设备都带安卓兼容层，版本号写进系统属性——
 * 但各代写入的键不同（鸿蒙3/4 写 `ro.build.version.harmonyos`，纯血 NEXT 5+ 写
 * `ro.build.version.ohos`）。这里会逐键探测只要是任一命中即视为鸿蒙。
 */
object DeviceInfoUtil {

    /** 设备型号，如 "ALT-AL00" / "Mate X5" */
    val model: String get() = Build.MODEL ?: "未知"

    /** 厂商，如 HUAWEI / HONOR / Xiaomi */
    val brand: String get() = Build.MANUFACTURER ?: Build.BRAND ?: "未知"

    /** Android 系统版本，如 "13"、"14" */
    val androidVersion: String get() = Build.VERSION.RELEASE ?: "未知"

    /** Android SDK 版本号 */
    val sdkInt: Int get() = Build.VERSION.SDK_INT

    /** 反射读系统属性（SystemProperties 非公开 API） */
    private fun readProp(key: String): String? = try {
        val c = Class.forName("android.os.SystemProperties")
        c.getMethod("get", String::class.java).invoke(null, key) as? String
    } catch (_: Throwable) {
        null
    }

    /**
     * 鸿蒙版本号常见的系统属性键（各代写入位置不同，需逐一代试）：
     *  - OpenHarmony/AOSP 兼容层（鸿蒙 3/4）写 `ro.build.version.harmonyos`；
     *  - 纯血 HarmonyOS NEXT（鸿蒙 5+）改成写 `ro.build.version.ohos` 等。
     * 实测 1T 鸿蒙5.1（NEXT）只在 ohos 键下有值，harmonyos 键读不到 → 只读一个键会误判成"非鸿蒙"。
     */
    private val HARMONY_BUILD_PROPS = listOf(
        "ro.build.version.harmonyos",
        "ro.build.version.ohos",
        "ro.harmony.version",
        "ro.build.version.harmony"
    )

    /** 鸿蒙系统版本号字符串；非鸿蒙为 null */
    val harmonyVersion: String?
        get() = HARMONY_BUILD_PROPS
            .map { readProp(it)?.takeIf { s -> s.isNotBlank() } }
            .firstOrNull { it != null }

    /** 是否鸿蒙系统 */
    val isHarmonyOs: Boolean
        get() = !harmonyVersion.isNullOrBlank()

    /** 鸿蒙主版本号；非鸿蒙返回 null */
    val harmonyMajor: Int?
        get() = harmonyVersion?.trim()?.split('.')?.firstOrNull()?.toIntOrNull()

    /** 是否鸿蒙主版本 >= n */
    fun harmonyAtLeast(n: Int): Boolean = (harmonyMajor ?: Int.MIN_VALUE) >= n

    /**
     * 纯血鸿蒙（NEXT，能装 APK 的兼容层）上传感器参考轴与安卓约定差 90°，导致
     * 由"绝对北"算出的方位整体偏 90°。实测：安卓设备朝向正南（应 180°）时，鸿蒙设备读成正东（90°）。
     * 因此需把头信息 +90°。2006-09 两台设备（1T 鸿蒙5.1）实测确认。
     */
    const val HARMONY_HEADING_OFFSET = 90f

    /**
     * 校正"由传感器算出的原始方位"为真实方位（度，[0,360)）。
     * 纯血鸿蒙 NEXT：原始方位 + [HARMONY_HEADING_OFFSET]（mod 360）；非鸿蒙原样返回。
     *
     * 补偿要套在"传感器原始读数"上，对所有来源统一生效（磁力/旋转向量/游戏旋转向量），
     * 因为鸿蒙上磁力常失效、实际走的是游戏旋转向量，而那台的方位同样偏 90°。
     * 调用方需在进入 GAME 的 gameOffset 换算 / syncHeading 之前对 raw 套一次本方法——
     * 这样 gameOffset 与 bearing（地理真北）加减后正好抵消，运动中有 bearing 时不会被重复 +90°。
     */
    fun absoluteHeading(deg: Float): Float =
        if (isHarmonyOs) {
            val d = (deg + HARMONY_HEADING_OFFSET) % 360f
            if (d < 0f) d + 360f else d
        } else {
            deg
        }

    /** 多行汇总：型号 / 厂商 / 安卓版本 / 鸿蒙版本与主版本判断。适合详情页直接展示 */
    fun summary(): String = StringBuilder().apply {
        append("型号：").append(model).append('\n')
        append("厂商：").append(brand).append('\n')
        append("安卓：").append(androidVersion).append(" (SDK ").append(sdkInt).append(')').append('\n')
        append("鸿蒙：").append(harmonyVersion ?: "非鸿蒙系统")
        harmonyMajor?.let {
            append("   ·  鸿蒙≥5：").append(if (it >= 5) "是" else "否")
        }
    }.toString()
}