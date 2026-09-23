package com.jwch.gwyt_project.util

import android.content.Context
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.findDrawable
import com.jwch.gwyt_project.ext.isNullObj
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.model.StyleConfig


class DrawUtil {


    companion object {
        const val DRAW_POINT = 0
        const val DRAW_POLYLINE = 1
        const val DRAW_POLYGON = 2
        const val DRAW_TEXT = 3
    }

    val style = StyleConfig()
    val caculationUtil = CaculationUtil()

    //画点
    fun drawPoint(layer: GraphicsOverlay, point: Point, isReDraw: Boolean = true, isShowXY: Boolean = false) {
        if (isReDraw) {
            layer.graphics.clear()
        }

        if (style.markerResId == 0) {
            val graphic = Graphic(point, style.getPointSymbol1())
            layer.graphics.add(graphic)
        } else {
            val graphic = Graphic(point, style.getPictureMarkerSymbol1())
            layer.graphics.add(graphic)
        }
        if (isShowXY) {
            drawXY(layer, point)
        }
    }

    //画点2
    fun drawPoint2(layer: GraphicsOverlay, point: Point, isReDraw: Boolean = true, isShowXY: Boolean = false) {
        if (isReDraw) {
            layer.graphics.clear()
        }

        if (style.markerResId == 0) {
            val graphic = Graphic(point, style.getPointSymbol2())
            layer.graphics.add(graphic)
        } else {
            val graphic = Graphic(point, style.getPictureMarkerSymbol1())
            layer.graphics.add(graphic)
        }
        if (isShowXY) {
            drawXY(layer, point)
        }
    }

    //画折线
    fun drawPolyline(
        layer: GraphicsOverlay,
        list: PointCollection,
        isReDraw: Boolean = true,
        isMeasure: Boolean = false,
        isShowXY: Boolean = false,
        isPanar: Boolean = false
    ) {

        if (isReDraw) {
            layer.graphics.clear()
        }
        if (list.size > 1) {
            //绘制折线
            val lineSymbol = style.getPolylineSymbol1()
            val polyline = Polyline(list)
            val graphic = Graphic(polyline, lineSymbol)
            layer.graphics.add(graphic)
        }
        //绘制点
        list.forEach {
            drawPoint(layer, it, false)
        }

        if (list.size > 1 && isMeasure) {
            //计算距离

            val polyline = Polyline(list)
            var length = caculationUtil.caculateLengthUnit(polyline, isPanar)
            "测量长度length $length".printMsg()
            drawText(layer, list.last(), length, false)

        }

        if (isShowXY) {
            list.forEach {
                drawXY(layer, it)
            }
        }


    }

    //画面
    fun drawPolygon(
        layer: GraphicsOverlay, list: PointCollection,
        isReDraw: Boolean = true,
        isMeasure: Boolean = false,
        isShowXY: Boolean = false,
        isPanar: Boolean = false,
        unitType : Int = CaculationUtil.UNIT_DEFAULT
    ) {

        if (isReDraw) {
            layer.graphics.clear()
        }

        if (list.size == 1) {
            drawPoint(layer, list.last(), isReDraw)
        } else if (list.size == 2) {
            drawPolyline(layer, list, isReDraw)

        } else if (list.size > 2) {

            //绘制面
            val polygonSymbol = style.getPolygonSymbol1()
            val polygon = Polygon(list)
            val graphic = Graphic(polygon, polygonSymbol)
            layer.graphics.add(graphic)

            //绘制点
            list.forEachIndexed { index, point ->
                if(index == list.size -1){
                    drawPoint2(layer, point, false)
                }else {
                    drawPoint(layer, point, false)
                }

            }

            if (isMeasure) {
                //计算距离

                val polygon = Polygon(list)
                var size = caculationUtil.caculateAreaSizeUnit(polygon, unitType, isPanar)
//                "测量面积areaSize $size".printMsg()
                drawText(layer, list.last(), size, false)

            }

        }
        if (isShowXY) {
            list.forEach {
                drawXY(layer, it)
            }
        }
    }

    fun drawGeometry(drawType: Int = 0, layer: GraphicsOverlay, geometry: Geometry, isReDraw: Boolean = true, isMeasure: Boolean = false) {

        if (isReDraw) {
            layer.graphics.clear()
        }
        val graphic = when (drawType) {
            DRAW_POINT -> Graphic(geometry, style.getPointSymbol1())
            DRAW_POLYLINE -> Graphic(geometry, style.getPolylineSymbol1())
            DRAW_POLYGON -> Graphic(geometry, style.getPolygonSymbol1())
            else -> throw  Exception("drawType error")
        }

        layer.graphics.add(graphic)
    }


    fun drawText(layer: GraphicsOverlay, point: Point, text: String, isReDraw: Boolean = true) {
        if (isReDraw) {
            layer.graphics.clear()
        }

        val graphic = Graphic(point, style.getTextSymbol1(text))
        layer.graphics.add(graphic)

    }


    fun drawImage(context: Context, layer: GraphicsOverlay, point: Point, imageResId: Int, data: Any? = null, isReDraw: Boolean = true) {
        if (isReDraw) {
            layer.graphics.clear()
        }

        val bitmapDrawable = findDrawable(context, imageResId)?.toBitmap()?.toDrawable(context.resources)
        val pictureMarkerSymbol = PictureMarkerSymbol(bitmapDrawable)

        var graphic: Graphic? = null

        //配参数
        data?.let {
            var attrs = mutableMapOf<String, Any>()
            attrs["data"] = it.toJson()
            graphic = Graphic(point, attrs, pictureMarkerSymbol)

        }

        isNullObj(graphic) {
            graphic = Graphic(point, pictureMarkerSymbol)
        }

        pictureMarkerSymbol.loadAsync()
        pictureMarkerSymbol.addDoneLoadingListener {
            graphic?.let {
                layer.graphics.add(it)
            }
        }
    }

    fun drawImageList(context: Context, layer: GraphicsOverlay, list: PointCollection, imageResId: Int, isReDraw: Boolean = true) {
        if (isReDraw) {
            layer.graphics.clear()
        }
        list.forEach { drawImage(context, layer, it, imageResId, null, false) }
    }


    fun drawXY(layer: GraphicsOverlay, point: Point) {
        val p = GeometryEngine.project(point, Config.def_sp) as Point
        val x = p.x
        val y = p.y
        "x:$x   y:$y".printMsg()
        val text = "x:$x\ny:$y"
        drawText(layer, point, text, false)
    }


    //画折线
    fun drawPolylineMeasure(
        layer: GraphicsOverlay,
        list: PointCollection,
        isPanar: Boolean = false
    ) {

        if (list.size > 1) {
            //计算距离

            val polyline = Polyline(list)
            var length = caculationUtil.caculateLengthUnit(polyline, isPanar)
            "测量长度length $length".printMsg()
            drawText(layer, list.last(), length, false)

        }


    }

    //画面
    fun drawPolygonMeasure(
        layer: GraphicsOverlay,
        list: PointCollection,

        isMeasure: Boolean = false,
        isShowXY: Boolean = false,
        isPanar: Boolean = false,
        unitType : Int = CaculationUtil.UNIT_DEFAULT
    ) {
        if(list.size > 2) {

            if (isMeasure) {
                //计算距离
                val polygon = Polygon(list)
                var size = caculationUtil.caculateAreaSizeUnit(polygon, unitType, isPanar)
                drawText(layer, list.last(), size, false)

            }

        }
        if (isShowXY) {
            list.forEach {
                drawXY(layer, it)
            }
        }
    }

}