package com.jwch.gwyt_project.adapter.area

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R

/**
 * 行政区划adapter
 */
class AreaAdapter : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(AreaLevel1(AreaItem.LEVEL1, R.layout.item_map_group))
        addFullSpanNodeProvider(AreaLevel2(AreaItem.LEVEL2, R.layout.item_map_group2))
        addNodeProvider(AreaLevel3(AreaItem.LEVEL3, R.layout.list_item))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is AreaItem) {
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
            val itemdata = item as AreaItem
            itemdata.isCheck = false
            if (CommonUtil.matchList(itemdata.childNode)) {
                unSelect(itemdata.childNode)
            }
        }
    }

}