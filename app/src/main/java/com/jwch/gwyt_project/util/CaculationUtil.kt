package com.jwch.gwyt_project.util

import com.esri.arcgisruntime.geometry.*
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs

class CaculationUtil {

    companion object {
        const val UNIT_DEFAULT = 0 //默认
        const val UNIT_M2 = 1 //平方米
        const val UNIT_KM2 = 2 //平方千米
        const val UNIT_MU = 3 //亩
    }

    //计算折线长度
    fun caculateLength(polyline: Polyline): Double {
        val distance = GeometryEngine.length(polyline)
        "测量长度distance $distance".printMsg()
        return distance

    }

    //计算折线长度
    fun caculateLengthUnit(polyline: Polyline, isPlanar: Boolean = false): String {
        val distance = caculateLength(polyline)
        "测量长度distance $distance".printMsg()
        var length = getLengthStringUnit(distance, isPlanar)
        "测量长度length $length".printMsg()
        return length

    }

    //计算面积
    fun caculateAreaSize(polygon: Polygon): Double {
        val areaSize = GeometryEngine.area(polygon)
        "测量面积areaSize $areaSize".printMsg()
        return areaSize
    }

    //计算面积亩
    fun caculateAreaSizeMu(polygon: Polygon, isPlanar: Boolean = false): Double {
        val areaSize = GeometryEngine.area(polygon)
        var result = 0.0
        val df = DecimalFormat("#.####")

        isPlanar.yes {
            val area = abs(areaSize)

            result = df.format(area * 0.0015).toDouble()



        }.no {
            val area = abs(areaSize * 1000000000 * 10)
            result = df.format(area * 0.0015).toDouble()

        }

        "测量面积areaSize $areaSize".printMsg()
        return result
    }

    /**
     *  计算面积(含单位)
     */
    fun caculateAreaSizeUnit(polygon: Polygon, unitType: Int, isPlanar: Boolean = false): String {
        val areaSize = caculateAreaSize(polygon)
        var size = getAreaStringUnit(areaSize, unitType, isPlanar)
//        var size = getAreaStringUnitMu(areaSize, isPlanar)
        "测量面积areaSize $size".printMsg()
        return size
    }

    public fun getLengthStringUnit(distance: Double, isPlanar: Boolean = false): String {
        val df = DecimalFormat("#.####")
        var result = ""
        isPlanar.yes {

            if (distance > 1000) {
                result = "  ${df.format(distance / 1000.0)}  公里"
            } else {
                result = "  ${df.format(distance)}  米"
            }
        }.no {
            val length = Math.round(distance * 100000)

            if (length > 1000) {
                result = "  ${df.format(length / 1000.0)}  公里"
            } else {
                result = "  $length  米"
            }
        }

        return result
    }

    public fun getAreaStringUnit(dValue: Double, unitType: Int ,isPlanar: Boolean = false): String {

        val df = DecimalFormat("#.####")
        var result = ""
        var value = 0.0

        isPlanar.yes {
            value = abs(dValue)
        }.no {
            value = Math.abs(dValue * 1000000000 * 10)
        }

        when (unitType) {

            UNIT_DEFAULT -> {
                if (value >= 1000000) {
                    val dArea = value / 1000000.0;
                    result = "  ${df.format(dArea)}  平方千米"
                } else {
                    result = "  ${df.format(value)}  平方米"
                }

                val mu = "平面：多少亩  ${df.format(dValue * 0.0015)}亩 "
                mu.printMsg()
            }

            UNIT_M2 -> {
                result = "  ${df.format(value)}  平方米"
            }

            UNIT_KM2 -> {
                val dArea = value / 1000000.0;
                result = "  ${df.format(dArea)}  平方千米"
            }

            UNIT_MU -> {
                result = "  ${df.format(value * 0.0015)}  亩"
            }

        }

        return result
    }

    public fun getAreaStringUnitMu(dValue: Double, isPlanar: Boolean = false): String {

        val df = DecimalFormat("#.####")
        var result = ""
        isPlanar.yes {
            var  value = abs(dValue)

            result = "${df.format(value * 0.0015)}亩 "
            result.printMsg()

        }.no {
            val area = abs(dValue * 1000000000 * 10)

            result = "${df.format(area * 0.0015)}亩 "
            result.printMsg()

        }

        return result
    }

    public fun getAreaStringMu(dValue: Double, isPlanar: Boolean = false): Double {

        val df = DecimalFormat("#.####")
        var result = 0.00
        isPlanar.yes {
            var  value = abs(dValue)

            result = df.format(value * 0.0015).toDouble()


        }.no {
            val area = abs(dValue * 1000000000 * 10)

            result = df.format(area * 0.0015).toDouble()
        }

        return result
    }

    public fun getLength(distance: Double): Double {

        val length = Math.round(distance * 100000)
        val df = DecimalFormat("#.####")
        var result = 0.0

        if (length > 1000) {
            result = df.format(length / 1000.0).toDouble()
        } else {
            result = length.toDouble()
        }

        "转化后的length:$result".printMsg()
        return result
    }

    public fun getArea(dValue: Double): Double {

        val area = Math.abs(dValue * 1000000000 * 10)
        val df = DecimalFormat("#.####")
        var result = 0.0

        if (area >= 1000000) {

            val dArea = area / 1000000.0;
            result = df.format(dArea).toDouble()
        } else {
            result = df.format(area).toDouble()
        }

//        val mu = "多少亩  ${df.format(area * 0.0015)}亩 "
//        mu.printMsg()

        "转化后的area:$result".printMsg()
        return result
    }

}