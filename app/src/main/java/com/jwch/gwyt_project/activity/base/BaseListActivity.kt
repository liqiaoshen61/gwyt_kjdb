package com.jwch.gwyt_project.activity.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jameni.jamenilistlib.i.ItemChildViewClickListener
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jameni.jamenilistlib.i.OnLoadMoreListener
import com.jameni.jamenilistlib.i.OnRefreshListener
import com.jameni.jamenilistlib.view.RefreshView
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.yes
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import java.lang.reflect.ParameterizedType

abstract class BaseListActivity<T : ViewBinding> : FinalBindingActivity(), ItemClickListener, ItemChildViewClickListener,
    OnRefreshListener, OnLoadMoreListener, OnItemClickListener, OnItemChildClickListener {

    protected lateinit var vb: T
    open protected var pageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val clazz = type.actualTypeArguments[0] as Class<T>
            val method = clazz.getMethod("inflate", LayoutInflater::class.java)
            vb = method.invoke(null, layoutInflater) as T
            setContentView(vb.root)
        }



        afterLayout()
        if (getPageTitle().isNotBlank()) setPageTitle(getPageTitle())
        setHeadVisible(setHeaderVisiable())
        QMUIStatusBarHelper.setStatusBarLightMode(this)
        initView()
        initPageData(null)
        initViewListener();
    }


    protected abstract fun initView()
    protected open fun getPageTitle(): String = ""
    protected open fun setHeaderVisiable(): Boolean = true
    protected open fun initPageData(data: Any?) {}
    protected open fun initViewListener() {}
    protected open fun beforeLayout() {}
    protected open fun afterLayout() {}
    protected open fun initRefreshView(list: RefreshView?, adapter: RecyclerView.Adapter<*>?, datalist: MutableList<*>?) {
        list?.setListBackground(com.jameni.basepage_lib.R.color.whiteColor)
        list?.setLinearManager(true)
        adapter?.let {
            list?.setAdapter(adapter)
        }

        list?.onRefreshListener = this
        list?.onLoadMoreListener = this
        list?.itemClickListener = this
        list?.itemChildViewClickListener = this


        datalist?.isEmpty() ?: list?.update(datalist)

        //设置空占位图
        getEmptyImageResId()?.let {
            list?.setEmptyImage(it)
        }

        //添加头部
        getHeaderView()?.let {
            list?.addHeadView(it)
        }

        //添加底部
        getFooterView()?.let {
            list?.addFootView(it)
        }
    }

    protected open fun initRecyclerview(list: RecyclerView?, adapter: BrvahAdapter<*>?, datalist: MutableList<*>?) {
        list?.setBackgroundColor(findColor(context, com.jameni.basepage_lib.R.color.whiteColor))
        list?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        adapter?.let {
            list?.setAdapter(adapter)
        }

        adapter?.setOnItemClickListener(this)
        adapter?.setOnItemChildClickListener(this)
        datalist?.isEmpty() ?: adapter?.update(datalist)
    }

    protected open fun getHeaderView(): View? {
        return null
    }

    protected open fun getFooterView(): View? {
        return null
    }

    protected open fun getEmptyImageResId(): Int? = null

    protected open fun loadComplete(list: RefreshView?) {
        list?.refreshComplete()
        list?.loadMoreComplete()
    }

    protected fun isFirstPage() = pageIndex == 1




    protected open fun loadEnd(list: RefreshView?) {
        list?.setLoadMoreEnd()
    }

    protected open fun showEmptyImage(list: RefreshView?) {
        list?.setEmptyViewVisiable(list.adapter.itemCount < 1)
    }

    override fun onItemChildViewClick(viewId: Int, position: Int) {
    }

    override fun onItemClick(itemData: Any?, position: Int) {
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {

    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {

    }


    override fun onRefresh() {
        pageIndex = 0
        onLoadMore()
    }

    override fun onLoadMore() {
        pageIndex++
    }


    protected fun updateList(adapter: BrvahAdapter<*>?, datalist: MutableList<*>?) =
        isNotNull(datalist).yes { adapter?.update(datalist) }

    protected open fun showloading(list: RefreshView?, show: Boolean) {
        list?.refreshLayout?.isRefreshing = show
    }


}
