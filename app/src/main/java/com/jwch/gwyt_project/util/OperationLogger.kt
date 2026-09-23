package com.jwch.gwyt_project.util

import android.content.Context
import com.jwch.gwyt_project.core.Config
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object OperationLogger {

    private const val FILE_SUFFIX = "操作记录.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 记录用户操作
     * @param context 上下文对象
     * @param operation 操作内容
     */
    fun logOperation(context: Context, operation: String) {
        // 在IO线程执行文件操作，避免阻塞主线程
        kotlin.runCatching {
            val currentTime = Date()
            val operationInfo = "${dateFormat.format(currentTime)} $operation"

            // 获取文件存储目录
            val filesDir = File(Config.LOG_PATH)

            val fileName = "${fileNameFormat.format(currentTime)}$FILE_SUFFIX"
            if (!filesDir.exists()) {
                filesDir.mkdirs()
            }
            val logFile = File(filesDir, fileName)

            var content = "$operationInfo\n".toByteArray()

            if(operation.contains("软件启动")){
                content = "\n$operationInfo\n".toByteArray()
            }

            // 写入文件（追加模式）
            FileOutputStream(logFile, true).use { fos ->
                fos.write(content)
            }
        }.onFailure {
            // 处理可能的异常（如存储权限问题等）
            it.printStackTrace()
        }
    }

}