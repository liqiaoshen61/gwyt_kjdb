package com.jwch.gwyt_project.model

import android.graphics.Color
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.util.ShpHolder
import org.gdal.ogr.ogr
import java.io.File

/**
 * SHP 文件模型 — 使用 GDAL OGR 读取 .shp 文件，转换为 ArcGIS Geometry
 */
class ShpModel(file: File) {

    var name: String = ""              // 文件名（含 .shp 后缀）
    var filePath: String = ""           // 绝对路径
    var featureCount: Int = 0           // 要素数量（预估值）
    var geometryType: String = "未知"    // "点" / "线" / "面" / "未知"
    var select: Boolean = false

    private var featureList: MutableList<ShpFeature>? = null

    init {
        this.name = file.name
        this.filePath = file.absolutePath
        loadInfo()
    }

    /**
     * 预加载：打开 SHP 统计要素数量和几何类型
     */
    private fun loadInfo() {
        if (!ShpHolder.isGdalReady) {
            featureCount = -1  // -1 表示 GDAL 未就绪
            return
        }
        var ds: org.gdal.ogr.DataSource? = null
        try {
            ds = ogr.Open(filePath, 0) // 0 = 只读
            if (ds == null) {
                "ShpModel: 无法打开文件 $filePath".printMsg()
                return
            }
            val layer = ds.GetLayer(0)
            if (layer != null) {
                featureCount = layer.GetFeatureCount().toInt()
                val geomType = layer.GetLayerDefn().GetGeomType()
                geometryType = when (geomType) {
                    ogr.wkbPoint, ogr.wkbMultiPoint, ogr.wkbPoint25D, ogr.wkbMultiPoint25D -> "点"
                    ogr.wkbLineString, ogr.wkbMultiLineString, ogr.wkbLineString25D, ogr.wkbMultiLineString25D -> "线"
                    ogr.wkbPolygon, ogr.wkbMultiPolygon, ogr.wkbPolygon25D, ogr.wkbMultiPolygon25D -> "面"
                    else -> "未知"
                }
            }
        } catch (e: Exception) {
            "ShpModel loadInfo 异常: ${e.message}".printMsg()
            featureCount = 0
        } finally {
            try {
                ds?.delete()
            } catch (_: Exception) {}
        }
    }

    /**
     * 懒加载：读取全部要素，转换为 ShpFeature 列表
     */
    fun getData(): MutableList<ShpFeature> {
        if (featureList != null) return featureList!!

        if (!ShpHolder.isGdalReady) {
            "ShpModel: GDAL 未初始化".printMsg()
            featureList = mutableListOf()
            return featureList!!
        }

        val list = mutableListOf<ShpFeature>()
        var ds: org.gdal.ogr.DataSource? = null
        try {
            ds = ogr.Open(filePath, 0)
            if (ds == null) {
                "ShpModel: 无法打开文件 $filePath".printMsg()
                featureList = mutableListOf()
                return featureList!!
            }

            val layer = ds.GetLayer(0) ?: run {
                featureList = mutableListOf()
                return featureList!!
            }

            val layerDefn = layer.GetLayerDefn()
            val fieldCount = layerDefn.GetFieldCount()
            val geomType = layerDefn.GetGeomType()
            val typeName = when (geomType) {
                ogr.wkbPoint, ogr.wkbMultiPoint, ogr.wkbPoint25D, ogr.wkbMultiPoint25D -> "点"
                ogr.wkbLineString, ogr.wkbMultiLineString, ogr.wkbLineString25D, ogr.wkbMultiLineString25D -> "线"
                ogr.wkbPolygon, ogr.wkbMultiPolygon, ogr.wkbPolygon25D, ogr.wkbMultiPolygon25D -> "面"
                else -> "未知"
            }

            // 获取 SHP 自身的空间参考，用于后续坐标转换
            val spatialRef = layer.GetSpatialRef()
            var sourceEpsg = 0
            if (spatialRef != null) {
                try {
                    val authorityCode = spatialRef.GetAuthorityCode(null)
                    if (authorityCode != null) {
                        sourceEpsg = authorityCode.toIntOrNull() ?: 0
                    }
                } catch (_: Exception) {}
            }

            layer.ResetReading()
            var ogrFeature = layer.GetNextFeature()
            while (ogrFeature != null) {
                try {
                    val gdalGeom = ogrFeature.GetGeometryRef()
                    if (gdalGeom != null) {
                        // 坐标系转换：SHP 源坐标系 → 目标坐标系
                        if (sourceEpsg > 0 && sourceEpsg != Config.sp4490Int) {
                            try {
                                ShpHolder.transformGeometry(gdalGeom, sourceEpsg, Config.sp4490Int)
                            } catch (_: Exception) {}
                        }
                        // 转换为 ArcGIS Geometry
                        val arcgisGeom = convertGdalToArcGISGeometry(gdalGeom)
                        if (arcgisGeom != null) {
                            // 提取字段属性
                            val props = mutableMapOf<String, Any?>()
                            // 添加FID
                            props["FID"] = ogrFeature.GetFID()
                            for (i in 0 until fieldCount) {
                                val fieldDefn = layerDefn.GetFieldDefn(i)
                                val fieldName = fieldDefn.GetName()
                                val value = ogrFeature.GetFieldAsString(i)
                                props[fieldName] = value ?: ""
                            }
                            val style = buildDefaultStyle(typeName)
                            list.add(ShpFeature(arcgisGeom, style, props))
                        }
                    }
                } catch (e: Exception) {
                    "ShpModel 要素解析异常: ${e.message}".printMsg()
                }
                ogrFeature = layer.GetNextFeature()
            }

            featureCount = list.size
        } catch (e: Exception) {
            "ShpModel getData 异常: ${e.message}".printMsg()
        } finally {
            try {
                ds?.delete()
            } catch (_: Exception) {}
        }

        featureList = list
        return list!!
    }

