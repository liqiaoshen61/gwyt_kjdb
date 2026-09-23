package com.jwch.gwyt_project.util.excel

class TableDataModel  {

    var key: String? = null
    var value: String? = null
    var value2: String? = null
    var isSelect = false



    constructor (key: String?, value: String?) {
        this.key = key
        this.value = value

    }

    constructor (key: String?, value: String?, value2: String?) {
        this.key = key
        this.value = value
        this.value2 = value2

    }

    constructor (key: String?, value: String?, isSelect :Boolean) {
        this.key = key
        this.value = value
        this.isSelect = isSelect

    }

    constructor (key: String?, value: String?, value2: String?,isSelect :Boolean) {
        this.key = key
        this.value = value
        this.value2 = value2
        this.isSelect = isSelect

    }
}


