package com.jwch.gwyt_project.util.download

import com.liulishuo.filedownloader.BaseDownloadTask

interface DownloadTaskListener {
    fun downloadStatusListener(task: BaseDownloadTask?, status: Int)
}