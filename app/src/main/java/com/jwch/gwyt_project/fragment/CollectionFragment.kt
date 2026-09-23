package com.jwch.gwyt_project.fragment

import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil
import com.jameni.basepage_lib.util.PageManager
import com.jameni.jamenilistlib.i.ItemChildViewClickListener
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.Info.*
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.PatchCollectionAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragCollectionBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.i.DoneAction
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.SoftUtil
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.model.NavigationPointModel
import com.jwch.gwyt_project.util.NavigationUtil
import com.loper7.date_time_picker.dialog.CardDatePickerDialog
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xutils.ex.DbException

/**
 * 图斑收藏夹（收藏夹功能已分离）
 */

class CollectionFragment : BaseFragment<FragCollectionBinding>(), ItemClickListener, ItemChildViewClickListener{

    var patchDataList: MutableList<CollecPatchInfo>? = null//图斑数据
    var patchAdapter: PatchCollectionAdapter? = null //图斑adapter

    val keyboard = SoftUtil()

    var startTime = 0L //开始时间戳
    var endTime = 0L  //结束时间戳
    var timeBuild: CardDatePickerDialog.Builder? = null
    lateinit var timeformatList: MutableList<Int>
    var timeStamp: Long? = null //临时保存时选择的间戳


    var selectDataList: MutableList<CollecPatchInfo>? = null//选中的图斑数据（导航用）
    lateinit var navigationUtil: NavigationUtil

    companion object {
        const val ACTION_DRAW = 0 //绘制标绘
        const val ACTION_DELETE = 1 //删除标绘
        const val ACTION_DELETE_FOLDER = 2 //删除标绘文件夹
        const val ACTION_MOVE = 3 //移动标绘
        const val ACTION_SELECT_FOLDER = 4 //选择移动的文件夹
        const val ACTION_EDIT = 5 //编辑
    }

    override fun initView() {

        //初始化图斑信息
        initPatchList()
        reloadPatchCollectData()
        EventBus.getDefault().register(this)

        navigationUtil = NavigationUtil(requireContext())
    }


    //初始化兴趣点搜索结果列表
    private fun initPatchList() {
        patchDataList = mutableListOf()
        vb.lvPatch.setLinearManager()
        patchAdapter = PatchCollectionAdapter()
        vb.lvPatch.adapter = patchAdapter
        vb.lvPatch.itemClickListener = this
        vb.lvPatch.itemChildViewClickListener = this
        vb.lvPatch.disableLoadMoreIfNotFullPage()
        vb.lvPatch?.update(patchDataList)
    }

    //加载图斑数据
    private fun reloadPatchCollectData() {
        try {
            patchDataList = db.queryAllCollectPatchList()
            patchAdapter?.setNewData(patchDataList)

            vb.tvNodata.visiable(!matchList(patchDataList))

        } catch (e: DbException) {
            e.printStackTrace()
        }
    }


    override fun initViewListener() {

        //关闭我的收藏
        vb.ivClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_COLLECTION_FRAG))
            changeNavigationMode(false)
        }
        //搜索
        vb.tvSearch.onClick {
//            if (vb.includeViewSearchBar.etSearch.isEmpty()) {
//                tip(vb.includeViewSearchBar.etSearch.hint.toString())
//                return@onClick
//            }
            queryData()
        }

        //  清空搜索内容
        vb.tvClear.onClick {
            vb.etSearch.setText("")
            clearLayer()
        }

