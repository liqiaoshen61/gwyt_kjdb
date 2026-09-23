package com.jwch.gwyt_project.i

interface GetDataListener {

    fun getData(data: Any?, type: String?)

    fun getDataError(
        t: Throwable?,
        strMsg: String?,
        type: String?
    )
}