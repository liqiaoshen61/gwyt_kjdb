package com.jwch.gwyt_project.util

import android.content.Context
import android.graphics.Point
import android.os.*
import android.util.Log
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView

import kotlin.math.roundToInt

/**
 * MapView 内存监控与自动重建管理器
 * - 自动检测 native 内存使用量
 * - 超过阈值时自动释放并重建 MapView
 *
 * 用法：
 *   val manager = MapViewMemoryManager(context, container, mapView)
 *   manager.startMonitoring()
 */
class MapViewMemoryManager(
    private val thresholdMB: Int = 600,  // native 内存上限（单位：MB）
    private val intervalSec: Int = 30,    // 检测间隔（秒）
    val block: () -> Unit
) {
    private val TAG = "MapViewMemoryManager"
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val checkTask = object : Runnable {
        override fun run() {
            try {
                val nativeHeap = Debug.getNativeHeapAllocatedSize() / 1024 / 1024
                Log.d(TAG, "Native Heap: ${nativeHeap} MB")

                if (nativeHeap > thresholdMB) {
                    Log.w(TAG, "⚠️ Native memory high: ${nativeHeap} MB > $thresholdMB MB, rebuilding MapView...")
                    block()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (running) handler.postDelayed(this, intervalSec * 1000L)
        }
    }

    fun startMonitoring() {
        if (!running) {
            running = true
            handler.post(checkTask)
        }
    }

    fun stopMonitoring() {
        running = false
        handler.removeCallbacks(checkTask)
    }

}
