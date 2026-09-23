package com.jwch.gwyt_project.util

import com.hjq.toast.ToastUtils
import com.jwch.gwyt_project.core.Config
import com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread
import java.io.File
import java.util.concurrent.Executors


class FileDeleteUtil {

    companion object {
        // 使用线程池提高效率
        private val executor = Executors.newFixedThreadPool(4)

        // 预定义的文件类型前缀 - 同时包含旧规则和新规则
        private val FILE_TYPE_PREFIXES = listOf(
            // 旧规则前缀
            "_整改前图片",
            "_整改后图片",
            "_可能存在的不认领文档_图片",
            // 新规则前缀
            "_前时相图片",
            "_后时相图片"
        )

        // 允许的文件扩展名
        private val ALLOWED_EXTENSIONS = listOf(".jpg", ".png", ".pdf", ".jpeg")

        /**
         * 删除单个或多个ID相关的文件
         * @param ids 要删除的ID列表，可以是单个ID或多个ID
         */
        fun deleteFilesByIds(ids: List<String>) {
            executor.execute {
                // 在后台线程执行文件查找
                val deleteList = getFilesByIds(ids)
                val successCount = deleteFiles(deleteList)

                runOnUiThread {
                    val message = if (ids.size == 1) {
                        "ID ${ids.first()} 删除完成: $successCount 成功, ${deleteList.size - successCount} 失败"
                    } else {
                        "多个ID删除完成: $successCount 成功, ${deleteList.size - successCount} 失败"
                    }
                    ToastUtils.show(message)
                }
            }
        }

        /**
         * 重载方法，支持单个ID删除
         * @param id 要删除的单个ID
         */
        fun deleteFilesByIds(id: String) {
            deleteFilesByIds(listOf(id))
        }

        /**
         * 获取多个ID相关的所有文件
         * @param ids ID列表
         * @return 文件列表
         */
        fun getFilesByIds(ids: List<String>): List<File> {
            return ids.flatMap { id ->
                FILE_TYPE_PREFIXES.flatMap { prefix ->
                    findFilesWithPrefix(Config.PHOTO_PATH, id + prefix, ALLOWED_EXTENSIONS)
                }
            }
        }

        /**
         * 优化后的文件查找方法 - 同时支持新旧两种文件名规则
         * @param directoryPath 目录路径
         * @param fileNamePrefix 文件名前缀
         * @param allowedExtensions 允许的扩展名列表
         * @return 匹配的文件列表
         */
        fun findFilesWithPrefix(
            directoryPath: String,
            fileNamePrefix: String,
            allowedExtensions: List<String> = ALLOWED_EXTENSIONS
        ): List<File> {
            val directory = File(directoryPath)

            // 检查目录是否存在
            if (!directory.exists() || !directory.isDirectory) {
                return emptyList()
            }

            // 使用序列提高效率
            return sequence {
                // 规则1: 查找不带序号的文件（新规则格式）
                allowedExtensions.forEach { ext ->
                    val fileWithoutNumber = File(directory, "$fileNamePrefix$ext")
                    if (fileWithoutNumber.exists() && fileWithoutNumber.isFile) {
                        yield(fileWithoutNumber)
                    }
                }

                // 规则2: 查找带序号的文件（旧规则格式，1-20）
                (1..20).forEach { i ->
                    allowedExtensions.forEach { ext ->
                        val fileWithNumber = File(directory, "${fileNamePrefix}_$i$ext")
                        if (fileWithNumber.exists() && fileWithNumber.isFile) {
                            yield(fileWithNumber)
                        }
                    }
                }
            }.toList()
        }

        /**
         * 删除文件列表中的所有文件
         * @param files 要删除的文件列表
         * @return 删除成功的文件数量
         */
        fun deleteFiles(files: List<File>): Int {
            return files.count { deleteFile(it) }
        }

        /**
         * 删除单个文件
         * @param file 要删除的文件
         * @return 是否删除成功
         */
        fun deleteFile(file: File): Boolean {
            return try {
                if (file.exists()) {
                    file.delete()
                } else {
                    true // 文件不存在视为删除成功
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        /**
         * 异步删除文件列表
         * @param files 要删除的文件列表
         * @param callback 删除结果回调
         */
        fun deleteFilesAsync(files: List<File>, callback: DeleteCallback? = null) {
            executor.execute {
                val successCount = deleteFiles(files)
                callback?.onDeleteCompleted(successCount, files.size - successCount)
            }
        }

        /**
         * 删除回调接口
         */
        interface DeleteCallback {
            fun onDeleteCompleted(successCount: Int, failedCount: Int)
        }

        // 扩展函数版本，可以直接在File对象上调用
        fun File.copyTo(target: File, overwrite: Boolean = true) {
            if (isDirectory) {
                target.mkdirs()
                listFiles()?.forEach { file ->
                    file.copyTo(File(target, file.name), overwrite)
                }
            } else {
                if (target.exists()) {
                    if (overwrite) {
                        target.delete()
                    } else {
                        return
                    }
                }

                inputStream().use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}