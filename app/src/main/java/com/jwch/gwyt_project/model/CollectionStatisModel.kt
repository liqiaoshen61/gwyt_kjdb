package com.jwch.gwyt_project.model

import com.jwch.gwyt_project.Info.MarkerInfo


class CollectionStatisModel {

    var type: String? = null//父数据 键
    var list: MutableList<MarkerInfo>? = null //值

    constructor() {}

    constructor(type: String?,list: MutableList<MarkerInfo>?) {
        this.type = type
        this.list = list
    }


}