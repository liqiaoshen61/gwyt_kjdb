package com.jwch.gwyt_project.util.download

import com.jameni.allutillib.common.PrintUtil
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.util.NumFormatUtil
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadSampleListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.util.FileDownloadUtils
import java.io.File

class DownloadTaskUtil(val url: String, var mSinglePath: String, var isCustomHeader: Boolean = false, downloadListener: DownloadTaskListener) {
    val listener: DownloadTaskListener = downloadListener

    var taskId = -1//下载任务id
    var downloadProgress: Double = 0.00
    var currentStatus = DOWNLOAD_NONE

    companion object {
        val DOWNLOAD_NONE = 0//还没开始
        val DOWNLOAD_START = 1 //刚开始
        val DOWNLOAD_PROCESSING = 2//正在下载
        val DOWNLOAD_PAUSE = 3//暂停
        val DOWNLOAD_STOP = 4//停止
        val DOWNLOAD_COMPLETE = 5//完成
        val DOWNLOAD_ERROR = 6//异常

        val ACTION_DOWNLOAD = "下载"
        val ACTION_DOWNLOADING = "正在下载"
        val ACTION_REDOWNLOAD = "继续下载"
        val ACTION_DOWNLOAD_FINISH = "已下载"
        val ACTION_LOADMAP = "已完成"
    }

    fun createDownloadTask(): BaseDownloadTask {

//        val token =
//            if (AppContext.app == null || AppContext.app.getUser() == null || !AppContext.app.getUser()!!.access_token.isNotEmpty()) {
//                ""
//            } else {
//                AppContext.app.getUser()!!.access_token
//            }
//        var token_type = if (AppContext.app == null || AppContext.app.getUser() == null || !AppContext.app.getUser()!!.token_type.isNotEmpty()) {
//            ""
//        } else {
//            AppContext.app.getUser()!!.token_type
//        }


        var task: BaseDownloadTask = FileDownloader.getImpl().create(url)
            .setPath(mSinglePath)
            .setCallbackProgressTimes(300)
            .setMinIntervalUpdateSpeed(400)
            .setTag("")

//        if (isCustomHeader) {
//            task.addHeader("Accept", "*/*")
//                .addHeader("Content-Type", "application/json;charset:utf-8")
//                .addHeader("Blade-Auth", "$token_type $token")
//                .addHeader("Accept-Language", "zh-CN,zh;g=0.9")
//                .addHeader("Authorization", "Basic c2FiZXI6c2FiZXJfc2VjcmV0")
//                .addHeader("Tenant-Id", "000000")
//        }

        task.setListener(object : FileDownloadSampleListener() {

            override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                print("pending taskId:" + task?.id + ",soFarBytes:" + soFarBytes + ",totalBytes:" + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes)
                currentStatus = DOWNLOAD_START
                listener.downloadStatusListener(task, currentStatus)
            }

            override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
//                    print("progress taskId:" + task?.id + ",soFarBytes:" + soFarBytes + ",totalBytes:" + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes + ",speed:" + task?.speed)
                val currentProgress = (soFarBytes * 1.0 / totalBytes) * 100
                downloadProgress = NumFormatUtil.formatFloat(currentProgress).toDouble()
                print("进度：$downloadProgress   当前下载速度：${task?.speed}")
                currentStatus = DOWNLOAD_PROCESSING
                listener.downloadStatusListener(task, currentStatus)
            }

            override fun blockComplete(task: BaseDownloadTask?) {
                print("blockComplete taskId:" + task?.id + ",filePath:" + task?.path + ",fileName:" + task?.filename + ",speed:" + task?.speed + ",isReuse:" + task?.reuse())
                currentStatus = DOWNLOAD_COMPLETE
                print("[状态]" + FileDownloader.getImpl().getStatus(url, mSinglePath).toInt())
                listener.downloadStatusListener(task, currentStatus)
            }

            override fun completed(task: BaseDownloadTask?) {
                print("completed taskId:" + task?.id + ",isReuse:" + task?.reuse())
                currentStatus = DOWNLOAD_COMPLETE
                listener.downloadStatusListener(task, currentStatus)
            }

            override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                print("paused taskId:" + task?.id + ",soFarBytes:" + soFarBytes + ",totalBytes:" + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes)
                currentStatus = DOWNLOAD_PAUSE
                listener.downloadStatusListener(task, currentStatus)

            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                print("error taskId:" + task?.id + ",e:" + e?.localizedMessage)
                currentStatus = DOWNLOAD_ERROR
                listener.downloadStatusListener(task, currentStatus)
            }

            override fun warn(task: BaseDownloadTask?) {
                print("warn taskId:" + task?.id)
            }
        })

        return task
    }


    //开始下载任务
    fun startTask() {
        taskId = createDownloadTask().start()
    }

    //暂停下载任务
    fun pauseTask() {
        if (taskId == -1) return
        FileDownloader.getImpl().pause(taskId)
    }

    fun deleteTask(): Boolean {
        //删除任务之前要先暂停，不然没效果
        if (currentStatus == DOWNLOAD_PROCESSING) {
            pauseTask()
        }
        if (currentStatus != DOWNLOAD_PROCESSING) {
            File(mSinglePath).delete()
            File(FileDownloadUtils.getTempPath(mSinglePath)).delete()
            return true
        }
        return false

    }

    fun print(msg: String) {
        PrintUtil.printMsg(msg)
    }


    fun getStatus(): String {

        val status = FileDownloader.getImpl().getStatus(url, mSinglePath).toInt()
        return when (status) {
            0 -> "未下载"
            1 -> "准备下载"
            3 -> "下载中"
            -1 -> "下载出错"
            -2 -> "暂停"
            4 -> "已完成"
            -3 -> "已完成"
            else -> ""
        }
    }

    fun getActionFromStatus(): String {

        return when (getStatus()) {
            "未下载" -> ACTION_DOWNLOAD
            "下载中" -> ACTION_DOWNLOADING
            "暂停", "下载出错" -> ACTION_REDOWNLOAD
            "已完成" -> ACTION_LOADMAP
            else -> ""
        }
    }
}