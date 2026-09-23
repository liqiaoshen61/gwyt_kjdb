package com.jwch.gwyt_project.util

import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.db.DbUtil
import com.jwch.gwyt_project.Info.EMapsInfo
import java.io.File

/**
 * 本地必要文件检查工具类
 */
object FileCheckUtil {

    /**
     * 检查应用必需的本地文件是否存在
     * @return 缺失文件列表
     */
    fun checkRequiredFiles(): List<String> {
        val missingFiles = mutableListOf<String>()

        // 检查AppDb数据库
        if (!isFileExists(Config.APPDB_PATH, "AppDb.db")) {
            missingFiles.add("AppDb.db")
        }

        return missingFiles
    }

    /**
     * 检查指定路径下的文件是否存在
     */
    private fun isFileExists(dirPath: String, fileName: String): Boolean {
        val dir = File(dirPath)
        if (!dir.exists()) {
            return false
        }
        val file = File(dir, fileName)
        return file.exists()
    }

    /**
     * 检查应用数据目录是否存在
     */
    fun checkAppDataDir(): Boolean {
        val dir = File(Config.HEAD_FILE_PATH)
        return dir.exists() && dir.isDirectory
    }

    /**
     * 检查应用数据库目录是否存在
     */
    fun checkAppDbDir(): Boolean {
        val dir = File(Config.APPDB_PATH)
        return dir.exists() && dir.isDirectory
    }

    /**
     * 检查数据库中是否有底图数据
     * @return true 如果有数据，false 如果没有数据或查询失败
     */
    fun checkHasBaseMapData(): Boolean {
        return try {
            val vector = DbUtil.db.appDb.selector(EMapsInfo::class.java).where("BaseMapType", "=", 1).findFirst()
            vector != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取缺失文件的详细描述信息
     */
    fun getMissingFilesDescription(): String {
        val missingFiles = checkRequiredFiles()
        if (missingFiles.isEmpty()) {
            return ""
        }
        return buildString {
            append("缺少必要的文件：\n")
            missingFiles.forEachIndexed { index, file ->
                append("${index + 1}. $file\n")
            }
            append("\n请检查OfficeMap/AppDb文件夹后重试。")
        }
    }
}