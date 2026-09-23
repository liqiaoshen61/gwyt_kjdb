package com.jwch.gwyt_project.model

import com.jwch.gwyt_project.util.PositionUtil

class NavigationPointModel {

    var name = ""  //
    var lat = 0.0  // 原始lat
    var lng = 0.0  // 原始lng

    var latBd09 = 0.0  // 百度坐标系的lat
    var lngBd09 = 0.0  // 百度坐标系的lng

    var latGcj02 = 0.0  // 高德坐标系的lat
    var lngGcj02 = 0.0  // 高德坐标系的lng


    constructor(name: String,  lat: Double, lng: Double) {
        this.name = name
        this.lat = lat
        this.lng = lng

        val bd09Point = PositionUtil.Gps84_To_bd09(lat, lng)
        latBd09 = bd09Point.lat
        lngBd09 = bd09Point.lng

        val gcj02Point = PositionUtil.gps84_To_Gcj02(lat, lng)

        latGcj02 = gcj02Point.lat
        lngGcj02 = gcj02Point.lng
    }



}