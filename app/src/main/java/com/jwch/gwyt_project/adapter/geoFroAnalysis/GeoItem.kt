package com.jwch.gwyt_project.adapter.geoFroAnalysis

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.Info.GraphicInfo


class GeoItem : BaseExpandNode {


    companion object {
        const val LEVEL1 = 0 //标绘文件夹
        const val LEVEL2 = 1 //标绘结果
    }

    var itemLevel : Int  //item级别
    override var childNode: MutableList<BaseNode>? = null
    var name: String? = null
    var isCheck = false
    var id : Int? = null//当前id
    var pid : Int? = null //fid -1表示该记录为文件夹 -2表示不在文件夹下的标绘 对饮文件夹id标出存放在文件夹内的标绘
    var isSelect = false //是否选中


    var graphicInfo : GraphicInfo? = null


    constructor(itemLevel: Int) {
        this.itemLevel = itemLevel
    }

    constructor(itemLevel: Int, childNode: List<BaseNode?>?, name: String?) {
        this.itemLevel = itemLevel
        this.name = name
    }

}