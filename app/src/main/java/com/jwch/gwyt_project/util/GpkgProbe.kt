package com.jwch.gwyt_project.util

import android.os.Build
import org.gdal.gdal.gdal
import org.gdal.ogr.DataSource
import org.gdal.ogr.Feature
import org.gdal.ogr.FieldDefn
import org.gdal.ogr.Geometry
import org.gdal.ogr.ogr
import org.gdal.osr.SpatialReference
import java.io.File

/** 在工作线程调用。仅创建独立测试文件，不修改业务数据。 */
object GpkgProbe {
    @Synchronized
    fun run(directory: File): String {
        val report = StringBuilder("设备架构：${Build.SUPPORTED_ABIS.joinToString()}\n")
        var stage = "初始化 GDAL"
        var dataSource: DataSource? = null
        try {
            check(ShpLoader.initGDAL()) { "GDAL 初始化失败，请检查设备架构和原生库" }
            report.append("GDAL：${gdal.VersionInfo("RELEASE_NAME")}\n")
            stage = "检查 GPKG 驱动"
            val driver = ogr.GetDriverByName("GPKG")
                ?: error("当前 GDAL 未包含 GPKG 驱动")
            report.append("GPKG 驱动：已找到\n")

            stage = "创建 GPKG 文件"
            check(directory.isDirectory || directory.mkdirs()) { "无法创建测试目录" }
            val file = File.createTempFile("gpkg_probe_", ".gpkg", directory)
            check(file.delete()) { "无法准备测试文件" }
            report.append("测试文件：${file.absolutePath}\n")
            val writer = driver.CreateDataSource(file.absolutePath)
                ?: error("创建数据源失败：${gdal.GetLastErrorMsg()}")
            dataSource = writer

            stage = "创建图层、字段并写入要素"
            val srs = SpatialReference()
            try {
                check(srs.ImportFromEPSG(4326) == 0) { "创建 EPSG:4326 失败" }
                val layer = writer.CreateLayer("probe_points", srs, ogr.wkbPoint)
                    ?: error("创建点图层失败：${gdal.GetLastErrorMsg()}")
                val field = FieldDefn("name", ogr.OFTString)
                try {
                    check(layer.CreateField(field) == 0) { "创建字段失败" }
                } finally {
                    field.delete()
                }
                val feature = Feature(layer.GetLayerDefn())
                val point = Geometry(ogr.wkbPoint)
                try {
                    feature.SetField("name", "勘界测试点")
                    point.AddPoint_2D(116.4, 39.9)
                    check(feature.SetGeometry(point) == 0) { "设置几何失败" }
                    check(layer.CreateFeature(feature) == 0) { "写入要素失败" }
                } finally {
                    point.delete()
                    feature.delete()
                }
            } finally {
                srs.delete()
            }
            // 必须关闭后重新打开，避免只验证到内存中的数据。
            writer.delete()
            dataSource = null
            stage = "关闭后重新读取并核验"
            check(file.length() > 0) { "文件为空" }
            val reader = ogr.Open(file.absolutePath, 0) ?: error("重新打开失败")
            dataSource = reader
            check(reader.GetLayerCount() == 1) { "图层数量不一致" }
            val layer = reader.GetLayer(0) ?: error("测试图层不存在")
            check(layer.GetName() == "probe_points") { "图层名称不一致" }
            check(layer.GetFeatureCount().toLong() == 1L) { "要素数量不一致" }
            val definition = layer.GetLayerDefn()
            check(definition.GetFieldCount() == 1) { "字段数量不一致" }
            val savedField = definition.GetFieldDefn(0)
            check(savedField.GetName() == "name" && savedField.GetFieldType() == ogr.OFTString) {
                "字段名称或类型不一致"
            }
            val savedSrs = layer.GetSpatialRef() ?: error("坐标系丢失")
            val expectedSrs = SpatialReference()
            try {
                check(expectedSrs.ImportFromEPSG(4326) == 0)
                check(savedSrs.IsSame(expectedSrs) != 0) { "坐标系不一致" }
            } finally {
                expectedSrs.delete()
            }
            val saved = layer.GetNextFeature() ?: error("无法读取测试要素")
            try {
                check(saved.GetFieldAsString("name") == "勘界测试点") { "中文属性不一致" }
                val geometry = saved.GetGeometryRef() ?: error("几何丢失")
                check(geometry.GetGeometryType() == ogr.wkbPoint) { "几何类型不一致" }
                check(kotlin.math.abs(geometry.GetX() - 116.4) < 1e-9 &&
                    kotlin.math.abs(geometry.GetY() - 39.9) < 1e-9) { "坐标不一致" }
            } finally {
                saved.delete()
            }
            report.append("图层：probe_points\n字段：name（字符串）\n要素数量：1\n")
            report.append("坐标系：EPSG:4326\n中文属性：勘界测试点\n坐标：116.4, 39.9\n")
            report.append("验证通过：GPKG 创建、写入、关闭后重读均成功。")
        } catch (e: Exception) {
            report.append("验证失败 [$stage]：${e.javaClass.simpleName}: ${e.message}")
        } catch (e: LinkageError) {
            report.append("验证失败 [$stage]：原生库不兼容或缺失：${e.message}")
        } finally {
            dataSource?.delete()
        }
        return report.toString()
    }
}
