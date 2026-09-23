package com.jwch.gwyt_project.adapter.ztt

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.loadable.LoadStatus
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.CatTypeInfo
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.Info.ThemesInfo
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.util.CryptoUtils


class ZttItem : BaseExpandNode {


    companion object {
        const val LEVEL1 = 0 //第一级 分类
        const val LEVEL2 = 1 //第二级 图层
        const val LEVEL3 = 2 //第三级 具体专题图层--废弃
    }

    var itemLevel: Int  //item级别
    override var childNode: MutableList<BaseNode>? = null


    var name: String? = null
    var progress = 100 //seekbar 进度条
    var isCheck = false
    var showOpactiy = false
    var id: String = ""//当前id
    var fid: String = "" //父id
    var levelIndex = 0//第几个层级
    var themesInfo: ThemesInfo? = null


    /**
     * 初始化分类数据时使用
     */
    constructor(data: CatTypeInfo) {
        this.itemLevel = LEVEL1
        this.levelIndex = data.levelIndex
        this.name = data.typeName
        this.fid = data.parentId
        this.id = data.id
        this.isExpanded = false
//        toString().printMsg()
    }

    /**
     * 初始化专题数据时使用
     */
    constructor(data: ThemesInfo, parentData: ZttItem) {
        this.itemLevel = LEVEL2
        this.levelIndex = parentData.levelIndex + 1
        this.name = data.themeName.self()
        this.fid = parentData.id
        this.id = data.id
        this.themesInfo = data
        this.isExpanded = false
        data.setData()
        data.loadGdbLayer()
        data.loadTileLayer()
        data.loadVectorTileLayer()

//        //判断是在线服务 还是离线数据
//        data.themeFilePath.startsWith("http").yes {
//            data.loadServiceLayer()
//        }.no {
//            data.loadGdbLayer()
//            data.loadTileLayer()
//        }

    }

    constructor(itemLevel: Int) {
        this.itemLevel = itemLevel
    }

    constructor(itemLevel: Int, childNode: List<BaseNode?>?, name: String?) {
        this.itemLevel = itemLevel
        this.name = name
    }


    /**
     * 添加子项
     */
    fun addChildNode(item: BaseNode) {

        if (childNode == null) {
            childNode = mutableListOf()
        }

        childNode!!.add(item)

    }

    override fun toString(): String {
        return "ZttItem(itemLevel=$itemLevel, name=$name, levelIndex=$levelIndex)"
    }

    var dataCount = 0

    fun queryThemeDataCount(eventStatusFilterParams : String,isQuestionFilterParams :String,  block: (Boolean) -> Unit) {


        if (themesInfo != null) {

            val realPath = CryptoUtils.decryptGeodatabase(themesInfo!!.getGdbPath())
            val geodatabase = Geodatabase(realPath)
            geodatabase.loadAsync()

            geodatabase.addDoneLoadingListener(Runnable {

                if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {

                    val tableList: List<GeodatabaseFeatureTable> = geodatabase.geodatabaseFeatureTables
                    if (CommonUtil.matchList(tableList)) {
                        val table = tableList[0]
                        table.loadAsync()
                        table.addDoneLoadingListener {
                            var queryDefinition = ""

                            val query = QueryParameters()
                            //先判断是否筛选了该参数
                            if(eventStatusFilterParams.isNotBlank()){
                                //其次判断图层是否存在该字段
                                if(checkField("事件状",table)){
                                    queryDefinition = "事件状 = '${eventStatusFilterParams}'"
                                }else{
                                    dataCount = 0
                                    "==2== ${themesInfo?.themeName} 没有事件状态字段".printMsg()
                                    block(true)
                                    return@addDoneLoadingListener
                                }
                            }

                            if(isQuestionFilterParams.isNotBlank()){

                                if(checkField("是否问",table)){
                                    if(queryDefinition.isNotBlank()){
                                        queryDefinition += "AND 是否问 = '${isQuestionFilterParams}'"
                                    }else{
                                        queryDefinition = "是否问 = '${isQuestionFilterParams}'"
                                    }
                                }else{
                                    dataCount = 0
                                    "==2== ${themesInfo?.themeName} 没有是否问字段".printMsg()
                                    block(true)
                                    return@addDoneLoadingListener
                                }
                            }

                            if(queryDefinition.isBlank()){
                                queryDefinition = "1 = 1"
                            }

                            query.whereClause = queryDefinition
                            // 查询要素总数
                            val queryResult = table.queryFeatureCountAsync(query)
                            queryResult?.addDoneListener {
                                dataCount = queryResult.get().toInt()
                                "${themesInfo?.themeName}===$dataCount".printMsg()
                                block(true)
                            }
                        }


                        //下面的方法已废弃 查询数量有专门的queryFeatureCountAsync 效率高 适用于只统计数量的情况
//                        val queryResult = table.queryFeaturesAsync(query)
//                        queryResult?.addDoneListener {
//                            val result = queryResult?.get()
//                            val iterator = result?.iterator()!!
//                            while (iterator.hasNext()!!) {
//
//                                val feature = iterator.next() ?: continue
//                                dataCount++
//
////                                val attr = feature.attributes
////                                "attrs: ${attr.toJson()}".printMsg()
//                            }
//                            block(true)
//                        }



                    }
                } else {
                    block(true)
                }
            })

        } else {
            block(true)
        }
    }

