package com.jwch.gwyt_project.view.popwindow

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewMypopwindowBinding
import com.jwch.gwyt_project.util.FunctionControlUtil


class ToolPopWindow(context: Context?, width: Int, height: Int) : PopupWindow(context), OnItemClickListener {

    constructor(context: Context?) : this(
        context, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ) {
    }

    private val context: Context?
    private val view: View

    lateinit var actionBlock: (PopWindowModel, Int) -> Unit

    var vb: ViewMypopwindowBinding? = null
    lateinit var popWinodwAdapter: PopWinodwAdapter
    var dataList = mutableListOf<PopWindowModel>()


    init {
        this.context = context
        setWidth(width)
        setHeight(height)
        isFocusable = true
        isOutsideTouchable = true
        isTouchable = true
        setBackgroundDrawable(BitmapDrawable())
        view = LayoutInflater.from(context).inflate(R.layout.view_mypopwindow, null)

        vb = ViewMypopwindowBinding.bind(view)
        contentView = vb!!.root

        contentView = view

        initMenuList()
    }

    fun initMenuList() {

        dataList.add(PopWindowModel("叠加分析", R.mipmap.logo_overlay_analysis))
        dataList.add(PopWindowModel("标绘", R.mipmap.logo_position_location))
        dataList.add(PopWindowModel("测量", R.mipmap.icon_tool_measure))
        if (FunctionControlUtil.instances.MODULE_TOOLS_COMPARE_MAP) {
            dataList.add(PopWindowModel("对比地图", R.mipmap.icon_tool_compare))
        }


//        dataList.add(PopWindowModel("外部导入", R.mipmap.icon_tool_mark))

        vb!!.lvMain.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        popWinodwAdapter = PopWinodwAdapter()
        vb!!.lvMain.adapter = popWinodwAdapter
        popWinodwAdapter.setOnItemClickListener(this)
        popWinodwAdapter.update(dataList)

        //添加自定义分割线
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context!!, com.jameni.basepage_lib.R.color.bg_gray)!!)
        vb!!.lvMain.addItemDecoration(divider)

    }



    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (::actionBlock.isInitialized) {
            actionBlock(dataList[position], position)
        }

    }
}

