package com.jwch.gwyt_project.fragment


import android.os.Handler
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.Info.PoiTypesInfo
import com.jwch.gwyt_project.Info.PoisInfo
import com.jwch.gwyt_project.adapter.PlaceQueryAdapter
import com.jwch.gwyt_project.adapter.PlaceTypeAdapter
import com.jwch.gwyt_project.common.Tools
import com.jwch.gwyt_project.databinding.FragSearchBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.SoftUtil
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.collections.forEachWithIndex
import org.xutils.ex.DbException

/**
 * 兴趣点搜索
 */
class SearchFragment : BaseFragment<FragSearchBinding>(), ItemClickListener {

    var dataList: MutableList<PoisInfo>? = null  //兴趣点
    var typeList: MutableList<PoiTypesInfo>? = null  //兴趣点类型
    lateinit var queryList: MutableList<String>  //兴趣点类型
    var queryAdapter: PlaceQueryAdapter? = null //搜索结果Adapter
    var typeAdapter: PlaceTypeAdapter? = null  //类型Adapter
    val keyboard = SoftUtil()
    var pageIndex = 0  //索引
    var currPage = 1   //当前页
    var totalCount = 0  //总条数
    var totalPage = 0   //总页数

    override fun initView() {

        initPoisList()
        initPoisTypeList()
        vb.includeViewPageController.llPageController.hide()
    }

    //初始化兴趣点搜索结果列表
    fun initPoisList() {
        dataList = mutableListOf()
        vb.lvSeaechResult.setLinearManager()
        queryAdapter = PlaceQueryAdapter()
        vb.lvSeaechResult.adapter = queryAdapter
        vb.lvSeaechResult.itemClickListener = this
        vb.lvSeaechResult.disableLoadMoreIfNotFullPage()
        vb.lvSeaechResult?.update(dataList)
    }

