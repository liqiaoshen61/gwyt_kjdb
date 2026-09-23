package com.jwch.gwyt_project.adapter.dtc

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

class DtcItem : BaseExpandNode {


    companion object {
        const val LEVEL1 = 0
        const val LEVEL2 = 1
        const val LEVEL3 = 2
    }

    var index = 1

    var itemLevel : Int  //item级别
    override var childNode: MutableList<BaseNode>? = null
    var isCheck = false

    var name: String? = null // 名称
    var path: String? = null //路径
    var parentPath: String? = null//上级路径

    constructor(itemLevel: Int) {
        this.itemLevel = itemLevel
    }

    constructor(itemLevel: Int, childNode: List<BaseNode?>?, name: String?) {
        this.itemLevel = itemLevel
        this.name = name
    }

}