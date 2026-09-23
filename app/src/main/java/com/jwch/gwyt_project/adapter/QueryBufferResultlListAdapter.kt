package com.jwch.gwyt_project.adapter

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.esri.arcgisruntime.data.ArcGISFeature
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.model.BufferAnalysisModel
import org.jetbrains.anko.backgroundResource

class QueryBufferResultlListAdapter : BrvahAdapter<BufferAnalysisModel>(R.layout.item_buffer_analysis) {

    var indexOffset = 0

    init {
        addChildClickViewIds(R.id.tvExpertExcel)
    }


    override fun convert(helper: BaseViewHolder, item: BufferAnalysisModel) {

        helper.let {
            item.apply {
                var maps = (map["feature"] as ArcGISFeature)

                val code = maps.attributes["问题编"] ?: maps.attributes["编码"] ?: "未知"

                val riverName = maps.attributes["河段"] ?: maps.attributes["河湖名"] ?: "未知"

                val status =maps.attributes["事件状"]


                helper.setText(R.id.tv1, "${layerName.self()}  $riverName")
                    .setText(R.id.tv2, "编号：${code}")
                    .setText(R.id.tvIndex, "${helper.absoluteAdapterPosition + 1 + indexOffset}.")


                val itemContent = it.getView<LinearLayout>(R.id.itemContent)
                val tv1 = it.getView<TextView>(R.id.tv1)
                val tvIndex = it.getView<TextView>(R.id.tvIndex)
                val vDashLine = it.getView<View>(R.id.vDashLine)

                (isSecletd).yes {
                    itemContent.backgroundResource = R.color.bg_village_select
                    tv1.setTextColor((Color.parseColor("#0664e5")))
                    tvIndex.setTextColor((Color.parseColor("#0664e5")))
                    vDashLine.backgroundResource = R.drawable.bg_dash_line_blue
                }.no {
                    itemContent.backgroundResource = R.color.transColor
                    tv1.setTextColor((Color.parseColor("#FFFFFF")))
                    tvIndex.setTextColor((Color.parseColor("#FFFFFF")))
                    vDashLine.backgroundResource = R.drawable.bg_dash_line
                }

            }
        }
    }


}