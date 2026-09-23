package com.jwch.gwyt_project.model

import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.ext.yes

class PointModel {

    var coordinateType = 0//坐标类型 0 球面坐标经纬度， 1 平面坐标 XY()  2 两种坐标都有
    var lat: Double = 0.0
    var lng: Double = 0.0

    var x: Double = 0.0//纬度
    var y: Double = 0.0//经度

    var des = ""//点的备注


    constructor() {}

    constructor(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
        coordinateType = 0
    }

    constructor(x: String, y: String) {
        CommonUtil.isNotEmpty(x).yes { this.x = x.toDouble() }
        CommonUtil.isNotEmpty(y).yes { this.y = y.toDouble() }
        coordinateType = 1
    }

    constructor(x: String, y: String, des: String) {
        CommonUtil.isNotEmpty(x).yes { this.x = x.toDouble() }
        CommonUtil.isNotEmpty(y).yes { this.y = y.toDouble() }
        this.des = des
        coordinateType = 1
    }

}