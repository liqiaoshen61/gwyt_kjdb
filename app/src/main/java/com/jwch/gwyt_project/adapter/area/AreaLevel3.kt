package com.jwch.gwyt_project.adapter.area

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.esri.arcgisruntime.geometry.Point
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.model.MapEvent
import org.greenrobot.eventbus.EventBus

class AreaLevel3(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {
    protected var dimen = 0

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as AreaItem?
        helper.let {
            item?.apply {
                helper.setText(R.id.id_treenode_label, name)

                val id_treenode_icon = it.getView<ImageView>(R.id.id_treenode_icon)
                id_treenode_icon.hide()
            }
        }

        if (dimen == 0) {
            dimen = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize5)
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
//        getAdapter()!!.expandOrCollapse(position)
        val item = data as AreaItem
        val point = Point(item.x, item.y)
        EventBus.getDefault().post(MapEvent(MapEvent.DRAW_AREA_POINT, point))
    }
}