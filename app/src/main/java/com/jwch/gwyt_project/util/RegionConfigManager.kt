package com.jwch.gwyt_project.util

import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.printMsg

object RegionConfigManager {

    // 当前选择的地区
    private var currentRegion: String = ""


    // 地区配置数据类
    data class RegionConfig(
        val areaCode: String,     // 区域代码
        val areaLevel: Int        // 地区等级 1省 2市 3县
    )

    // 地区配置映射表 有新的地方就往里面加
    private val regionMap = mapOf(
        //省级
        "福建省" to RegionConfig("35", 1),
        //市级
        "福州市" to RegionConfig("3501", 2),
        "漳州市" to RegionConfig("3506", 2),
        "龙岩市" to RegionConfig("3508", 2),
        "南平市" to RegionConfig("3507", 2),
        "三明市" to RegionConfig("3504", 2),
        //县级
        "永春县" to RegionConfig("350525", 3), //泉州永春
        "闽清县" to RegionConfig("350124", 3),  //福州闽清
        "光泽县" to RegionConfig("350723", 3), //南平光泽
        "长汀县" to RegionConfig("350821", 3),  //龙岩长汀
        "闽侯县" to RegionConfig("350121", 3),  //福州闽侯
        "明溪县" to RegionConfig("350421", 3), //三明明溪
        "漳平市" to RegionConfig("350881", 3), //龙岩漳平
        "仙游县" to RegionConfig("350322", 3),  //莆田仙游
        "龙海区" to RegionConfig("350681", 3),  //漳州龙海
        "泰宁县" to RegionConfig("350429", 3),  //
        "永安市" to RegionConfig("350481", 3),  //
        "将乐县" to RegionConfig("350428", 3),  //
    )

    // 获取当前地区名称
    fun getCurrentRegionName(): String = currentRegion

    // 获取当前地区配置
    fun getCurrentConfig(): RegionConfig {
        return regionMap[currentRegion] ?: throw IllegalArgumentException("未知地区: $currentRegion")
    }

    // 设置当前地区
    fun setRegion(regionName: String) {
        if (regionMap.containsKey(regionName)) {
            currentRegion = regionName
            val config = getCurrentConfig()
            Config.AreaCode = config.areaCode
            Config.userLevel = config.areaLevel

        } else {
            currentRegion = "福建省"
            val config = getCurrentConfig()
            Config.AreaCode = config.areaCode
            Config.userLevel = config.areaLevel
        }
    }


    // 直接获取特定地区的配置
    fun getConfigForRegion(regionName: String): RegionConfig {
        return regionMap[regionName] ?: throw IllegalArgumentException("未知地区: $regionName")
    }

}