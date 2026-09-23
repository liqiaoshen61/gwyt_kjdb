package com.jwch.gwyt_project.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import com.jwch.gwyt_project.R

/**
 * 联网锁定弹窗
 *
 * 当检测到网络连接时，作为独立窗口铺满全屏并拦截一切操作：
 * - setCancelable(false)：无法通过点击外部 / 返回关闭；
 * - 拦截返回键（KEYCODE_BACK），禁止用户退出锁定层；
 * - 独立窗口天然压在 GL 地图等所有视图之上，整屏触摸都被吸收，下面的业务无法响应。
 *
 * 网络断开后由上层 leader（MainActivity）调用 dismiss() 移除。
 */
class NetworkLockDialog(context: Context) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.view_network_lock)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onStart() {
        super.onStart()
        val window = window ?: return
        // 半透明深色遮罩由布局整体绘制；窗口本身透明无边框
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        // 拦截硬件/软返回键，锁定期间无法退出
        setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
    }
}