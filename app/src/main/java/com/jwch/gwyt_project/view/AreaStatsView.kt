package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.reflect.TypeToken
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.AreaStatsAdapter
import com.jwch.gwyt_project.adapter.StatisTableAdapter
import com.jwch.gwyt_project.adapter.ztt.ZttItem
import com.jwch.gwyt_project.databinding.ViewAreaStatsBinding
import com.jwch.gwyt_project.ext.clearAllKv
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.getObjByType
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.selfTempNo
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.model.StatisticDataModel
import com.jwch.gwyt_project.model.StatisticModel
import com.jwch.gwyt_project.util.FunctionControlUtil
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.util.excel.SmartTableUtil
import com.jwch.gwyt_project.util.excel.TableDataModel
import com.jwch.gwyt_project.util.mpchart.ChartUtil
import com.jwch.gwyt_project.util.mpchart.model.ChartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.time.Year
import java.util.Calendar


//按照行政区划统计专题图的图斑数量
class AreaStatsView : LinearLayout {
    val layoutId: Int = R.layout.view_area_stats
    var vb: ViewAreaStatsBinding? = null
    var areaData: DistrictsInfo? = null

    var zttList = mutableListOf<ZttItem>()
    private var adapter: AreaStatsAdapter? = null

    val tableUtil = SmartTableUtil(context)
    var tableList = mutableListOf<TableDataModel>()
    private var statisTableAdapter: StatisTableAdapter? = null
    var barChartDataList = mutableListOf<StatisticDataModel>()

    var chartUtil = ChartUtil(context)

    var year = "" //统计的年份


    fun initView(context: Context?) {
        EventBus.getDefault().register(this)
        context?.let {
            mContext = context
            val contentView: View = LayoutInflater.from(it).inflate(layoutId, null)
            var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params
            vb = ViewAreaStatsBinding.bind(contentView)
            vb?.apply {
                addView(root)
            }
        }
    }

    lateinit var mContext: Context

