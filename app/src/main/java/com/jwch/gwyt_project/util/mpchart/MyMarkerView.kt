package com.jwch.gwyt_project.util.mpchart


import android.content.Context
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.util.mpchart.model.ChartDesModel


class MyMarkerView(context: Context?) : MarkerView(context, R.layout.chart_mark_view) {

    var des : ChartDesModel?= null//单位
    var value = ""


    override fun refreshContent(e: Entry, highlight: Highlight?) {

        val tvName = findViewById<View>(R.id.tvName) as TextView
        val tvValue = findViewById<View>(R.id.tvValue) as TextView

        if (e.data != null) {
            des = e.data!! as ChartDesModel
        }

        var value = ""

        if (e is CandleEntry) {
            value  = Utils.formatNumber(e.high, 2, false)
        } else {
            value  = Utils.formatNumber(e.y, 2, false)
        }

        tvName.gone()


        des?.name?.apply {
            tvName.text = this
            tvName.show()
        }

        if (des?.value?.isNotBlank() == true) {
            tvValue.text = "${des?.value}${des?.unit}"
        } else {
            tvValue.text = value
        }


        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }


}