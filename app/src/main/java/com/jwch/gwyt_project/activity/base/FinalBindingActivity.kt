package com.jwch.gwyt_project.activity.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.hjq.toast.ToastUtils
import com.jameni.basepage_lib.baseactivity.FinalActivity
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.LayoutFinalBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.core.AppContext
import com.qmuiteam.qmui.kotlin.onClick



abstract class FinalBindingActivity : FinalActivity() {

    protected lateinit var fBinding: LayoutFinalBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fBinding = LayoutFinalBinding.inflate(layoutInflater)
        super.setContentView(fBinding.root)


        fBinding?.viewPageHeader?.rlBack?.onClick { backClick() }
        fBinding?.viewPageHeader?.rlRight?.onClick { rightClick() }
        fBinding?.viewPageHeader?.tvPageTitle?.onClick { pageTitleClick() }

        fBinding?.llLoading?.onClick {}
        fBinding.viewPageHeader.rlPageHeaderView.setBackgroundColor(getColor(com.jameni.basepage_lib.R.color.main_color))
    }


    /**
     * 设置头部标题
     */
    protected fun setPageTitle(title: String) {
        fBinding?.viewPageHeader?.tvPageTitle?.text = title
    }

    /**
     * 设置头部显示，隐藏
     */
    protected fun setHeadVisible(visiable: Boolean) {
        fBinding?.viewPageHeader?.root?.visibility = if (visiable) View.VISIBLE else View.GONE
    }

    /**
     * 设置头部返回按钮显示隐藏
     */
    protected fun setBackVisible(visiable: Boolean) {
        fBinding?.viewPageHeader?.rlBack?.visibility = if (visiable) View.VISIBLE else View.GONE
    }

    /**
     * 设置头部右侧按钮文本
     */
    protected fun setRightText(txt: String) {
        fBinding?.viewPageHeader?.rlRight?.visibility = View.VISIBLE
        fBinding?.viewPageHeader?.tvRight?.visibility = View.VISIBLE
        fBinding?.viewPageHeader?.imgRight?.visibility = View.GONE
        fBinding?.viewPageHeader?.tvRight?.text = txt
    }


    /**
     * 设置头部右侧按钮文本
     */
    protected fun setRightImageRes(resId: Int) {
        fBinding?.viewPageHeader?.rlRight?.visibility = View.VISIBLE
        fBinding?.viewPageHeader?.tvRight?.visibility = View.GONE
        fBinding?.viewPageHeader?.imgRight?.visibility = View.VISIBLE
        fBinding?.viewPageHeader?.imgRight?.setImageResource(resId)
    }

    override fun setContentView(view: View?) {

        if (view == null) return
        view?.let {
            fBinding.llMainContent.addView(
                it, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            )
        }
    }

    /**
     * 头部右边的点击事件
     */
    protected open fun rightClick() {}

    /**
     * 头部标题的点击事件
     */
    protected open fun pageTitleClick() {}

    /**
     * 头部回退的点击事件
     */
    protected open fun backClick() {
        finish()
    }

    override fun tip(str: String) {
        ToastUtils.show(this)
    }

    protected fun addPageDatas(key: String, data: Any) = AppContext.map.put(key.self(), data)
    protected fun getPageDatas(key: String): Any? = AppContext.map[key]
    protected fun clearPageDatas() = AppContext.map.clear()
    override fun onDestroy() {
        super.onDestroy()
        clearPageDatas()
    }

    protected fun isBackSuccess(resultCode: Int) = resultCode == Config.RESULT_CODE
    protected fun successBack(data: Intent? = null, resultCode: Int = Config.RESULT_CODE) {
        if (isNotNull(data)) {
            setResult(resultCode, data)
        } else {
            setResult(resultCode)
        }
        finish()
    }

    fun showLoading() {
        fBinding?.llLoading?.show()
    }

    fun hideLoading() {
        fBinding?.llLoading?.gone()
    }

    override fun onResume() {
        super.onResume()
    }

}