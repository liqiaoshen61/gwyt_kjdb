package com.jwch.gwyt_project.adapter.overlayanalysis

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.InterSectionModel
import com.jwch.gwyt_project.model.MapEvent
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.backgroundResource

class OverlayLevel2(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {


    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as InterSectionModel?
        helper.let {
            item?.apply {
                helper.setText(R.id.tvName, "类型：${geometryName}")
                    .setText(R.id.tvAreaSize, "面积：${areaSize}")
                val itemContent = it.getView<LinearLayout>(R.id.itemContent)
                val tvName = it.getView<TextView>(R.id.tvName)
                val tvAreaSize = it.getView<TextView>(R.id.tvAreaSize)
                val vDashLine = it.getView<View>(R.id.vDashLine)

                (isSecletd).yes{
                    itemContent.backgroundResource = R.color.bg_village_select
                    tvName.setTextColor((Color.parseColor("#0664e5")))
                    tvAreaSize.setTextColor((Color.parseColor("#0664e5")))
                    vDashLine.backgroundResource = R.drawable.bg_dash_line_blue
                }.no{
                    itemContent.backgroundResource = R.color.transColor
                    tvName.setTextColor((Color.parseColor("#FFFFFF")))
                    tvAreaSize.setTextColor((Color.parseColor("#FFFFFF")))
                    vDashLine.backgroundResource = R.drawable.bg_dash_line
                }

            }
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
//        getAdapter()!!.expandOrCollapse(position)

        val item = data as InterSectionModel

        EventBus.getDefault().post(MapEvent(MapEvent.DRAW_ANALYSIS_RESULT, item))
        EventBus.getDefault().post(DataEvent(DataEvent.SELECT_ITEM, item))
//        EventBus.getDefault().post(MapEvent(MapEvent.CLEAR_ANALYSIS_MARKER, item))


    }
}