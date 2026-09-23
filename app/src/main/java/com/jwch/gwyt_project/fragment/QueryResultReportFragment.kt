package com.jwch.gwyt_project.fragment

import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.LayerType
import com.jwch.gwyt_project.Info.SaType
import com.jwch.gwyt_project.adapter.QueryResultReportAdapter
import com.jwch.gwyt_project.databinding.FragQueryResultReportBinding
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.AnalysisListModel
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.KVData
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
 * 新增页面 -- 一键分析报告
 */
class QueryResultReportFragment : BaseFragment<FragQueryResultReportBinding>() {

    private var list: MutableList<AnalysisListModel>? = null
    private var currentInfo: AnalysisListModel? = null
    var unit = "" //单位
    private var dataList: List<SaType>? = null
    private var adapter: QueryResultReportAdapter? = null
    var layerTypeListLV1: MutableList<LayerType>? = null  //第二级 大类
    var layerTypeListLV2: MutableList<LayerType>? = null  //第三级 小类

    var typeListV1: MutableList<SaType> = ArrayList<SaType>()  //大类统计列表
    var typeListV2: MutableList<SaType> = ArrayList<SaType>()  //小类统计列表

    val caculationUtil = CaculationUtil()

    var reportList: MutableList<KVData>? = null

    var countyName: String = ""

    override fun initView() {
        EventBus.getDefault().register(this)

        initList()

        layerTypeListLV1 = db.queryLayerTypeByTypeType("1")
        layerTypeListLV2 = db.queryLayerTypeByTypeType("2")
    }

    private fun initList() {
        dataList = mutableListOf()
        reportList = mutableListOf()
        vb.lvTable.setLinearManager()
        adapter = QueryResultReportAdapter()
        vb.lvTable.adapter = adapter
        vb.lvTable.disableLoadMoreIfNotFullPage()
    }

