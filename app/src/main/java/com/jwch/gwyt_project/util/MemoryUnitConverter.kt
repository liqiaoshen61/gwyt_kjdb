package com.jwch.gwyt_project.util

object MemoryUnitConverter {
    // 字节转MB（保留两位小数）
    fun bytesToMB(bytes: Long): Double {
        return (bytes.toDouble() / (1024 * 1024)).roundTo(2)
    }

    // 扩展函数：保留指定位数小数
    private fun Double.roundTo(digits: Int): Double {
        var multiplier = 1.0
        repeat(digits) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}