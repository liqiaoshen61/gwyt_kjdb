package com.jwch.gwyt_project.fragment

import com.esri.arcgisruntime.geometry.Polygon
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenilistlib.i.ItemChildViewClickListener
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.QueryResultDetailListAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragQueryResultDetailListBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.AnalysisListModel
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.InterSectionModel
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.ExcelOutPutUtil
import com.jwch.gwyt_project.util.JxlExcelUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.ArrayList
import kotlin.collections.isNotEmpty

/**
 * 空间查询--手势查询结果 图斑
 * 第三个页面  -- 图斑列表
 */
class QueryResultDetailListFragment : BaseFragment<FragQueryResultDetailListBinding>(), ItemClickListener,
ItemChildViewClickListener{

    private var dataList: MutableList<InterSectionModel>? = null
    private var adapter: QueryResultDetailListAdapter? = null
    var info: AnalysisListModel? = null

    val titleList = mutableListOf<String>() //导出excel的标题栏
    val valueListList = mutableListOf<MutableList<String>>()  //导出excel的多行数据

    override fun initView() {
        EventBus.getDefault().register(this)
        initList()
    }

    private fun initList() {
        dataList = mutableListOf()
        vb.lvDetailList.setLinearManager()
        adapter = QueryResultDetailListAdapter()
        vb.lvDetailList.setAdapter(adapter)
        vb.lvDetailList.disableLoadMoreIfNotFullPage()
        vb.lvDetailList.itemClickListener = this
        vb.lvDetailList.itemChildViewClickListener = this
        vb.lvDetailList.update(dataList)
    }

    override fun initViewListener() {

        vb.ivDetailListBack.onClick {
            when (getKV("queryStates", "1-2")) {
                "1-2" -> {
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_RESULT_DETAIL_LIST))
                }
                "1-3" -> {
                    EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_LIST))
                }
            }
        }

        vb.ivDetailListClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_RESULT))
        }

        vb.rlOutputAll.onClick {
            val fileName = "${dataList?.first()?.layerName}"

            dataList?.forEachIndexed { index, item ->
                val valueList = mutableListOf<String>()  //导出excel的一行数据
                item.attr?.forEach {
                    //标题只需要添加一次
                    (index == 0).yes {
                        titleList.add(it.key)
                    }
                    if (it.value is String || it.value is Double || it.value is Float || it.value is Int || it.value is Long) {
                        valueList.add("${it.value}")
                    } else {
                        valueList.add(it.value.toJson()) //时间参数用的类是java.util.GregorianCalendar 转成json
                    }
                }

                //图斑与红线相交的图形坐标
                val polygon = item.geometry as Polygon
                (index == 0).yes {
                    titleList.add("叠加图形坐标")
                    titleList.add("叠加图形面积")
                }
                valueList.add(polygon.toJson())
                valueList.add(item.areaSize)

                valueListList.add(valueList)
            }
            ExcelOutPutUtil.export(Config.OUTPUT_OVERLAY_PATH, fileName, titleList, valueListList)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleData(event: DataEvent) {
        when (event.actionType) {
            DataEvent.SHOW_QUERY_RESULT_DETAIL_LIST -> {
                dataList?.clear()
                vb.lvDetailList.show()
                info = event.data as AnalysisListModel
                vb.tvDetailListTitle.text = info?.layerName.self()

                dataList!!.addAll(info!!.interSectionModel)
                dataList!!.forEachIndexed { index, it ->
                    it.index = index
                }

                vb.lvDetailList.update(dataList)
            }

            //叠加分析交集所在的图斑的属性 用于定位到列表
            DataEvent.SEND_ATTR -> {

                var idFromEventBus = 1
                var idFromList = -1

                event.data?.let {
                    val attr = it as MutableMap<*, *>
                    if(attr.containsKey(Config.primaryKey)){
                        idFromEventBus = attr.get(Config.primaryKey) as Int
                    }else if (attr.containsKey("FID")){
                        idFromEventBus = attr.get("FID") as Int
                    }

                    dataList!!.forEachIndexed { index, data ->

                        data.isSecletd = false
                        if(data.attr?.contains(Config.primaryKey) == true){
                            idFromList = data.attr!!.get(Config.primaryKey) as Int
                        }else if(data.attr?.contains("FID") == true){
                            idFromList = data.attr!!.get("FID") as Int
                        }

                        if(idFromEventBus == idFromList){
                            data.isSecletd = true

                            vb.lvDetailList.recycleview.scrollToPosition(index)
                            vb.lvDetailList.update(dataList)
                            EventBus.getDefault().post(MapEvent(MapEvent.DRAW_ANALYSIS_RESULT, data))
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(itemData: Any?, position: Int) {

        val item = itemData as InterSectionModel

        dataList!!.forEach {
            it.isSecletd = false
        }
        item.isSecletd = true
        vb.lvDetailList.update(dataList)

        EventBus.getDefault().post(MapEvent(MapEvent.DRAW_ANALYSIS_RESULT, item))
        EventBus.getDefault().post(DataEvent(DataEvent.SELECT_ITEM, item))
    }


    override fun onItemChildViewClick(viewId: Int, position: Int) {
        val item: InterSectionModel = dataList?.get(position) ?: return
        when (viewId) {
            R.id.tvExpertExcel -> {
                item.attr?.isNotEmpty()?.yes {

                    val fileName = "${item.layerName}_${item.geometryName}"
                    val valueList = mutableListOf<String>()  //导出excel的一行数据
                    item.attr?.forEach {
                        titleList.add(it.key)

                        if(it.value is String || it.value is Double || it.value is Float || it.value is Int || it.value is Long){
                            valueList.add("${it.value}")
                        }else{
                            valueList.add(it.value.toJson()) //时间参数用的类是java.util.GregorianCalendar 转成json
                        }
                    }
                    //图斑与红线相交的图形坐标
                    val polygon = item.geometry as Polygon
                    titleList.add("叠加图形坐标")
                    valueList.add(polygon.toJson())
                    //图斑与红线相交的图形面积
                    titleList.add("叠加图形面积")
                    valueList.add(item.areaSize)

                    valueListList.add(valueList)
                    ExcelOutPutUtil.export(Config.OUTPUT_OVERLAY_PATH, fileName, titleList, valueListList)
                }
            }
        }
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}
