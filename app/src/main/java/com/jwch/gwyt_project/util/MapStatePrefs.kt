package com.jwch.gwyt_project.util

import android.content.Context

/**
 * 地图状态持久化工具类
 * 用于保存和恢复底图选择、地图视角等状态
 * 解决进程后台被杀后重建时状态丢失的问题
 */
object MapStatePrefs {
    private const val PREFS_NAME = "map_state"
    private const val KEY_BASE_MAP_NAME = "base_map_name"
    private const val KEY_MAP_CENTER_X = "map_center_x"
    private const val KEY_MAP_CENTER_Y = "map_center_y"
    private const val KEY_MAP_SCALE = "map_scale"

    /**
     * 保存当前底图名称
     */
    fun saveBaseMapName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_MAP_NAME, name)
            .apply()
    }

    /**
     * 获取保存的底图名称
     * @return 底图名称，如"影像底图2018"、"影像底图2022"等，未保存时返回null
     */
    fun getBaseMapName(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BASE_MAP_NAME, null)
    }

    /**
     * 保存地图视角状态
     * @param centerX 中心点X坐标
     * @param centerY 中心点Y坐标
     * @param scale 缩放级别
     */
    fun saveMapViewpoint(context: Context, centerX: Double, centerY: Double, scale: Double) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_MAP_CENTER_X, java.lang.Double.doubleToLongBits(centerX))
            .putLong(KEY_MAP_CENTER_Y, java.lang.Double.doubleToLongBits(centerY))
            .putLong(KEY_MAP_SCALE, java.lang.Double.doubleToLongBits(scale))
            .apply()
    }

    /**
     * 获取地图视角状态
     * @return Triple(centerX, centerY, scale)，未保存时返回null
     */
    fun getMapViewpoint(context: Context): Triple<Double, Double, Double>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val centerXBits = prefs.getLong(KEY_MAP_CENTER_X, 0L)
        val centerYBits = prefs.getLong(KEY_MAP_CENTER_Y, 0L)
        val scaleBits = prefs.getLong(KEY_MAP_SCALE, 0L)

        // 检查是否保存过（全0表示未保存）
        if (centerXBits == 0L && centerYBits == 0L && scaleBits == 0L) {
            return null
        }

        return Triple(
            java.lang.Double.longBitsToDouble(centerXBits),
            java.lang.Double.longBitsToDouble(centerYBits),
            java.lang.Double.longBitsToDouble(scaleBits)
        )
    }

    /**
     * 清除所有保存的状态
     */
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
