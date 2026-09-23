package com.jwch.gwyt_project.util.mpchart

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.util.mpchart.model.ChartData
import com.jwch.gwyt_project.util.mpchart.model.ChartDataList
import com.jwch.gwyt_project.util.mpchart.model.ChartDesModel

/**
 * MPChart 图表
 */
class ChartUtil  {
    var isInit = false
    private val barColor1 = Color.parseColor("#ffab9b")
    private val barColor2 = Color.parseColor("#fedc8b")
    private val barColor3 = Color.parseColor("#b7ea7c")
    private val barColor4 = Color.parseColor("#78f6e6")
    private val barColor5 = Color.parseColor("#70cdea")
    private val barColor6 = Color.parseColor("#44bdfd")
    private val barColor7 = Color.parseColor("#a880f6")
    private val barColor8 = Color.parseColor("#daa793")
    private val barColor9 = Color.parseColor("#ffbbe1")

    private val color_blue = Color.parseColor("#01effc")
    private val color_blue_light = Color.parseColor("#0065a9")
    private val color_blue_midd = Color.parseColor("#01a8d4")
    private val color_red = Color.parseColor("#FE3434")
    private val color_pink = Color.parseColor("#FA8686")
    private val color_null = Color.parseColor("#00000000")
    private var barColorList : MutableList<Int> = mutableListOf()


    private val color_blue_tide = Color.parseColor("#2DB7F5")
    private val color_orange = Color.parseColor("#FF6600")

    private val txt_black = Color.parseColor("#999999")
    private val txt_black_half = Color.parseColor("#80999999")


    private val txt_black_80 = Color.parseColor("#CCFFFFFF")

    private val color_blue_start = Color.parseColor("#2DB7F5")
    private val color_blue_end = Color.parseColor("#CC98D8F5")
    private val color_blue_border = Color.parseColor("#2DB7F5")

    private val color_blue_top = Color.parseColor("#4F91FF")
    private val color_blue_bottom = Color.parseColor("#7CB8FE")



    private val blackColor = Color.rgb(80, 80, 80)
    private val animateTime = 900
    private val toYx = 5f
    private val legendTextSize = 13f
    private val barTopTextSize = 13f
    private val yAxisEnable = true
    private val yAxisLineColor = Color.GRAY  //y轴 颜色
    private val yAxisLableColor = Color.GRAY  //y轴标签 颜色
    private val xAxisLineColor = Color.GRAY  //x轴 颜色
    private val xAxisLableColor = Color.GRAY  //x轴标签 颜色
    private val xAxisLableEnable = true
    private var context: Context? = null

    val RotationAngle = 0f
    val RotationAngle2 = 45f
    val isHighlight = true

    init {
        barColorList.add(barColor1)
        barColorList.add(barColor2)
        barColorList.add(barColor3)
        barColorList.add(barColor4)
        barColorList.add(barColor5)
        barColorList.add(barColor6)
        barColorList.add(barColor7)
        barColorList.add(barColor8)
        barColorList.add(barColor9)
    }
    constructor() { }

    constructor(context: Context?) {
        this.context = context
    }



    //	基础柱状图 (使用一个barDataSet 所以图例只有1个)
    fun singleBarChart(chartView: BarChart, datalist: List<ChartData>, isShowLegend: Boolean = false, intValue: Boolean = false) {
        if (!CommonUtil.matchList(datalist)) {
            chartView.setNoDataText("暂无数据")
            chartView.invalidate()
            return
        }
        val xCount = CommonUtil.getListSize(datalist)

        val yMaxVlue = getListMaxValue(datalist)
        chartView.setTouchEnabled(false) //启用/禁用所有触碰动作
        chartView.isDragEnabled = true //启用/禁用拖动（平移）
        chartView.setScaleEnabled(false) // 启用/禁用缩放图表上的两个轴。
        chartView.isScaleXEnabled = true // 启用/禁用缩放在x轴上。
        chartView.isScaleYEnabled = true // 启用/禁用缩放在y轴
        chartView.isHighlightPerTapEnabled = true //  设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        chartView.legend.isEnabled = true //是否显示角标
        chartView.description.isEnabled = false
        chartView.setPinchZoom(false) //按比例放大缩小图表
        //        chartView.extraTopOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraLeftOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraRightOffset = 20f;//右边缩进 防止左下角x轴文字被遮挡
        chartView.extraBottomOffset = 5f

        // 设置MarkerView
//        val markView = XYMarkerView(context)
//        markView.chartView = chartView
//        chartView.marker = markView

        val axisValueFormatter  = ChartValueFormatUtil(datalist)
        val xAxis = chartView.xAxis
        xAxis.granularity = 1f  //设置最小间隔，防止当放大时出现重复标签
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(xAxisLableEnable)
        xAxis.labelCount = xCount
        xAxis.valueFormatter = axisValueFormatter
        xAxis.textSize = 14f
        xAxis.textColor = txt_black_80  //x轴标签 颜色
        xAxis.axisLineColor = txt_black_80 //x轴 颜色
        xAxis.labelRotationAngle = RotationAngle  //设置X轴字体显示角度

        val leftAxis = chartView.axisLeft
        leftAxis.setDrawLabels(yAxisEnable)
        leftAxis.setDrawAxisLine(yAxisEnable)
        leftAxis.textColor = txt_black_80 //y轴标签 颜色
        leftAxis.axisLineColor =  txt_black_half //y轴 颜色
        leftAxis.setDrawGridLines(true)
        leftAxis.setStartAtZero(true)
        leftAxis.axisMaximum = yMaxVlue * 1.3F  //设置y轴最高点位1.3倍的最大值
        leftAxis.xOffset = toYx // offset 这个方法是在有drawlable true的时候才有作用
        leftAxis.spaceBottom = 1f //只不过是最小值距离底部比例。默认10，y轴独有

        leftAxis.enableGridDashedLine(5f,5f,0f) // 设置为虚线

        val rightAxis = chartView.axisRight
        rightAxis.isEnabled = false

        val l = chartView.legend
        if(isShowLegend) {
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.textColor = blackColor
            l.setDrawInside(true)
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 9f
            l.textSize = legendTextSize
            l.xEntrySpace = 4f
        }else {
            l.isEnabled = false
        }

        //设置数据值的格式 展示整数 或者展示小数
        val yaxisValueFormatter = if(intValue){
            ChartYAxisValueFormatUtil(datalist)
        }else{
            ChartYAxisValueFormatUtil2(datalist)
        }

        val barEntryList = ArrayList<BarEntry>()
        datalist.forEachIndexed { index, chartData ->
//            val name  = datalist[index].getxLable()
            val value  = datalist[index].getyValue()
            barEntryList.add(BarEntry(index.toFloat(), value))
        }

        val barDataSet = BarDataSet(barEntryList, "图例1")
        barDataSet.setDrawValues(true) //启用/禁用 绘制所有 DataSets 数据对象包含的数据的值文本。
        barDataSet.isHighlightEnabled = isHighlight//是否可以选中高亮 (此参数为true时 marker才会显示)
        barDataSet.highLightColor = blackColor
//            barDataSet.color = barColorList[3] //设置柱子的颜色
//        barDataSet.color = color_blue //设置柱子的颜色 单一
        barDataSet.setGradientColor(color_blue_top, color_blue_bottom)
//        barDataSet.setGradientColor(color_blue_start, color_blue_end)
        barDataSet.barBorderColor = color_blue_border
        barDataSet.valueTextSize = barTopTextSize
        barDataSet.valueFormatter = yaxisValueFormatter
        barDataSet.valueTextColor = Color.WHITE


        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(barDataSet)

        val barData = BarData(dataSets)
        chartView.data = barData  //给控件设置数据
        chartView.fitScreen()
        chartView.animateXY(animateTime, animateTime)

////        分组设置
//        //(0.4 + 0.06) * 2 + 0.08 = 1.00 -> interval per "group" 一定要等于1,乘以2是表示每组有两个数据
        val groupSpace = 0.08f  //不同组的间距
        val barSpace = 0.06f  //同一组中不同bar的间距
        val barWidth = 0.28f //bar宽度
        chartView.barData.barWidth = barWidth
//        chartView.xAxis.axisMinimum = 0F
//        chartView.xAxis.axisMaximum = chartView.barData.getGroupWidth(groupSpace, barSpace) * datalist.size
//        chartView.xAxis.setCenterAxisLabels(true);   //设置柱子（柱子组）居中对齐X轴上的点
//        //分组柱状图一定要增加这个配置，且要在setData之后调用否则会程序崩溃
//        chartView.groupBars(0F, groupSpace, barSpace)
        chartView.invalidate()                    //将图表重绘以显示设置的属性和数据

    }

