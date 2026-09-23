package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.chad.library.adapter.base.entity.node.BaseNode
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.CollecPatchInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.GeoCollectionAdapter
import com.jwch.gwyt_project.adapter.PatchCollectionAdapter
import com.jwch.gwyt_project.adapter.PatchCollectionSelectAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewNavigationSelect2DialogBinding
import com.jwch.gwyt_project.databinding.ViewNavigationSelectDialogBinding
import com.jwch.gwyt_project.databinding.ViewOutputDialogBinding
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.GeoCollectionModel
import com.jwch.gwyt_project.util.OutputWorkRecordFileUtil
import com.jwch.gwyt_project.db.DbUtil
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.showSingleDialog
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.model.NavigationPointModel
import com.jwch.gwyt_project.util.NavigationUtil
import com.qmuiteam.qmui.kotlin.onClick

import org.xutils.ex.DbException

/**
 * 多点位导航选择(收藏夹列表)
 */
class NavigationSelect2Dialog(context: Context) : JameniBaseDialog(context) {

    var adapter: PatchCollectionSelectAdapter? = null
    var dataList: MutableList<CollecPatchInfo>? = null

    var vb: ViewNavigationSelect2DialogBinding? = null
    lateinit var navigationUtil: NavigationUtil

    val MAX_COUNT = 9

    init {
        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_navigation_select2_dialog, null)
        vb = ViewNavigationSelect2DialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)
        initList()
        reloadPatchCollectData()
        navigationUtil = NavigationUtil(context)
        //取消
        vb!!.imgClose.onClick {
            dismiss()
        }

        vb!!.tvOk.onClick {
            checkOutputData()
        }


        dialogFitScreen()
    }

    private fun dialogFitScreen() {
        val util = GetWindowSize(context)
        val width = util.windowWidth / 2 * 1
        val height = util.windowHeight / 4 * 3
        vb!!.llDialog.layoutParams.width = width
        vb!!.llDialog.layoutParams.height = height
    }

    //加载图斑数据
    fun reloadPatchCollectData() {
        try {
            dataList = db.queryAllCollectPatchList()
            CommonUtil.matchList(dataList).yes{
                dataList = dataList!!.filter {!it.centerPointJson.isNullOrBlank() } as MutableList
                adapter?.setNewData(dataList)
            }.no{
//                vb.tvNodata.visiable(!matchList(patchDataList))
            }




        } catch (e: DbException) {
            e.printStackTrace()
        }
    }



    private fun initList() {

        dataList = mutableListOf()
        vb!!.lvList.setLinearManager()
        adapter = PatchCollectionSelectAdapter()
        vb!!.lvList.adapter = adapter
        vb!!.lvList.setItemClickListener { itemData, position ->
            val item = itemData as CollecPatchInfo
            item.isSelect = !item.isSelect

            vb!!.lvList.update(dataList)
        }
        vb!!.lvList.disableLoadMoreIfNotFullPage()
        vb!!.lvList.update(dataList)

    }

    override fun show() {
        super.show()

        reloadPatchCollectData()
    }

    private fun checkOutputData() {

        val selectList: MutableList<CollecPatchInfo> = mutableListOf()
        dataList?.forEach {
            val item1 = it as CollecPatchInfo

            item1.isSelect.yes { selectList.add(item1) }

        }
        if (selectList.size > MAX_COUNT) {
            showSingleDialog(context, "最多同时选择${MAX_COUNT}条数据")
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
                Toast.makeText(context, "操作是失败，未查找到空间数据", Toast.LENGTH_LONG).show()
            }
        } catch (e: DbException) {
            "异常：${e.message}".printMsg()
            e.printStackTrace()
        }
        dismiss()
    }


}
