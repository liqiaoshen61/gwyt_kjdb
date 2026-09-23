package com.jwch.gwyt_project.util.download

import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection
import java.net.URL

internal class NoEtagFileDownloadUrlConnection : FileDownloadUrlConnection {
    constructor(originUrl: String?, configuration: Configuration?) : super(originUrl, configuration) {}
    constructor(url: URL?, configuration: Configuration?) : super(url, configuration) {}
    constructor(originUrl: String?) : super(originUrl) {}

    override fun addHeader(name: String, value: String) {
        if ("If-Match" == name) {
            return
        }
        super.addHeader(name, value)
    }
}