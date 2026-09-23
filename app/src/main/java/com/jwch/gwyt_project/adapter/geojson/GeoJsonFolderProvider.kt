package com.jwch.gwyt_project.adapter.geojson

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R

class GeoJsonFolderProvider(override val itemViewType: Int, override val layoutId: Int) :
    BaseNodeProvider() {

    private var padding = 0

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        if (padding == 0) {
            padding = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize6)
        }

        val item = data as GeoJsonItem
        helper.setText(R.id.id_treenode_label, item.name)
            .setImageResource(
                R.id.id_treenode_icon,
                if (item.isExpanded) R.mipmap.tree_ex else R.mipmap.tree_ec_right
            )

        val viewContent = helper.getView<android.widget.LinearLayout>(R.id.itemContent)
        viewContent.setPadding(padding * item.levelIndex, padding, padding, padding)

        val ivCheck = helper.getView<ImageView>(R.id.ivLayerCheck)
        ivCheck.visibility = View.GONE

        // 删除按钮
        helper.getView<ImageView>(R.id.ivDelete).setOnClickListener {
            (getAdapter() as? GeoJsonTreeAdapter)?.onDeleteItem?.invoke(item)
        }
    }
}