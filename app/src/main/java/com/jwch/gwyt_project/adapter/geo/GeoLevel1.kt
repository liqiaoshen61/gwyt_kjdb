package com.jwch.gwyt_project.adapter.geo

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.GeoCollectionModel
import com.jwch.gwyt_project.fragment.CollectionFragment


class GeoLevel1(override val itemViewType: Int, override val layoutId: Int, val listener: ActionListener) : BaseNodeProvider() {

    init {
        addChildClickViewIds(R.id.imgDelete)
    }


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

    override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onChildClick(helper, view, data, position)
        when (view.id) {
            //删除
            R.id.imgDelete -> {
                //删除文件夹
                listener.onAction(data, CollectionFragment.ACTION_DELETE_FOLDER)
            }
        }
    }

}