    //	基础柱状图2 (使用一组barDataSet 每个使用一组barDataSet一个数据  可以画出多个图例)
    fun singleBarChart2(chartView: BarChart, datalist: List<ChartData>, isShowLegend: Boolean) {
        if (!CommonUtil.matchList(datalist)) {
            chartView.setNoDataText("暂无数据");
            chartView.invalidate();
            return
        }
        val xCount = CommonUtil.getListSize(datalist)
        val yMaxVlue = getListMaxValue(datalist)
        chartView.setTouchEnabled(true) //启用/禁用所有触碰动作
        chartView.isDragEnabled = true //启用/禁用拖动（平移）
        chartView.setScaleEnabled(false) // 启用/禁用缩放图表上的两个轴。
        chartView.isScaleXEnabled = true // 启用/禁用缩放在x轴上。
        chartView.isScaleYEnabled = true // 启用/禁用缩放在y轴
        chartView.isHighlightPerTapEnabled = true //  设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        chartView.legend.isEnabled = true //是否显示角标
        chartView.description.isEnabled = false
        chartView.setPinchZoom(false) //按比例放大缩小图表
        //        chartView.extraTopOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraLeftOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraRightOffset = 40f;//右边缩进 防止左下角x轴文字被遮挡


        val axisValueFormatter = ChartValueFormatUtil(datalist)
        val xAxis = chartView.xAxis
        xAxis.granularity = 1f  //设置最小间隔，防止当放大时出现重复标签
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(xAxisLableEnable)
        xAxis.labelCount = xCount
        xAxis.valueFormatter = axisValueFormatter
        xAxis.textColor = xAxisLableColor  //x轴标签 颜色
        xAxis.axisLineColor = xAxisLineColor  //x轴 颜色
        xAxis.labelRotationAngle = RotationAngle  //设置X轴字体显示角度


        val leftAxis = chartView.axisLeft
        leftAxis.setDrawLabels(yAxisEnable)
        leftAxis.setDrawAxisLine(yAxisEnable)
        leftAxis.textColor = yAxisLableColor //y轴标签 颜色
        leftAxis.axisLineColor = yAxisLineColor  //x轴 颜色
        leftAxis.setDrawGridLines(false)
        leftAxis.setStartAtZero(true)
        leftAxis.axisMaximum = yMaxVlue * 1.3F  //设置y轴最高点位1.3倍的最大值
        leftAxis.xOffset = toYx // offset 这个方法是在有drawlable true的时候才有作用
        leftAxis.spaceBottom = 0f //只不过是最小值距离底部比例。默认10，y轴独有
        val rightAxis = chartView.axisRight
        rightAxis.isEnabled = false

        val l = chartView.legend
        if(isShowLegend) {
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.textColor = blackColor
            l.setDrawInside(true)
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 9f
            l.textSize = legendTextSize
            l.xEntrySpace = 4f
        }else {
            l.isEnabled = false
        }

        val yaxisValueFormatter = ChartYAxisValueFormatUtil(datalist)

        val dataSets = ArrayList<IBarDataSet>()
        datalist.forEachIndexed { index, chartData ->
            val name  = datalist[index].getxLable()
            val value  = datalist[index].getyValue()
            val barEntryList = ArrayList<BarEntry>()
            barEntryList.add(BarEntry(index.toFloat(), value))
            val barDataSet = BarDataSet(barEntryList, name) //这个name是图例的名字

            barDataSet.setDrawValues(true) //启用/禁用 绘制所有 DataSets 数据对象包含的数据的值文本。
            barDataSet.isHighlightEnabled = isHighlight //是否可以选中高亮
            barDataSet.color = barColorList[index] //设置柱子的颜色
//            barDataSet.color = color_red //设置柱子的颜色 单一
//            barDataSet.setGradientColor(color_blue, color_blue_light)
//            barDataSet.barBorderColor = barColorList[index] //柱子图边框颜色
            barDataSet.valueTextSize = barTopTextSize
            barDataSet.valueFormatter = yaxisValueFormatter
            barDataSet.valueTextColor = Color.GRAY
            dataSets.add(barDataSet)
        }

        val barData = BarData(dataSets)
        chartView.data = barData  //给控件设置数据
        chartView.fitScreen()
        chartView.animateXY(animateTime, animateTime)

        val barWidth = 0.4f //bar宽度
        chartView.barData.barWidth = barWidth
//        chartView.animateY(animateTime);
    }

