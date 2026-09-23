package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.ReviewRecordInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.ReviewOutputAdapter
import com.jwch.gwyt_project.databinding.ViewOutputDialogBinding
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.util.OutputWorkRecordFileUtil
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.showSingleDialog
import com.jwch.gwyt_project.util.OutputReviewRecordFileUtil
import com.qmuiteam.qmui.kotlin.onClick

import org.xutils.ex.DbException

/**
 * 复核记录导出Excel文件Dialog
 */
class ReviewRecordOutPutDialog(context: Context) : JameniBaseDialog(context){


    var dataList: MutableList<ReviewRecordInfo>? = mutableListOf()
    private var adapter: ReviewOutputAdapter? = null

    var vb: ViewOutputDialogBinding? = null
    var linkId = ""

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
        dataList?.clear()
        dataList = db.queryReviewRecordListById(linkId)
        adapter?.setNewData(dataList)
    }


    private fun initList() {

        vb!!.lvOutputList.setLinearManager()
        adapter = ReviewOutputAdapter()
        vb!!.lvOutputList.adapter = adapter
        adapter!!.setOnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as ReviewRecordInfo
            item.isSelect = !item.isSelect
            vb!!.lvOutputList.update(dataList)
        }
    }

    fun show(linkId : String) {
        super.show()
        this.linkId = linkId
        loadGeoCollectData()
    }

    private fun checkOutputData() {

        var selectList: MutableList<ReviewRecordInfo>? = mutableListOf()
        selectList = dataList?.filter {it.isSelect == true } as MutableList<ReviewRecordInfo>
        if(!CommonUtil.matchList(selectList)){
            showSingleDialog(context,"请勾选需要导出的记录")
            return
        }
        if(selectList.size > 5){
            showSingleDialog(context,"最多同时导出5条记录")
            return
        }
        try {
            if (CommonUtil.matchList(selectList)) {
                val outputFileUtil = OutputReviewRecordFileUtil()
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
