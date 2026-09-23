package com.jwch.gwyt_project.util


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class WpsUtils2 {

    companion object {
        /**
         * 使用 WPS 打开文件
         * @param context 上下文
         * @param filePath 文件路径
         * @param fileProviderAuthority FileProvider 的 authority (需要在 AndroidManifest.xml 中配置)
         */
        fun openFileWithWps(context: Context, filePath: String, fileProviderAuthority: String) {
            // 检查 WPS 是否安装
            if (!isWpsInstalled(context)) {
                Toast.makeText(context, "未安装 WPS Office", Toast.LENGTH_SHORT).show()
                return
            }

            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                // 获取文件的 Uri (使用 FileProvider 以支持 Android 7.0+)
                val fileUri = FileProvider.getUriForFile(
                    context,
                    fileProviderAuthority,
                    file
                )

                val intent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    setDataAndType(fileUri, getMimeType(filePath))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK

                    // 指定 WPS 的包名
                    setPackage("cn.wps.moffice_eng")
                }

                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "打开文件失败", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 检查 WPS 是否安装
         */
        private fun isWpsInstalled(context: Context): Boolean {
            return try {
                val packageManager = context.packageManager
                packageManager.getPackageInfo("cn.wps.moffice_eng", PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        /**
         * 根据文件后缀获取 MIME 类型
         */
        private fun getMimeType(filePath: String): String {
            val extension = filePath.substringAfterLast('.', "").lowercase()
            return when (extension) {
                "doc", "docx" -> "application/msword"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                "pdf" -> "application/pdf"
                "txt" -> "text/plain"
                else -> "*/*"
            }
        }
    }
}