package com.jwch.gwyt_project.util.mpchart

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.charts.HorizontalBarChart


//自定义横向柱状图 解决设置渐变色无效的bug
class MyHorizontalBarChart : HorizontalBarChart {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun init() {
        super.init()
        mRenderer = MyHorizontalBarChartRenderer(this, mAnimator, mViewPortHandler)
    }
}