    constructor(context: Context?) : super(context) {
        initViewData(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewData(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViewData(context, attrs)
    }

    override fun onFinishInflate() {
        initViewData()
        super.onFinishInflate()
    }

    fun initViewData(context: Context?, attrs: AttributeSet?) {
        initView(context)

        if (context == null || attrs == null) return

        initTableList()

        vb!!.cbType.setNewLabelArray(getYearsFromStartToNow(2022))

        vb!!.cbType.selectionActionBlock2 = { pos, item ->
            year = item.value

            reloadData(areaData!!,year)

            EventBus.getDefault().post(MapEvent(MapEvent.UPDATE_STATS_YEAR, year))
        }
    }

    fun initViewData() {


    }


    fun initTableList() {
        vb!!.lvTable.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        statisTableAdapter = StatisTableAdapter()
        vb!!.lvTable.adapter = statisTableAdapter
        statisTableAdapter!!.setOnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as TableDataModel
            if(item.key != "专题类型"){
                tableList.forEach {
                    it.isSelect = false
                }
                item.isSelect = true
                statisTableAdapter!!.update(tableList)


                val chartData = barChartDataList.firstOrNull { it.title == item.key }
                if (chartData != null) {
                    hanldeChart(chartData)
                    EventBus.getDefault().post(MapEvent(MapEvent.STATUS_DATA_BY_AREA, chartData))
                }
            }

        }
    }

    var index = 0
    fun reloadData(areaData: DistrictsInfo, y: String) {
        vb!!.cbType.setSelectionByKey(y)
        year = y
        this.areaData = areaData
        barChartDataList.clear()
        tableList.clear()
        vb!!.barChart.gone()
        vb!!.barChartTitle.gone()


        FunctionControlUtil.instances.CLEAR_ALL_KV.yes {
            clearAllKv()
        }
//

        //取缓存 有的数据直接加载
        val json = getKV<String>("${Keys.AREA_ZTT_COUNT_LIST}-${areaData.distName}-${year}", "")
        val json2 = getKV<String>("${Keys.AREA_ZTT_PIE_LIST}-${areaData.distName}-${year}", "")

        if (json.isNotBlank() && json2.isNotBlank()) {
            "===--------- 读取统计缓存".printMsg()

            vb!!.rlLoading.gone()
            vb!!.lvTable.show()

            tableList = getObjByType(json, object : TypeToken<MutableList<TableDataModel>>() {}.type)
//            showTableView(tableList)
            tableList.first { it.key == "全部" }.isSelect = true
            statisTableAdapter?.update(tableList)

            barChartDataList = getObjByType(json2, object : TypeToken<MutableList<StatisticDataModel>>() {}.type)

            CommonUtil.matchList(barChartDataList).yes {
                vb!!.barChart.show()
                vb!!.barChartTitle.show()
            }.no {
                vb!!.barChart.gone()
                vb!!.barChartTitle.gone()
            }
            hanldeChart(barChartDataList[barChartDataList.size -1])

            EventBus.getDefault().post(MapEvent(MapEvent.STATUS_DATA_BY_AREA, barChartDataList.firstOrNull { it.title == "全部" }))
        } else {
            "===--------- 正常统计计算".printMsg()
            vb!!.rlLoading.show()
            vb!!.lvTable.gone()

            index = 0

            val filterList = mutableListOf<ZttItem>()
            filterList.addAll(zttList.filter { it.name.self().contains(year)})
            loadThemeDataCount(filterList)
        }

    }


    private fun loadThemeDataCount(filterList: MutableList<ZttItem>) {
        if(!CommonUtil.matchList(filterList)){
            vb!!.rlLoading.gone()
            handleResultData(filterList, year)
            return
        }
        "index ==1== $index".printMsg()
        val item = filterList[index]
        item.queryThemeDataCountByArea(areaData!!) { success ->
            success.yes {
                index++
                if (index < filterList.size) {
                    loadThemeDataCount(filterList)

                } else {
                    "结束时间  ${TimeUtil.getCurrentStamp()}".printMsg()
                    adapter?.update(filterList)

                    handleResultData(filterList, year)
                }
            }.no {
                index++
            }
        }

    }


    fun handleResultData(list : MutableList<ZttItem>, year :String){
        tableList.clear()
        barChartDataList.clear()

        var myZttList = mutableListOf<ZttItem>()

        myZttList.addAll(list.filter { it.name.self().contains(year)})

        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            vb!!.rlLoading.gone()
            //排个序 把数量最多的放前面
            myZttList.sortByDescending { it.dataCountArea }

            //先把其他放在四乱下
            val (siluan, qita) = myZttList.partition { !it.name.self().contains("其他") }
            myZttList = (siluan + qita) as MutableList
            //把水葫芦放在四乱下面
            val (siluan1, shuihul) = myZttList.partition { !it.name.self().contains("督办") }
            myZttList = (siluan1 + shuihul) as MutableList
            //把水利部图斑在水葫芦下面
            val (siluan2, shuilibu) = myZttList.partition { !it.name.self().contains("水利部图斑") }
            myZttList = (siluan2 + shuilibu) as MutableList
            //把暗访事件放在水利部图下面
            val (siluan3, anfang) = myZttList.partition { !it.name.self().contains("暗访") }
            myZttList = (siluan3 + anfang) as MutableList
            //自查自纠问题
            val (siluan4, zichazijiu) = myZttList.partition { !it.name.self().contains("自查自纠") }
            myZttList = (siluan4 + zichazijiu) as MutableList

            val totalItem = ZttItem(0)
            totalItem.name = "全部"
            myZttList.forEach {
                totalItem.dataCountArea += it.dataCountArea
                totalItem.dataCountAreaFinish += it.dataCountAreaFinish
                totalItem.dataCountAreaIsQuestion += it.dataCountAreaIsQuestion
                totalItem.queryAttrList.addAll(it.queryAttrList)

            }
            myZttList.add(0, totalItem)

            tableList.clear()

            myZttList.forEach{
                tableList.add(TableDataModel(it.name, "${it.dataCountAreaFinish} / ${it.dataCountArea}","${it.dataCountAreaIsQuestion} / ${it.dataCountArea}"))

                val barData = groupByTypeCount(areaData!!, it.queryAttrList ) as MutableList<StatisticModel>
                barChartDataList.add(StatisticDataModel(it.name.self(), barData))

            }

            CommonUtil.matchList(barChartDataList).yes {
                vb!!.barChart.show()
                vb!!.barChartTitle.show()
                hanldeChart(barChartDataList[barChartDataList.size -1]) //展示全部对应的柱状图
            }.no {
                vb!!.barChart.gone()
                vb!!.barChartTitle.gone()

            }

            EventBus.getDefault().post(MapEvent(MapEvent.STATUS_DATA_BY_AREA, barChartDataList.firstOrNull { it.title == "全部" }))
            tableList.add(0, TableDataModel("专题类型","已办 / 全部","是问题 / 全部"))

            saveKV("${Keys.AREA_ZTT_COUNT_LIST}-${areaData!!.distName}-${year}", tableList.toJson())
            saveKV("${Keys.AREA_ZTT_PIE_LIST}-${areaData!!.distName}-${year}", barChartDataList.toJson())

//            showTableView(tableList)
            vb!!.lvTable.show()
            tableList.first { it.key == "全部" }.isSelect = true
            statisTableAdapter?.update(tableList)

        }

    }

    // 分组统计方法
    fun groupByTypeCount(areaData: DistrictsInfo, list: MutableList<Map<String, Any>>): List<StatisticModel> {
        //根据当前区域的下一级区域 来作为分组名称
        val groupName = when (areaData.distLevel) {
            DistrictsInfo.LEVEL_PROVINCE -> "所在市"
            DistrictsInfo.LEVEL_CITY -> "所在县"
            DistrictsInfo.LEVEL_COUNTY -> "所在镇"
            DistrictsInfo.LEVEL_TOWN -> "所在村"
            DistrictsInfo.LEVEL_VILLAGE -> ""
            else -> ""
        }

        return list
            // 按type分组，处理可能的null值（这里用空字符串替代）
            .groupBy { it[groupName] ?: "" }
            // 转换为Model对象列表

            .map { (type, items) ->
                // 计算总数量
                val totalCount = items.size
                // 计算"已办结"数量
                val resolvedCount = items.count {
                    (it["事件状"] as? String) == "已办结"
                }
                // 假设StatisticModel现在有totalCount和resolvedCount两个参数
                StatisticModel(
                    (type as String).selfTempNo("未知"),
                    totalCount,
                    resolvedCount
                )
            }
    }

