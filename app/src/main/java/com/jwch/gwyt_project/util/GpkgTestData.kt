package com.jwch.gwyt_project.util

import org.gdal.gdal.gdal
import org.gdal.ogr.DataSource
import org.gdal.ogr.Feature
import org.gdal.ogr.FieldDefn
import org.gdal.ogr.ogr
import org.gdal.osr.SpatialReference
import java.io.File
import java.util.Vector
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** 测试工具的数据层；所有方法应在同一个后台队列运行，调用前关闭 ArcGIS GPKG。 */
object GpkgTestData {
    private fun initialize() {
        check(ShpLoader.initGDAL()) { "GDAL 初始化失败，请检查原生库与设备架构" }
    }

    fun newDirectory(root: File, prefix: String): File {
        check(root.isDirectory || root.mkdirs()) { "无法创建测试目录：$root" }
        val directory = File(root, prefix + "_" + java.util.UUID.randomUUID().toString().take(12))
        check(directory.mkdir()) { "无法创建测试目录：$directory" }
        return directory
    }

    private fun <T> read(file: File, action: (DataSource) -> T): T {
        initialize()
        val source = ogr.Open(file.absolutePath, 0) ?: error("无法打开 ${file.name}：${gdal.GetLastErrorMsg()}")
        try { return action(source) } finally { source.delete() }
    }

    fun createSample(root: File): File {
        initialize()
        val driver = ogr.GetDriverByName("GPKG") ?: error("现有 GDAL 不含 GPKG 驱动")
        val output = File(newDirectory(root, "sample"), "sample.gpkg")
        val target = driver.CreateDataSource(output.absolutePath, Vector(listOf("VERSION=1.2")))
            ?: error("创建 GPKG 失败：${gdal.GetLastErrorMsg()}")
        val srs = SpatialReference()
        try {
            check(srs.ImportFromEPSG(4326) == 0) { "初始化坐标系失败" }
            val samples = listOf(
                Triple("points", ogr.wkbPoint, "POINT (116.40 39.90)"),
                Triple("lines", ogr.wkbLineString, "LINESTRING (116.40 39.90,116.41 39.91,116.42 39.90)"),
                Triple("polygons", ogr.wkbPolygon,
                    "POLYGON ((116.40 39.92,116.42 39.92,116.42 39.94,116.40 39.94,116.40 39.92)," +
                        "(116.405 39.925,116.405 39.935,116.415 39.935,116.415 39.925,116.405 39.925))")
            )
            samples.forEach { (name, type, wkt) ->
                val layer = target.CreateLayer(name, srs, type) ?: error("创建 $name 图层失败")
                listOf("name" to ogr.OFTString, "number" to ogr.OFTInteger, "value" to ogr.OFTReal).forEach { (n, t) ->
                    val field = FieldDefn(n, t)
                    try {
                        if (t == ogr.OFTString) field.SetWidth(80)
                        check(layer.CreateField(field) == 0) { "创建字段 $n 失败" }
                    } finally { field.delete() }
                }
                val feature = Feature(layer.GetLayerDefn())
                val geometry = ogr.CreateGeometryFromWkt(wkt) ?: error("创建测试几何失败")
                try {
                    feature.SetField("name", "勘界测试_$name")
                    feature.SetField("number", 1)
                    feature.SetField("value", 12.5)
                    check(feature.SetGeometry(geometry) == 0)
                    check(layer.CreateFeature(feature) == 0) { "写入 $name 失败" }
                } finally { geometry.delete(); feature.delete() }
            }
        } finally { srs.delete(); target.delete() }
        read(output) { ds ->
            check(ds.GetLayerCount() == 3) { "重开后图层数不一致" }
            for (i in 0 until ds.GetLayerCount()) {
                val layer = ds.GetLayer(i)
                check(layer.GetFeatureCount().toLong() == 1L) { "重开后要素数不一致" }
                val feature = layer.GetNextFeature() ?: error("无法重读测试要素")
                try {
                    check(feature.GetFieldAsString("name") == "勘界测试_${layer.GetName()}") { "中文属性重读失败" }
                    check(feature.GetGeometryRef() != null) { "几何重读失败" }
                } finally { feature.delete() }
            }
        }
        return output
    }

