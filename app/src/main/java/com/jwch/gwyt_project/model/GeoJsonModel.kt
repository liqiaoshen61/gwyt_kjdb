package com.jwch.gwyt_project.model

import android.graphics.Color
import com.esri.arcgisruntime.geometry.Geometry
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.util.EsriGeoJsonUtil
import org.json.JSONObject
import java.io.File

class GeoJsonModel {

    var name = ""//文件名
    var ext = ""//文件后缀
    var fileSizeStr = ""//文件大小（可读格式）
    var fileSizeLong = 0L//文件大小（字节）
    var filePath = ""//文件路径
    var select = false
    var geometryJson = ""
    var featureCount = 0//特征数量

    private var featureList: MutableList<GeoJsonFeature>? = null
    private var dataLoaded = false

    constructor(file: File) {
        this.name = file.name
        this.ext = getFileExt()
        this.fileSizeLong = file.length()
        this.fileSizeStr = formatFileSize(file.length())
        this.filePath = file.absolutePath
        loadInfo()
    }

    private fun loadInfo() {
        try {
            val geoJson = File(filePath).readText()
            val result = CommonUtil.makeJSON(geoJson)
            val featuresArray = CommonUtil.getJSONArray(result, "features")
            featureCount = featuresArray?.length() ?: 0
        } catch (e: Exception) {
            featureCount = 0
        }
    }

    fun getData(): MutableList<GeoJsonFeature>? {
        if (featureList != null) return featureList

        return try {
            val geoJson = File(filePath).readText()
            val result = CommonUtil.makeJSON(geoJson)
            val featuresArray = CommonUtil.getJSONArray(result, "features")

            if (featuresArray == null || featuresArray.length() == 0) {
                featureCount = 0
                featureList = mutableListOf()
                return featureList
            }

            featureCount = featuresArray.length()
            val list = mutableListOf<GeoJsonFeature>()

            for (index in 0 until featuresArray.length()) {
                try {
                    val item = CommonUtil.getJSONArrayItem(featuresArray, index)
                    val geometry = CommonUtil.getJSONObject(item, "geometry")
                    if (geometry != null) {
                        geometryJson = EsriGeoJsonUtil.geo2ersi(geometry.toString(), index.toString())
                        val geom = Geometry.fromJson(geometryJson)
                        val properties = CommonUtil.getJSONObject(item, "properties")
                        val propsMap = jsonObjectToMap(properties)
                        val style = buildStyleConfig(properties)
                        list.add(GeoJsonFeature(geom, style, propsMap))
                    }
                } catch (e: Exception) {
                    "GeoJSON 第${index}条geometry解析失败: ${e.message}".printMsg()
                }
            }

            featureList = list
            dataLoaded = true
            list
        } catch (e: Exception) {
            "GeoJSON 文件解析失败: ${e.message}".printMsg()
            featureList = mutableListOf()
            featureList
        }
    }

    /**
     * 从 feature 的 properties 中解析样式配置
     * 支持的属性: fillColor, fillOpacity, strokeColor, strokeWidth, radius
     */
    private fun buildStyleConfig(properties: JSONObject?): StyleConfig {
        val config = StyleConfig()
        config.pointSize = 10f // 默认点更大，避免太小看不见
        if (properties == null) return config

        // 填充颜色
        properties.optString("fillColor")?.takeIf { it.isNotEmpty() }?.let {
            try { config.fillColr = Color.parseColor(it) } catch (_: Exception) {}
        }
        // 填充透明度 (0~1)，叠加到 fillColor 的 alpha 通道
        if (properties.has("fillOpacity")) {
            try {
                val opacity = properties.optDouble("fillOpacity", 1.0).coerceIn(0.0, 1.0)
                val alpha = (opacity * 255).toInt()
                config.fillColr = (config.fillColr and 0x00FFFFFF) or (alpha shl 24)
            } catch (_: Exception) {}
        }
        // 边框颜色
        properties.optString("strokeColor")?.takeIf { it.isNotEmpty() }?.let {
            try { config.lineColor = Color.parseColor(it) } catch (_: Exception) {}
        }
        // 边框宽度
        if (properties.has("strokeWidth")) {
            try {
                config.lineSize = properties.optDouble("strokeWidth", 1.0).toFloat()
            } catch (_: Exception) {}
        }
        // 点半径
        if (properties.has("radius")) {
            try {
                config.pointSize = properties.optDouble("radius", 5.0).toFloat()
            } catch (_: Exception) {}
        }

        return config
    }

    /**
     * 将 JSONObject 转为 Map
     */
    private fun jsonObjectToMap(json: JSONObject?): Map<String, Any?> {
        if (json == null) return emptyMap()
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.opt(key)
        }
        return map
    }

    private fun getFileExt(): String {
        val lastIndexOf = this.name.lastIndexOf(".")
        if (lastIndexOf < 0) return ""
        return this.name.substring(lastIndexOf)
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            else -> "${"%.1f".format(size.toDouble() / (1024 * 1024))}MB"
        }
    }
}