    fun hanldeChart(item: StatisticDataModel) {

        //乡镇级别 不展示柱状统计图
        if(areaData?.distLevel == DistrictsInfo.LEVEL_TOWN){
            vb!!.barChart.hide()
            vb!!.barChartTitle.hide()
            return
        }

//        if (!CommonUtil.matchList(item.dataList)) {
//            vb!!.barChart.hide()
//            vb!!.barChartTitle.hide()
//            return
//        }


        val list: MutableList<ChartData> = ArrayList()
        //判断 是否大于4个数据
        if (item.dataList.size > 4) {
            // 分割前3项和剩余项
            val sortedList = item.dataList.sortedByDescending { it.count }
            val top3 = sortedList.take(3) as MutableList
            val remaining = sortedList.drop(3) as MutableList

            // 计算剩余项的 count 总和
            val sumCount = remaining.sumOf { it.count }


            // 创建合并项
            val mergedItem = if (remaining.isNotEmpty()) {
                StatisticModel("其他", sumCount)
            } else {
                null // 如果没有剩余项则不添加
            }

            // 构建新列表（只有4项：前三和其他地区）
            val newList = mutableListOf<StatisticModel>()
            newList.addAll(top3)
            if (mergedItem != null) {
                newList.add(mergedItem)
            }

            newList.forEach {
                list.add(ChartData(it.count, it.type))
            }

        } else {
            item.dataList.forEach {
                list.add(ChartData(it.count, it.type))
            }
        }
        vb!!.barChart.show()
        vb!!.barChartTitle.show()
        vb!!.barChartTitle.text = "${item.title}统计图"
        chartUtil.singleBarChart(vb!!.barChart, list, false, true)
    }

    /**
     * 通用版：获取从指定起始年份到当前年份的倒序字符串
     * @param startYear 起始年份（如2022）
     * @return 倒序年份字符串
     */
    fun getYearsFromStartToNow(startYear: Int): String {
        val currentYear = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Year.now().value
        } else {
            Calendar.getInstance().get(Calendar.YEAR)
        }

        if (currentYear <= startYear) {
            return startYear.toString()
        }

        return (startYear..currentYear).reversed().joinToString(",")
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun handleData(event: DataEvent) {
        when (event.actionType) {
            DataEvent.ZTT_DATA_TO_AREA_STAT -> {
                event.data?.apply {
                    val list = event.data as MutableList<ZttItem>
                    zttList = list.filter { it.name.self().contains("乱占")
                            || it.name.self().contains("乱建")
                            || it.name.self().contains("乱堆")
                            || it.name.self().contains("乱采")
                            || it.name.self().contains("其他")
                            || it.name.self().contains("水葫芦")
                            || it.name.self().contains("水利部图斑")
                            || it.name.self().contains("暗访事件")
                            || it.name.self().contains("自查自纠")
                    } as MutableList
                }
            }
            DataEvent.REVERSE_TABLE -> {
                event.data?.apply {
                    val districtsList = event.data as MutableList<DistrictsInfo>
                    val myDataList = districtsList!!.map { item ->
                        StatisticModel(
                            type = item.distName,
                            count = item.count,
                            countFinish = item.countFinish
                        )
                    }

                    if (CommonUtil.matchList(myDataList)){

                        val list: MutableList<ChartData> = ArrayList()
                        //判断 是否大于4个数据
                        if (myDataList.size > 4) {
                            // 分割前3项和剩余项
                            val sortedList = myDataList.sortedByDescending { it.count }
                            val top3 = sortedList.take(3) as MutableList
                            val remaining = sortedList.drop(3) as MutableList

                            // 计算剩余项的 count 总和
                            val sumCount = remaining.sumOf { it.count }

                            // 创建合并项
                            val mergedItem = if (remaining.isNotEmpty()) {
                                StatisticModel("其他地区", sumCount)
                            } else {
                                null // 如果没有剩余项则不添加
                            }

                            // 构建新列表（只有4项：前三和其他地区）
                            val newList = mutableListOf<StatisticModel>()
                            newList.addAll(top3)
                            if (mergedItem != null) {
                                newList.add(mergedItem)
                            }

                            newList.forEach {
                                list.add(ChartData(it.count, it.type))
                            }

                        } else {
                            myDataList.forEach {
                                list.add(ChartData(it.count, it.type))
                            }
                        }
                        chartUtil.singleBarChart(vb!!.barChart, list, false, true)

                    }


                }

            }
        }
    }


}