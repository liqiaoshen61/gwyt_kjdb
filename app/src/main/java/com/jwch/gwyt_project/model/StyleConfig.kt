package com.jwch.gwyt_project.model

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import com.esri.arcgisruntime.symbology.*

class StyleConfig {

    var context: Context? = null
    var drawType = 0 //绘图类型
    var pointColor = Color.RED //点的颜色
    var pointColor2 = Color.BLUE //点的颜色
    var pointSize = 5f //点的大小
    var pointSize2 = 8f //点的大小
    var pointStyle = SimpleMarkerSymbol.Style.CIRCLE
    var pointSymbol: SimpleMarkerSymbol? = null
    var pointSymboll: SimpleMarkerSymbol? = null
    var pictureMarkerSymbol: PictureMarkerSymbol? = null
    var markerResId = 0 //标点的资源图片


    var lineColor = Color.RED //边线颜色
    var lineSize = 1f //线的粗细

    var polylineStyle = SimpleLineSymbol.Style.SOLID
    private var polylineSymbol: SimpleLineSymbol? = null

    var fillColr = Color.parseColor("#90444444") //中间填充颜色
    var polygonFillStyle = SimpleFillSymbol.Style.SOLID
    private var polygonSymbol: SimpleFillSymbol? = null //多边形符号

    //文本样式

    val textHorizonAlign = TextSymbol.HorizontalAlignment.LEFT
    val textVerticalAlign = TextSymbol.VerticalAlignment.BOTTOM
    var textHoleColor = Color.WHITE//文本光环颜色
    var textHoleWidth = 2f//文本光环宽度
    var textSize = 12f//文本大小
    var textColor = Color.BLACK//文本颜色
    var textSymbol: TextSymbol? = null

    //包含数据
    var data: Any? = null

    //重置样式
    fun resetSymbol() {
        pointSymbol = null
        polylineSymbol = null
        polygonSymbol = null
    }

    fun getPointSymbol1(): SimpleMarkerSymbol? {
        if (pointSymbol == null) {
            pointSymbol = SimpleMarkerSymbol(pointStyle, pointColor, pointSize)
        }
        return pointSymbol
    }

    fun getPointSymbol2(): SimpleMarkerSymbol? {
        if (pointSymboll == null) {
            pointSymboll = SimpleMarkerSymbol(pointStyle, pointColor2, pointSize2)
        }
        return pointSymboll
    }

    fun getPictureMarkerSymbol1(): PictureMarkerSymbol? {
        if (context == null) return null
        context.let {
            if (pictureMarkerSymbol == null) {
                var bitmap = BitmapFactory.decodeResource(it!!.resources, markerResId)
                var bitmapDrawable = BitmapDrawable(it!!.resources, bitmap)
                pictureMarkerSymbol = PictureMarkerSymbol(bitmapDrawable)
            }
        }
        return pictureMarkerSymbol
    }

    fun getPolylineSymbol1(): SimpleLineSymbol? {
        if (polylineSymbol == null) {
            polylineSymbol = SimpleLineSymbol(polylineStyle, lineColor, lineSize)
        }
        return polylineSymbol
    }


    fun getPolygonSymbol1(): SimpleFillSymbol? {
        if (polygonSymbol == null) {
            polygonSymbol = SimpleFillSymbol(polygonFillStyle, fillColr, getPolylineSymbol1())
        }
        return polygonSymbol
    }

    fun getTextSymbol1(text: String): TextSymbol? {
        textSymbol = TextSymbol(textSize, text, textColor, textHorizonAlign, textVerticalAlign)
        textSymbol!!.haloColor = textHoleColor
        textSymbol!!.haloWidth = textHoleWidth
        return textSymbol
    }
}