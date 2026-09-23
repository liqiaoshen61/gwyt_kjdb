package com.jwch.gwyt_project.util

import android.content.Context
import java.io.*
import java.util.concurrent.Executors

class FileCopyHelper(private val context: Context) {

    // 定义回调接口
    interface CopyCallback {
        fun onStart(totalFiles: Int, totalBytes: Long)
        fun onProgress(progress: Int, copiedBytes: Long, totalBytes: Long, currentFile: String)
        fun onComplete(success: Boolean, message: String)
        fun onError(error: String)
    }

    // 使用线程池执行复制任务
    private val executor = Executors.newSingleThreadExecutor()

    // 取消标志
    private var isCancelled = false

    fun copyFolderWithProgress(
        sourcePath: String,
        destinationPath: String,
        callback: CopyCallback
    ) {
        isCancelled = false

        executor.execute {
            try {
                val source = File(sourcePath)
                val destination = File(destinationPath)

                // 检查源文件夹是否存在
                if (!source.exists() || !source.isDirectory) {
                    callback.onError("源文件夹不存在或不是目录")
                    return@execute
                }

                // 获取所有文件列表并计算总大小
                val fileList = mutableListOf<File>()
                val totalBytes = calculateTotalSize(source, fileList)
                val totalFiles = fileList.size

                // 通知开始
                callback.onStart(totalFiles, totalBytes)

                // 如果目标文件夹不存在，则创建
                if (!destination.exists()) {
                    destination.mkdirs()

                }

                var copiedBytes = 0L
                var copiedFiles = 0

                // 复制每个文件
                for (file in fileList) {
                    if (isCancelled) {
                        callback.onComplete(false, "操作已取消")
                        return@execute
                    }

                    val relativePath = getRelativePath(source, file)
                    val targetFile = File(destination, relativePath)

                    // 确保目标目录存在
                    targetFile.parentFile?.mkdirs()

                    // 通知当前正在复制的文件
                    callback.onProgress(
                        calculateProgress(copiedBytes, totalBytes),
                        copiedBytes,
                        totalBytes,
                        file.name
                    )

                    // 复制文件
                    val fileSize = copyFileWithProgress(file, targetFile) { bytesCopied ->
                        if (!isCancelled) {
                            val currentProgress = calculateProgress(copiedBytes + bytesCopied, totalBytes)
                            callback.onProgress(
                                currentProgress,
                                copiedBytes + bytesCopied,
                                totalBytes,
                                file.name
                            )
                        }
                    }

                    copiedBytes += fileSize
                    copiedFiles++

                    // 通知文件复制完成
                    val progress = calculateProgress(copiedBytes, totalBytes)
                    callback.onProgress(progress, copiedBytes, totalBytes, "已完成: ${file.name}")
                }

                // 通知完成
                callback.onComplete(true, "复制完成，共复制 $copiedFiles 个文件")

            } catch (e: Exception) {
                e.printStackTrace()
                callback.onError("复制过程中发生错误: ${e.message}")
            }
        }
    }

    // 计算总大小和文件列表
    private fun calculateTotalSize(dir: File, fileList: MutableList<File>): Long {
        var totalSize = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                totalSize += calculateTotalSize(file, fileList)
            } else {
                fileList.add(file)
                totalSize += file.length()
            }
        }
        return totalSize
    }

    // 复制单个文件并报告进度
    private fun copyFileWithProgress(
        sourceFile: File,
        destFile: File,
        progressCallback: (Long) -> Unit
    ): Long {
        var totalBytesCopied = 0L
        val totalSize = sourceFile.length()

        FileInputStream(sourceFile).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                val buffer = ByteArray(8 * 1024) // 8KB缓冲区
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                    if (isCancelled) {
                        break
                    }

                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesCopied += bytesRead

                    // 回调进度
                    progressCallback(totalBytesCopied)
                }
            }
        }

        return totalSize
    }

    // 计算相对路径
    private fun getRelativePath(base: File, file: File): String {
        val basePath = base.absolutePath
        val filePath = file.absolutePath

        return if (filePath.startsWith(basePath)) {
            filePath.substring(basePath.length).let {
                if (it.startsWith("/")) it.substring(1) else it
            }
        } else {
            file.name
        }
    }

    // 计算进度百分比
    private fun calculateProgress(copied: Long, total: Long): Int {
        return if (total > 0) {
            (copied * 100 / total).toInt()
        } else {
            100
        }
    }

    // 取消复制操作
    fun cancel() {
        isCancelled = true
    }
}