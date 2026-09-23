package com.jwch.gwyt_project.model

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.Info.FolderInfo
import com.jwch.gwyt_project.Info.MarkerInfo
/**
 * 标绘收藏列表模型
 */
class GeoCollectionModel : BaseExpandNode {
    companion object {
        const val LEVEL1 = 0 //标绘文件夹
        const val LEVEL2 = 1 //标绘结果
    }

    var itemLevel: Int  //item级别
    var folderData: FolderInfo? = null // 文件夹信息
    var markerInfo: MarkerInfo? = null//标绘信息
    override var childNode: MutableList<BaseNode>? = null
    var folderName = ""//标绘锁在的文件夹名称，导出标绘到excel时候用这个字段

    var isFolder = false //是否为文件夹
    var isSelect = false //是否选中

    constructor(folderData: FolderInfo?) : super() {
        this.folderData = folderData
        this.itemLevel = LEVEL1
        isFolder = true
        isExpanded = false
    }

    constructor(markerInfo: MarkerInfo?, itemLevel: Int) : super() {
        this.markerInfo = markerInfo
        this.itemLevel = itemLevel
        isFolder = false
    }


    fun addChildNode(node: GeoCollectionModel){
        if (childNode == null) {
            childNode = mutableListOf()
        }

        childNode!!.add(node)
    }
}
