package com.jwch.gwyt_project.util

import android.content.Context
import java.io.File
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.ext.printMsg
import org.gdal.gdal.gdal
import org.gdal.ogr.ogr
import org.gdal.osr.CoordinateTransformation
import org.gdal.osr.SpatialReference
import org.gdal.osr.osrConstants


object ShpLoader {
    private var gdalInitialized = false
    private const val GDAL_VERSION = "3.7.0"

    fun initGDAL(): Boolean {
        if (gdalInitialized) return true
        return try {
            val app = AppContext.app
            val dataRoot = File(app.filesDir, "gdal/$GDAL_VERSION")
            val gdalData = File(dataRoot, "gdal_data")
            val projData = File(dataRoot, "proj_data")
            prepareDataDirectory(app, gdalData, projData)

            // Load dependencies first so Android can resolve them in the app's JNI directory.
            System.loadLibrary("proj")
            System.loadLibrary("gdal")
            System.loadLibrary("gdalalljni")

            gdal.SetConfigOption("GDAL_DATA", gdalData.absolutePath)
            gdal.SetConfigOption("PROJ_DATA", projData.absolutePath)
            gdal.SetConfigOption("PROJ_LIB", projData.absolutePath)
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES")
            gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8")
            gdal.AllRegister()
            ogr.RegisterAll()
            check(ogr.GetDriverByName("GPKG") != null) {
                "GDAL ${gdal.VersionInfo("RELEASE_NAME")} 未注册 GPKG 驱动"
            }
            gdalInitialized = true
            "ShpLoader: GDAL ${gdal.VersionInfo("RELEASE_NAME")} 初始化成功，GPKG 驱动可用".printMsg()
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

    private fun prepareDataDirectory(context: Context, gdalData: File, projData: File) {
        if (File(gdalData, "epsg.wkt").isFile && File(projData, "proj.db").isFile) return

        val root = gdalData.parentFile ?: error("无法创建 GDAL 数据目录")
        root.deleteRecursively()
        check(gdalData.mkdirs() && projData.mkdirs()) { "无法创建 GDAL/PROJ 数据目录" }
        copyAssetsRecursively(context, "gdal_data", gdalData)
        copyAssetsRecursively(context, "proj_data", projData)
        check(File(gdalData, "epsg.wkt").isFile && File(projData, "proj.db").isFile) {
            "GDAL/PROJ 数据文件缺失"
        }
    }

    private fun copyAssetsRecursively(context: Context, assetPath: String, destination: File) {
        val children = context.assets.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            destination.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            }
            return
        }

        if (!destination.exists()) check(destination.mkdirs()) { "无法创建目录: ${destination.path}" }
        for (child in children) {
            copyAssetsRecursively(context, "$assetPath/$child", File(destination, child))
        }
    }

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
        sourceSR.SetAxisMappingStrategy(osrConstants.OAMS_TRADITIONAL_GIS_ORDER)

        val targetSR = SpatialReference()
        targetSR.ImportFromEPSG(targetEPSG)
        targetSR.SetAxisMappingStrategy(osrConstants.OAMS_TRADITIONAL_GIS_ORDER)

        val transform = CoordinateTransformation(sourceSR, targetSR)
        try {
            geometry.Transform(transform)
        } finally {
            transform.delete()
            sourceSR.delete()
            targetSR.delete()
        }
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
