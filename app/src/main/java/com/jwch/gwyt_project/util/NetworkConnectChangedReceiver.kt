package com.jwch.gwyt_project.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.model.MapEvent
import org.greenrobot.eventbus.EventBus

class NetworkConnectChangedReceiver : BroadcastReceiver() {

    companion object {
        private val handler = Handler(Looper.getMainLooper())
        private var pendingCheck: Runnable? = null
        private const val DEBOUNCE_DELAY = 500L // 500ms 防抖延迟
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            // 取消之前的待处理检查
            pendingCheck?.let { handler.removeCallbacks(it) }

            // 延迟检查网络状态，避免短时间内多次广播导致状态混乱
            pendingCheck = Runnable {
                val isConnected = NetUtil.isNetworkConnected(context)
                "网络检测: isConnected=$isConnected, 当前hasNet=${AppContext.hasNet}".printMsg()

                // 只有当网络状态真正变化时才更新和发送事件
                if (AppContext.hasNet != isConnected) {
                    AppContext.hasNet = isConnected
                    EventBus.getDefault().post(MapEvent(isConnected.getOne(MapEvent.NET_CONNECT, MapEvent.NET_DISCONNECT)))
                    isConnected.getOne("网络状态变化：有网络", "网络状态变化：无网络").printMsg()
                }
            }
            handler.postDelayed(pendingCheck!!, DEBOUNCE_DELAY)
        }
    }
}