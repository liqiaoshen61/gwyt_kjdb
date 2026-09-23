package com.jwch.gwyt_project.util

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class TextFileReader(private val context: Context) {

    /**
     * 读取指定路径下的TXT文件内容
     * @param filePath 文件路径（可以是绝对路径或相对路径）
     * @return 文件内容字符串，如果读取失败则返回null
     */
    fun readTextFile(filePath: String): String? {
        return try {
            val file = File(filePath)

            // 检查文件是否存在
            if (!file.exists()) {
                return null
            }

            // 使用缓冲读取器读取文件内容
            val inputStream = FileInputStream(file)
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

            val content = StringBuilder()
            var line: String?

            // 逐行读取文件内容
            while (reader.readLine().also { line = it } != null) {
                content.append(line)
            }

            // 关闭资源
            reader.close()
            inputStream.close()

            // 返回文件内容
            content.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 读取应用内部存储目录下的TXT文件
     * @param fileName 文件名（不需要完整路径）
     * @return 文件内容字符串，如果读取失败则返回null
     */
    fun readInternalFile(fileName: String): String? {
        val filePath = File(context.filesDir, fileName).absolutePath
        return readTextFile(filePath)
    }

    /**
     * 读取应用外部存储目录下的TXT文件
     * @param fileName 文件名（不需要完整路径）
     * @return 文件内容字符串，如果读取失败则返回null
     */
    fun readExternalFile(fileName: String): String? {
        val filePath = File(context.getExternalFilesDir(null), fileName).absolutePath
        return readTextFile(filePath)
    }

    /**
     * 读取SD卡或其他外部存储中的TXT文件（需要权限）
     * @param filePath 完整文件路径
     * @return 文件内容字符串，如果读取失败则返回null
     */
    fun readExternalStorageFile(filePath: String): String? {
        // 注意：从Android 10开始，需要MANAGE_EXTERNAL_STORAGE权限才能访问外部存储
        return readTextFile(filePath)
    }
}