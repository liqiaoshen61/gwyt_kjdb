package com.jwch.gwyt_project.util

import android.content.Context
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.PointModel
import com.jwch.gwyt_project.util.DrawUtil.Companion.DRAW_POINT
import com.jwch.gwyt_project.util.DrawUtil.Companion.DRAW_POLYGON
import com.jwch.gwyt_project.util.DrawUtil.Companion.DRAW_POLYLINE
import com.jwch.gwyt_project.util.DrawUtil.Companion.DRAW_TEXT

open class MarkerUtil(layer: GraphicsOverlay, mapView: MapView, context: Context) {


    var layer = layer //要绘制的图解图层
    var mapView = mapView
    val context = context
    var drawUtil = DrawUtil()//绘制对象
    var drawType = 0 //绘图类型
    var isMeasure = false //是否是测量
    var isShowXY = false //是否展示点的 经纬度坐标 或 平面坐标
    var isPlanar = false //是否是平面的
    val pointCollection = PointCollection(Config.def_sp)
    var textList = mutableListOf<String>()

    var unitType = CaculationUtil.UNIT_DEFAULT  //面积单位

    fun addPoint(point: Point,planar: Boolean = true) {
        this.isPlanar = planar
        if (point.spatialReference.wkid == Config.def_sp_Int) {
            pointCollection.add(point)
        } else if (point.spatialReference.wkid == Config.sp4490Int) {
            pointCollection.add(transToDefPoint(point))
        }
        refresh()
    }

    fun addPointOnly(point: Point,planar: Boolean = true) {
        this.isPlanar = planar
        if (point.spatialReference.wkid == Config.def_sp_Int) {
            pointCollection.add(point)
        } else if (point.spatialReference.wkid == Config.sp4490Int) {
            pointCollection.add(transToDefPoint(point))
        }
    }

    fun addPointCollection(list: PointCollection) {
        pointCollection.clear()
        pointCollection.addAll(list)
        refresh()
    }


    fun addPointText(point: Point, text: String) {

        textList.add(text)
        addPoint(point)
        refresh()
    }


    fun refresh() {
        layer.graphics.clear()
        pointCollection.isNotEmpty().yes {
            when (drawType) {
                DRAW_POINT -> {
                    drawUtil.drawPoint(layer, pointCollection.last(), true,isShowXY)
                }
                DRAW_POLYLINE -> {
                    drawUtil.drawPolyline(layer, pointCollection, true, isMeasure,isShowXY,isPlanar)
                }
                DRAW_POLYGON -> {
                    drawUtil.drawPolygon(layer, pointCollection, true, isMeasure,isShowXY, isPlanar, unitType)
                }
                DRAW_TEXT -> {
                    drawUtil.drawText(layer, pointCollection.last(), textList.last(), true)
                }
                else -> throw Exception("draw type error")
            }
        }
    }

    //回退、 上一步
    fun preStep() {

        if (drawType == DRAW_TEXT && pointCollection.size == textList.size) {
            //标绘文本，上一步
            textList.removeLast()
        }

        pointCollection.isNotEmpty().yes {
            pointCollection.removeLast()
        }
        refresh()
    }

    //清空、重置
    fun reset() {
        pointCollection.clear()
        layer.graphics.clear()
    }

    //获取到图形
    fun getGeometry(): Geometry = when (drawType) {

        DRAW_POINT -> {
            pointCollection.last()
        }
        DRAW_POLYLINE -> {
            Polyline(pointCollection)
        }
        DRAW_POLYGON -> {
            Polygon(pointCollection)
        }
        else -> throw Exception("draw type error")
    }

    fun getPointCount(): Int {
        return pointCollection.size
    }

    /**
     * 4490点 转成 漳浦的 4548
     */
    private fun transToDefPoint(point: Point): Point {
        val poi = GeometryEngine.project(point, Config.def_sp) as Point
        return poi
    }

    fun addPointList(list: MutableList<PointModel>) {
        pointCollection.clear()
        list.forEach {
            if (it.coordinateType == 1 || it.coordinateType == 2) {
                //平面坐标
                val p = Point(it.x, it.y, Config.def_sp)
                pointCollection.add(p)
            } else if (it.coordinateType == 0) {
                //球面坐标
                val p = Point(it.lng, it.lat, Config.sp4490)
                pointCollection.add(transToDefPoint(p))
            }
        }

        refresh()

    }
}