    fun inspect(file: File): String = read(file) { ds ->
        check(ds.GetDriver().GetName() == "GPKG") { "所选文件不是 GDAL 可识别的 GPKG 数据源" }
        buildString {
            append("文件：${file.absolutePath}\nGDAL：${gdal.VersionInfo("RELEASE_NAME")}\n图层数：${ds.GetLayerCount()}\n")
            for (i in 0 until ds.GetLayerCount()) {
                val layer = ds.GetLayer(i)
                val definition = layer.GetLayerDefn()
                val srs = layer.GetSpatialRef()
                append("\n[${layer.GetName()}]\n要素数：${layer.GetFeatureCount()}\n几何类型：${ogr.GeometryTypeToName(layer.GetGeomType())}\n")
                append("坐标系：${srs?.ExportToWkt() ?: "未知（不应直接编辑）"}\n字段：\n")
                for (j in 0 until definition.GetFieldCount()) {
                    val field = definition.GetFieldDefn(j)
                    append("  ${field.GetName()} / ${ogr.GetFieldTypeName(field.GetFieldType())} / 宽度 ${field.GetWidth()} / 精度 ${field.GetPrecision()}\n")
                }
                layer.ResetReading()
                val first = layer.GetNextFeature()
                if (first != null) {
                    try {
                        append("首条 FID：${first.GetFID()}\n")
                        for (j in 0 until definition.GetFieldCount()) {
                            append("  ${definition.GetFieldDefn(j).GetName()} = ${first.GetFieldAsString(j)}\n")
                        }
                        append("几何预览：${first.GetGeometryRef()?.ExportToWkt()?.take(600)}\n")
                    } finally { first.delete() }
                }
            }
        }
    }

    fun importShp(shp: File, root: File): File {
        initialize()
        // 旧初始化代码强制 UTF-8；转换时尊重 cpg/DBF，完成后恢复原配置。
        val previousEncoding = gdal.GetConfigOption("SHAPE_ENCODING")
        gdal.SetConfigOption("SHAPE_ENCODING", null)
        try { return importShpInternal(shp, root) }
        finally { gdal.SetConfigOption("SHAPE_ENCODING", previousEncoding) }
    }

    private fun importShpInternal(shp: File, root: File): File {
        listOf("shp", "shx", "dbf", "prj").forEach { extension ->
            check(File(shp.parentFile, "${shp.nameWithoutExtension}.$extension").isFile) { "缺少 .$extension 配套文件" }
        }
        val output = File(newDirectory(root, "import"), "work.gpkg")
        val driver = ogr.GetDriverByName("GPKG") ?: error("缺少 GPKG 驱动")
        read(shp) { source ->
            val layer = source.GetLayer(0) ?: error("SHP 无图层")
            check(layer.GetSpatialRef() != null) { "SHP 坐标系无法识别，未转换" }
            val target = driver.CreateDataSource(output.absolutePath, Vector(listOf("VERSION=1.2")))
                ?: error("创建 GPKG 失败")
            try {
                // 直接复制 OGR 图层，保留字段类型、空值、多部件和内环，不经过 ShpModel。
                check(target.CopyLayer(layer, "imported") != null) { "转换失败：${gdal.GetLastErrorMsg()}" }
            } finally { target.delete() }
            read(output) { converted ->
                check(converted.GetLayer(0).GetFeatureCount() == layer.GetFeatureCount()) { "转换前后数量不一致" }
                check(converted.GetLayer(0).GetLayerDefn().GetFieldCount() == layer.GetLayerDefn().GetFieldCount()) { "转换前后字段数量不一致" }
            }
        }
        return output
    }

    /** ArcGIS 已保存并关闭后，用另一个引擎确认同一 FID 的属性已落盘。 */
    fun verifySaved(file: File, layerName: String, fid: Long, changed: Map<String, Any?>): String = read(file) { ds ->
        val layer = ds.GetLayerByName(layerName) ?: error("保存后图层不存在")
        val feature = layer.GetFeature(fid) ?: error("保存后 FID=$fid 不存在")
        try {
            changed.forEach { (name, expected) ->
                val index = feature.GetFieldIndex(name)
                check(index >= 0) { "字段 $name 丢失" }
                if (expected == null) {
                    // 兼容旧 gdal.jar：部分版本 IsFieldSet 不能区分显式 NULL 与已赋值。
                    fun quote(value: String) = "\"" + value.replace("\"", "\"\"") + "\""
                    val fidColumn = layer.GetFIDColumn()
                    check(!fidColumn.isNullOrEmpty()) { "无法定位主键列以核验 NULL" }
                    val result = ds.ExecuteSQL("SELECT 1 FROM ${quote(layerName)} WHERE ${quote(fidColumn)} = $fid AND ${quote(name)} IS NULL")
                        ?: error("NULL 核验查询失败")
                    try {
                        val row = result.GetNextFeature()
                        check(row != null) { "字段 $name 未保存为空值" }
                        row.delete()
                    } finally { ds.ReleaseResultSet(result) }
                } else if (expected is Number) {
                    check(kotlin.math.abs(feature.GetFieldAsDouble(index) - expected.toDouble()) <=
                        1e-10 * kotlin.math.max(1.0, kotlin.math.abs(expected.toDouble()))) { "字段 $name 数值不一致" }
                } else {
                    check(feature.GetFieldAsString(index) == expected.toString()) { "字段 $name 内容不一致" }
                }
            }
            val geometry = feature.GetGeometryRef() ?: error("保存后几何为空")
            "GDAL 重开核验通过\n图层：$layerName\nFID：$fid\n属性修改数：${changed.size}\n已落盘几何：${geometry.ExportToWkt().take(1200)}"
        } finally { feature.delete() }
    }

