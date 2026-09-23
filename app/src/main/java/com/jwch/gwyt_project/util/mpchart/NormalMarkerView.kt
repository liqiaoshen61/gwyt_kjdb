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


class NormalMarkerView(context: Context?) : MarkerView(context, R.layout.view_normal_mark) {

    var desList : MutableList<ChartDesModel>?= null//单位
    var value = ""


    override fun refreshContent(e: Entry, highlight: Highlight?) {

        val tv1 = findViewById<View>(R.id.tv1) as TextView
        val tv2 = findViewById<View>(R.id.tv2) as TextView

        if (e.data != null) {
            desList = e.data!! as MutableList<ChartDesModel>
        }

        var value = ""

        if (e is CandleEntry) {
            value  = Utils.formatNumber(e.high, 2, false)
        } else {
            value  = Utils.formatNumber(e.y, 2, false)
        }

        if(desList?.size ==1){
            tv1.text = "${desList!![0].name}：${desList!![0].value}"
            tv2.gone()
        }else if(desList?.size ==2){
            tv1.text = "${desList!![0].name}：${desList!![0].value}"
            tv2.text = "${desList!![1].name}：${desList!![1].value}"
            tv2.show()
        }



        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }


}