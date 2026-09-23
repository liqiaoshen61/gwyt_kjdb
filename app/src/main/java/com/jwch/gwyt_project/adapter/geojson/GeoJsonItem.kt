package com.jwch.gwyt_project.adapter.geojson

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.model.GeoJsonModel

class GeoJsonItem : BaseExpandNode {

    companion object {
        const val TYPE_FOLDER = 0
        const val TYPE_FILE = 1
    }

    var itemType: Int = TYPE_FOLDER
    var name: String = ""
    var filePath: String = ""
    var levelIndex: Int = 0
    var select: Boolean = false

    // 文件节点
    var geoJsonModel: GeoJsonModel? = null

    override var childNode: MutableList<BaseNode>? = null

    constructor(type: Int) {
        this.itemType = type
        this.isExpanded = false
    }

    fun addChildNode(item: BaseNode) {
        if (childNode == null) {
            childNode = mutableListOf()
        }
        childNode!!.add(item)
    }
}