    //  多组柱状图
    fun MultipleBarChart(chartView: BarChart, dataListList: List<ChartDataList>, isShowLegend: Boolean = false, intValue: Boolean = false) {

        //默认不绘制mark
        chartView.setDrawMarkers(false)

        val datalist : MutableList<ChartData> = mutableListOf()

        dataListList.forEach {
            it.chartData?.forEach {
                datalist.add(it)
            }
        }
        if (!CommonUtil.matchList(datalist)) {
            chartView.setNoDataText("暂无数据");
            chartView.invalidate();
            return
        }

        //点击时绘制mark
        chartView.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                chartView.setDrawMarkers(true)

            }
            override fun onNothingSelected() {
                chartView.setDrawMarkers(false)
            }
        })

        val xCount = CommonUtil.getListSize(dataListList[0].chartData)
        val yMaxVlue = getListMaxValue(datalist)
        chartView.setTouchEnabled(true) //启用/禁用所有触碰动作
        chartView.isDragEnabled = true //启用/禁用拖动（平移）
        chartView.setScaleEnabled(false) // 启用/禁用缩放图表上的两个轴。
        chartView.isScaleXEnabled = true // 启用/禁用缩放在x轴上。
        chartView.isScaleYEnabled = true // 启用/禁用缩放在y轴
        chartView.isHighlightPerTapEnabled = true //  设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        chartView.legend.isEnabled = true //是否显示角标
        chartView.description.isEnabled = false
        chartView.setPinchZoom(false) //按比例放大缩小图表
//        chartView.extraTopOffset = 10f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraLeftOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraRightOffset = 40f;//右边缩进 防止左下角x轴文字被遮挡

        val axisValueFormatter = ChartValueFormatUtil(dataListList[0].chartData)
        val xAxis = chartView.xAxis
        xAxis.granularity = 1f  //设置最小间隔，防止当放大时出现重复标签
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(xAxisLableEnable)
        xAxis.labelCount = xCount
        xAxis.valueFormatter = axisValueFormatter
        xAxis.textColor = xAxisLableColor  //x轴标签 颜色
        xAxis.axisLineColor = xAxisLineColor  //x轴 颜色
        xAxis.labelRotationAngle = RotationAngle  //设置X轴字体显示角度


        val leftAxis = chartView.axisLeft
        leftAxis.setDrawLabels(yAxisEnable)
        leftAxis.setDrawAxisLine(yAxisEnable)
        leftAxis.textColor = yAxisLableColor //y轴标签 颜色
        leftAxis.axisLineColor = yAxisLineColor  //x轴 颜色
        leftAxis.setDrawGridLines(false)
        leftAxis.setStartAtZero(true)
        leftAxis.axisMaximum = yMaxVlue * 1.3F
        leftAxis.xOffset = toYx // offset 这个方法是在有drawlable true的时候才有作用
        leftAxis.spaceBottom = 0f //只不过是最小值距离底部比例。默认10，y轴独有
        val rightAxis = chartView.axisRight
        rightAxis.isEnabled = false

        val l = chartView.legend
        if(isShowLegend) {
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.textColor = blackColor
            l.setDrawInside(true)
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 9f
            l.textSize = legendTextSize
            l.xEntrySpace = 4f
        }else {
            l.isEnabled = false
        }

        //设置数据值的格式 展示整数 或者展示小数
        val yaxisValueFormatter = if(intValue){
            ChartYAxisValueFormatUtil(dataListList.first().chartData)
        }else{
            ChartYAxisValueFormatUtil2(dataListList.first().chartData)
        }

        val dataSets = ArrayList<IBarDataSet>()

        dataListList.forEachIndexed { index, chartDataList ->
            val barEntryList = ArrayList<BarEntry>()
            chartDataList.chartData?.forEachIndexed { i, chartData ->
                val value = chartData.getyValue()
                barEntryList.add(BarEntry(i.toFloat(), value))
            }
            val barDataSet = BarDataSet(barEntryList, chartDataList.name)
            barDataSet.setDrawValues(true) //启用/禁用 绘制所有 DataSets 数据对象包含的数据的值文本。
            barDataSet.isHighlightEnabled = isHighlight //是否可以选中高亮
            barDataSet.highLightColor = blackColor
            barDataSet.color = barColorList[index]
            barDataSet.barBorderColor = Color.BLACK //柱子图边框颜色
            barDataSet.valueTextSize = barTopTextSize
            barDataSet.valueFormatter = yaxisValueFormatter
            barDataSet.valueTextColor = Color.GRAY

            dataSets.add(barDataSet)
        }

        val barData = BarData(dataSets)
        chartView.data = barData  //给控件设置数据
        chartView.fitScreen()
        chartView.animateXY(animateTime, animateTime)

        //(0.4 + 0.06) * 2 + 0.08 = 1.00 -> interval per "group" 一定要等于1,乘以2是表示每组有两个数据
        val groupSpace = 0.08f  //不同组的间距
        val barSpace = 0.02f  //同一组中不同bar的间距
        val barWidth = 0.2f //bar宽度
        chartView.barData.barWidth = barWidth
        chartView.xAxis.axisMinimum = 0F
        chartView.xAxis.axisMaximum = chartView.barData.getGroupWidth(groupSpace, barSpace) * dataListList[0].chartData?.size!!
        chartView.xAxis.setCenterAxisLabels(true);   //设置柱子（柱子组）居中对齐X轴上的点
        //分组柱状图一定要增加这个配置，且要在setData之后调用否则会程序崩溃
        chartView.groupBars(0F, groupSpace, barSpace)
        chartView.invalidate()                    //将图表重绘以显示设置的属性和数据
    }

    //	基础折线图
    fun singleLineChart(chartView: LineChart, datalist: List<ChartData>?, isShowLegend: Boolean,
                        intValue: Boolean, unit :String ="") {
        //默认不绘制mark
        chartView.setDrawMarkers(false)


        if (!CommonUtil.matchList(datalist)) {
//            chartView.description = null;
            chartView.setNoDataText("暂无数据");
            chartView.invalidate();
            return
        }

        //点击时绘制mark
        chartView.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                chartView.setDrawMarkers(true)

            }
            override fun onNothingSelected() {
                chartView.setDrawMarkers(false)
            }
        })

        val xCount = CommonUtil.getListSize(datalist)
        val yMaxVlue = getListMaxValue(datalist!!)
        chartView.setTouchEnabled(true) //禁止所有触碰动作
        chartView.isDragEnabled = true //启用/禁用拖动（平移）
        chartView.setScaleEnabled(true) // 启用/禁用缩放图表上的两个轴。
        chartView.isScaleXEnabled = true // 启用/禁用缩放在x轴上。
        chartView.isScaleYEnabled = false // 启用/禁用缩放在y轴
        chartView.isHighlightPerTapEnabled = true //  设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        chartView.legend.isEnabled = true //是否显示角标
        chartView.description.isEnabled = false
        chartView.setPinchZoom(false) //按比例放大缩小图表
