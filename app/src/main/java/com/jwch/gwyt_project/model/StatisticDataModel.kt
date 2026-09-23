package com.jwch.gwyt_project.model



class StatisticDataModel  {

    var title: String = ""
    var dataList = mutableListOf<StatisticModel>()

    constructor(title: String, dataList: MutableList<StatisticModel> ){
        this.title = title
        this.dataList = dataList
    }

}