    //初始化兴趣点类型列表
    fun initPoisTypeList() {
        typeList = mutableListOf()
        queryList = mutableListOf()
        typeList = db.queryPoiTypesInfo()
        vb.lvPlaceType.layoutManager = GridLayoutManager(context, 3)
        typeAdapter = PlaceTypeAdapter()
        vb.lvPlaceType.adapter = typeAdapter
        typeAdapter?.update(typeList)
        typeAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val item = typeList?.get(position)
                item?.isSelect = !item?.isSelect!!
                typeAdapter?.update(typeList)
            }
        })
    }

    override fun initViewListener() {
//        llSearchContent.onClick {  }

        //搜索
        vb.includeViewSearchBar.tvSearch.onClick {
            if (vb.includeViewSearchBar.etSearch.isEmpty()) {
                tip(vb.includeViewSearchBar.etSearch.hint.toString())
                return@onClick
            }
            pageIndex = 0
            currPage = pageIndex + 1
            queryData()
        }
        //上一页
        vb.includeViewPageController.tvPrePage.onClick(2000) {
            pageIndex--
            currPage = pageIndex + 1
            //如果当前页<1 则是首页 无需执行查询
            (currPage < 1).yes {
                pageIndex = 0
                currPage = 1
                tip("当前已是首页")
                return@onClick
            }
            queryData()
        }
        //下一页
        vb.includeViewPageController.tvNextPage.onClick(2000)  {
            pageIndex++
            currPage = pageIndex + 1
            //如果当前页>总页数 则是末页 无需执行查询
            (currPage > totalPage).yes {
                pageIndex = totalPage - 1
                currPage = totalPage
                tip("当前已是末页")
                return@onClick
            }
            queryData()
        }
        //清除
        vb.includeViewSearchBar.tvClear.onClick {
            vb.includeViewSearchBar.etSearch.setText("")
            vb.includeViewPageController.tvResultPage.text = ""
            dataList?.clear()
            vb.lvSeaechResult.update(dataList)
            vb.includeViewPageController.llPageController.hide()
            vb.tvTotalCount.gone()
            clearLayer()
        }

        vb.ivSearchClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_SEARCH_FRAG))
        }

        //软键盘回车
        vb.includeViewSearchBar.etSearch.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                keyboard.hideKeyboard(activity)
                vb.includeViewSearchBar.tvSearch.callOnClick()
                return@OnKeyListener true
            }
            false
        })

        if (isNotNull(vb.llSearchContent)){
            "llSearchContent1111111111".printMsg()
        }
    }

    //查询
    fun queryData() {
        //隐藏键盘
        keyboard.hideKeyboard(activity)
        dataList?.clear()
        queryList.clear()
        vb.lvSeaechResult?.update(dataList)
        queryAdapter!!.notifyDataSetChanged()
        setLoadingVisiable(true)
        vb.tvNodata.gone()

        typeList?.filter { it.isSelect }?.forEach {
            queryList.add(it.typeName)
        }

        if (queryList.size == 0 && vb.includeViewSearchBar.etSearch.text.toString() == "") {
            vb.includeViewPageController.tvResultPage.text = ""
            return
        }
        val keyWord = vb.includeViewSearchBar.etSearch.text.toString().toCharArray()
        var str = ""
        for (i in keyWord.indices) {
            str += "%" + keyWord[i]
        }
        str += "%"
        val finalStr = str

        try {
            dataList?.clear()
            //协程中 进行数据库查询操作
            GlobalScope.launch(Dispatchers.IO) {
                totalCount = if (queryList.size == 0) {
                    dataList?.addAll(Tools.changData(db.queryPoisByName(finalStr, pageIndex)))
                    db.queryPoisCountByName(finalStr)
                } else {
                    dataList?.addAll(Tools.changData(db.queryPoisByNameAndType(finalStr, queryList, pageIndex)))
                    db.queryPoisCountByNameAndType(finalStr, queryList)
                }
                //利用handler 展示列表数据
                handler.sendEmptyMessage(1)
            }

        } catch (e: DbException) {
            e.printStackTrace()
            print("搜索 data errror:" + e.message)
        }
    }

    val handler: Handler = Handler {
        when (it.what) {
            1 -> {
                displayData()
            }

        }
        false
    }

    fun displayData() {
        setLoadingVisiable(false)
        totalPage = if (totalCount % 10 == 0) {
            totalCount / 10
        } else {
            totalCount / 10 + 1
        }

        vb.includeViewPageController.llPageController.show()
        //给数据赋予编号 Adapter展示带编号的地标图片
        dataList?.forEachWithIndex { i, poisInfo ->
            poisInfo.index = i
        }

        //更新展示数据
        vb.lvSeaechResult?.update(dataList)
        vb.includeViewPageController.tvResultPage.text = "第${currPage}页/共${totalPage}页"
        vb.tvTotalCount.text = "共${totalCount}条"

        (dataList?.size == 0).yes {
            vb.tvNodata.show()
            vb.includeViewPageController.llPageController.hide()
            vb.tvTotalCount.gone()
        }.no {
            vb.tvNodata.gone()
            vb.includeViewPageController.llPageController.show()
            vb.tvTotalCount.show()
        }

        EventBus.getDefault().post(MapEvent(MapEvent.DRAW_SEARCH_RESULT, dataList))
    }

    private fun setLoadingVisiable(visiable: Boolean) {
        if (isNotNull(vb.llLoading)) {
            vb.llLoading.visibility = if (visiable) View.VISIBLE else View.GONE
        }
    }

    override fun onItemClick(itemData: Any?, position: Int) {
        val item = itemData as PoisInfo
//        tip(item.poiName)
        EventBus.getDefault().post(MapEvent(MapEvent.CLICK_SEARCH_RESULT_ITEM, item))
    }

    private fun clearLayer() {
        EventBus.getDefault().post(MapEvent(MapEvent.HIDE_CALLOUT))
        EventBus.getDefault().post(MapEvent(MapEvent.CLEAR_GRAPHIC_LAYER))
    }
}
