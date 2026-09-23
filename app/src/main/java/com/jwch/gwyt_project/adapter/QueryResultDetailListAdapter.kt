package com.jwch.gwyt_project.adapter

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.model.InterSectionModel
import org.jetbrains.anko.backgroundResource

class QueryResultDetailListAdapter : BrvahAdapter<InterSectionModel>(R.layout.item_map_layer_analysis) {

    init {
        addChildClickViewIds(R.id.tvExpertExcel)
    }


    override fun convert(helper: BaseViewHolder, item: InterSectionModel) {

        helper.let {
            item.apply {
                helper.setText(R.id.tvName, "类型：${geometryName.selfTempString("未知")}")
                    .setText(R.id.tvAreaSize, "面积：${areaSize}")
                    .setText(R.id.tvIndex, "${index+1}")
                val itemContent = it.getView<LinearLayout>(R.id.itemContent)
                val tvName = it.getView<TextView>(R.id.tvName)
                val tvAreaSize = it.getView<TextView>(R.id.tvAreaSize)
                val tvIndex = it.getView<TextView>(R.id.tvIndex)
                val vDashLine = it.getView<View>(R.id.vDashLine)
                val tvExpertExcel = it.getView<TextView>(R.id.tvExpertExcel)

                (isSecletd).yes{
                    itemContent.backgroundResource = R.color.bg_village_select
                    tvName.setTextColor((Color.parseColor("#0664e5")))
                    tvAreaSize.setTextColor((Color.parseColor("#0664e5")))
                    tvIndex.setTextColor((Color.parseColor("#0664e5")))
                    vDashLine.backgroundResource = R.drawable.bg_dash_line_blue
                    tvExpertExcel.visiable(true)
                }.no{
                    itemContent.backgroundResource = R.color.transColor
                    tvName.setTextColor((Color.parseColor("#FFFFFF")))
                    tvAreaSize.setTextColor((Color.parseColor("#FFFFFF")))
                    tvIndex.setTextColor((Color.parseColor("#FFFFFF")))
                    vDashLine.backgroundResource = R.drawable.bg_dash_line
                    tvExpertExcel.visiable(false)
                }

            }
        }
    }


}