    fun verifyDeleted(file: File, layerName: String, fid: Long) = read(file) { ds ->
        val layer = ds.GetLayerByName(layerName) ?: error("删除后图层不存在")
        val feature = layer.GetFeature(fid)
        if (feature != null) {
            feature.delete()
            error("删除未落盘：FID $fid 仍存在")
        }
    }

    fun exportShp(file: File, root: File): File {
        initialize()
        val driver = ogr.GetDriverByName("ESRI Shapefile") ?: error("缺少 SHP 驱动")
        val directory = newDirectory(root, "export")
        read(file) { source ->
            check(source.GetLayerCount() > 0) { "没有可导出的矢量图层" }
            for (i in 0 until source.GetLayerCount()) {
                val layer = source.GetLayer(i)
                val definition = layer.GetLayerDefn()
                check(layer.GetSpatialRef() != null) { "${layer.GetName()} 缺少坐标系" }
                val names = mutableSetOf<String>()
                for (j in 0 until definition.GetFieldCount()) {
                    val field = definition.GetFieldDefn(j)
                    val name = field.GetName()
                    check(name.toByteArray(Charsets.UTF_8).size <= 10 && names.add(name.lowercase())) {
                        "${layer.GetName()} 的字段 $name 无法原名导出 SHP（10字节限制/重名），请先规范字段"
                    }
                    check(field.GetFieldType() in listOf(ogr.OFTString, ogr.OFTInteger, ogr.OFTReal, ogr.OFTDate)) {
                        "字段 $name 的类型暂不支持无损导出，本次测试已停止"
                    }
                    check(field.GetFieldType() != ogr.OFTString || field.GetWidth() <= 254) { "字段 $name 超过 SHP 字符串长度限制" }
                }
            }
            val target = driver.CreateDataSource(directory.absolutePath) ?: error("无法创建 SHP 输出目录")
            try {
                for (i in 0 until source.GetLayerCount()) {
                    val layer = source.GetLayer(i)
                    val copied = target.CopyLayer(layer, "layer_${i + 1}", Vector(listOf("ENCODING=UTF-8")))
                    check(copied != null) { "导出 ${layer.GetName()} 失败：${gdal.GetLastErrorMsg()}" }
                }
            } finally { target.delete() }
            val report = StringBuilder("源文件：${file.name}\n编码：UTF-8\n")
            for (i in 0 until source.GetLayerCount()) {
                val original = source.GetLayer(i)
                read(File(directory, "layer_${i + 1}.shp")) { exported ->
                    val layer = exported.GetLayer(0)
                    check(layer.GetFeatureCount() == original.GetFeatureCount()) { "${original.GetName()} 导出数量不一致" }
                    check(layer.GetLayerDefn().GetFieldCount() == original.GetLayerDefn().GetFieldCount()) { "导出字段数量不一致" }
                    for (j in 0 until original.GetLayerDefn().GetFieldCount()) {
                        check(layer.GetLayerDefn().GetFieldDefn(j).GetName() == original.GetLayerDefn().GetFieldDefn(j).GetName()) { "导出字段名发生变化" }
                    }
                    val exportedSrs = layer.GetSpatialRef() ?: error("导出坐标系丢失")
                    check(exportedSrs.IsSame(original.GetSpatialRef()) != 0) { "导出坐标系不一致" }
                    report.append("${original.GetName()} → layer_${i + 1}.shp，${layer.GetFeatureCount()} 个要素；数量/字段名/坐标系核验通过\n")
                }
            }
            report.append("本次未逐条比较全部属性和几何；请在桌面 GIS 中复核。样式和照片不包含在 SHP 中。\n")
            File(directory, "export_report.txt").writeText(report.toString())
        }
        val zip = File(directory.parentFile, directory.name + ".zip")
        ZipOutputStream(zip.outputStream()).use { output ->
            directory.listFiles().orEmpty().filter { it.isFile }.forEach { child ->
                output.putNextEntry(ZipEntry(child.name))
                child.inputStream().use { it.copyTo(output) }
                output.closeEntry()
            }
        }
        return zip
    }
}
