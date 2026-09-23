package com.jwch.gwyt_project.fragment

import android.graphics.Color
import android.widget.CompoundButton
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.LayerType
import com.jwch.gwyt_project.Info.SaType
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.QueryResultDetailAdapter
import com.jwch.gwyt_project.databinding.FragQueryResultDetailBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.AnalysisListModel
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.util.CaculationUtil
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat
import java.util.*


/**
 * 空间查询--手势查询结果详情
 * 第二个页面  -- 有图表，有明细
 */
class QueryResultDetailFragment : BaseFragment<FragQueryResultDetailBinding>(), CompoundButton.OnCheckedChangeListener {

    private var currentInfo: AnalysisListModel? = null
    var unit = "" //单位
    private var dataList: List<SaType>? = null
    private var adapter: QueryResultDetailAdapter? = null
    var layerTypeListLV1: MutableList<LayerType>? = null  //第二级 大类
    var layerTypeListLV2: MutableList<LayerType>? = null  //第三级 小类


    var typeListV1: MutableList<SaType> = ArrayList<SaType>()  //大类统计列表
    var typeListV2: MutableList<SaType> = ArrayList<SaType>()  //小类统计列表

    val caculationUtil = CaculationUtil()

    override fun initView() {
        EventBus.getDefault().register(this)
        vb.rbList.setOnCheckedChangeListener(this)
        vb.rbPie.setOnCheckedChangeListener(this)
        initList()

        layerTypeListLV1 = db.queryLayerTypeByTypeType("1")
        layerTypeListLV2 = db.queryLayerTypeByTypeType("2")
    }

    private fun initList() {
        dataList = mutableListOf()
        vb.lvTable.setLinearManager()
        adapter = QueryResultDetailAdapter()
        vb.lvTable.adapter = adapter
        vb.lvTable.disableLoadMoreIfNotFullPage()
    }

