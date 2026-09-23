package com.jwch.gwyt_project.util

import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import com.jwch.gwyt_project.ext.printMsg

class MemoryMonitorManager(private val context: Context) {
    private val memoryMonitor = MemoryMonitor(context)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // 每2秒检查一次
    private val memoryThreshold = 0.9f // 内存使用率阈值（80%）

    // 启动内存监听
    fun startMonitoring() {
        mainHandler.postDelayed(memoryCheckRunnable, checkInterval)
    }

    // 停止内存监听
    fun stopMonitoring() {
        mainHandler.removeCallbacks(memoryCheckRunnable)
    }

    private val memoryCheckRunnable = object : Runnable {
        override fun run() {
            val appMem = memoryMonitor.getAppMemoryInfo()
            val usageRate = appMem.usedMem / appMem.maxMem

            "java内存使用情况：${appMem.usedMem} / ${appMem.maxMem}  使用率：${usageRate}".printMsg()
            // 内存使用率超过阈值，触发处理逻辑
            if (usageRate >= memoryThreshold) {
                onHighMemoryUsage()
            }

            val appMemNative = memoryMonitor.getNativeMemoryInfo()
            val usageRateNative = appMemNative.allocated / appMemNative.totalSize

            "Native内存使用情况：${appMemNative.allocated} / ${appMemNative.totalSize}  使用率：${usageRateNative}".printMsg()
            // 内存使用率超过阈值，触发处理逻辑
            if (usageRateNative >= memoryThreshold) {
                onHighMemoryUsageNative()
            }

            // 继续循环监听
            mainHandler.postDelayed(this, checkInterval)
        }
    }

    // 高内存占用时的处理（需释放资源）
    private fun onHighMemoryUsage() {
//        "java 我得做点什么了".printMsg()
    }

    // 高内存占用时的处理（需释放资源）
    private fun onHighMemoryUsageNative() {
//        "native 我得做点什么了".printMsg()
    }
}