package com.jwch.gwyt_project.model

import com.jameni.allutillib.common.PrintUtil
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.self

class AppUpdateModel {
    var downloadUrl: String? = null // 下载链接
    var updateContent: String? = null//更新内容
    var updateStatus: String? = null//


    fun getUrl(): String {
        var url = ""
        if (downloadUrl.self().isNotEmpty()) {
            if (downloadUrl.self().startsWith("http")) {
                url = downloadUrl.self()
            } else {
                url = Config.requestUrl + downloadUrl
            }
        }

        PrintUtil.printMsg("apk下载地址==$url")
        return url
    }
}
