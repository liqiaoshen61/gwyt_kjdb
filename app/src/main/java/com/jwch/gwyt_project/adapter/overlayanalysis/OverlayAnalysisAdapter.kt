package com.jwch.gwyt_project.adapter.overlayanalysis

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.model.InterSectionModel

/**
 * 叠加分析adapter
 */
class OverlayAnalysisAdapter : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(OverlayLevel1(InterSectionModel.LEVEL1, R.layout.item_map_group))
        addNodeProvider(OverlayLevel2(InterSectionModel.LEVEL2, R.layout.item_map_layer_analysis))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is InterSectionModel) {
            return node.itemLevel
        }
        return -1
    }

    fun clearData() {
        val list: List<BaseNode> = data
        notifyDataSetChanged()
    }


}