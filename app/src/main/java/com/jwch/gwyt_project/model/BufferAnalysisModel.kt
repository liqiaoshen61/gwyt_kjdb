package com.jwch.gwyt_project.model

class BufferAnalysisModel  {

    var layerName : String = ""
    var geometryJson : String? = null
    var centerPointJson : String? = null
    var map = HashMap<String, Any>() //展示callout需要的数据
    var isSecletd = false
}