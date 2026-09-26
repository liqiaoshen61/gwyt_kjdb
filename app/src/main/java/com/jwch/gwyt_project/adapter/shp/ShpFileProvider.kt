package com.jwch.gwyt_project.adapter.shp

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.yes

class ShpFileProvider(override val itemViewType: Int, override val layoutId: Int) :
    BaseNodeProvider() {

    private var padding = 0
    private var paddingLeft = 0

    init {
        addChildClickViewIds(R.id.ivLayerCheck)
    }

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        if (padding == 0) {
            padding = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize8)
            paddingLeft =
                context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize8)
        }

        val item = data as ShpItem
        val model = item.shpModel ?: return

        val featureLabel = if (model.featureCount >= 0) {
            "${model.featureCount}"
        } else {
            "N/A"
        }
        val displayName = "${model.name.substringBeforeLast(".").self()} ($featureLabel)"
        helper.setText(R.id.tvFileName, displayName)

        // 选择框图标
        val ivCheck = helper.getView<ImageView>(R.id.ivLayerCheck)
        item.select.yes {
            ivCheck.setImageResource(R.mipmap.icon_square_select)
        }.no {
            ivCheck.setImageResource(R.mipmap.icon_square_unselect)
        }

        // 删除按钮
        helper.getView<ImageView>(R.id.ivDelete).setOnClickListener {
            (getAdapter() as? ShpTreeAdapter)?.onDeleteItem?.invoke(item)
        }

        // 根目录下隐藏占位图标，子级保留对齐
        val ivTreeNode = helper.getView<ImageView>(R.id.id_treenode_icon)
        ivTreeNode.visibility = if (item.levelIndex == 0) View.GONE else View.INVISIBLE

        val viewContent = helper.getView<LinearLayout>(R.id.llFileItem)
        val leftPad = if (item.levelIndex == 0) padding * 2 else paddingLeft * (item.levelIndex - 1) + padding
        viewContent.setPadding(leftPad, padding, padding, padding)
    }
}
