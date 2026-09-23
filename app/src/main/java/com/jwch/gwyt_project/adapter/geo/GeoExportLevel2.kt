package com.jwch.gwyt_project.adapter.geo

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.selfTempString
import com.jwch.gwyt_project.model.GeoCollectionModel

class GeoExportLevel2(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {
    protected var dimen = 0

    init {
//        addChildClickViewIds(R.id.imgSelect)
    }

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as GeoCollectionModel?

        helper.let {
            item?.apply {
                val imgSelect = it.getView<ImageView>(R.id.imgSelect)


                it.setText(R.id.tvName, "${"${markerInfo?.city}${markerInfo?.county}${markerInfo?.town}"}")
                    .setText(R.id.tvTime, "${TimeUtil.getDateToString(markerInfo?.createTimeStamp.self(), Config.timeFormat1)}")

                imgSelect.setImageResource(isSelect.getOne(R.mipmap.icon_square_select, R.mipmap.icon_square_unselect))
            }
        }

        if (dimen == 0) {
            dimen = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize5)
        }

        var llMarkerItem = helper.getView<LinearLayout>(R.id.llMarkerItem)
        llMarkerItem.setPadding(if (item?.markerInfo?.folderId == 0) dimen.times(2) else dimen.times(7), 0, 0, 0)
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        val item = data as GeoCollectionModel
        item.isSelect = !item.isSelect
        getAdapter()?.notifyDataSetChanged()
    }
}