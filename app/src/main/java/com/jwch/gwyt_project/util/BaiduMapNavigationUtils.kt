package com.jwch.gwyt_project.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.esri.arcgisruntime.geometry.Point
import kotlin.math.*

/**
 * 百度地图导航工具类
 * 支持CGCS2000坐标系到百度坐标系的转换，并跳转到百度地图进行导航
 */
object BaiduMapNavigationUtils {

    // 百度地图包名，用于检查是否安装百度地图
    private const val BAIDU_MAP_PACKAGE_NAME = "com.baidu.BaiduMap"

    // 百度地图Web版URL
    private const val BAIDU_MAP_WEB_URL = "https://api.map.baidu.com/direction"

    // 百度地图App调起URI
    private const val BAIDU_MAP_APP_URI = "baidumap://map/direction"

    // 坐标转换相关常量
    private const val PI = 3.1415926535897932384626
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323

    /**
     * 跳转到百度地图导航
     * @param context 上下文
     * @param startPoint 起点坐标(CGCS2000)
     * @param endPoint 终点坐标(CGCS2000)
     * @param startName 起点名称(可选)
     * @param endName 终点名称(可选)
     */
    fun navigateToBaiduMap(
        context: Context,
        startPoint: Point?,
        endPoint: Point,
        startName: String = "我的位置",
        endName: String = "目的地"
    ) {
        try {
            // 将CGCS2000坐标转换为百度坐标
            val baiduStartPoint = if (startPoint != null) {
                convertCGCS2000ToBaidu(startPoint.y, startPoint.x)
            } else {
                null
            }

            val baiduEndPoint = convertCGCS2000ToBaidu(endPoint.y, endPoint.x)

            // 尝试使用百度地图App导航
            if (openBaiduMapApp(context, baiduStartPoint, baiduEndPoint, startName, endName)) {
                return
            }

            // 如果百度地图未安装，使用Web版百度地图
            openBaiduMapWeb(context, baiduStartPoint, baiduEndPoint, startName, endName)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "导航启动失败，请检查百度地图是否安装", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 使用百度地图App进行导航
     */
    private fun openBaiduMapApp(
        context: Context,
        startPoint: BaiduCoord?,
        endPoint: BaiduCoord,
        startName: String,
        endName: String
    ): Boolean {
        // 检查百度地图是否安装
        if (!isBaiduMapInstalled(context)) {
            return false
        }

        try {
            val uriBuilder = StringBuilder(BAIDU_MAP_APP_URI)

            // 添加起点（如果提供）
            startPoint?.let {
                uriBuilder.append("?origin=name:${Uri.encode(startName)}|latlng:${it.lat},${it.lng}")
            }

            // 添加终点
            if (startPoint != null) {
                uriBuilder.append("&destination=name:${Uri.encode(endName)}|latlng:${endPoint.lat},${endPoint.lng}")
            } else {
                uriBuilder.append("?destination=name:${Uri.encode(endName)}|latlng:${endPoint.lat},${endPoint.lng}")
            }

            // 设置导航模式：driving（驾车）、transit（公交）、walking（步行）、riding（骑行）
            uriBuilder.append("&mode=driving")

            // 调起百度地图
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriBuilder.toString()))
            intent.setPackage(BAIDU_MAP_PACKAGE_NAME)
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 使用Web版百度地图进行导航
     */
    private fun openBaiduMapWeb(
        context: Context,
        startPoint: BaiduCoord?,
        endPoint: BaiduCoord,
        startName: String,
        endName: String
    ) {
        try {
            val uriBuilder = StringBuilder(BAIDU_MAP_WEB_URL)

            // 添加起点和终点坐标
            startPoint?.let {
                uriBuilder.append("?origin=${it.lat},${it.lng}")
            } ?: run {
                uriBuilder.append("?origin=") // 不设置起点，使用当前位置
            }

            uriBuilder.append("&destination=${endPoint.lat},${endPoint.lng}")

            // 添加起点和终点名称
            uriBuilder.append("&origin_region=${Uri.encode(startName)}")
            uriBuilder.append("&destination_region=${Uri.encode(endName)}")

            // 设置导航模式
            uriBuilder.append("&mode=driving")

            // 输出类型为html
            uriBuilder.append("&output=html")

            // 调起浏览器打开Web版百度地图
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriBuilder.toString()))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "无法打开百度地图", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 检查百度地图是否已安装
     */
    private fun isBaiduMapInstalled(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(BAIDU_MAP_PACKAGE_NAME, 0) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 坐标转换：CGCS2000 -> WGS84 -> GCJ02 -> 百度坐标系
     */
    private fun convertCGCS2000ToBaidu(lat: Double, lng: Double): BaiduCoord {
        // 第一步：CGCS2000转WGS84（差异很小，通常可以忽略）
        val wgs84Lat = lat
        val wgs84Lng = lng

        // 第二步：WGS84转GCJ02（火星坐标系）
        val gcj02Coord = wgs84ToGcj02(wgs84Lat, wgs84Lng)

        // 第三步：GCJ02转百度坐标系
        return gcj02ToBd09(gcj02Coord.lat, gcj02Coord.lng)
    }

    /**
     * WGS84转GCJ02（火星坐标系）
     */
    private fun wgs84ToGcj02(lat: Double, lng: Double): GpsCoord {
        if (outOfChina(lat, lng)) {
            return GpsCoord(lat, lng)
        }

        var dLat = transformLat(lng - 105.0, lat - 35.0)
        var dLng = transformLng(lng - 105.0, lat - 35.0)

        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = sqrt(magic)

        dLat = (dLat * 180.0) / (A * (1 - EE) / (magic * sqrtMagic) * PI)
        dLng = (dLng * 180.0) / (A / sqrtMagic * cos(radLat) * PI)

        val mgLat = lat + dLat
        val mgLng = lng + dLng

        return GpsCoord(mgLat, mgLng)
    }

    /**
     * GCJ02转百度坐标系
     */
    private fun gcj02ToBd09(lat: Double, lng: Double): BaiduCoord {
        val z = sqrt(lng * lng + lat * lat) + 0.00002 * sin(lat * PI)
        val theta = atan2(lat, lng) + 0.000003 * cos(lng * PI)
        val bdLng = z * cos(theta) + 0.0065
        val bdLat = z * sin(theta) + 0.006
        return BaiduCoord(bdLat, bdLng)
    }

    /**
     * 判断坐标是否在中国境外
     */
    private fun outOfChina(lat: Double, lng: Double): Boolean {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
    }

    /**
     * 纬度转换
     */
    private fun transformLat(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(y * PI) + 40.0 * sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(y / 12.0 * PI) + 320 * sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    /**
     * 经度转换
     */
    private fun transformLng(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(x * PI) + 40.0 * sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(x / 12.0 * PI) + 300.0 * sin(x / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

    /**
     * GPS坐标数据类
     */
    private data class GpsCoord(val lat: Double, val lng: Double)

    /**
     * 百度坐标数据类
     */
    data class BaiduCoord(val lat: Double, val lng: Double)
}