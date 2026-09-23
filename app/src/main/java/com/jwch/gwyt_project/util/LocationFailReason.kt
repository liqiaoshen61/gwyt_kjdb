package com.jwch.gwyt_project.util

/**
 * 定位失败原因枚举
 * 用于区分定位失败的具体根因，便于提示用户与线上排查
 */
enum class LocationFailReason(val msg: String) {
    GPS_DISABLED("GPS未开启"),
    PERMISSION_DENIED("定位权限不足"),
    TTFF_TIMEOUT("GPS冷启动超时，请到开阔地带重试"),
    ACCURACY_TOO_LOW("卫星信号弱"),
    HMS_UNAVAILABLE("HMS Core定位服务不可用"),
    PROVIDER_FAILED("定位服务失效"),
    ALL_PROVIDERS_FAILED("所有定位方式均失败"),
    USER_CANCELED("已取消定位")
}
