package com.jwch.gwyt_project.adapter.area

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.Info.DistrictsInfo

class AreaItem : BaseExpandNode {


    companion object {
        const val LEVEL1 = 0 //县级
        const val LEVEL2 = 1 //镇级
        const val LEVEL3 = 2 //村级
    }

    var itemLevel : Int  //item级别
    override var childNode: MutableList<BaseNode>? = null
    var name: String? = null
    var isCheck = false
    var id : Long? = null//当前id
    var fid : Long? = null //父id
    var x = 0.0  //纬度
    var y = 0.0  //经度

    var districts : DistrictsInfo? = null
    constructor(itemLevel: Int) {
        this.itemLevel = itemLevel
    }

    constructor(itemLevel: Int, childNode: List<BaseNode?>?, name: String?) {
        this.itemLevel = itemLevel
        this.name = name
    }

}