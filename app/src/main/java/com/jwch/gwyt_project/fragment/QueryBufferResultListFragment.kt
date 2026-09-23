package com.jwch.gwyt_project.fragment

import android.view.View
import com.esri.arcgisruntime.data.ArcGISFeature
import com.jameni.allutillib.common.CommonUtil
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.adapter.QueryBufferResultlListAdapter
import com.jwch.gwyt_project.databinding.FragQueryBufferListBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.showSelectionDialog
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.BufferAnalysisModel
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.model.SelectionListModel
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 缓冲区查询结果
 *
 */
class QueryBufferResultListFragment : BaseFragment<FragQueryBufferListBinding>(), ItemClickListener {


    private var allData: MutableList<BufferAnalysisModel> = mutableListOf()
    private var dataList: MutableList<BufferAnalysisModel>? = null
    private var filterList: MutableList<BufferAnalysisModel>? = null //筛选过的列表
    private var adapter: QueryBufferResultlListAdapter? = null
    var selectItem : BufferAnalysisModel? =null
    var radiusList = mutableListOf<SelectionListModel>()

    var queryType = 0 //0 缓冲区点位查询，关键词搜索查询

    var eventStatusSelectionList = mutableListOf<SelectionListModel>()
    var eventStatusFilterParams = ""
    var isQuestionFilterSelectionList = mutableListOf<SelectionListModel>()
    var isQuestionFilterParams = ""

    companion object {
        const val PAGE_SIZE = 500
    }

    private var currentPage = 1
    private var totalPages = 1

    override fun initView() {
        EventBus.getDefault().register(this)
        initList()
        initRadiusSelectData()
        initSelectionList()
    }

    private fun initList() {
        dataList = mutableListOf()
        vb.lvQueryList.setLinearManager()
        adapter = QueryBufferResultlListAdapter()
        vb.lvQueryList.adapter = adapter
        vb.lvQueryList.disableLoadMoreIfNotFullPage()
        vb.lvQueryList.itemClickListener = this
        vb.lvQueryList.update(dataList)

        vb.tvPrevPage.onClick {
            if (currentPage > 1) {
                currentPage--
                showCurrentPage()
            }
        }

        vb.tvNextPage.onClick {
            if (currentPage < totalPages) {
                currentPage++
                showCurrentPage()
            }
        }
    }

    fun initRadiusSelectData(){
        radiusList.add(SelectionListModel("100","100米"))
        radiusList.add(SelectionListModel("300","300米"))
        radiusList.add(SelectionListModel("500","500米"))
        radiusList.add(SelectionListModel("1000","1公里"))
        radiusList.add(SelectionListModel("3000","3公里"))
        radiusList.add(SelectionListModel("5000","5公里"))
        radiusList.add(SelectionListModel("10000","10公里"))
//        radiusList.add(SelectionListModel("100000","100公里"))
    }

    fun initSelectionList(){
        eventStatusSelectionList.add(SelectionListModel("","全部"))
        eventStatusSelectionList.add(SelectionListModel("未办结","未办结"))
        eventStatusSelectionList.add(SelectionListModel("已办结","已办结"))


        isQuestionFilterSelectionList.add(SelectionListModel("","全部"))
        isQuestionFilterSelectionList.add(SelectionListModel("是问题","是问题"))
        isQuestionFilterSelectionList.add(SelectionListModel("不是问题","不是问题"))
        isQuestionFilterSelectionList.add(SelectionListModel("待复核","待复核"))
    }


