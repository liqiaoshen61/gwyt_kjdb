package com.jwch.gwyt_project.adapter.geo

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.GeoCollectionModel


/**
 * 标绘收藏夹adapter
 */
class GeoAdapter(listener: ActionListener) : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(GeoLevel1(GeoCollectionModel.LEVEL1, R.layout.item_marker_folder, listener))
        addNodeProvider(GeoLevel2(GeoCollectionModel.LEVEL2, R.layout.item_marker_collect, listener))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is GeoCollectionModel) {
            return node.itemLevel
        }
        return -1
    }

    fun clearData() {
        val list: List<BaseNode> = data
        unSelect(list)
        notifyDataSetChanged()
    }

    private fun unSelect(list: List<BaseNode>?) {
        for (item in list!!) {
            val itemdata = item as GeoCollectionModel
            itemdata.isSelect = false
            if (CommonUtil.matchList(itemdata.childNode)) {
                unSelect(itemdata.childNode)
            }
        }
    }

}