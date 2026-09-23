package com.jwch.gwyt_project.model

class EventData(eventType: Int) {
    var type: Int = 0
    lateinit var data: Any

    init {
        this.type = eventType
    }


    constructor(eventType: Int, data: Any) : this(eventType) {
        this.data = data
    }


    companion object {
        val SHOW_ZTT_LAYER = 1 //展示和隐藏图层
        val HIDE_ZTT_LAYER = 2//展示和隐藏图层
        val SHOW_All_EQU = 3 //展示所有设置
        val SHOW_CHENG_TOU_EQU = 4//城投养护设施
        val SHOW_LU_ZHENG_EQU = 5//路政设施
        val SHOW_XCKH = 6//现场考核
        val SHOW_KHJL = 7//考核记录
    }
}