package com.jwch.gwyt_project.util

import com.jwch.gwyt_project.ext.clearAllKv
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.saveKV
import com.tencent.mmkv.MMKV
import java.io.File

class GdbSizeManager {
    private val mmkv: MMKV = MMKV.defaultMMKV()
    private val FOLDER_SIZE_KEY = "folder_size_key"

    fun checkAndUpdateFolderSize(folderPath: String) {
        val currentSize = calculateFolderSize(File(folderPath))
        val savedSize = mmkv.decodeLong(FOLDER_SIZE_KEY, -1L)

        when {
            savedSize == -1L -> {
                // 首次记录文件夹大小
                saveKV(FOLDER_SIZE_KEY,currentSize)
                "gdb文件夹大小 首次记录 $currentSize".printMsg()
            }
            savedSize != currentSize -> {
                // 大小发生变化，清理 MMKV 并保存新值
                clearAllKv()
                saveKV(FOLDER_SIZE_KEY,currentSize)
                "gdb文件夹 大小发生变化  $currentSize".printMsg()
            }
            else -> {
                "gdb文件夹 大小发生变化  $currentSize".printMsg()
            }

        }
    }

    private fun calculateFolderSize(folder: File): Long {
        if (!folder.exists() || !folder.isDirectory) return 0L

        return folder.walk()
            .filter { it.isFile }
            .fold(0L) { acc, file -> acc + file.length() }
    }
}