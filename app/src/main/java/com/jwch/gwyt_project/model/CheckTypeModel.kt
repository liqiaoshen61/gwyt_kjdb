package com.jwch.gwyt_project.model

/**
 * 字典模型
 */
open class CheckTypeModel {
    var name: String? = null//父数据 键
    var value: String? = null //值

    constructor() {}

    constructor(name: String?, value: String?) {
        this.name = name
        this.value = value
    }


}