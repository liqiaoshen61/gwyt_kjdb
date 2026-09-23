package com.jwch.gwyt_project.fragment

import com.bin.david.form.data.column.Column
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.Info.MarkerInfo
import com.jwch.gwyt_project.adapter.TownAdapter2
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragGeoCollectionStatisBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.StatisticDataModel
import com.jwch.gwyt_project.model.StatisticModel
import com.jwch.gwyt_project.util.excel.SmartTableUtil
import com.jwch.gwyt_project.util.excel.TableDataModel
import com.jwch.gwyt_project.util.mpchart.ChartUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.Collator
import java.util.Locale

/**
 * 新增的工作记录 统计功能
 */

class GeoCollectionStatisFragment : BaseFragment<FragGeoCollectionStatisBinding>() {


    var geoList: MutableList<MarkerInfo> = mutableListOf()
    var townAdapter2: TownAdapter2<DistrictsInfo>? = null

    var districtsList: MutableList<DistrictsInfo>? = null//行政区划数据

    var selectData: DistrictsInfo? = null //当前选中的行政区
    var initCode = Config.AreaCode //这里需要需要设置 初始展示的区域代码
    var maxLevel = DistrictsInfo.LEVEL_COUNTY  //行政区划最大级别设置
    var minLevel = DistrictsInfo.LEVEL_VILLAGE  //行政区划最小级别设置


    lateinit var tableUtil: SmartTableUtil
    var tableList = mutableListOf<TableDataModel>()

    override fun initView() {
        tableUtil = SmartTableUtil(requireContext())
        initList()
        initData(initCode)
        EventBus.getDefault().register(this)
    }

    fun initData(distCode: String) {

        val item = db.queryDistrictByCode(distCode)
        if (item != null) {
            maxLevel = item.distLevel
            selectData = item
            "当前区域：${selectData!!.distName}".printMsg()
            districtsList = getChildDistrictList(selectData!!)
            townAdapter2!!.update(districtsList)

            vb.lvTown2.visiable(CommonUtil.matchList(districtsList))
        }
    }

    fun initList() {
        townAdapter2 = TownAdapter2(requireContext())
        vb.lvTown2.adapter = townAdapter2
        vb.lvTown2.gone()
    }


    //加载标绘数据
    private fun reloadGeoCollectData() {
        tableList.clear()
        geoList.clear()
        db.queryAllMarkerListByFolderId(0)?.let {
            geoList = it
        }

        //按四乱类型分组
        val typeMap = geoList.groupBy { it.questionType }
        typeMap.forEach {
            val Allcount = it.value.size
            val finishCount = it.value.filter { item -> item.isFinish == "已整改" }.size

            tableList.add(TableDataModel(it.key, "${finishCount} / ${Allcount}"))
        }

        showTableView(tableList)

        //按行政区划分组
        val groupList = groupByTypeCount(selectData!!, geoList) as MutableList<StatisticModel>
        //再匹配
        mergeLists(groupList, districtsList!!)
        districtsList?.sortByDescending { it.count }
        townAdapter2!!.update(districtsList)
        vb.lvTown2.show()
    }

    fun showTableView(dataList: MutableList<TableDataModel>) {

        if (CommonUtil.matchList(dataList)) {
            vb.viewTable2.show()
        } else {
            vb.viewTable2.gone()
        }

        //标题部分
        val column0 = Column<String>("问题类型", "key")
        val column1 = Column<String>("已整改 / 全部", "value")
        val columnList = mutableListOf(column0, column1) as MutableList<Column<*>>

        tableUtil.showTable(vb.viewTable2, columnList, dataList)
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


    override fun initViewListener() {

        //关闭我的收藏
        vb.ivCollectionClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_COLLECTION_FRAG))
        }

        //返回
        vb.ivBack.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.BACK_COLLECTION_FRAG))
        }

    }


    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlePageView(event: DataEvent?) {
        if (event == null) return
        when (event.actionType) {

            DataEvent.GO_COLLECTION_STATIS_FRAG -> {
                reloadGeoCollectData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun getChildDistrictList(model: DistrictsInfo): MutableList<DistrictsInfo>? {
        var districtList: MutableList<DistrictsInfo>? = null

        val nextLevel = model.distLevel + 1
        val nextList = db.queryDistrictListByLevel(nextLevel)

        val formatCode = getDistrictCode(model)
        CommonUtil.matchList(nextList).yes {
            districtList =
                nextList?.filter { it.distCode.startsWith(formatCode) } as MutableList<DistrictsInfo>
        }
        //拼音排序
        sortChineseList(districtList)
        return districtList
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

    // 排序函数
    fun sortChineseList(list: MutableList<DistrictsInfo>?) {
        CommonUtil.matchList(list).yes {
            val collator = Collator.getInstance(Locale.CHINA) // 使用中文排序规则
            list!!.sortWith { o1, o2 ->
                collator.compare(o1.distName, o2.distName)
            }
        }
    }

    // 分组统计方法
    fun groupByTypeCount(
        areaData: DistrictsInfo,
        list: MutableList<MarkerInfo>
    ): List<StatisticModel>? {
        val result: List<StatisticModel>?

        //根据当前区域的下一级区域 来作为分组名称
        val map = when (areaData.distLevel) {
            DistrictsInfo.LEVEL_PROVINCE -> {
                list.groupBy { it.city ?: "" }
            }

            DistrictsInfo.LEVEL_CITY -> {
                list.groupBy { it.county ?: "" }
            }

            DistrictsInfo.LEVEL_COUNTY -> {
                list.groupBy { it.town ?: "" }
            }

            DistrictsInfo.LEVEL_TOWN -> {
                list.groupBy { it.village ?: "" }
            }

            else -> null
        }

        result = map?.map { (type, items) ->
            // 计算总数量
            val totalCount = items.size
            val finishCount = items.filter { item -> item.isFinish == "已整改" }.size

            // 假设StatisticModel现在有totalCount和resolvedCount两个参数
            StatisticModel(type, totalCount, finishCount)
        }

        return result
    }

}