//        chartView.extraTopOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraLeftOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraRightOffset = 40f;//右边缩进 防止左下角x轴文字被遮挡
        chartView.isHighlightPerDragEnabled = true;


        if (datalist.size > 10 ){
            //倍率可以根据实际情况而定
            chartView.setScaleMinima(3.0f, 1.0f);
        }else {
            chartView.setScaleMinima(1.5f, 1.0f);
        }

        // 设置MarkerView
        val myMarkView = MyMarkerView(context)
        myMarkView.chartView = chartView
        chartView.marker = myMarkView

        val axisValueFormatter = ChartValueFormatUtil(datalist)
        val xAxis = chartView.xAxis

        xAxis.granularity = 1f  //设置最小间隔，防止当放大时出现重复标签
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(xAxisLableEnable)
        xAxis.labelCount = xCount
        xAxis.valueFormatter = axisValueFormatter
        xAxis.textColor = xAxisLableColor  //x轴标签 颜色
        xAxis.axisLineColor = xAxisLineColor  //x轴 颜色
        xAxis.labelRotationAngle = RotationAngle  //设置X轴字体显示角度
        xAxis.setLabelCount(5, true);//x轴上的文字显示个数


        val leftAxis = chartView.axisLeft
        leftAxis.setDrawLabels(yAxisEnable)
        leftAxis.setDrawAxisLine(yAxisEnable)
        leftAxis.textColor = yAxisLableColor  //y轴标签 颜色
        leftAxis.axisLineColor = yAxisLineColor  //x轴 颜色
        leftAxis.setDrawGridLines(false)
        if (yMaxVlue > 6.0F) {
            leftAxis.axisMaximum = yMaxVlue
        } else {
            leftAxis.axisMaximum = 6f
        }
//        leftAxis.setStartAtZero(true)
        leftAxis.axisMinimum = -6f
        leftAxis.xOffset = toYx // offset 这个方法是在有drawlable true的时候才有作用
        leftAxis.spaceBottom = 0f //只不过是最小值距离底部比例。默认10，y轴独有

        val rightAxis = chartView.axisRight
        rightAxis.isEnabled = false

        val l = chartView.legend
        if(isShowLegend) {
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.textColor = blackColor
            l.setDrawInside(true)
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 9f
            l.textSize = legendTextSize
            l.xEntrySpace = 4f
        }else {
            l.isEnabled = false
        }

        //设置数据值的格式 展示整数 或者展示小数
        val yaxisValueFormatter = if(intValue){
            ChartYAxisValueFormatUtil(datalist)
        }else{
            ChartYAxisValueFormatUtil2(datalist)
        }


        val entryList = ArrayList<Entry>()
        datalist.forEachIndexed { index, chartData ->
            val name = datalist[index].getxLable()
            val value = datalist[index].getyValue()
            val des = ChartDesModel(value.toString(), name, unit)
            entryList.add(Entry(index.toFloat(), value, des))
        }

        val lineDataSet = LineDataSet(entryList, "数据1")
        lineDataSet.setDrawValues(false) //启用/禁用 绘制所有 DataSets 数据对象包含的数据的值文本。
        lineDataSet.isHighlightEnabled = isHighlight //是否可以选中高亮
        lineDataSet.highLightColor = color_orange
        lineDataSet.highlightLineWidth = 2f
        lineDataSet.setDrawCircles(false) //是否显示圆点
        lineDataSet.color = color_blue_tide //设置折线的颜色
        lineDataSet.lineWidth = 3f  //折线宽度
        lineDataSet.setDrawFilled(true)   //设置折线图填充
        lineDataSet.fillFormatter = IFillFormatter { dataSet, dataProvider ->
            chartView.axisLeft.axisMinimum
        }
        lineDataSet.fillDrawable = context?.getResources()?.getDrawable(R.drawable.bg_bule_gradient) //填充区颜色
        lineDataSet.valueTextSize = barTopTextSize
        lineDataSet.valueFormatter = yaxisValueFormatter
        lineDataSet.valueTextColor = blackColor
        lineDataSet.isVisible = true // //设置折线是否可见
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER  //设置曲线模式
        lineDataSet.setDrawHighlightIndicators(true)


        // customize legend entry
        lineDataSet.setFormLineWidth(1f)
        lineDataSet.setFormSize(15f)

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet)

        //设置x轴总页数
        val matrix = Matrix()

        //因为潮汐数据是72个 设置x轴显示3页 我们让表格一屏显示总数/3的数据量
        matrix.postScale(3f, 1.0f)
        chartView.viewPortHandler.refresh(matrix, chartView, false)
