package com.jwch.gwyt_project.model


class StatisticModel {

    var type: String = ""  // 分类使用的type
    var count: Int = 0    // 该分类的数量
    var countFinish: Int = 0    // 该分类的数量 (已完成的)

    constructor(type: String, count: Int) {
        this.type = type
        this.count = count
    }

    constructor(type: String, count: Int, countFinish: Int) {
        this.type = type
        this.count = count
        this.countFinish = countFinish
    }

}
