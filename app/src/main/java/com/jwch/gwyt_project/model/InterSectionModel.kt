package com.jwch.gwyt_project.model

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.esri.arcgisruntime.geometry.Geometry

class InterSectionModel : BaseExpandNode {


    companion object {
        const val LEVEL1 = 0 //县级
        const val LEVEL2 = 1 //镇级
    }


    var geometry: Geometry? = null//相交部分的图形
    var areaSize = ""//有交集的面积大小
    var areaSizeValue: Double = 0.0
    var attr: MutableMap<String, Any>? = null
    var isSecletd = false  //是否选中
    var index = 0


    var itemLevel: Int = 0  //item级别
    override var childNode: MutableList<BaseNode>? = null
    var layerName = "aaaa"
    var geometryName = "bbb"
    var childTotalSize = ""//子项面积的总和
    var childTotalSizeValue: Double = 0.0

    var listType = 0  // 0明细 1分组

    constructor(geometry: Geometry?, areaSize: String, attr: MutableMap<String, Any>?) {
        this.geometry = geometry
        this.areaSize = areaSize
        this.attr = attr
    }

    constructor(itemLevel: Int, layerName: String) : super() {
        this.itemLevel = itemLevel
        this.layerName = layerName
    }

    constructor(geometry: Geometry?, areaSize: String, areaSizeValue: Double, attr: MutableMap<String, Any>?) {
        this.geometry = geometry
        this.areaSize = areaSize
        this.attr = attr
        this.areaSizeValue = areaSizeValue
    }

    fun addChildNode(data: InterSectionModel) {

        if (childNode == null) {
            childNode = mutableListOf()
        }
        childNode!!.add(data)

    }


}