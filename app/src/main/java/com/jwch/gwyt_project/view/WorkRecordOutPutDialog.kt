package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.GeoCollectionAdapter
import com.jwch.gwyt_project.databinding.ViewOutputDialogBinding
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.GeoCollectionModel
import com.jwch.gwyt_project.util.OutputWorkRecordFileUtil
import com.jwch.gwyt_project.db.DbUtil
import com.jwch.gwyt_project.ext.showSingleDialog
import com.qmuiteam.qmui.kotlin.onClick

import org.xutils.ex.DbException

/**
 * 工作记录导出Excel文件Dialog
 */
class WorkRecordOutPutDialog(context: Context) : JameniBaseDialog(context){

    var adapter: GeoCollectionAdapter? = null
    var dataList: MutableList<BaseNode> = mutableListOf()

    var vb: ViewOutputDialogBinding? = null

    init {
        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_output_dialog, null)
        vb = ViewOutputDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)
        initList()
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



    //加载标绘数据
    private fun loadGeoCollectData() {
        dataList.clear()

        //1、查出所有文件夹
        val folderList = DbUtil.db.queryAllFolderList()

        folderList?.forEach {
            val data = GeoCollectionModel(it)
            data.folderName = it.name
            dataList.add(data)
        }
        //2、查出所有不在文件夹下的标绘
        val markerList_noFolder = DbUtil.db.queryAllMarkerListByFolderId(0)
        markerList_noFolder?.forEach {
            val data = GeoCollectionModel(it, GeoCollectionModel.LEVEL2)
            data.folderName = "无"
            dataList.add(data)
        }

        //2、查出所有文件夹下的标绘
        dataList.forEach {
            val item = it as GeoCollectionModel

            item.isFolder.yes {
                val folder = item.folderData!!
                //查出文件夹下标绘的列表
                val markerList = DbUtil.db.queryAllMarkerListByFolderId(folder.id)
                markerList?.forEach { marker ->
                    val data = GeoCollectionModel(marker, GeoCollectionModel.LEVEL2)
                    data.folderName = folder.name
                    item.addChildNode(data)
                }
            }
        }
        adapter?.setNewData(dataList)
    }


    private fun initList() {

        vb!!.lvOutputList.setLinearManager()
        adapter = GeoCollectionAdapter()
        vb!!.lvOutputList.adapter = adapter
//        lvOutputList.itemClickListener = this
        vb!!.lvOutputList.update(dataList)
    }

    override fun show() {
        super.show()

        loadGeoCollectData()
    }

    private fun checkOutputData() {

        val selectList: MutableList<GeoCollectionModel> = mutableListOf()
        dataList?.forEach {
            val item1 = it as GeoCollectionModel

            it.isFolder.yes {
                item1.childNode?.forEach { node2 ->
                    val item2 = node2 as GeoCollectionModel
                    item2.isSelect.yes { selectList.add(item2) }
                }
            }.no {
                item1.isSelect.yes { selectList.add(item1) }
            }
        }
        if(selectList.size > 5){
            showSingleDialog(context,"最多同时导出5条记录")
            return
        }
        try {
            if (CommonUtil.matchList(selectList)) {
                val outputFileUtil =
                    OutputWorkRecordFileUtil()
                outputFileUtil.output(selectList, handler)
            } else {
                Toast.makeText(context, "导出失败，未查找到空间数据", Toast.LENGTH_LONG).show()
            }
        } catch (e: DbException) {
            "导出excel异常：${e.message}".printMsg()
            e.printStackTrace()
        }
        dismiss()
    }

    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                val outputPath = msg.obj as String
                tip(context, "已导出至" + CommonUtil.getSelfValue(outputPath))
            }
        }
    }
}
