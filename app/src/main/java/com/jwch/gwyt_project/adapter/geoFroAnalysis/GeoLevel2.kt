package com.jwch.gwyt_project.adapter.geoFroAnalysis

import android.view.View
import android.widget.LinearLayout
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.GeoCollectionModel
import com.jwch.gwyt_project.fragment.CollectionFragment


class GeoLevel2(override val itemViewType: Int, override val layoutId: Int, val listener: ActionListener) : BaseNodeProvider() {
    protected var dimen = 0

    init {
    }

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as GeoCollectionModel?

        helper.let {
            item?.apply {
                it.setText(R.id.tvName, markerInfo?.name.self())
            }
        }

        if (dimen == 0) {
            dimen = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize6)
        }

        var llMarkerItem = helper.getView<LinearLayout>(R.id.llMarkerItem)
        llMarkerItem.setPadding(if (item?.markerInfo?.folderId == 0) dimen else dimen.times(4), 0, 0, 0)
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
//        getAdapter()!!.expandOrCollapse(position)
        val item = data as GeoCollectionModel

        listener.onAction(item, CollectionFragment.ACTION_DRAW)

    }

    override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onChildClick(helper, view, data, position)

        getAdapter()?.notifyDataSetChanged()
    }

}