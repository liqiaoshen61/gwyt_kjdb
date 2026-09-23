package com.jwch.gwyt_project.util.mpchart


import android.content.Context
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.jwch.gwyt_project.R
import java.text.DecimalFormat


class XYMarkerView(context: Context?) : MarkerView(context, R.layout.chart_mark_view) {

    private val format: DecimalFormat



    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        val tvContent = findViewById<View>(R.id.tvValue) as TextView
        tvContent.text = format.format(e.y)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }

    init {
        format = DecimalFormat("###")
    }
}
