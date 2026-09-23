package com.jwch.gwyt_project.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.findDrawable
import com.jwch.gwyt_project.ext.self


//自定义的图层展示Util
object CustomLayerDisplayUtils {

    //获取图标
    fun getIconResource(layerName: String?)  : Int{
        var icon = -1
        when {
            layerName.self().contains("乱建") -> {
                icon = R.mipmap.icon_luanjian
            }
            layerName.self().contains( "乱占") -> {
                icon = R.mipmap.icon_luanzhan
            }
            layerName.self().contains("乱采") -> {
                icon = R.mipmap.icon_luancai
            }
            layerName.self().contains("乱堆") -> {
                icon = R.mipmap.icon_luandui
            }
            layerName.self().contains("其他") -> {
                icon = R.mipmap.icon_qita
            }
            layerName.self().contains("水葫芦") -> {
                icon = R.mipmap.icon_shl
            }
            layerName.self().contains("水利部图斑") -> {
                icon = R.mipmap.icon_shuilibu
            }
            layerName.self().contains("自查自纠") -> {
                icon = R.mipmap.icon_zichazijiu
            }
            else -> {
                icon = R.mipmap.icon_little_location2
            }
        }
        return icon
    }

    fun getBitmapDrawableIcon(context: Context,iconResource : Int) : BitmapDrawable?{
        return findDrawable(context, iconResource)?.toBitmap()?.toDrawable(context.resources)
    }

    fun getBitmapDrawableIcon(context: Context,layerName : String) : BitmapDrawable?{
        val icon =  getIconResource(layerName)
        return findDrawable(context, icon)?.toBitmap()?.toDrawable(context.resources)
    }

    fun getColor(layerName : String) : Int{
        var color = -1

        when {
            layerName.self().contains("乱建") -> {
                color = Color.parseColor("#FFAE00")
            }
            layerName.self().contains( "乱占") -> {
                color = Color.parseColor("#FF0000")
            }
            layerName.self().contains("乱采") -> {
                color = Color.parseColor("#A900E6")
            }
            layerName.self().contains("乱堆") -> {
                color = Color.parseColor("#FF00C5")
            }
            layerName.self().contains("其他") -> {
                color = Color.parseColor("#38A800")
            }
            layerName.self().contains("水葫芦") -> {
                color = Color.parseColor("#4BEA00")
            }
            layerName.self().contains("水利部图斑") -> {
                color = Color.parseColor("#73DFFF")
            }
            layerName.self().contains("自查自纠") -> {
                color = Color.parseColor("#FFF200")
            }
            else -> {
                color = Color.parseColor("#FFF200")
            }
        }

        return color
    }




}