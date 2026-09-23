package com.jwch.gwyt_project.model

/**
 * 字典模型
 */
class KVModel {
    var pKey: String? = null//父数据 键
    var key: String? = null//键
    var value: String? = null //值
    var subList: MutableList<KVModel>? = null //子数组

    constructor() {}
    constructor(value: String?) {
        this.value = value
    }

    constructor(key: String?, value: String?) {
        this.key = key
        this.value = value
    }


}