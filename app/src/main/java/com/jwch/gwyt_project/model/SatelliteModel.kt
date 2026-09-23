package com.jwch.gwyt_project.model

import android.graphics.Color
import android.location.GnssStatus

/**
 * 单颗卫星信息（来自 GnssStatus）
 */
data class SatelliteModel(
    val constellationType: Int,  // GnssStatus.CONSTELLATION_*
    val svid: Int,               // 卫星编号（GPS 1-32、北斗 201-235、GLONASS 65-96 等）
    val cn0: Float,              // 信号强度 dB-Hz
    val elevation: Float,        // 仰角（度）
    val azimuth: Float,          // 方位角（度）
    val carrierFreqHz: Double,   // 载波频率 Hz，未知为 0
    val usedInFix: Boolean,      // 是否参与当前定位解算
    val hasEphemeris: Boolean    // 是否已获取星历
)

/** 星座中文/通用简称 */
fun constellationName(type: Int): String = when (type) {
    GnssStatus.CONSTELLATION_GPS -> "GPS"
    GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
    GnssStatus.CONSTELLATION_BEIDOU -> "北斗"
    GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
    GnssStatus.CONSTELLATION_QZSS -> "QZSS"
    GnssStatus.CONSTELLATION_SBAS -> "SBAS"
    GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
    else -> "未知"
}

/** 星座专属颜色（文字/信号条用） */
fun constellationColor(type: Int): Int = when (type) {
    GnssStatus.CONSTELLATION_GPS -> Color.parseColor("#4CAF50")
    GnssStatus.CONSTELLATION_GLONASS -> Color.parseColor("#F0A020")
    GnssStatus.CONSTELLATION_BEIDOU -> Color.parseColor("#F5222D")
    GnssStatus.CONSTELLATION_GALILEO -> Color.parseColor("#40A9FF")
    GnssStatus.CONSTELLATION_QZSS -> Color.parseColor("#B37FEB")
    GnssStatus.CONSTELLATION_SBAS -> Color.parseColor("#13C2C2")
    GnssStatus.CONSTELLATION_IRNSS -> Color.parseColor("#FA8C16")
    else -> Color.parseColor("#8C9BA8")
}

/**
 * 星座统计分组（顶部概览：GPS / 北斗 / GLONASS / Galileo / 其他）
 * 返回数组下标：0=GPS 1=北斗 2=GLONASS 3=Galileo 4=其他
 */
fun constellationGroup(type: Int): Int = when (type) {
    GnssStatus.CONSTELLATION_GPS -> 0
    GnssStatus.CONSTELLATION_BEIDOU -> 1
    GnssStatus.CONSTELLATION_GLONASS -> 2
    GnssStatus.CONSTELLATION_GALILEO -> 3
    else -> 4
}

/** 概览分组：名称（全称，含缩写与国别）与颜色（顺序与 constellationGroup 对应） */
val OVERVIEW_GROUPS: List<Pair<String, Int>> = listOf(
    "全球定位系统(GPS)美国" to Color.parseColor("#4CAF50"),
    "北斗卫星信号导航系统(BDS)中国" to Color.parseColor("#F5222D"),
    "格洛纳斯系统(GLONASS)俄罗斯" to Color.parseColor("#F0A020"),
    "伽利略系统(GALILEO)欧盟" to Color.parseColor("#40A9FF"),
    "其他" to Color.parseColor("#8C9BA8")
)
