package com.jwch.gwyt_project.fragment

import android.annotation.SuppressLint
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.adapter.TownAdapter2
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragAreaList2Binding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.isShow
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.model.StatisticDataModel
import com.jwch.gwyt_project.model.StatisticModel
import com.jwch.gwyt_project.util.GetJsonUtil
import com.jwch.gwyt_project.util.OperationLogger
import com.jwch.gwyt_project.util.RegionConfigManager
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.Collator
import java.util.Locale


/**
 * 行政区划--层级结构
 * 代码简化完成
 */
class AreaListFragment2 : BaseFragment<FragAreaList2Binding>() {

//    private var townAdapter: TownAreaAdapter? = null

    var dataList: MutableList<DistrictsInfo>? = null//行政区划数据
    var selectData: DistrictsInfo? = null //当前选中的行政区

    var maxLevel = DistrictsInfo.LEVEL_COUNTY  //行政区划最大级别设置
    var minLevel = DistrictsInfo.LEVEL_VILLAGE  //行政区划最小级别设置


    var year = "2026" //初始化统计的年份

    var townAdapter2: TownAdapter2<DistrictsInfo>? = null

    var firstOpen = true


    override fun initView() {

        initList()
        initData(Config.AreaCode)
        EventBus.getDefault().register(this)
    }

    fun initList(){
//        vb.lvTown.setGridManager(3)
//        townAdapter = TownAreaAdapter()
//        vb.lvTown.adapter = townAdapter
//        vb.lvTown.gone()

        townAdapter2 = TownAdapter2(requireContext())
        vb.lvTown2.adapter = townAdapter2
        vb.lvTown2.gone()

    }

    fun doStats() {
        if (selectData == null) {
            tip("请先选择一个查询范围")
            return
        }
//        if(selectData!!.distLevel == maxLevel){
//            vb.viewAreaStats.visiable(selectData!!.distLevel != maxLevel)
//            return
//        }
//        if (selectData!!.geometryList == null) {
//            tip("暂无行政区划范围数据，统计失败")
//            return
//        }
        vb.viewAreaStats.show()
        vb.viewAreaStats.reloadData(selectData!!, year)
    }


    override fun initViewListener() {
        vb.ivClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_AREA_FRAG))

            //关闭行政区 不清空范围
//            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_AREA))


            if (vb.viewAreaStats.isShow()) {
                vb.viewAreaStats.gone()
            }
            //还原
            initData(Config.AreaCode)
        }

        //返回上一层
        vb.tvAreaBack.onClick {

            selectData?.apply {
                vb.viewAreaStats.gone()

                val lastCode = getLastDistCode(selectData!!)
                val item = AppContext.app.districtHelper.queryDistrictByCode(lastCode)
                if (item != null) {
                    selectData = item
                    vb.tvCurrentArea.text = "当前区域：${selectData!!.distName}"
                    dataList = AppContext.app.districtHelper.getNextLevelDistricts(selectData!!.distCode)
                    dataList?.sortByDescending { it.count }
                    townAdapter2!!.update(dataList)

                    vb.llChildArea.visiable(CommonUtil.matchList(dataList))

                    //查询gdb 绘制行政区划范围
                    val map = mutableMapOf<String, String>()
                    map.put("name", getSelfValue(selectData!!.distName))
                    map.put("code", getDistrictCode(selectData!!))
                    when (selectData!!.distLevel) {
                        DistrictsInfo.LEVEL_PROVINCE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_PROVINCE, map, selectData!!))
                        DistrictsInfo.LEVEL_CITY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_CITY, map, selectData!!))
                        DistrictsInfo.LEVEL_COUNTY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_AREA, map, selectData!!))
                        DistrictsInfo.LEVEL_TOWN -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_COUNTY, map, selectData!!))
                        DistrictsInfo.LEVEL_VILLAGE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_VILLAGE, map, selectData!!))
                    }
