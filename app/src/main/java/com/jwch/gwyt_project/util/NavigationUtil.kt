package com.jwch.gwyt_project.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.showSingleDialog
import java.net.URLEncoder
import androidx.core.net.toUri
import com.jwch.gwyt_project.model.NavigationPointModel
import org.json.JSONArray
import org.json.JSONObject

class NavigationUtil(context: Context) {
    val mContent = context

    private val packageName_gaode = "com.autonavi.minimap"
    private val packageName_baidu = "com.baidu.BaiduMap"

    fun startNavigation(pointModel: NavigationPointModel) {

        if (CommonUtil.isNotEmpty(pointModel.name) && pointModel.latBd09 !=0.0 && pointModel.lngBd09 !=0.0) {

            if (isInstallApk(packageName_baidu)) {

                val intent = Intent()

                //路径规划
                intent.data = Uri.parse("baidumap://map/direction?destination=name:${pointModel.name}|latlng:${pointModel.latBd09},${pointModel.lngBd09}&coord_type=bd09ll&mode=driving&sy=3&index=0&target=1&src=andr.hhz.ningxia")

                mContent.startActivity(intent)

            } else if (isInstallApk(packageName_gaode)) {
//                val uri = Uri.parse("amapuri://route/plan/?dlat=$destinationLat&dlon=$destinationLng&dname=$destinationName&dev=0&t=0")
//                mContent.startActivity(Intent(Intent.ACTION_VIEW, uri))

                //路线规划
                val uriPlan = Uri.parse("androidamap://navi?sourceApplication=appname&poiname=${pointModel.name}&lat=${pointModel.latBd09}&lon=${pointModel.lngBd09}&dev=0&style=0")

                //直接导航
//                val uriNavi =
//                    Uri.parse("androidamap://navi?sourceApplication=appname&poiname=$destinationName&lat=$destinationLat&lon=$destinationLng&dev=0&style=0")

                val uriNavi = Uri.parse("androidamap://navi?sourceApplication=appname&poiname=${pointModel.name}&lat=${pointModel.latGcj02}&lon=${pointModel.lngGcj02}&dev=0&style=0")

                val it = Intent(Intent.ACTION_VIEW, uriNavi)
                it.setPackage(packageName_gaode)
                it.addCategory(Intent.CATEGORY_DEFAULT)
                mContent.startActivity(it)
            } else {
                showSingleDialog(mContent, "请安装百度地图或高德地图手机版后开始导航")
            }
        } else {
            showSingleDialog(mContent, "导航信息有误")
        }
    }


    //带有途经点的导航 因此需要传入多个点
    // startEqualEnd 为true 起点=终点  可以理解为包含返程的导航 途经点才是真正的目的地
    fun startNavigation2(pointList: MutableList<NavigationPointModel>, startEqualEnd :Boolean = false) {

        if (CommonUtil.matchList(pointList)) {

            if (isInstallApk(packageName_baidu)) {
                var viaEncoded = ""
                //把列表的最后一个点作为目的地
                var destinationPoint = pointList.removeAt(pointList.lastIndex)

                //去掉最后一个item 如果还有 就需要处理途径点信息
                if (CommonUtil.matchList(pointList)) {
                    val viaArray = JSONArray()
                    pointList.forEach {
                        val obj = JSONObject()
                        obj.put("name", it.name)
                        obj.put("lat", it.latBd09)
                        obj.put("lng", it.lngBd09)
                        viaArray.put(obj)
                    }
                    // 外层 viaPoints 对象
                    val viaObject = JSONObject()
                    viaObject.put("viaPoints", viaArray)

                    viaEncoded = URLEncoder.encode(viaObject.toString(), "UTF-8")
                }


                val intent = Intent()

                val uriBuilder = StringBuilder()
                uriBuilder.append("baidumap://map/direction?")
                if(startEqualEnd){
                    uriBuilder.append("origin=${destinationPoint.latBd09},${destinationPoint.lngBd09}")
                }
                uriBuilder.append("&destination=${destinationPoint.latBd09},${destinationPoint.lngBd09}")
                uriBuilder.append("&mode=driving")
                uriBuilder.append("&coord_type=bd09ll")
                if (viaEncoded.isNotBlank()) {
                    uriBuilder.append("&viaPoints=$viaEncoded")
                }
                uriBuilder.append("&src=andr.hhz.ningxia")
                val uriString = uriBuilder.toString()


                intent.data = uriString.toUri()

                mContent.startActivity(intent)

            }else if (isInstallApk(packageName_gaode)) {

                // 把列表的最后一个点作为目的地
                var destinationPoint = pointList.removeAt(pointList.lastIndex)

                val uriBuilder = StringBuilder()
                uriBuilder.append("amapuri://route/plan/?")
                if (startEqualEnd) {
                    uriBuilder.append("sname=${destinationPoint.name}")
                    uriBuilder.append("&slat=${destinationPoint.latGcj02}&slon=${destinationPoint.lngGcj02}")
                }
                uriBuilder.append("&dlat=${destinationPoint.latGcj02}&dlon=${destinationPoint.lngGcj02}")
                uriBuilder.append("&dev=0")  // 开启导航时，选择0：不偏航，1：偏航
                uriBuilder.append("&t=0")  // 0：驾车，1：骑行，2：步行，3：公交
                if (CommonUtil.matchList(pointList)) {
                    uriBuilder.append("&vian=${pointList.size}")  //途经点的数量
                    val vialats = pointList.joinToString("|") { it.latGcj02.toString() }
                    val vialons = pointList.joinToString("|") { it.lngGcj02.toString() }
                    val vianames = pointList.joinToString("|") { it.name }
                    uriBuilder.append("&vialats=${vialats}")
                    uriBuilder.append("&vialons=${vialons}")
                    uriBuilder.append("&vianames=${vianames}")
                }
                uriBuilder.append("&sourceApplication=andr.hhz.ningxia")
                val uriString = uriBuilder.toString().toUri()

                // 打印URI调试
                Log.d("GaodeUri", "Generated URI: $uriString")


                val it = Intent(Intent.ACTION_VIEW, uriString)
                it.setPackage(packageName_gaode)
                it.addCategory(Intent.CATEGORY_DEFAULT)
                mContent.startActivity(it)

            }
            else {
                showSingleDialog(mContent, "请安装百度地图或高德地图手机版后开始导航")

            }
        } else {
            showSingleDialog(mContent, "导航信息有误")
        }
    }








    private fun isInstallApk(name: String): Boolean {
        //如果获取不到百度地图包名，需要到manifest中加入QUERY_ALL_PACKAGES权限
        val packages = mContent.packageManager.getInstalledPackages(0)
        packages.forEach {
            it.packageName.printMsg()
            if (it.packageName == name) {
                return true
            }
        }
        return false
    }


}