//        chartView.fitScreen()
        val lineData = LineData(dataSets)
        chartView.data = lineData

        chartView.animateXY(animateTime, animateTime)
//        chartView.animateY(animateTime);

        //刷新
//        chartView.invalidate()
    }

    //基础 饼状图
    fun singlePieChart(chartView: PieChart, chartTitle :String, datalist: List<ChartData>, isShowLegend: Boolean = false) {

        chartView.centerText = generateCenterSpannableText("")
        if (!CommonUtil.matchList(datalist)) {
            chartView.setNoDataText("暂无数据");
            chartView.invalidate();
            return
        }

        //设置饼图是否接收点击事件，默认为true
        chartView.setTouchEnabled(true)
        //设置饼图是否使用百分比
        chartView.setUsePercentValues(false)
        chartView.description.isEnabled = false
        //设置饼图右下角的文字描述
        //chartView.getDescription().setPosition(460,730);

        //饼状图位置 设置边距
        chartView.setExtraOffsets(5f, 3f, 3f, 3f)
        //摩擦系数
        chartView.dragDecelerationFrictionCoef = 0.95f
        //饼状图是否空心(环状)
        chartView.isDrawHoleEnabled = false
        //圆盘中心的颜色
        chartView.setHoleColor(Color.TRANSPARENT)
        //设置圆盘中间文字
        //chartView.setCenterText(generateCenterSpannableText("123"));
        chartView.setTransparentCircleColor(Color.WHITE)
        chartView.setTransparentCircleAlpha(110)
        //设置中间圆盘的半径,值为所占饼图的百分比
        chartView.holeRadius = 50f
        //设置中间透明圈的半径,值为所占饼
        chartView.transparentCircleRadius = 50f
        //是否显示圆盘中间文字，默认显示
        chartView.setDrawCenterText(true)
        //设置圆盘是否转动，默认转动
        chartView.isRotationEnabled = true
        //设置初始旋转角度
        chartView.rotationAngle = 0f
        chartView.isHighlightPerTapEnabled = true

        chartView.setCenterTextSize(15f)
        chartView.setCenterTextColor(Color.WHITE)
        // 设置一个选中区域监听
        chartView.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                e as PieEntry
                val str = "${e.label}\n${e.value.toInt()}".trimIndent()
                chartView.centerText = generateCenterSpannableText(str)
            }
            override fun onNothingSelected() {
                chartView.centerText = ""
            }
        })

        //设置图例
        val l = chartView.legend

        l.isEnabled = isShowLegend //是否显示图例
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.textColor = context!!.resources.getColor(R.color.white)
        l.form = Legend.LegendForm.CIRCLE
        l.formSize = 8f
        l.textSize = 10f
        l.xEntrySpace = 3f
        //图例自动换行
        l.isWordWrapEnabled = true


        // 输入标签样式
        chartView.setEntryLabelColor(Color.WHITE)
        chartView.setEntryLabelTextSize(12f)
        val entries = ArrayList<PieEntry>()
        var sum = 0f

        datalist.forEachIndexed { index, chartData ->
            val name = datalist[index].getxLable()
            val value = datalist[index].getyValue()
            sum += value
            entries.add(PieEntry(value, name))
        }

        //如果饼状图没有任何数据
        if (sum <= 0) {
            chartView.centerText = generateCenterSpannableText("暂无数据")
        }
        //设置数据
        val dataSet = PieDataSet(entries, "")
        //设置各个饼状图之间的距离
        dataSet.sliceSpace = 0f
        // 部分区域被选中时多出的长度
        dataSet.selectionShift = 5f
        //设置颜色
        dataSet.colors = barColorList
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE;//y轴数据显示在饼图内/外

        dataSet.valueFormatter = ChartValuePercentFormatUtil(datalist)

        dataSet.setDrawValues(false)
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE;//x轴数据显示在饼图内/外
        val data = PieData(dataSet)
        //传入PieChat对象 才能显示%
        data.setValueFormatter(PercentFormatter(chartView))
        //不显示饼状图上的label
        chartView.setDrawEntryLabels(false)
        //数据的文字大小
        data.setValueTextSize(14f)
        //数据的文字颜色
        data.setValueTextColor(Color.WHITE)
        chartView.data = data
        chartView.highlightValues(null)
        chartView.animateXY(animateTime, animateTime)
        //刷新
        chartView.invalidate()
    }

    //饼状图 中间显示的文字数据
    private fun generateCenterSpannableText(str: String, textSize : Float = 0.9f): SpannableString {
        val s = SpannableString(str)
        s.setSpan(RelativeSizeSpan(textSize), 0, s.length, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 0, s.length, 0)
        s.setSpan(ForegroundColorSpan(Color.WHITE), 0, s.length, 0)
        return s
    }


    //取最大值
    fun getListMaxValue(list: List<ChartData>): Float {
        var maxValue = 0f
        if (CommonUtil.matchList(list)) {
            for (model in list) {
                if (model.getyValue() > maxValue) {
                    maxValue = model.getyValue()
                }
            }
        }
        return maxValue
    }

    //取最小值
    fun getListMinValue(list: List<ChartData>): Float {
        var minValue = 0f
        if (CommonUtil.matchList(list)) {
            for (model in list) {
                if (model.getyValue() < minValue) {
                    minValue = model.getyValue()
                }
            }
        }
        return minValue
    }

    //判断是不是偶数
    private fun isEvenNum(value: Int): Boolean {
        return value % 2 == 0
    }


    //产值趋势图
    fun drawBar1(chartView: BarChart, dataListList: List<ChartDataList>, isShowLegend: Boolean = false,
                 intValue: Boolean = false) {

        //默认不绘制mark
//        chartView.setDrawMarkers(false)
//
        val datalist : MutableList<ChartData> = mutableListOf()

        dataListList.forEachIndexed { index, chartDataList ->
            chartDataList.chartData?.forEach {
                datalist.add(it)
            }
        }
        for(index in 0 until  dataListList[0].chartData?.size.self()){
            val inValue = dataListList[0].chartData?.get(index)?.getyValue().self()
            val outValue = dataListList[1].chartData?.get(index)?.getyValue().self()
            datalist.add(ChartData(inValue + outValue, ""))
        }

        if (!CommonUtil.matchList(datalist)) {
            chartView.setNoDataText("暂无数据");
            chartView.invalidate();
            return
        }

        val markView = NormalMarkerView(context)
        markView.chartView = chartView
        chartView.marker = markView


        //点击时绘制mark
        chartView.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                chartView.setDrawMarkers(true)

            }
            override fun onNothingSelected() {
                chartView.setDrawMarkers(false)
            }
        })

        val xCount = CommonUtil.getListSize(dataListList[0].chartData)
        val yMaxVlue = getListMaxValue(datalist)
        chartView.setTouchEnabled(true) //启用/禁用所有触碰动作
        chartView.isDragEnabled = true //启用/禁用拖动（平移）
        chartView.setScaleEnabled(false) // 启用/禁用缩放图表上的两个轴。
        chartView.isScaleXEnabled = false // 启用/禁用缩放在x轴上。
        chartView.isScaleYEnabled = false // 启用/禁用缩放在y轴
        chartView.isHighlightPerTapEnabled = true //  设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        chartView.legend.isEnabled = true //是否显示角标
        chartView.description.isEnabled = false
        chartView.setPinchZoom(false) //按比例放大缩小图表
        chartView.setExtraOffsets(5f, 5f, 5f, 15f)
        chartView.setDrawValueAboveBar(true) //数值不展示在上方
        chartView.isHighlightFullBarEnabled = true

        val axisValueFormatter = ChartValueFormatUtil(dataListList[0].chartData)
        val xAxis = chartView.xAxis
        xAxis.granularity = 1f  //设置最小间隔，防止当放大时出现重复标签
        xAxis.axisLineWidth = 3f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(xAxisLableEnable)
        xAxis.labelCount = xCount
        xAxis.valueFormatter = axisValueFormatter
        xAxis.textColor = txt_black //x轴标签 颜色
        xAxis.axisLineColor = Color.parseColor("#3391CA") //x轴 颜色
        xAxis.labelRotationAngle = 45f  //设置X轴字体显示角度
        xAxis.textSize = 12f

        val leftAxis = chartView.axisLeft
        leftAxis.setDrawLabels(yAxisEnable)
        leftAxis.setDrawAxisLine(false)
        leftAxis.gridColor = txt_black_half// grid颜色
        leftAxis.gridLineWidth = 1f // grid颜色
        leftAxis.textColor = txt_black //y轴标签 颜色
        leftAxis.axisLineColor = Color.parseColor("#2C77C3") //y轴 颜色
        leftAxis.setDrawGridLines(true)
        leftAxis.setStartAtZero(true)
        leftAxis.axisMaximum = yMaxVlue * 1.1F
        leftAxis.xOffset = toYx // offset 这个方法是在有drawlable true的时候才有作用
        leftAxis.spaceBottom = 0f //只不过是最小值距离底部比例。默认10，y轴独有
        leftAxis.textSize = 12f

        val rightAxis = chartView.axisRight
        rightAxis.isEnabled = false

        val l = chartView.legend
        if(isShowLegend) {
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)
            l.textColor = Color.BLACK
            l.textSize = 20f
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 15f
            l.textSize = barTopTextSize
            l.xEntrySpace = 50f
            l.xOffset = 30f
            l.formToTextSpace = 8f
        }else {
            l.isEnabled = false
        }

        val colorList = mutableListOf(
            context!!.resources.getColor(R.color.bar1_c1),
            context!!.resources.getColor(R.color.bar1_c2),
        )

        //设置数据值的格式 展示整数 或者展示小数
        val yaxisValueFormatter = if(intValue){
            ChartYAxisValueFormatUtil(dataListList.first().chartData)
        }else{
            ChartYAxisValueFormatUtil2(dataListList.first().chartData)
        }

        val dataSets = ArrayList<IBarDataSet>()


        val inPortList = dataListList[0].chartData
        val outPortList = dataListList[1].chartData


        val barEntryList = ArrayList<BarEntry>()


        for(index in 0 until  inPortList?.size.self()){
            val inValue = inPortList?.get(index)?.getyValue().self()
            val outValue = outPortList?.get(index)?.getyValue().self()
            val desList = mutableListOf<ChartDesModel>()
            desList.add(ChartDesModel(inValue.toString(),"漳州市GDP",""))
            desList.add(ChartDesModel(outValue.toString(),"林业产业产值",""))

            barEntryList.add(BarEntry(index.toFloat(), floatArrayOf(inValue, outValue), desList))
        }

        val barDataSet = BarDataSet(barEntryList, "")
        barDataSet.setDrawValues(false) //启用/禁用 绘制所有 DataSets 数据对象包含的数据的值文本。
        barDataSet.isHighlightEnabled = isHighlight //是否可以选中高亮
        barDataSet.highLightColor = blackColor
        barDataSet.colors = mutableListOf(colorList[0], colorList[1])
        barDataSet.valueTextSize = 16f
        barDataSet.valueFormatter = yaxisValueFormatter
        barDataSet.valueTextColor = Color.WHITE
        barDataSet.stackLabels = arrayOf("漳州市GDP", "林业产业产值")



        dataSets.add(barDataSet)
        val barData = BarData(dataSets)
        barData.barWidth = 0.50f //bar宽度设置为原始的0.2
        chartView.data = barData  //给控件设置数据
        chartView.fitScreen()
        chartView.animateXY(animateTime, animateTime)

        //(0.4 + 0.06) * 3 + 0.08 = 1.00 -> interval per "group" 一定要等于1,乘以2是表示每组有两个数据
