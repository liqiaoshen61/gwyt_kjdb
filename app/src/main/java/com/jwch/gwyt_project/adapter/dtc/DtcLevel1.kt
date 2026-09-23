package com.jwch.gwyt_project.adapter.dtc

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R

class DtcLevel1(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {


    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as DtcItem?
        helper.let {
            item?.apply {
                helper.setText(R.id.id_treenode_label, name)
                    .setImageResource(R.id.id_treenode_icon, if (isExpanded) R.mipmap.tree_ex else R.mipmap.tree_ec)
                val icon = helper.getView<ImageView>(R.id.id_treenode_icon)

                val parmas = icon.layoutParams as LinearLayout.LayoutParams
                val dimen  = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize10)
                parmas.leftMargin = dimen.times(index)
            }
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        getAdapter()!!.expandOrCollapse(position)
    }
}