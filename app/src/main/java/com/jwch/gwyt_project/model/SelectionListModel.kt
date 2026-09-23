package com.jwch.gwyt_project.model

import com.jameni.jamenidialoglib.i.SelectionItemModel
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self

class SelectionListModel : SelectionItemInterface {

    var key: String? = null
    var value: String
    var data : Any? = null
    var select = false





    constructor(key: String?, value: String) {
        this.key = key
        this.value = value
    }

    constructor(value: String) {
        this.value = value
        this.key = value
    }


    constructor(key: String?, value: String,data :Any) {
        this.key = key
        this.value = value
        this.data = data
    }

    override fun getText(): String {
        return value
    }



    var txtSize: Float? = 14F
    var bgColorResId: Int? = null
    var txtColorResId: Int? = R.color.txt_black

    override fun getTextSize(): Float? = txtSize

    override fun getTextColorResId(): Int? = txtColorResId

    override fun getBackgroundColorResId(): Int? = bgColorResId
}