    override fun initViewListener() {
        vb.ivQueryDetailClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_RESULT))
        }
        vb.ivQueryDetailBack.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_RESULT_REPORT))
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleData(event: DataEvent) {
        when (event.actionType) {
            DataEvent.SHOW_QUERY_RESULT_REPORT -> if (CommonUtil.isNotNull(event.data)) {
                list = event.data as MutableList<AnalysisListModel>
                reportList?.clear()
                list!!.forEachIndexed { index, it ->
                    currentInfo = it
                    statisticalAnalysis(index, currentInfo)
                }

                vb.lvTable.update(reportList)

                countyName.isNullOrEmpty().yes {
                    vb.tvBriefInfo.text = "该地块总面积${currentInfo?.geomterySize}亩。"
                }.no {
                    vb.tvBriefInfo.text = "该地块位于${countyName}，总面积${currentInfo?.geomterySize}亩。"
                }


            }
            DataEvent.COUNTY_NAME_LIST -> if (CommonUtil.isNotNull(event.data)) {
                val countyNameList = event.data as MutableList<String>
                countyName = ""
                countyNameList.forEachIndexed { index, s ->
                    if (index != countyNameList.size - 1) {
                        countyName += "${s}、"
                    } else {
                        countyName += s
                    }
                }

            }

        }
    }

    /**
     * 统计分析
     *
     * @param info
     */
    private fun statisticalAnalysis(index: Int, info: AnalysisListModel?) {
        if (info == null) return

        val list = info.interSectionModel
        var typeList: MutableList<SaType> = ArrayList<SaType>()

        val totalSaType = SaType("总计", list.size, SaType.Mode.TOTAL)
        val elseType = SaType("其他", SaType.Mode.OTHER)
        val uncoverType = SaType("未覆盖区域", SaType.Mode.UNCOVER)

        for (item in list) {

            val featureType = item.geometryName//获取到属性值
//            "属性类型：${featureType}".printMsg()
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

        dataList = chooseV1orV2()


        initTable(index, dataList!!)

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


    private fun chooseV1orV2(): List<SaType> {
        val name = currentInfo?.layerName
        if(name=="建设用地管制区" || name=="土地利用总体规划管制区" || name=="城市总体规划" || name=="控制性详细规划" ||
            name!!.contains("土地利用现状") || name.contains("更新数据地类图斑")) {

                return typeListV2
            }
            else  {
                return typeListV1
            }

    }

    private fun getBigTypeList(list: List<SaType>): List<SaType> {
        //大类的List
        val typeBigList: MutableList<SaType> = ArrayList<SaType>()

        val df = DecimalFormat("#.##")

        //先筛选出各小分类的list  即不包括TOTAL 和UNCOVER
        val listWithoutTotal =
            list.filter { it.dataType != SaType.Mode.TOTAL && it.dataType != SaType.Mode.UNCOVER }
        listWithoutTotal.forEach { saType ->
            saType.totalAreaPercentage =
                df.format(saType.totalArea / currentInfo!!.geomterySize * 100).toFloat()

            //给小类设置对应的大类
            val item = layerTypeListLV2?.firstOrNull { it.typeName == saType.name }
            (item != null).yes {
                //有对应的大类 给parentName赋值
                saType.parentName = item?.parentId1Name
                saType.parentParentName = item?.parentId0Name
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

//                    if (type.typeName == "耕地" || type.typeName == "林地") {
//                        typeBigList.add(SaType(item.name, item.totalArea, item.totalAreaPercentage, "1"))
//                    }
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
            uncoverPart!!.totalAreaPercentage =
                df.format(uncoverPart.totalArea / currentInfo!!.geomterySize * 100).toFloat()
            typeBigList.add(list.first { it.dataType == SaType.Mode.UNCOVER })
        }


//        typeBigList.add( list.first { it.dataType == SaType.Mode.TOTAL  })


        typeBigList.forEach {
            it.totalAreaPercentage = df.format(it.totalAreaPercentage).toFloat()
        }

        return typeBigList

    }

    private fun initTable(index: Int, list: List<SaType>) {

        val layerName = currentInfo?.layerName
        val name = "${index + 1}、${layerName}"
        var value = "未重叠。"

        if (layerName!!.contains("土地利用现状")|| layerName.contains("地类图斑")) {


            var bigBigList: MutableList<SaType> = ArrayList<SaType>()

            var nongyongdiSize = 0F
            var gengdiSize = 0F
            var shuitianSize = 0F
            var handiSize = 0F
            var shuijiaodiSize = 0F
            var lindiSize = 0F
            var jiansheyongdiSize = 0F
            var weiliyongdiSize = 0F

            //水田面积
            list.firstOrNull { it.name == "水田" }?.let {
                shuitianSize = it.totalArea
            }
            //旱地面积
            list.firstOrNull { it.name == "旱地" }?.let {
                handiSize = it.totalArea
            }
            //水浇地面积
            list.firstOrNull { it.name == "水浇地" }?.let {
                shuijiaodiSize = it.totalArea
            }
            //耕地面积 = 水田+旱地+水浇地
            gengdiSize = shuitianSize + handiSize + shuijiaodiSize
            //林地面积
            var ld = list.filter { it.parentName == "林地" }
            ld.isNullOrEmpty().no {
                ld.forEach {
                    lindiSize += it.totalArea
                }
            }

            //农用地面积
            var nyd = list.filter { it.parentParentName == "农用地" }
            nyd.isNullOrEmpty().no {
                nyd.forEach {
                    nongyongdiSize += it.totalArea
                }
            }
            //建设用地面积
            var jsyd = list.filter { it.parentParentName == "建设用地" }
            jsyd.isNullOrEmpty().no {
                jsyd.forEach {
                    jiansheyongdiSize += it.totalArea
                }
            }
            //未利用地
            var wlyd = list.filter { it.parentParentName == "未利用地" }
            wlyd.isNullOrEmpty().no {
                wlyd.forEach {
                    weiliyongdiSize += it.totalArea
                }
            }

            value = "农用地${nongyongdiSize}亩（其中耕地${gengdiSize}：水田${shuitianSize}亩、" +
                    "旱地${handiSize}亩、水浇地${shuijiaodiSize}亩；林地${lindiSize}亩）、" +
                    "建设用地${jiansheyongdiSize}亩、未利用地${weiliyongdiSize}亩。"

        } else {
            value = ""
            list.filter { it.dataType != SaType.Mode.TOTAL && it.dataType != SaType.Mode.UNCOVER }
                .forEach {
                    value += "${it.name}：${it.totalArea}亩、"
                }
            value = value.replaceAfterLast("亩", "。")
        }




        reportList?.add(KVData(name, value))

    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}
