package com.jwch.gwyt_project.adapter.dtc

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R

/**
 * 地图册adapter
 */
class DtcAdapter : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(DtcLevel1(DtcItem.LEVEL1, R.layout.item_map_group))
        addFullSpanNodeProvider(DtcLevel2(DtcItem.LEVEL2, R.layout.item_map_group2))
        addNodeProvider(DtcLevel3(DtcItem.LEVEL3, R.layout.list_item))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is DtcItem) {
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
            val itemdata = item as DtcItem
            itemdata.isCheck = false
            if (CommonUtil.matchList(itemdata.childNode)) {
                unSelect(itemdata.childNode)
            }
        }
    }

}