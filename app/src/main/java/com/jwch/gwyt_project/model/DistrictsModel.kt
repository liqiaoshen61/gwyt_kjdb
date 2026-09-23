package com.jwch.gwyt_project.model

import com.esri.arcgisruntime.geometry.Geometry

class DistrictsModel  {

    var id: String? = null


    var distName: String? = null //地区名称
    var distLevel = 0 //地区级别
    var distCode: String? = null //地区行政编码

    var centerX = 0.0 //经度
    var centerY = 0.0 //纬度


    var isSelect = false
    var geometryList: List<Geometry>? = null


    companion object {
        const val LEVEL_PROVINCE: Int = 1 //省级
        const val LEVEL_CITY: Int = 2 //市级
        const val LEVEL_COUNTY: Int = 3 //区县级
        const val LEVEL_TOWN: Int = 4 //乡镇
        const val LEVEL_VILLAGE: Int = 5 //村庄
    }

}