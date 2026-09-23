package com.jwch.gwyt_project.adapter.geo

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.model.GeoCollectionModel

class GeoExportLevel1(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as GeoCollectionModel?
        helper.let {
            item?.apply {
                it.setText(R.id.tvName, folderData?.name.self())
                    .setImageResource(R.id.imgFolder, if (isExpanded) R.mipmap.folder_open else R.mipmap.folder_closed)
            }
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        getAdapter()!!.expandOrCollapse(position)
    }
}