    override fun initViewListener() {
        vb.ivHandSearchClose.onClick {

            when(queryType){
                0 ->{
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_BUFFER_RESULT_LIST))
                }
                1 ->{
                    EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_SWITCH_MAPS_2))
                }
            }

        }


        //办结情况筛选
        vb.tvEventStatusFilter.onClick {
            showSelectionDialog(requireContext(), eventStatusSelectionList) { Any, i ->
                val item = Any as SelectionListModel
                eventStatusFilterParams = item.key.self()
                if(eventStatusFilterParams.isBlank()){
                    vb.tvEventStatusFilter.text = "事件状态"
                }else{
                    vb.tvEventStatusFilter.text = item.value
                }
                doFilterData()
            }
        }

        //是否问题筛选
        vb.tvIsQuestionFilter.onClick {
            showSelectionDialog(requireContext(), isQuestionFilterSelectionList) { Any, i ->
                val item = Any as SelectionListModel
                isQuestionFilterParams = item.key.self()
                if(isQuestionFilterParams.isBlank()){
                    vb.tvIsQuestionFilter.text = "是否问题"
                }else{
                    vb.tvIsQuestionFilter.text = item.value
                }
                doFilterData()
            }
        }
    }


    fun doFilterData(){

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleData(event: DataEvent) {
        when (event.actionType) {
            DataEvent.SHOW_QUERY_BUFFER_RESULT_LIST -> {

                vb.llQueryList.show()
                vb.tvNoData.gone()

                if (CommonUtil.isNotNull(event.data)) {
                    allData = event.data as MutableList<BufferAnalysisModel>
                    if (!CommonUtil.isNotNull(allData)) {
                        allData = mutableListOf()
                    }else{
                        allData.sortWith(compareByDescending<BufferAnalysisModel> { it.extractYear() })
                    }

                    totalPages = if (allData.isEmpty()) 1 else (allData.size + PAGE_SIZE - 1) / PAGE_SIZE
                    currentPage = 1
                    showCurrentPage()

                    vb.tvTotoalCount.text = "- 共${allData.size}条 -"
                    if (allData.size == 0) {
                        vb.tvNoData.show()
                        vb.llQueryList.gone()
                    }
                }
                if (CommonUtil.isNotNull(event.data2)) {
                    queryType = event.data2 as Int

                }

            }
            DataEvent.INIT_BUFFER_ANALYSIS -> {
            }
            DataEvent.INIT_BUFFER_ANALYSIS_BY_KEYWORDS -> {
            }

            DataEvent.RESERT_BUFFER_ANALYSIS -> {
                allData.clear()
                dataList?.clear()
                currentPage = 1
                totalPages = 1
                vb.lvQueryList.update(dataList)
                updatePaginationUI()
                if (dataList?.size == 0) {
                    vb.tvNoData.show()
                    vb.llQueryList.gone()
                }

            }
        }
    }

    private fun showCurrentPage() {
        val fromIndex = (currentPage - 1) * PAGE_SIZE
        val toIndex = Math.min(fromIndex + PAGE_SIZE, allData.size)
        val pageData = if (fromIndex < allData.size) allData.subList(fromIndex, toIndex) else mutableListOf()
        dataList = pageData.toMutableList()
        adapter?.indexOffset = fromIndex
        vb.lvQueryList.update(dataList)
        updatePaginationUI()

        // 显示loading遮罩
        EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING))
        // 发送事件通知地图绘制当前页数据
        EventBus.getDefault().post(MapEvent(MapEvent.DRAW_BUFFER_PAGE_RESULT, dataList))
    }

    private fun updatePaginationUI() {
        if (totalPages > 1) {
            vb.llPagination.visibility = View.VISIBLE
            vb.tvPageInfo.text = "${currentPage}/${totalPages}"
            vb.tvPrevPage.isEnabled = currentPage > 1
            vb.tvPrevPage.alpha = if (currentPage > 1) 1f else 0.4f
            vb.tvNextPage.isEnabled = currentPage < totalPages
            vb.tvNextPage.alpha = if (currentPage < totalPages) 1f else 0.4f
        } else {
            vb.llPagination.visibility = View.GONE
        }
    }


    override fun onItemClick(itemData: Any?, position: Int) {

        val item = itemData as BufferAnalysisModel

        // 简化功能 不用选中了 这样不需要在点图斑的时候 还匹配列表项
//        if(selectItem != null){
//            selectItem!!.isSecletd = false
//            selectItem = null
//        }
//        selectItem = item
//        item.isSecletd = true
//        vb.lvQueryList.update(dataList)

        EventBus.getDefault().post(MapEvent(MapEvent.DRAW_BUFFER_ANALYSIS_RESULT, item))

    }

    fun BufferAnalysisModel.extractYear(): Int? {

        if (this.map.isEmpty()) return null  // map为空

        val maps = this.map["feature"] as ArcGISFeature ?: return null  // 没有年份字段或值为null

        val yearValue = maps.attributes["年份"]  ?:  return null

        return when (yearValue) {
            is Int -> if (yearValue > 0) yearValue else null
            is Long -> if (yearValue > 0) yearValue.toInt() else null
            is Double -> {
                val doubleValue = yearValue
                // 处理 2026.0 这样的浮点数
                if (doubleValue > 0 && doubleValue == doubleValue.toInt().toDouble()) {
                    doubleValue.toInt()
                } else {
                    null  // 如果不是整数形式的浮点数，视为无效
                }
            }
            is Float -> {
                val floatValue = yearValue
                // 处理 2026.0f 这样的浮点数
                if (floatValue > 0 && floatValue == floatValue.toInt().toFloat()) {
                    floatValue.toInt()
                } else {
                    null
                }
            }
            is String -> {
                val str = yearValue.trim()
                if (str.isEmpty()) return null
                // 先尝试直接解析为整数
                str.toIntOrNull()?.let { return it }

                // 如果不行，尝试解析为浮点数，检查是否是整数形式
                str.toDoubleOrNull()?.let { doubleValue ->
                    if (doubleValue > 0 && doubleValue == doubleValue.toInt().toDouble()) {
                        return doubleValue.toInt()
                    }
                }

                // 都不是有效格式
                null
            }
            else -> null

        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}
