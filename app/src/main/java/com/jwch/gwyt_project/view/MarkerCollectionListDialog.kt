package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil.matchList
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.geoFroAnalysis.GeoAdapter
import com.jwch.gwyt_project.databinding.DialogMarkerListBinding
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.FileModel
import com.jwch.gwyt_project.model.GeoCollectionModel
import com.jwch.gwyt_project.db.DbUtil
import com.jwch.gwyt_project.fragment.CollectionFragment
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

/**
 * 标绘收藏列表对话框
 */
class MarkerCollectionListDialog(context: Context) : JameniBaseDialog(context), ItemClickListener,
    ActionListener {

    //用于存放数据库查询到的标绘列表
    var geoDataList: MutableList<BaseNode> = mutableListOf()
    var geoAdapter: GeoAdapter? = null //Adapter

    var vb: DialogMarkerListBinding? = null
    var datalist = mutableListOf<FileModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_marker_list, null)
        vb = DialogMarkerListBinding.bind(view)
        setContentView(vb!!.root)

        initView()
        setOnClick()

        dialogFitScreen()
    }

    private fun dialogFitScreen() {
        val util = GetWindowSize(context)
        val width = util.windowWidth / 2 * 1
        val height = util.windowHeight / 3 * 2
        vb!!.llDialog.layoutParams.width = width
        vb!!.llDialog.layoutParams.height = height
    }

    fun initView() {
        initGeoList()
        reloadGeoCollectData()
    }

    //初始化标绘收藏列表
    private fun initGeoList() {
        vb!!.lvGeometyList.setLinearManager()
        geoAdapter = GeoAdapter(this)
        vb!!.lvGeometyList.adapter = geoAdapter
        vb!!.lvGeometyList.disableLoadMoreIfNotFullPage()
        vb!!.lvGeometyList?.update(geoDataList)
    }

    //加载标绘数据
    private fun reloadGeoCollectData() {
        geoDataList.clear()
        //1、查出所有文件夹
        val folderList = DbUtil.db.queryAllFolderList()
        "文件夹：${folderList.toJson()}".printMsg()

        folderList?.forEach {
            geoDataList.add(GeoCollectionModel(it))
        }
        //2、查出所有不在文件夹下的标绘
        val markerList_noFolder = DbUtil.db.queryAllMarkerListByFolderId(0)
        markerList_noFolder?.forEach {
            geoDataList.add(GeoCollectionModel(it, GeoCollectionModel.LEVEL2))
        }

        //3、查出所有文件夹下的标绘
        geoDataList.forEach {
            val item = it as GeoCollectionModel

            item.isFolder.yes {

                val folder = item.folderData!!

                //查出文件夹下标绘的列表
                val markerList = DbUtil.db.queryAllMarkerListByFolderId(folder.id)
                markerList?.forEach { marker ->
                    item.addChildNode(GeoCollectionModel(marker, GeoCollectionModel.LEVEL2))
                }
            }
        }

        geoAdapter?.setNewData(geoDataList)
        vb!!.tvGeoNodata.visiable(!matchList(geoDataList))
    }

    private fun setOnClick() {
        //关闭dialog
        vb!!.imgClose.onClick {
            dismiss()
        }
    }

    override fun onItemClick(itemData: Any?, position: Int) {
        dismiss()
    }


    override fun onAction(obj: Any?, flag: Int) {
        when (flag) {
            //绘制
            CollectionFragment.ACTION_DRAW -> {
                drawSelectGeo(obj as GeoCollectionModel)
                dismiss()
            }
        }
    }

    private fun drawSelectGeo(data: GeoCollectionModel?) {
        data?.let {
            EventBus.getDefault().post(DataEvent(DataEvent.DRAW_COLLECTION_MARKER_ANALYSIS, it.markerInfo!!))
        }

    }
}