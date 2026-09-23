package com.jwch.gwyt_project.model

class AnalysisListModel () {

    var layerName : String = ""
    var groupField : String? = ""
    var geomterySize : Double = 0.0  //绘制区域的总面积

    var interSectionModel = mutableListOf<InterSectionModel>()

    var percentage : String = "0.0"  // 相交总面积/geomterySize 的占比

    var size =0.0 //相交的总面积

    constructor( layerName : String,  groupField : String? = "" ,  geomterySize : Double) : this() {
        this.layerName = layerName
        this.groupField = groupField
        this.geomterySize = geomterySize
    }

}