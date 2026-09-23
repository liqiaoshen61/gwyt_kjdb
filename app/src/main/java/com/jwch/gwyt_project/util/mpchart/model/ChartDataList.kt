package com.jwch.gwyt_project.util.mpchart.model

import com.jwch.gwyt_project.util.mpchart.model.ChartData



class ChartDataList(chartData: MutableList<ChartData>?, name: String?) {

    var chartData : MutableList<ChartData>? = null
    var name: String?= null

    init {
        this.chartData = chartData
        this.name = name
    }
}
