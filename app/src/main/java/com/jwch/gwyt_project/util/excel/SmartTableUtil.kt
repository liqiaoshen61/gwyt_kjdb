package com.jwch.gwyt_project.util.excel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import androidx.core.content.ContextCompat
import com.bin.david.form.core.SmartTable
import com.bin.david.form.core.TableConfig
import com.bin.david.form.data.CellInfo
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.format.bg.BaseBackgroundFormat
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat
import com.bin.david.form.data.format.bg.ICellBackgroundFormat
import com.bin.david.form.data.style.FontStyle
import com.bin.david.form.data.table.TableData
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self


class SmartTableUtil {

    private var context: Context? = null
    var smartTable: SmartTable<*>? = null
    var selectPositon = -1

    constructor() { }

    constructor(context: Context?) {
        this.context = context
    }

    lateinit var block: (TableDataModel) -> Unit


    fun  showTable(smartTable: SmartTable<*>, columnList: MutableList<Column<*>>, dataList: MutableList<*>){

        this.smartTable = smartTable
        //组装数据模型
        val tableData = TableData("统计结果", dataList, columnList)
        //设置排序列 （默认从小到大）
//        tableData.sortColumn = columnList[0]
//        column0.isReverseSort = true //设置排序为 从大到小

        //设置项
        smartTable.setZoom(false) //是否可以缩放
        val tConfig = smartTable.config
        tConfig.isShowXSequence = false //设置是否显示顶部序号列
        tConfig.isShowYSequence = false //设置是否显示左侧序号列
        tConfig.isShowTableTitle = false //设置是否显示表格标题


        //标题背景颜色
        tConfig.columnTitleBackground = BaseBackgroundFormat(ContextCompat.getColor(context!!, R.color.light_white))
        val titleFontStyle = FontStyle()
        titleFontStyle.textColor = ContextCompat.getColor(context!!, R.color.white)
        titleFontStyle.textSize = 26
        tConfig.columnTitleStyle = titleFontStyle



        //设置隔行颜色区分
        val backgroundFormat: ICellBackgroundFormat<CellInfo<*>> =
            object : BaseCellBackgroundFormat<CellInfo<*>>() {
                override fun getBackGroundColor(cellInfo: CellInfo<*>): Int {
                    var color = -1
                    if(cellInfo.row == selectPositon){
                        color = ContextCompat.getColor(context!!, R.color.dark_blue)
                    }else if (cellInfo.row % 2 == 0) {
                        color = TableConfig.INVALID_COLOR
                    } else  {
                        color = ContextCompat.getColor(context!!, R.color.light_white)
                    }
                    return color
                }

                override fun getTextColor(cellInfo: CellInfo<*>): Int {
                    var color = -1
                   if (cellInfo.row % 2 == 0) {
                        color = ContextCompat.getColor(context!!, R.color.main_blue)
                    } else  {
                        color = ContextCompat.getColor(context!!, R.color.white)
                    }

                    return color

                }
            }
        tConfig.contentCellBackgroundFormat = backgroundFormat


        val backgroundFormat2: ICellBackgroundFormat<Int> =
            object : BaseCellBackgroundFormat<Int>() {
                override fun getBackGroundColor(position: Int): Int {
                    return if (position % 2 == 0) {
                        ContextCompat.getColor(context!!, com.jameni.basepage_lib.R.color.green)
                    } else TableConfig.INVALID_COLOR
                }
                override fun getTextColor(position: Int): Int {
                    var color = -1
                    if (position % 2 == 0) {
                        color = ContextCompat.getColor(context!!, R.color.white)
                    } else {
                        color = TableConfig.INVALID_COLOR
                    }
                    return color
                }
            }
        tConfig.ySequenceCellBgFormat = backgroundFormat2 //左侧序列号的样式

//        tConfig.contentStyle = backgroundFormat2

        tConfig.isFixedYSequence = false //固定左侧序列号
        tConfig.isFixedXSequence = false //固定顶部序列号
        tConfig.horizontalPadding = 24 //单元格 左右padding (会影响标题宽度)
        tConfig.verticalPadding = 7 //单元格 上下padding  (不会影响标题高度)
        tConfig.columnTitleVerticalPadding = 7
        tConfig.columnTitleHorizontalPadding = 24


//        tableData.setOnRowClickListener { column, t, col, row ->
//            smartTable.config.setContentCellBackgroundFormat(object : ICellBackgroundFormat<CellInfo<*>>{
//                override fun drawBackground(canvas: Canvas?, rect: Rect?, cellInfo: CellInfo<*>?, paint: Paint?) {
//
//                    if (cellInfo?.row == row) {
//                        paint?.color = ContextCompat.getColor(context!!, R.color.dark_blue)
//                        if (::block.isInitialized) {
//                            block((t as TableDataModel))
//                        }
//                    }else if(cellInfo?.row.self() % 2 == 0){
//                        paint?.color = TableConfig.INVALID_COLOR
//                    }else {
//                        paint?.color = ContextCompat.getColor(context!!, R.color.light_white)
//                    }
//                    canvas?.drawRect(rect!!, paint!!);
//
//                    mHandler.sendEmptyMessageDelayed(1,50);
//
//                }
//
//                override fun getTextColor(cellInfo: CellInfo<*>): Int {
//                    var color = -1
//                    if (cellInfo.row % 2 == 0) {
//                        color = ContextCompat.getColor(context!!, R.color.main_blue)
//                    } else  {
//                        color = ContextCompat.getColor(context!!, R.color.white)
//                    }
//                    return color
//                }
//
//            })
//        }

        //展示表格
        smartTable.tableData = tableData
    }

    fun select(position :Int){
        selectPositon  = position
        mHandler.sendEmptyMessageDelayed(1,50);
    }


    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            this.removeMessages(1)
            smartTable?.notifyDataChanged();
            super.handleMessage(msg)
        }
    }




}