package com.jwch.gwyt_project.fragment

import com.jameni.allutillib.common.CommonUtil
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.adapter.QueryResultListAdapter
import com.jwch.gwyt_project.databinding.FragQueryListBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.AnalysisListModel
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.util.CaculationUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat

/**
 * 空间查询--手势查询结果列表
 * 第一个页面 -- 每个专题图查询结果
 */
class QueryResultListFragment : BaseFragment<FragQueryListBinding>(), ItemClickListener {

    private var dataList: MutableList<AnalysisListModel>? = null
    private var adapter: QueryResultListAdapter? = null
    var caculationUtil = CaculationUtil()

    override fun initView() {
        EventBus.getDefault().register(this)
        initList()
    }

    private fun initList() {
        dataList = mutableListOf()
        vb.lvQueryList.setLinearManager()
        adapter = QueryResultListAdapter()
        vb.lvQueryList.adapter = adapter
        vb.lvQueryList.disableLoadMoreIfNotFullPage()
        vb.lvQueryList.itemClickListener = this
        vb.lvQueryList.update(dataList)
    }

    override fun initViewListener() {
        vb.ivHandSearchClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_QUERY_RESULT))
        }

        //一键分析报告
        vb.rlReport.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_REPORT, dataList))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleData(event: DataEvent) {
        when (event.actionType) {
            DataEvent.SHOW_QUERY_RESULT_LIST -> {

                vb.llQueryList.show()
                vb.tvNoData.gone()
                if (CommonUtil.isNotNull(event.data)) {
                    dataList = event.data as MutableList<AnalysisListModel>
                    if (!CommonUtil.isNotNull(dataList)) {
                        dataList = mutableListOf()
                    }
                    dataList!!.forEach {
                        var size = 0.0
                        it.interSectionModel.forEach {
                            size += caculationUtil.getAreaStringMu(it.areaSizeValue,true)
                        }
                        it.size = size
                        val df = DecimalFormat("#.##")
                        it.percentage =  df.format(size / it.geomterySize *100)

                    }

                    vb.lvQueryList.update(dataList)
                    if (dataList?.size == 0) {
                        vb.tvNoData.show()
                        vb.llQueryList.gone()
                    }
                }
            }
        }
    }

    override fun onItemClick(itemData: Any?, position: Int) {
        val info = itemData as AnalysisListModel

        //如果没有groupField 那就把图层名当作类型
        if(info.groupField.isNullOrBlank()){
            info.groupField = ""
            info.interSectionModel.forEach {
                it.geometryName = info.layerName
            }
        }

        EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_DETAIL, info))
            saveKV("queryStates","1-2")

//        if (!info.groupField.isNullOrBlank()) {
//            //显示--图表详情
//            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_DETAIL, info))
//            saveKV("queryStates","1-2")
//            //queryStates 1 QueryResultListFragment 2 QueryResultDetailFragment 3 QueryResultDetailListFragment
//        } else {
//            //显示列表详情
//            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_DETAIL_LIST, info))
//            saveKV("queryStates","1-3")
//        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}