    private fun checkField(fieldName :String, table: GeodatabaseFeatureTable) : Boolean{

        return table.fields.any { field ->
            field.name.equals(fieldName, ignoreCase = true)
        }
    }


    var dataCountArea = 0 //数量
    var dataCountAreaFinish = 0 //已办结
    var dataCountAreaIsQuestion = 0 //是否问题：是问题
    var queryAttrList = mutableListOf<Map<String, Any>>()


    fun queryThemeDataCountByArea(areaData: DistrictsInfo, block: (Boolean) -> Unit) {
        dataCountArea = 0
        dataCountAreaFinish = 0
        dataCountAreaIsQuestion = 0
        queryAttrList.clear()

        val queryKey = when (areaData.distLevel) {
            DistrictsInfo.LEVEL_CITY -> "所在市"
            DistrictsInfo.LEVEL_COUNTY -> "所在县"
            DistrictsInfo.LEVEL_TOWN -> "所在镇"
            DistrictsInfo.LEVEL_VILLAGE -> ""
            else -> ""
        }

        if (themesInfo != null) {

            val realPath = CryptoUtils.decryptGeodatabase(themesInfo!!.getGdbPath())
            val geodatabase = Geodatabase(realPath)
            geodatabase.loadAsync()

            geodatabase.addDoneLoadingListener(Runnable {

                if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {


                    val tableList: List<GeodatabaseFeatureTable> = geodatabase.geodatabaseFeatureTables
                    if (CommonUtil.matchList(tableList)) {
                        try {
                            val table = tableList[0]
                            table.loadAsync()
                            table.addDoneLoadingListener {

                                if (table.fields.any { it.name == queryKey } || areaData.distLevel == DistrictsInfo.LEVEL_PROVINCE) {
                                    val query = QueryParameters()
//                                    query.whereClause = "1 = 1"
//                                    query.whereClause = "$queryKey = '${areaData.distName}'"
                                    if (areaData.distLevel != DistrictsInfo.LEVEL_PROVINCE) {
                                        query.whereClause = "$queryKey like '%${areaData.distName.substring(0, 2)}%'"
                                    }
                                    query.isReturnGeometry = false

//                                query.geometry = geometry
                                    val queryResult = table.queryFeaturesAsync(query)
                                    queryResult?.addDoneListener {
                                        val result = queryResult?.get()
                                        val iterator = result?.iterator()!!
                                        while (iterator.hasNext()!!) {
                                            val feature = iterator.next() ?: continue
                                            dataCountArea++
                                            "$name count ====$dataCountArea".printMsg()
                                            val attr = feature.attributes
                                            if (attr.get("事件状") == "已办结") {
                                                dataCountAreaFinish++
                                            }

                                            if (attr.get("是否问") == "是问题") {
                                                dataCountAreaIsQuestion++
                                            }
//                                "attrs: ${attr.toJson()}".printMsg()
                                            queryAttrList.add(attr)
                                        }
                                    }
                                }
                            }


                        } catch (e: ArcGISRuntimeException) {
                            "Failed to execute query: ${e.message}".printMsg()

                        }

                        block(true)
                    } else {
                        block(true)
                    }
                } else {
                    block(true)
                }
            })
        }
    }


}