//        val groupSpace = 0.4f //不同组的间距
//        val barSpace = 0.03f //同一组中不同bar的间距
//        val barWidth = 0.17f //bar宽度
//        chartView.barData.barWidth = barWidth
//        chartView.xAxis.axisMinimum = 0F
//        chartView.xAxis.axisMaximum = chartView.barData.getGroupWidth(groupSpace, barSpace) * dataListList[0].chartData?.size!!
//        chartView.xAxis.setCenterAxisLabels(true)   //设置柱子（柱子组）居中对齐X轴上的点
        //分组柱状图一定要增加这个配置，且要在setData之后调用否则会程序崩溃
//        chartView.groupBars(0F, groupSpace, barSpace)
        chartView.invalidate()                    //将图表重绘以显示设置的属性和数据
    }


    // 公益林商品林占比
    fun drawBar4(chartView: BarChart, datalist: List<ChartData>, isShowLegend: Boolean = false, intValue: Boolean = false) {
        if (!CommonUtil.matchList(datalist)) {
            chartView.setNoDataText("暂无数据");
            chartView.invalidate();
            return
        }
        val xCount = CommonUtil.getListSize(datalist)

        val yMaxVlue = getListMaxValue(datalist)
        chartView.setTouchEnabled(false) //启用/禁用所有触碰动作
        chartView.isDragEnabled = true //启用/禁用拖动（平移）
        chartView.setScaleEnabled(false) // 启用/禁用缩放图表上的两个轴。
        chartView.isScaleXEnabled = true // 启用/禁用缩放在x轴上。
        chartView.isScaleYEnabled = true // 启用/禁用缩放在y轴
        chartView.isHighlightPerTapEnabled = true //  设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        chartView.legend.isEnabled = true //是否显示角标
        chartView.description.isEnabled = false
        chartView.setPinchZoom(false) //按比例放大缩小图表
        //        chartView.extraTopOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.setExtraOffsets(10f, 10f, 10f, 5f)

        // 设置MarkerView
//        val markView = XYMarkerView(context)
//        markView.chartView = chartView
//        chartView.marker = markView

        val axisValueFormatter  = ChartValueFormatUtil(datalist)
        val xAxis = chartView.xAxis
        xAxis.granularity = 1f  //设置最小间隔，防止当放大时出现重复标签
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawLabels(xAxisLableEnable)
        xAxis.labelCount = xCount
        xAxis.valueFormatter = axisValueFormatter
        xAxis.textSize = 14f
        xAxis.textColor = Color.parseColor("#000000")   //x轴标签 颜色
        xAxis.axisLineColor = Color.parseColor("#3D7BD7")  //x轴 颜色
        xAxis.labelRotationAngle = 0f  //设置X轴字体显示角度
        xAxis.axisLineWidth = 3f

        val leftAxis = chartView.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.textColor = Color.parseColor("#FCFFFE") //y轴标签 颜色
        leftAxis.axisLineColor = yAxisLineColor  //y轴 颜色
        leftAxis.setDrawGridLines(false)
        leftAxis.setStartAtZero(true)
        leftAxis.axisMaximum = yMaxVlue * 1.2F  //设置y轴最高点位1.3倍的最大值
        leftAxis.xOffset = toYx // offset 这个方法是在有drawlable true的时候才有作用
        leftAxis.spaceBottom = 0f //只不过是最小值距离底部比例。默认10，y轴独有
        leftAxis.valueFormatter = ChartValuePercentFormatUtil(datalist)
        leftAxis.textSize = 16f

        val rightAxis = chartView.axisRight
        rightAxis.isEnabled = false


        val l = chartView.legend
        if(isShowLegend) {
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.textColor = Color.parseColor("#FCFFFE")
            l.setDrawInside(false)
            l.textSize = 16f
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 16f
            l.textSize = legendTextSize
            l.xEntrySpace = 4f
        }else {
            l.isEnabled = false
        }

        //设置数据值的格式 展示整数 或者展示小数
        val yaxisValueFormatter = if(intValue){
            ChartYAxisValueFormatUtil(datalist)
        }else{
            ChartYAxisValueFormatUtil2(datalist)
        }

        val gradientColorList = mutableListOf<MutableList<Int>>()
        gradientColorList.add(mutableListOf(Color.parseColor("#159270"),Color.parseColor("#086669")))
        gradientColorList.add(mutableListOf(Color.parseColor("#1fdaa8"),Color.parseColor("#0a81a4")))
        val borderColorList = mutableListOf(Color.parseColor("#00ffbc"),Color.parseColor("#00ffba"))

        val dataSets = ArrayList<IBarDataSet>()
        datalist.forEachIndexed { index, chartData ->
//            val name  = datalist[index].getxLable()
            val value  = datalist[index].getyValue()

            val barEntryList = ArrayList<BarEntry>()
            barEntryList.add(BarEntry(index.toFloat(), value,"万公顷"))

            val barDataSet = BarDataSet(barEntryList, "公益林商品林占比")
            barDataSet.setDrawValues(true) //启用/禁用 绘制所有 DataSets 数据对象包含的数据的值文本。
            barDataSet.isHighlightEnabled = isHighlight//是否可以选中高亮 (此参数为true时 marker才会显示)
            barDataSet.highLightColor = blackColor
            barDataSet.color = color_blue_midd //设置柱子的颜色 单一
            barDataSet.setGradientColor(gradientColorList[index][1], gradientColorList[index][0])
            barDataSet.valueTextSize = barTopTextSize
            barDataSet.valueFormatter = yaxisValueFormatter
            barDataSet.valueTextColor = Color.parseColor("#009a45")
            barDataSet.barBorderColor = borderColorList[index] //柱子图边框颜色
            barDataSet.barBorderWidth = 1f

            dataSets.add(barDataSet)
        }



        val barData = BarData(dataSets)
        barData.barWidth = 0.35f //bar宽度设置为原始的0.2
        chartView.data = barData  //给控件设置数据
        chartView.fitScreen()
        chartView.animateXY(animateTime, animateTime)

////        分组设置
//        //(0.4 + 0.06) * 2 + 0.08 = 1.00 -> interval per "group" 一定要等于1,乘以2是表示每组有两个数据
//        val groupSpace = 0.08f  //不同组的间距
//        val barSpace = 0.06f  //同一组中不同bar的间距
//        val barWidth = 0.4f //bar宽度
//        chartView.barData.barWidth = barWidth
//        chartView.xAxis.axisMinimum = 0F
//        chartView.xAxis.axisMaximum = chartView.barData.getGroupWidth(groupSpace, barSpace) * datalist.size
//        chartView.xAxis.setCenterAxisLabels(true);   //设置柱子（柱子组）居中对齐X轴上的点
//        //分组柱状图一定要增加这个配置，且要在setData之后调用否则会程序崩溃
//        chartView.groupBars(0F, groupSpace, barSpace)
//        chartView.invalidate()                    //将图表重绘以显示设置的属性和数据

    }


    //沿海防护林 图斑分布
    fun drawYhfhlBar(chartView: BarChart, datalist: List<ChartData>, isShowLegend: Boolean) {
        if (!CommonUtil.matchList(datalist)) {
            chartView.setNoDataText("暂无数据");
            chartView.invalidate();
            return
        }
        val xCount = CommonUtil.getListSize(datalist)
        val yMaxVlue = getListMaxValue(datalist)
        chartView.setTouchEnabled(false) //启用/禁用所有触碰动作
        chartView.isDragEnabled = false //启用/禁用拖动（平移）
        chartView.setScaleEnabled(false) // 启用/禁用缩放图表上的两个轴。
        chartView.isScaleXEnabled = false // 启用/禁用缩放在x轴上。
        chartView.isScaleYEnabled = false // 启用/禁用缩放在y轴
        chartView.isHighlightPerTapEnabled = true //  设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        chartView.legend.isEnabled = true //是否显示角标
        chartView.description.isEnabled = false
        chartView.setPinchZoom(false) //按比例放大缩小图表
        //        chartView.extraTopOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraLeftOffset = 20f; //左边缩进 防止左下角x轴文字被遮挡
        chartView.extraRightOffset = 20f;//右边缩进 防止左下角x轴文字被遮挡
        chartView.extraBottomOffset = 10f

//         设置MarkerView
        val markView = XYMarkerView(context)
        markView.chartView = chartView
        chartView.marker = markView

        val axisValueFormatter = ChartValueFormatUtil(datalist)
        val xAxis = chartView.xAxis
        xAxis.granularity = 1f  //设置最小间隔，防止当放大时出现重复标签
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(xAxisLableEnable)
        xAxis.labelCount = xCount
        xAxis.valueFormatter = axisValueFormatter
        xAxis.textColor = Color.parseColor("#000000") //x轴标签 颜色
        xAxis.yOffset = 12f
        xAxis.textSize = 12f
        xAxis.axisLineColor = Color.parseColor("#1C4662")  //x轴 颜色
        xAxis.labelRotationAngle = RotationAngle2  //设置X轴字体显示角度


        val leftAxis = chartView.axisLeft
        leftAxis.setDrawLabels(yAxisEnable)
        leftAxis.setDrawAxisLine(false)
        leftAxis.textColor = Color.parseColor("#000000") //y轴标签 颜色
        leftAxis.axisLineColor = Color.parseColor("#1C4662") //y轴 颜色
        leftAxis.setDrawGridLines(true)
        leftAxis.setStartAtZero(true)
        leftAxis.axisMaximum = yMaxVlue * 1.1F  //设置y轴最高点位最大值
        leftAxis.xOffset = toYx // offset 这个方法是在有drawlable true的时候才有作用
        leftAxis.spaceBottom = 0f //只不过是最小值距离底部比例。默认10，y轴独有
        leftAxis.textSize = 12f
        leftAxis.enableGridDashedLine(5f, 5f, 0f)

        val rightAxis = chartView.axisRight
        rightAxis.isEnabled = false


        val l = chartView.legend
        if(isShowLegend) {
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.textColor = blackColor
            l.setDrawInside(true)
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 9f
            l.textSize = legendTextSize
            l.xEntrySpace = 4f
        }else {
            l.isEnabled = false
        }

        val yaxisValueFormatter = ChartYAxisValueFormatUtil(datalist)

        val gradientColor = mutableListOf(context!!.resources.getColor(R.color.green3),context!!.resources.getColor(R.color.bar1_c1))
        val borderColorList = Color.parseColor("#cccccc")

        val barEntryList = ArrayList<BarEntry>()
        val dataSets = ArrayList<IBarDataSet>()
        datalist.forEachIndexed { index, chartData ->
            val name  = datalist[index].getxLable()
            val value  = datalist[index].getyValue()
            barEntryList.add(BarEntry(index.toFloat(), value))
        }


        val barDataSet = BarDataSet(barEntryList, "") //这个name是图例的名字
        barDataSet.setDrawValues(true) //启用/禁用 绘制所有 DataSets 数据对象包含的数据的值文本。
        barDataSet.isHighlightEnabled = isHighlight //是否可以选中高亮
//            barDataSet.color = color_red //设置柱子的颜色 单一
        barDataSet.setGradientColor(gradientColor[0], gradientColor[1])
        barDataSet.barBorderColor = borderColorList //柱子图边框颜色
        barDataSet.barBorderWidth = 1f
        barDataSet.valueTextSize = barTopTextSize
        barDataSet.valueFormatter = yaxisValueFormatter
        barDataSet.valueTextColor = Color.parseColor("#000000")

        dataSets.add(barDataSet)

        val barData = BarData(dataSets)
        barData.barWidth = 0.35f

        chartView.data = barData  //给控件设置数据
        chartView.fitScreen()
        chartView.animateXY(animateTime, animateTime)
//        chartView.animateY(animateTime);
    }

}