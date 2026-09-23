package com.jwch.gwyt_project.adapter.ztt

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R

/**
 * 专题图adapter
 */
class ZttAdapter : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(ZttLevel1(ZttItem.LEVEL1, R.layout.item_map_group))
        addNodeProvider(ZttLevel3(ZttItem.LEVEL2, R.layout.item_map_switch))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is ZttItem) {
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
            val itemdata = item as ZttItem
            itemdata.isCheck = false
            if (CommonUtil.matchList(itemdata.childNode)) {
                unSelect(itemdata.childNode)
            }
        }
    }

}