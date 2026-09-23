package com.jwch.gwyt_project.activity.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.view.WatermarkView
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<T : ViewBinding> : FinalBindingActivity() {

    protected lateinit var vb: T
    private var watermarkView: WatermarkView? = null

    companion object {
        private const val TAG = "BaseActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val clazz = type.actualTypeArguments[0] as Class<T>
            val method = clazz.getMethod("inflate", LayoutInflater::class.java)
            vb = method.invoke(null, layoutInflater) as T
            setContentView(vb.root)
        }


        getPageDatas("title")?.let {
            var pageTitle = it as String
            setPageTitle(pageTitle)
        }

        afterLayout()
        if (getPageTitle().isNotBlank()) setPageTitle(getPageTitle())
        setHeadVisible(setHeaderVisiable())
        QMUIStatusBarHelper.setStatusBarDarkMode(this)
        initView()
        initPageData(null)
        initViewListener();

        //初始化水印
        initWatermark()

        //注册EventBus（如果子类已经注册，则不再重复注册）
        if (!EventBus.getDefault().isRegistered(this)) {
            try {
                EventBus.getDefault().register(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    protected abstract fun initView()
    protected open fun getPageTitle(): String = ""
    protected open fun setHeaderVisiable(): Boolean = true
    protected open fun initPageData(data: Any?) {}
    protected open fun initViewListener() {}
    protected open fun beforeLayout() {}
    protected open fun afterLayout() {}

    override fun onResume() {
        super.onResume()
    }

    protected open fun <T> findFragemntById(id: Int): T = supportFragmentManager.findFragmentById(id) as T


    /**
     * 初始化水印
     */
    private fun initWatermark() {
        //检查是否启用水印
        val isWatermarkEnabled = getKV(Keys.WATERMARK_ENABLED, false)
        Log.d(TAG, "${this.javaClass.simpleName} 初始化水印: isEnabled=$isWatermarkEnabled")

        if (isWatermarkEnabled) {
            showWatermark()
        }
    }

    /**
     * 显示水印
     */
    private fun showWatermark() {
        if (watermarkView != null) {
            Log.d(TAG, "水印视图已存在，跳过创建")
            return
        }

        Log.d(TAG, "创建并显示水印视图")

        //创建水印视图
        watermarkView = WatermarkView(this).apply {
            //获取配置
            val customText = getKV(Keys.WATERMARK_CUSTOM_TEXT, "")
            val textSize = getKV(Keys.WATERMARK_TEXT_SIZE, 42)
            val alpha = getKV(Keys.WATERMARK_ALPHA, 60)
            val angle = getKV(Keys.WATERMARK_ANGLE, -30)
            Log.d(TAG, "水印配置: customText=$customText, textSize=$textSize, alpha=$alpha, angle=$angle")
            setConfig(customText, textSize, alpha, angle)
        }

        //添加到DecorView
        val decorView = window.decorView as ViewGroup
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        decorView.addView(watermarkView, params)
        Log.d(TAG, "水印视图已添加到DecorView")
    }

    /**
     * 隐藏水印
     */
    private fun hideWatermark() {
        Log.d(TAG, "隐藏水印")
        watermarkView?.let {
            val decorView = window.decorView as ViewGroup
            decorView.removeView(it)
            it.destroy()
            watermarkView = null
        }
    }

    /**
     * 更新水印配置
     */
    private fun updateWatermarkConfig() {
        watermarkView?.let {
            val customText = getKV(Keys.WATERMARK_CUSTOM_TEXT, "")
            val textSize = getKV(Keys.WATERMARK_TEXT_SIZE, 42)
            val alpha = getKV(Keys.WATERMARK_ALPHA, 60)
            val angle = getKV(Keys.WATERMARK_ANGLE, -30)
            Log.d(TAG, "更新水印配置: customText=$customText, textSize=$textSize, alpha=$alpha, angle=$angle")
            it.setConfig(customText, textSize, alpha, angle)
        }
    }

    /**
     * 处理水印事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun handleWatermarkEvent(event: DataEvent) {
        Log.d(TAG, "收到水印事件: actionType=${event.actionType}, data=${event.data}")

        when (event.actionType) {
            DataEvent.WATERMARK_STATE_CHANGED -> {
                val isEnabled = event.data as Boolean
                Log.d(TAG, "水印状态改变: $isEnabled")
                if (isEnabled) {
                    showWatermark()
                } else {
                    hideWatermark()
                }
            }
            DataEvent.WATERMARK_CONFIG_CHANGED -> {
                Log.d(TAG, "水印配置改变")
                updateWatermarkConfig()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //清理水印资源
        hideWatermark()
        //注销EventBus
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }


    var clickItervalTime = 2500
    var localTimeStamp = 0L
    protected fun clickEnable(): Boolean {
        val currentTimeStamp = TimeUtil.getCurrentStamp()
        val internal = currentTimeStamp.minus(localTimeStamp)
        if (internal > clickItervalTime) {
            localTimeStamp = TimeUtil.getCurrentStamp()
            return true
        } else {
            return false
        }
    }


}