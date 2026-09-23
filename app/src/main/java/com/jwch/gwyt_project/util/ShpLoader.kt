package com.jwch.gwyt_project.util

import com.jwch.gwyt_project.ext.printMsg
import org.gdal.gdal.gdal
import org.gdal.ogr.ogr
import org.gdal.osr.CoordinateTransformation
import org.gdal.osr.SpatialReference


object ShpLoader {
    private var gdalInitialized = false

    fun initGDAL(): Boolean {
        if (gdalInitialized) return true
        return try {
            System.loadLibrary("gdaljni")
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES")
            gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8")
            gdal.AllRegister()
            ogr.RegisterAll()
            gdalInitialized = true
            "ShpLoader: GDAL 初始化成功".printMsg()
            true
        } catch (e: UnsatisfiedLinkError) {
            "ShpLoader: GDAL 加载失败，当前设备架构可能缺少 GDAL .so 文件: ${e.message}".printMsg()
            gdalInitialized = false
            false
        } catch (e: Exception) {
            "ShpLoader: GDAL 初始化异常: ${e.message}".printMsg()
            gdalInitialized = false
            false
        }
    }

    fun isReady(): Boolean = gdalInitialized

    /**
     * 坐标系转换：将 GDAL Geometry 从 sourceEPSG 投影到 targetEPSG
     */
    fun transformGeometry(
        geometry: org.gdal.ogr.Geometry,
        sourceEPSG: Int,
        targetEPSG: Int
    ): org.gdal.ogr.Geometry {
        val sourceSR = SpatialReference()
        sourceSR.ImportFromEPSG(sourceEPSG)

        val targetSR = SpatialReference()
        targetSR.ImportFromEPSG(targetEPSG)

        val transform = CoordinateTransformation(sourceSR, targetSR)
        geometry.Transform(transform)
        return geometry
    }
}


/**
 * 保持与 ShpModel 中引用的兼容别名
 */
object ShpHolder {
    val isGdalReady: Boolean
        get() = ShpLoader.isReady()

    fun initGDAL(): Boolean = ShpLoader.initGDAL()

    fun transformGeometry(
        geometry: org.gdal.ogr.Geometry,
        sourceEPSG: Int,
        targetEPSG: Int
    ): org.gdal.ogr.Geometry = ShpLoader.transformGeometry(geometry, sourceEPSG, targetEPSG)
}