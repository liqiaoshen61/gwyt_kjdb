package com.jwch.gwyt_project.util

object Keys {


    val ACCOUNT = "ACCOUNT"
    val PW = "PW"
    val UPDATE_APP = "UPDATE_APP"

    const val KEY_LAT = "LAT"
    const val KEY_LNG = "LNG"

    //是否默认加载城区高清影像图
    const val IS_DEFLOAD = "isDefLoad"

    const val IMG_URL_LIST = "imgUrls"
    const val IMG_MODEL_LIST = "imgModels"
    const val INDEX = "index"
    const val IMAGE_DES_VISIABLE = "desVisiable"
    const val IMAGE_LOAD_FROM_CACHE = "form_cache"

    //设置里 是否开启持续定位
    const val IS_CONTINUOUS_LOCATION = "isContinuousLocation"

    //水印相关配置
    const val WATERMARK_ENABLED = "watermarkEnabled"  //是否启用水印
    const val WATERMARK_CUSTOM_TEXT = "watermarkCustomText"  //水印自定义文字
    const val WATERMARK_TEXT_SIZE = "watermarkTextSize"  //水印文字大小
    const val WATERMARK_ALPHA = "watermarkAlpha"  //水印透明度 (0-100)
    const val WATERMARK_ANGLE = "watermarkAngle"  //水印倾斜角度 (-90 到 90度)

    //行政区划 统计列表结果 （第一次计算后保存，后面调用本地结果）
    const val AREA_ZTT_COUNT_LIST = "areaZttCountList"
    const val AREA_ZTT_PIE_LIST = "areaZttPieList"

    const val pageData = "pageData"


    const val USER_DATA = "userData"


    const val HISTORY_SEARCH_KEYWORD = "historySearchKeyword"


}