    /**
     * 根据几何类型生成默认样式
     */
    private fun buildDefaultStyle(geomType: String): StyleConfig {
        val config = StyleConfig()
        when (geomType) {
            "点" -> {
                config.pointColor = Color.parseColor("#FF6B6B")
                config.pointSize = 10f
            }
            "线" -> {
                config.lineColor = Color.parseColor("#4ECDC4")
                config.lineSize = 2f
            }
            "面" -> {
                config.fillColr = Color.parseColor("#506E7B8B")
                config.lineColor = Color.parseColor("#4ECDC4")
                config.lineSize = 1.5f
            }
            else -> {
                config.pointColor = Color.parseColor("#FFD93D")
                config.pointSize = 8f
            }
        }
        return config
    }

    companion object {
        /**
         * 将 GDAL OGR Geometry 转换为 ArcGIS Geometry（CGCS2000）
         */
        fun convertGdalToArcGISGeometry(gdalGeometry: org.gdal.ogr.Geometry): Geometry? {
            return try {
                when (gdalGeometry.GetGeometryType()) {
                    ogr.wkbPoint, ogr.wkbPoint25D -> {
                        Point(
                            gdalGeometry.GetX(),
                            gdalGeometry.GetY(),
                            SpatialReferences.getWgs84()
                        )
                    }

                    ogr.wkbMultiPoint, ogr.wkbMultiPoint25D -> {
                        // 取第一个点
                        val count = gdalGeometry.GetGeometryCount()
                        if (count > 0) {
                            val pt = gdalGeometry.GetGeometryRef(0)
                            Point(pt.GetX(), pt.GetY(), SpatialReferences.getWgs84())
                        } else null
                    }

                    ogr.wkbLineString, ogr.wkbLineString25D -> {
                        val builder = PolylineBuilder(SpatialReferences.getWgs84())
                        val count = gdalGeometry.GetPointCount()
                        for (i in 0 until count) {
                            val pt = gdalGeometry.GetPoint(i)
                            builder.addPoint(Point(pt[0], pt[1]))
                        }
                        builder.toGeometry()
                    }

                    ogr.wkbMultiLineString, ogr.wkbMultiLineString25D -> {
                        val builder = PolylineBuilder(SpatialReferences.getWgs84())
                        val geomCount = gdalGeometry.GetGeometryCount()
                        for (g in 0 until geomCount) {
                            val line = gdalGeometry.GetGeometryRef(g)
                            val pointCount = line.GetPointCount()
                            for (i in 0 until pointCount) {
                                val pt = line.GetPoint(i)
                                builder.addPoint(Point(pt[0], pt[1]))
                            }
                        }
                        builder.toGeometry()
                    }

                    ogr.wkbPolygon, ogr.wkbPolygon25D -> {
                        val builder = PolygonBuilder(SpatialReferences.getWgs84())
                        val ringCount = gdalGeometry.GetGeometryCount()
                        for (r in 0 until ringCount) {
                            val ring = gdalGeometry.GetGeometryRef(r)
                            val pointCount = ring.GetPointCount()
                            for (i in 0 until pointCount) {
                                val pt = ring.GetPoint(i)
                                builder.addPoint(Point(pt[0], pt[1]))
                            }
                        }
                        builder.toGeometry()
                    }

                    ogr.wkbMultiPolygon, ogr.wkbMultiPolygon25D -> {
                        val builder = PolygonBuilder(SpatialReferences.getWgs84())
                        val polyCount = gdalGeometry.GetGeometryCount()
                        for (p in 0 until polyCount) {
                            val polygon = gdalGeometry.GetGeometryRef(p)
                            val ringCount = polygon.GetGeometryCount()
                            for (r in 0 until ringCount) {
                                val ring = polygon.GetGeometryRef(r)
                                val pointCount = ring.GetPointCount()
                                for (i in 0 until pointCount) {
                                    val pt = ring.GetPoint(i)
                                    builder.addPoint(Point(pt[0], pt[1]))
                                }
                            }
                        }
                        builder.toGeometry()
                    }

                    else -> {
                        "ShpModel: 不支持的几何类型 ${gdalGeometry.GetGeometryName()}".printMsg()
                        null
                    }
                }
            } catch (e: Exception) {
                "ShpModel 几何转换异常: ${e.message}".printMsg()
                null
            }
        }
    }
}