//        //软键盘回车
        vb.etSearch.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                keyboard.hideKeyboard(activity)
                vb.tvSearch.callOnClick()
                return@OnKeyListener true
            }
            false
        })


        vb.ivNavigation.onClick {
            if(!CommonUtil.matchList(patchDataList)){
                ToastUtils.show("暂无收藏数据")
                return@onClick
            }


            if (vb.llNavigationTool.isShow()){
                showNormalDialog(requireContext(), "是否退出多点位导航模式？"){
                    it.yes {
                        changeNavigationMode(false)
                    }
                }
            }else{
                changeNavigationMode(true)
            }
        }

        vb.tvCancelNavigation.onClick {
            showNormalDialog(requireContext(), "是否退出多点位导航模式？"){
                it.yes {
                    changeNavigationMode(false)
                }
            }
        }

        vb.tvStartNavigation.onClick {
            showNormalDialog(requireContext(), "确定跳转去导航吗？") {
                it.yes {
                    checkNavigationData()
                }
            }
        }
    }

    fun changeNavigationMode(open : Boolean){

        open.yes {
            vb.llNavigationTool.show()
            vb.ivNavigation.setImageResource(R.mipmap.icon_navigation_select)

            patchAdapter?.selectMode = true
            vb.lvPatch.update(patchDataList)
        }.no {
            vb.llNavigationTool.gone()
            vb.ivNavigation.setImageResource(R.mipmap.icon_navigation_unselect)


            vb.tvSelectNavigationCount.text = "请选择需要导航的点位（最多支持5个）"

            patchAdapter?.selectMode = false
            patchDataList?.forEach { it.isSelect = false }
            vb.lvPatch.update(patchDataList)
        }

    }

    private fun checkNavigationData() {

        val selectList: MutableList<CollecPatchInfo> = mutableListOf()
        selectList.addAll(patchDataList?.filter { it.isSelect } as MutableList)


        if (selectList.size > 5) {
            showSingleDialog(requireContext(), "最多选择5条数据")
            return
        }
        try {
            if (CommonUtil.matchList(selectList)) {

                val pointList = mutableListOf<NavigationPointModel>()
                selectList.forEachIndexed { index, item ->
                    val geometry = Geometry.fromJson(item.centerPointJson)
                    val cp = GeometryEngine.project(geometry.extent.center, Config.sp4490) as Point
                    pointList.add(NavigationPointModel("位置${index + 1}", cp.y, cp.x))
                }
                navigationUtil.startNavigation2(pointList, false)


            } else {
                Toast.makeText(context, "操作失败，未查找到空间数据", Toast.LENGTH_LONG).show()
            }
        } catch (e: DbException) {
            "异常：${e.message}".printMsg()
            e.printStackTrace()
        }
    }

    //查询
    fun queryData() {
        //隐藏键盘
        keyboard.hideKeyboard(activity)
        patchDataList?.clear()
        vb.lvPatch?.update(patchDataList)
//        setLoadingVisiable(true)
        vb.tvNodata.gone()

        val finalStr =  vb.etSearch.text.toString()

        try {
            patchDataList = db.queryCollectPatchByName(finalStr)
            patchAdapter?.setNewData(patchDataList)
            vb.tvNodata.visiable(!matchList(patchDataList))

        } catch (e: DbException) {
            e.printStackTrace()
            print("搜索 data errror:" + e.message)
        }
    }


    fun getKeyWord(): String {
        val keyWord = vb.etSearch.text.toString().toCharArray()
        var str = ""
        for (i in keyWord.indices) {
            str += "%" + keyWord[i]
        }
        str += "%"

        return str
    }

    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlePageView(event: DataEvent?) {
        if (event == null) return
        when (event.actionType) {
            //更新收藏信息
            DataEvent.UPDATE_COLLECTION_DATA -> {

                val flag = event.data as Int
                if (flag == 0) {

                } else {
                    //重新加载图斑信息收藏数据
                    reloadPatchCollectData()
                }

            }
            DataEvent.SELECT_NAVIGATION_LIST_CHANGE -> {

                val count = patchDataList?.filter { it.isSelect }?.size
                vb.tvSelectNavigationCount.text = "当前已选择${count}条数据"

            }
            DataEvent.CLOSE_COLLECTION_FRAG_NAVIGATION -> {
                changeNavigationMode(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClick(itemData: Any?, position: Int) {

        val item = itemData as CollecPatchInfo

        var themesInfo = db.queryThemesById(item.themeId.self())
        if (themesInfo == null) {
            Toast.makeText(context, "该图斑的专题图层不存在!", Toast.LENGTH_LONG).show()
            return
        }

        themesInfo.loadGdbLayer(object : ActionListener {
            override fun onAction(obj: Any?, flag: Int) {

                try {
                    EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING))

                    val table = obj as FeatureTable
                    var keyword = ""
                    //table里数据里id 有的用OBJECTID 有的用OBJECTID_1 要先确定是哪个字段 否则用不存在的字段查询 SQL执行会报错
                    val model = table.fields.find { it.name == Config.primaryKey || it.name == Config.primaryKey2 }
//                    val model = table.fields.find { it.name == Config.primaryKey   }
                    if (model != null) {
                        keyword = model.name
                    }

                    val query = QueryParameters()
                    query.whereClause = "${keyword} = '${item.linkId}'"
                    "=123==  ${keyword} = ${item.linkId}".printMsg()
                    query.spatialRelationship = QueryParameters.SpatialRelationship.WITHIN
                    query.isReturnGeometry = true

                    val feature = table.queryFeaturesAsync(query)
                    feature.addDoneListener(DoneAction({ o, tag ->

                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator = result.iterator()

                        while (iterator.hasNext()) {

                            val f = iterator.next()
                            val attr = f.attributes
                            print(attr)
                            if (attr.get(keyword).toString() == item.linkId) {
                                val map = mutableMapOf<String, Any>()
                                map["feature"] = f
                                map["info"] = themesInfo
                                EventBus.getDefault().post(MapEvent(MapEvent.CLICK_QUERY_RESULT_FEATURE_LIST, map))
                                break
                            }
                        }
                    }, feature, 0))
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
                } catch (e: Exception) {
                    "收藏模块==图层查询异常==${e.message}".printMsg()
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
                }
            }
        })


    }

    fun clearLayer() {
        EventBus.getDefault().post(MapEvent(MapEvent.HIDE_CALLOUT))
        EventBus.getDefault().post(MapEvent(MapEvent.CLEAR_GRAPHIC_LAYER))
    }

    private fun setLoadingVisiable(visiable: Boolean) {
//        if (isNotNull(vb.llLoading)) {
//            vb.llLoading.visibility = if (visiable) View.VISIBLE else View.GONE
//        }
    }

    override fun onItemChildViewClick(viewId: Int, position: Int) {

    }



}
