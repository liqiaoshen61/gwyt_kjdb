package com.jwch.gwyt_project.util

import android.util.Log
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.ext.self



/**
 * 行政区划查询工具类
 */
class DistrictQueryHelper(private val allDistricts: List<DistrictsInfo>) {


    /**
     * 通过行政区划级别筛选
     * @param level 行政区划级别 (1-省, 2-市, 3-区县, 4-乡镇)
     * @return 对应级别的行政区划列表
     */
    fun getListByLevel(level: Int): MutableList<DistrictsInfo> {
        return allDistricts.filter { it.distLevel == level } as MutableList
    }

    /**
     * 通过行政区划代码筛选
     * @param code 行政区划代码
     * @return 匹配的行政区划列表
     */
    fun queryDistrictByCode(code: String): DistrictsInfo? {
        return allDistricts.firstOrNull { it.distCode == code }
    }

    /**
     * 通过行政区划名称筛选
     * @param name 行政区划名称
     * @param exactMatch 是否精确匹配，默认为false
     * @return 匹配的行政区划列表
     */
    fun getDistrictByName(name: String, fuzzyMatch: Boolean = false): DistrictsInfo? {
        return if (fuzzyMatch) {
            allDistricts.firstOrNull { it.distName.self().contains(name) }
        } else {
            allDistricts.firstOrNull { it.distName == name }
        }
    }

    /**
     * 获取指定行政区划代码的下一级行政区划
     * @param code 当前行政区划代码
     * @return 下一级行政区划列表
     */
    fun getNextLevelDistricts(code: String): MutableList<DistrictsInfo>? {
        // 首先查找当前行政区划
        val currentDistrict = allDistricts.find { it.distCode == code }

        if (currentDistrict == null) {
            Log.w("DistrictQueryHelper", "未找到行政区划代码: $code")
            return null
        }

        // 确定下一级行政区划的级别
        val nextLevel = currentDistrict.distLevel + 1



        // 根据当前行政区划级别确定筛选逻辑
        return when (currentDistrict.distLevel) {
            1 -> getListByLevel(nextLevel).filter { it.distCode.self().startsWith(code.substring(0, 2)) } as MutableList
            2 -> getListByLevel(nextLevel).filter { it.distCode.self().startsWith(code.substring(0, 4)) } as MutableList
            3 -> getListByLevel(nextLevel).filter { it.distCode.self().startsWith(code.substring(0, 6)) } as MutableList
            4 -> getListByLevel(nextLevel).filter { it.distCode.self().startsWith(code.substring(0, 9)) } as MutableList
            else -> null
        }
    }

    /**
     * 获取指定行政区划代码的所有上级行政区划
     * @param code 当前行政区划代码
     * @return 上级行政区划列表，从最高级到直接上级
     */
    fun getParentDistricts(code: String): List<DistrictsInfo> {
        val result = mutableListOf<DistrictsInfo>()

        when (code.length) {
            12 -> { // 乡镇级别
                // 获取区县
                val countyCode = code.substring(0, 6) + "000000"
                val county = allDistricts.find { it.distCode == countyCode }
                county?.let { result.add(it) }

                // 获取市
                val cityCode = code.substring(0, 4) + "00000000"
                val city = allDistricts.find { it.distCode == cityCode }
                city?.let { result.add(it) }

                // 获取省
                val provinceCode = code.substring(0, 2) + "0000000000"
                val province = allDistricts.find { it.distCode == provinceCode }
                province?.let { result.add(it) }
            }
            6 -> { // 区县级别
                // 获取市
                val cityCode = code.substring(0, 4) + "000000"
                val city = allDistricts.find { it.distCode == cityCode }
                city?.let { result.add(it) }

                // 获取省
                val provinceCode = code.substring(0, 2) + "00000000"
                val province = allDistricts.find { it.distCode == provinceCode }
                province?.let { result.add(it) }
            }
            4 -> { // 市级
                // 获取省
                val provinceCode = code.substring(0, 2) + "00000000"
                val province = allDistricts.find { it.distCode == provinceCode }
                province?.let { result.add(it) }
            }
            else -> {
                Log.w("DistrictQueryHelper", "不支持的行政区划代码格式: $code")
            }
        }

        return result.reversed() // 反转列表，使最高级在前面
    }

    /**
     * 获取完整的行政区划路径
     * @param code 当前行政区划代码
     * @return 从省到当前区的完整路径字符串
     */
    fun getFullDistrictPath(code: String): String {
        val parents = getParentDistricts(code)
        val current = allDistricts.find { it.distCode == code }

        return if (current != null) {
            (parents.map { it.distName } + current.distName).joinToString(" ")
        } else {
            parents.map { it.distName }.joinToString(" ")
        }
    }


}