//                    vb.lvTown.gone()
                    vb.lvTown2.gone()
                }

                vb.tvAreaBack.visiable(selectData!!.distLevel != maxLevel)
            }
        }
    }



    @SuppressLint("SetTextI18n")
    fun initData(distCode: String) {

        val item = AppContext.app.districtHelper.queryDistrictByCode(distCode)
        if (item != null) {
            maxLevel = item.distLevel
            selectData = item
            vb.tvCurrentArea.text = "当前区域：${selectData!!.distName}"
            dataList =  AppContext.app.districtHelper.getNextLevelDistricts(selectData!!.distCode)
//            vb.lvTown.update(dataList)
            dataList?.sortByDescending { it.count }
            townAdapter2!!.update(dataList)


            vb.llChildArea.visiable(CommonUtil.matchList(dataList))
        }


        vb.lvTown2.setOnItemClickListener { adapterView, view, position, id ->

            val model  = dataList?.get(position)

            if( model!!.distLevel >= minLevel){
                tip("已经是最后一级")
                return@setOnItemClickListener
            }

            if(model.distName == "未知"){
                return@setOnItemClickListener
            }

            selectData = model

            vb.tvCurrentArea.text = "当前区域：${selectData!!.distName}"

            dataList = AppContext.app.districtHelper.getNextLevelDistricts(selectData!!.distCode)
            CommonUtil.matchList(dataList).yes {
                vb.llChildArea.show()
            }.no {
                vb.llChildArea.gone()
            }
//            vb.lvTown.update(dataList)
            dataList?.sortByDescending { it.count }
            townAdapter2!!.update(dataList)


            //判断返回按钮是否展示
            vb.tvAreaBack.visiable(selectData!!.distLevel != maxLevel)
            vb.viewAreaStats.gone()

            //查询gdb 绘制行政区划范围
            val map = mutableMapOf<String, String>()
            map.put("name", getSelfValue(selectData?.distName))
            map.put("code", getDistrictCode(selectData!!))
            when (selectData?.distLevel) {
                DistrictsInfo.LEVEL_PROVINCE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_PROVINCE, map, selectData!!))
                DistrictsInfo.LEVEL_CITY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_CITY, map, selectData!!))
                DistrictsInfo.LEVEL_COUNTY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_AREA, map, selectData))
                DistrictsInfo.LEVEL_TOWN -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_COUNTY, map, selectData))
                DistrictsInfo.LEVEL_VILLAGE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_VILLAGE, map, selectData))
            }
        }
    }


    // 排序函数
    fun sortChineseList(list: MutableList<DistrictsInfo>?) {
        CommonUtil.matchList(list).yes {
            val collator = Collator.getInstance(Locale.CHINA) // 使用中文排序规则
            list!!.sortWith { o1, o2 ->
                collator.compare(o1.distName, o2.distName)
            }
        }
    }


    /**
     * 规范化 行政区code
     */
    private fun getDistrictCode(model: DistrictsInfo): String {
        var code = ""
        when (model.distLevel) {
            DistrictsInfo.LEVEL_PROVINCE -> code = model.distCode.substring(0, 2) //省 2位
            DistrictsInfo.LEVEL_CITY -> code = model.distCode.substring(0, 4) //市 4位
            DistrictsInfo.LEVEL_COUNTY -> code = model.distCode.substring(0, 6) //县 6位
            DistrictsInfo.LEVEL_TOWN -> code = model.distCode.substring(0, 9) //乡镇 9位
            DistrictsInfo.LEVEL_VILLAGE -> code = model.distCode.substring(0, 12) //村 12位
        }
        return code
    }

    /**
     * 获取上一级的DistCode
     */
    private fun getLastDistCode(model: DistrictsInfo): String {
        var code = ""
        when (model.distLevel) {
            DistrictsInfo.LEVEL_CITY -> code = model.distCode.substring(0, 2) //市的上一级是省 2位
            DistrictsInfo.LEVEL_COUNTY -> code = model.distCode.substring(0, 4) //县的上一级是市 4位
            DistrictsInfo.LEVEL_TOWN -> code = model.distCode.substring(0, 6) //乡镇的上一级是县 6位
            DistrictsInfo.LEVEL_VILLAGE -> code = model.distCode.substring(0, 9) //村的上一级是乡镇 9位
        }
        return code
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun handleData(event: MapEvent) {
        when (event.actionType) {
            MapEvent.GO_STRAT -> {
                doStats()
            }
            MapEvent.INIT_ARAE_FEAG -> {
                if(firstOpen){
                    val regionInfoJson = GetJsonUtil.getJsonFromFile(Config.APPDB_PATH +"region_info.txt").self().trim()
                    val regionInfo = try {
                        org.json.JSONObject(regionInfoJson).optString("region", "")
                    } catch (e: Exception) {
                        regionInfoJson
                    }
                    RegionConfigManager.setRegion(regionInfo)
                    initData(Config.AreaCode)
                    firstOpen =false
                }

                val map = mutableMapOf<String, String>()
                map.put("name", getSelfValue(selectData!!.distName))
                map.put("code", getDistrictCode(selectData!!))
                when (selectData!!.distLevel) {
                    DistrictsInfo.LEVEL_PROVINCE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_PROVINCE, map, selectData!!))
                    DistrictsInfo.LEVEL_CITY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_CITY, map, selectData!!))
                    DistrictsInfo.LEVEL_COUNTY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_AREA, map, selectData!!))
                    DistrictsInfo.LEVEL_TOWN -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_COUNTY, map, selectData!!))
                    DistrictsInfo.LEVEL_VILLAGE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_VILLAGE, map, selectData!!))
                }
            }
            MapEvent.STATUS_DATA_BY_AREA -> {
                val item = event.data as StatisticDataModel

                if(CommonUtil.matchList(dataList)){
                    mergeLists(item.dataList, dataList!!)

                    dataList?.sortByDescending { it.count }
                    townAdapter2!!.update(dataList)
                    vb.lvTown2.show()

                    EventBus.getDefault().post(DataEvent(DataEvent.REVERSE_TABLE, dataList))
                }


            }
            MapEvent.UPDATE_STATS_YEAR -> {
                year = event.data as String
            }

        }
    }


    fun mergeLists(listA: List<StatisticModel>, listB: MutableList<DistrictsInfo>) {
        // 1. 移除 "未知" 的项（如果有的话）
        listB.removeIf { it.distName == "未知" }

        // 2. 构建映射：前两个字符 -> (count, countFinish)
        val countMap = listA.groupBy { it.type.take(2) }
            .mapValues { entry ->
                val totalCount = entry.value.sumOf { it.count }
                val totalCountFinish = entry.value.sumOf { it.countFinish }
                Pair(totalCount, totalCountFinish)
            }

        // 3. 遍历 listB，更新 count 和 countFinish
        listB.forEach { itemB ->
            val prefix = itemB.distName.take(2)
            if (countMap.containsKey(prefix)) {
                // 如果匹配到，更新 count 和 countFinish
                val (count, countFinish) = countMap[prefix]!!
                itemB.count = count
                itemB.countFinish = countFinish
            } else {
                // 如果未匹配到，置为 0
                itemB.count = 0
                itemB.countFinish = 0
            }
        }

        // 4. 处理未匹配的 type，添加 "未知" 项
        val matchedPrefixes = listB.map { it.distName.take(2) }.toSet()
        val unmatchedTypes = countMap.keys - matchedPrefixes

        if (unmatchedTypes.isNotEmpty()) {
            // 计算所有未匹配 type 的总和
            val totalUnmatchedCount = unmatchedTypes.sumOf { countMap[it]?.first ?: 0 }
            val totalUnmatchedCountFinish = unmatchedTypes.sumOf { countMap[it]?.second ?: 0 }

            // 添加 "未知" 项到 listB
            listB.add(DistrictsInfo().apply {
                distName = "未知"
                count = totalUnmatchedCount
                countFinish = totalUnmatchedCountFinish
            })
        }
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}