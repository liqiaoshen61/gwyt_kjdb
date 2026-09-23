package com.jwch.gwyt_project.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Process
import java.lang.Runtime

class MemoryMonitor(private val context: Context) {

    // 获取java/kotlin内存信息
    fun getSystemMemoryInfo(): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    // 获取当前应用内存使用情况
    fun getAppMemoryInfo(): AppMemory {
        val runtime = Runtime.getRuntime()
        val totalMem = MemoryUnitConverter.bytesToMB(runtime.totalMemory())  // 应用已分配的总内存
        val freeMem = MemoryUnitConverter.bytesToMB(runtime.freeMemory())  // 应用当前空闲内存
        val usedMem = totalMem - freeMem    // 应用已使用内存
        val maxMem = MemoryUnitConverter.bytesToMB(runtime.maxMemory())    // 应用最大可使用内存（受系统限制）
        return AppMemory(totalMem, freeMem, usedMem, maxMem)
    }


    // 获取Native层内存信息
    fun getNativeMemoryInfo(): NativeMemory {
        val nativeHeapSize = MemoryUnitConverter.bytesToMB(Debug.getNativeHeapSize()) // Native堆总大小
        val nativeHeapAllocated = MemoryUnitConverter.bytesToMB(Debug.getNativeHeapAllocatedSize())  // 已分配的Native内存
        val nativeHeapFree = MemoryUnitConverter.bytesToMB(Debug.getNativeHeapFreeSize())  // 空闲的Native内存
        return NativeMemory(nativeHeapSize, nativeHeapAllocated, nativeHeapFree)
    }

    data class NativeMemory(
        val totalSize: Double, // Native堆总容量
        val allocated: Double, // 已使用的Native内存
        val free: Double // 空闲的Native内存
    )

    // 数据类：存储应用内存信息
    data class AppMemory(
        val totalMem: Double,
        val freeMem: Double,
        val usedMem: Double,
        val maxMem: Double
    )
}