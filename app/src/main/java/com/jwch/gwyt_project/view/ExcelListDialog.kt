package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.esri.arcgisruntime.data.Feature
import com.jameni.allutillib.common.FileUtil
import com.jameni.allutillib.common.NumUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.ExcelListAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.DialogExcelListBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.model.FileModel
import com.jwch.gwyt_project.model.PointModel
import com.jwch.gwyt_project.util.excelutil.ExcelUtil
import com.qmuiteam.qmui.kotlin.onClick
import java.io.File

/**
 * Excel列表对话框
 */
class ExcelListDialog(context: Context, block: (MutableList<PointModel>) -> Unit = {}) :
    JameniBaseDialog(context), ItemClickListener {

    val actionBlock = block


    var vb: DialogExcelListBinding? = null

    var datalist = mutableListOf<FileModel>()
    var adapter = ExcelListAdapter()

    //    val titleArray = arrayOf("序号", "X", "Y", "备注：构面数据需要数量大于等于三个坐标点")
    val titleArray = arrayOf("序号", "经度", "纬度", "备注：构面数据需要数量大于等于三个坐标点")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_excel_list, null)
        vb = DialogExcelListBinding.bind(view)
        setContentView(vb!!.root)

        initView()
        setOnClick()

    }

    fun initView() {


        val fileUtil = FileUtil()
        fileUtil.createPath(Config.EXCEL_PATH)
        val file = File(Config.EXCEL_PATH)


        val fileArray = file.listFiles()

        (fileArray.size > 0).yes {

            fileArray.forEach {
                val item = FileModel(it)
                datalist.add(item)
            }
        }


        vb!!.includeList.lvMain.setLinearManager()
        vb!!.includeList.lvMain.setAdapter(adapter)
        vb!!.includeList.lvMain.update(datalist)
        vb!!.includeList.lvMain.itemClickListener = this

    }


    private fun setOnClick() {
        //关闭dialog
        vb!!.imgClose.onClick {
            dismiss()
        }
    }

    override fun onItemClick(itemData: Any?, position: Int) {

        val data = itemData as FileModel

        if (!isExcel(data.ext)) {
            showSingleDialog(context, "该文件不是excel")
            return
        }

        val path = data.filePath

        val list = ExcelUtil.readExcel(File(path), titleArray)

        if (list.size < 3) {
            showSingleDialog(context, "该excel文件中，坐标点不足3个，不能构成面状图形。")
            return
        }
        "excel content : ${list.toJson()}".printMsg()

        val resultList = mutableListOf<PointModel>()

        list.forEach {
            val lng = NumUtil.getDoubleVale(it[titleArray[1]].self())
            val lat = NumUtil.getDoubleVale(it[titleArray[2]].self())
//            val des = it[titleArray[3]].self()
            resultList.add(PointModel(lat, lng))
        }

        actionBlock(resultList)
        dismiss()
    }

    private fun isExcel(ext: String): Boolean = ext.contains("xls") || ext.contains("xlsx")
}