    override fun initViewListener() {
        vb.ivQueryDetailClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_RESULT))
        }
        vb.ivQueryDetailBack.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_RESULT_DETAIL))
        }
        vb.rlDetail.onClick {
            //去详情列表
            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_DETAIL_LIST, currentInfo))
        }
    }

    //Tap切换
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.rbPie -> if (isChecked) {
                //兴趣点收藏
                vb.pieChart.show()
                vb.llList.gone()
            }
            R.id.rbList -> if (isChecked) {
                //标绘收藏
                vb.pieChart.gone()
                vb.llList.show()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleData(event: DataEvent) {
        when (event.actionType) {
            DataEvent.SHOW_QUERY_RESULT_DETAIL -> if (CommonUtil.isNotNull(event.data)) {
                currentInfo = event.data as AnalysisListModel
                statisticalAnalysis(currentInfo)
            }
        }
    }

    /**
     * 统计分析
     *
     * @param info
     */
    private fun statisticalAnalysis(info: AnalysisListModel?) {
        if (info == null) return

        vb.tvTitle.text = info.layerName.self()

        val list = info.interSectionModel
        var typeList: MutableList<SaType> = ArrayList<SaType>()

        val totalSaType = SaType("总计", list.size, SaType.Mode.TOTAL)
        val elseType = SaType("其他", SaType.Mode.OTHER)
        val uncoverType = SaType("未覆盖区域", SaType.Mode.UNCOVER)

        for (item in list) {

            val featureType = item.geometryName//获取到属性值
            "属性类型：${featureType}".printMsg()
            var flag = true

            for (st in typeList) {
                if (st.name == featureType) {
                    st.addCount()
                    val SizeValueMu = caculationUtil.getAreaStringMu(item.areaSizeValue, true)
                    st.addTotalArea(SizeValueMu.toFloat())
                    flag = false
                    break
                }

            }
            if (flag) {
                val SizeValueMu = caculationUtil.getAreaStringMu(item.areaSizeValue, true)
                typeList.add(SaType(featureType, SizeValueMu.toFloat()))
            }
            val SizeValueMu = caculationUtil.getAreaStringMu(item.areaSizeValue, true)
            totalSaType.addTotalArea(SizeValueMu.toFloat())


        }

        val uncoverSize = info.geomterySize.toFloat() - totalSaType.totalArea
        if (uncoverSize > 0.01) {
            //未压盖区域
            uncoverType.name = "未覆盖区域" //可能会变化
            uncoverType.addTotalArea((info.geomterySize.toFloat()) - totalSaType.totalArea)
            typeList.add(uncoverType)
        }


        typeList.add(elseType)
        typeList.add(totalSaType)

        val total: Float = totalSaType.totalArea
//        for (st in typeList) {
//            if (st.totalArea / total < 0.05 && st.dataType === SaType.Mode.NORMAL) {
//                st.setDataType(SaType.Mode.IGNOR)
//                elseType.addTotalArea(st.totalArea)
//            }
////                        st.setTotalArea((float) (Math.round(st.totalArea / 100)) / 100);
//        }
        if (elseType.totalArea.toInt() == 0) {
            typeList.remove(elseType)
        }


        //小类统计列表
        typeListV2 = specialHandle(typeList) as MutableList<SaType>
        //大类统计列表
        typeListV1 = getBigTypeList(typeListV2) as MutableList<SaType>

        //初始化图表数据，列表数据

//        initBarChart(typeList)

        dataList = chooseV1orV2()

        initTable(dataList!!)
        initPieChart(dataList!!)
    }

    private fun chooseV1orV2(): List<SaType> {
        when (currentInfo?.layerName) {
            "建设用地管制区", "土地利用总体规划管制区", "城市总体规划", "控制性详细规划" -> {
                return typeListV2
            }
            else -> {
                return typeListV1
            }
        }
    }

    //特定的图层展示会有区别 对统计数据做特殊处理
    private fun specialHandle(typeList: List<SaType>): List<SaType> {

        when (currentInfo?.layerName) {
            //关于永久基本农田图层 所有的统计结果中就写 基本农田类型 不要分类统计的结果
            "永久基本农田" -> {
                val listWithoutNormal = typeList.filter { it.dataType != SaType.Mode.NORMAL }
                listWithoutNormal.firstOrNull { it.dataType == SaType.Mode.TOTAL }?.let {
                    it.name = "永久基本农田"
                    it.dataType = SaType.Mode.NORMAL
                }

                return listWithoutNormal

            }
            else -> {
                return typeList
            }
        }

    }

    private fun initPieChart(typeList: List<SaType>) {
        //筛选出大类
        val list = typeList.filter { it.level == "0" }
        val yVals = mutableListOf<PieEntry>() //值坐标
        val xVals = mutableListOf<String>() //对应的Lable,可以理解成X轴
        for (i in list.indices) {
            if (list[i].dataType != SaType.Mode.TOTAL) {
                xVals.add(list[i].name)
                yVals.add(PieEntry(list[i].totalArea, i.toFloat()))
            }
        }

        //设置很多颜色
        val colors = ArrayList<Int>()
        ColorTemplate.VORDIPLOM_COLORS.forEach {
            colors.add(it)
        }
        ColorTemplate.JOYFUL_COLORS.forEach {
            colors.add(it)
        }
        ColorTemplate.COLORFUL_COLORS.forEach {
            colors.add(it)
        }
        ColorTemplate.LIBERTY_COLORS.forEach {
            colors.add(it)
        }
        ColorTemplate.PASTEL_COLORS.forEach {
            colors.add(it)
        }

        val pieDataSet = PieDataSet(yVals, "") //创建饼图的一个数据集
        pieDataSet.setColors(colors) //设置成丰富多彩的颜色
        val metrics = requireContext().resources.displayMetrics
        val px = 10 * (metrics.densityDpi / 160f)
        pieDataSet.selectionShift = px //点击后延伸出来的长度
        val piedata = PieData(pieDataSet) //生成PieData
        pieDataSet.selectionShift = 10f
        piedata.setDrawValues(true) //设置是否显示数据实体(百分比，true:以下属性才有意义)
        piedata.setValueTextColor(Color.BLACK) //设置所有DataSet内数据实体（百分比）的文本颜色
        piedata.setValueTextSize(12f) //设置所有DataSet内数据实体（百分比）的文本字体大小
        piedata.setValueFormatter(PercentFormatter()) //设置所有DataSet内数据实体（百分比）的文本字体格式
        vb.pieChart.data = piedata //给PieChart填充数据
        vb.pieChart.setExtraOffsets(3f, 3f, 20f, 3f)
        vb.pieChart.legend.horizontalAlignment =  Legend.LegendHorizontalAlignment.RIGHT
        vb.pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        vb.pieChart.legend.form = Legend.LegendForm.CIRCLE //设置注解的位置和形状
        vb.pieChart.setUsePercentValues(true)
        vb.pieChart.isDrawHoleEnabled = false
//        vb.pieChart.setDescription("") //设置描述
        vb.pieChart.setDrawSliceText(false) //不展示文字
//        pieChart.setTransparentCircleAlpha(100) //透明圈的透明度，分3圈，一个是外面的值，然后是这个，然后就是下面的那个Hole
//        pieChart.setTransparentCircleColor(Color.RED) //设置颜色
//        pieChart.transparentCircleRadius = 50f //设置半径
//        pieChart.holeRadius = 30f  //设置空洞半径


//        piechart.setDescription("");//设置描述文字
        vb.pieChart.invalidate()
        vb.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {


            override fun onValueSelected(e: Entry?, h: Highlight?) {

            }

            override fun onNothingSelected() {}
        })
    }

