package com.jwch.gwyt_project.adapter.overlayanalysis

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.model.InterSectionModel
class OverlayLevel1(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {


    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as InterSectionModel?

        helper.let {
            item?.apply {
                helper.setImageResource(R.id.id_treenode_icon, if (isExpanded) R.mipmap.tree_ex else R.mipmap.tree_ec)
                when(listType){
                    0 ->{
                        helper.setText(R.id.id_treenode_label, "图层明细：${layerName}")
                    }
                    1 ->{
                        helper.setText(R.id.id_treenode_label, "图层分组：${layerName}")
                    }
                }

            }
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        getAdapter()!!.expandOrCollapse(position)
    }
}