//    private fun initBarChart(typeList: List<SaType>) {
//        val yVals = ArrayList<BarEntry>() //Y轴方向第一组数组
//        val xVals = ArrayList<String>() //X轴数据
//        val lin: MutableList<SaType> = ArrayList()
//        for (i in typeList.indices) {
//            if (typeList[i].dataType != SaType.Mode.TOTAL) {
//                lin.add(typeList[i])
//            }
//        }
//        for (i in lin.indices) {
//            xVals.add(lin[i].name)
//            yVals.add(BarEntry(lin[i].totalArea, i))
//        }
//        val barDataSet = BarDataSet(yVals, "")
//        barDataSet.color = Color.RED //设置第一组数据颜色
//        val threebardata =
//            ArrayList<BarDataSet>() //IBarDataSet 接口很关键，是添加多组数据的关键结构，LineChart也是可以采用对应的接口类，也可以添加多组数据
//        threebardata.add(barDataSet)
//        val bardata = BarData(xVals, threebardata)
//        barChart.setDrawGridBackground(false)
//        barChart.data = bardata
//        barChart.legend.isEnabled = false
//        barChart.legend.position = Legend.LegendPosition.BELOW_CHART_CENTER //设置注解的位置在左上方
//        barChart.legend.form = Legend.LegendForm.CIRCLE //这是左边显示小图标的形状
//        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM //设置X轴的位置
//        barChart.xAxis.setDrawGridLines(false) //不显示网格
//        barChart.axisRight.isEnabled = false //右侧不显示Y轴
//        barChart.axisLeft.axisMinValue = 0.0f //设置Y轴显示最小值，不然0下面会有空隙
//        barChart.axisLeft.setDrawGridLines(false) //不设置Y轴网格
//        barChart.setDescription("") //设置描述
//        barChart.setDescriptionTextSize(20f) //设置描述字体
//        barChart.animateXY(1000, 2000) //设置动画
//        bardata.setValueFormatter { v, entry, i, viewPortHandler ->
//            entry.getVal().toString() + unit //只用拿到对应Entry的值然后加个“元”即可，传入的这几个参数，v就是Y轴的value, entry为数据入口，i就是X轴方向的位置，viewPortHandler应该就是对应View的操作手，控制视图的移动缩放什么的
//        }
//    }


    private fun getBigTypeList(list: List<SaType>): List<SaType> {
        //大类的List
        val typeBigList: MutableList<SaType> = ArrayList<SaType>()

        val df = DecimalFormat("#.##")

        //先筛选出各小分类的list  即不包括TOTAL 和UNCOVER
        val listWithoutTotal = list.filter { it.dataType != SaType.Mode.TOTAL && it.dataType != SaType.Mode.UNCOVER }
        listWithoutTotal.forEach { saType ->
            saType.totalAreaPercentage = df.format(saType.totalArea / currentInfo!!.geomterySize * 100).toFloat()

            //给小类设置对应的大类
            val item = layerTypeListLV2?.firstOrNull { it.typeName == saType.name }
            (item != null).yes {
                //有对应的大类 给parentName赋值
                saType.parentName = item?.parentId1Name
            }.no {
                //没有对应大类的 给parentName赋值为图斑名称
                saType.parentName = currentInfo?.layerName
            }
        }


        layerTypeListLV1?.forEach { type ->
            //通过相同的大类名称 筛选出list
            val result = listWithoutTotal.filter { it.parentName == type.typeName }
            (!result.isNullOrEmpty()).yes {
                val sa = SaType(type.typeName, 0F)
                typeBigList.add(sa)
                //各小类的面积和占比加起来 作为大类的数据
                result.forEach { item ->
                    sa.addTotalArea(item.totalArea)
                    sa.addTotalPercentage(item.totalAreaPercentage)

                    if (type.typeName == "耕地" || type.typeName == "林地") {
                        typeBigList.add(SaType(item.name, item.totalArea, item.totalAreaPercentage, "1"))
                    }
                }
            }
        }

        //处理没有大类的数据 即大类名为图斑名称的
        val noBigResult = listWithoutTotal.filter { it.parentName == currentInfo?.layerName }
        (!noBigResult.isNullOrEmpty()).yes {
            val sa = SaType(currentInfo?.layerName, 0F)
            typeBigList.add(sa)
            //小类的面积和占比加起来 作为大类的数据
            noBigResult.forEach { item ->
                sa.addTotalArea(item.totalArea)
                sa.addTotalPercentage(item.totalAreaPercentage)
            }
        }

        //判断是否有未压盖区域 有则添加
        val uncoverPart = list.firstOrNull { it.dataType == SaType.Mode.UNCOVER }
        (uncoverPart != null).yes {
            uncoverPart!!.totalAreaPercentage = df.format(uncoverPart.totalArea / currentInfo!!.geomterySize * 100).toFloat()
            typeBigList.add(list.first { it.dataType == SaType.Mode.UNCOVER })
        }

//        typeBigList.add( list.first { it.dataType == SaType.Mode.TOTAL  })

        typeBigList.forEach {
            it.totalAreaPercentage = df.format(it.totalAreaPercentage).toFloat()
        }

        return typeBigList

    }

    private fun initTable(list: List<SaType>) {

        this.dataList = list
        vb.lvTable.update(dataList)
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}
