package com.jwch.gwyt_project.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.MotionEvent
import android.widget.*
import androidx.documentfile.provider.DocumentFile
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.coroutines.*
import java.io.File
import com.google.gson.JsonParser

/** 首页独立测试面板；只操作应用测试目录中的副本。 */
class GpkgTestController(
    private val activity: Activity,
    private val map: MapView,
    private val actions: LinearLayout,
    private val status: TextView
) {
    companion object {
        private const val PICK_GPKG = 28701
        private const val PICK_SHP_FOLDER = 28702
        private const val SAVE_ZIP = 28703
        private const val GRAPHIC_FID = "__gdal_internal_fid__"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val root = File(activity.getExternalFilesDir(null) ?: activity.filesDir, "gpkg_test")
    private val prefs = activity.getSharedPreferences("gpkg_test", 0)
    private var currentFile: File? = prefs.getString("file", null)?.let { File(it) }?.takeIf {
        it.isFile && it.canonicalPath.startsWith(root.canonicalPath + File.separator)
    }
    private val displayLayers = mutableListOf<DisplayLayer>()
    private var selectedDisplayLayer: DisplayLayer? = null
    private var selectedGraphic: Graphic? = null
    private var newFeature = false
    private val attributes = linkedMapOf<String, Any?>()
    private var sketch: SketchEditor? = null
    private var previousSketch: SketchEditor? = null
    private var busy = false
    private var disposed = false
    private var exportZip: File? = null
    private val buttons = mutableListOf<Button>()

    private data class DisplayLayer(
        val name: String,
        val overlay: GraphicsOverlay,
        val extent: Envelope?,
        val fields: List<GpkgTestData.EditableField>,
        val geometryType: GeometryType
    )

    init {
        button("1 创建点线面 GPKG") { ensureClean { task("创建测试数据") {
            unload()
            val file = withContext(Dispatchers.IO) { GpkgTestData.createSample(root) }
            setFile(file)
            message("创建成功", "已创建点、线、带洞面三个图层，并关闭重读核验。\n$file\n下一步点击“读取信息”或“加载地图”。")
        } } }
        button("2 SHP → GPKG") { ensureClean {
            message("选择 SHP 所在目录", "接下来选择包含 SHP 的文件夹，再选择其中的数据集。需要同名 shp、shx、dbf、prj 文件；如有 cpg 也会复制。") {
                activity.startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), PICK_SHP_FOLDER)
            }
        } }
        button("3 选择 GPKG") { ensureClean {
            activity.startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE)
            }, PICK_GPKG)
        } }
        button("4 读取信息") { ensureClean { task("读取图层、字段和坐标系") {
            val file = requireFile()
            unload()
            message("GPKG 信息", withContext(Dispatchers.IO) { GpkgTestData.inspect(file) })
        } } }
        button("5 加载地图") { ensureClean { task("加载 GPKG") { loadMap(true) } } }
        button("6 选择要素") { ensureClean { chooseLayer() } }
        button("查看所选属性") {
            val graphic = selectedGraphic
            if (graphic != null) {
                message("${selectedDisplayLayer?.name ?: "GPKG 图层"} 属性",
                    graphic.attributes.entries.filter { it.key != GRAPHIC_FID }
                        .joinToString("\n") { "${it.key} = ${it.value ?: "NULL"}" })
            } else error("请先在地图选择要素")
        }
        button("新增要素") { ensureClean { addFeature() } }
        button("删除要素") { ensureClean { deleteFeature() } }
        button("7 编辑形状") { editGeometry() }
        button("撤销节点修改") {
            val editor = sketch ?: error("请先进入形状编辑")
            editor.undo()
        }
        button("8 编辑属性") { editAttributes() }
        button("9 保存修改") { save() }
        button("取消编辑") { cancelEdit(); refreshStatus("已取消草稿，文件未改变") }
        button("10 重开核验") { ensureClean { task("关闭重开核验") {
            val file = requireFile()
            unload()
            val report = withContext(Dispatchers.IO) { GpkgTestData.inspect(file) }
            loadMap(false)
            message("已从磁盘重新读取", report)
        } } }
        button("11 导出 SHP ZIP") { ensureClean { task("导出 SHP 并重读检查") {
            val file = requireFile()
            unload()
            exportZip = withContext(Dispatchers.IO) { GpkgTestData.exportShp(file, root) }
            message("导出完成", "${exportZip!!.absolutePath}\n\n已核对数量、字段名和坐标系。ZIP 内包含配套文件和核验报告；全部属性及几何仍需桌面复核。点击确定选择另存位置。") {
                activity.startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "application/zip"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, exportZip!!.name)
                }, SAVE_ZIP)
            }
        } } }
        button("退出地图测试") { ensureClean { unload(); refreshStatus("已退出测试图层，恢复业务地图操作") } }
        refreshStatus()
    }

    private fun button(title: String, action: () -> Unit) {
        val button = Button(activity).apply {
            text = title
            textSize = 12f
            setOnClickListener {
                if (!busy && !disposed) {
                    try { action() } catch (e: Exception) { message("操作失败", errorText(e)) }
                }
            }
        }
        actions.addView(button)
        buttons.add(button)
    }

    private fun task(label: String, block: suspend () -> Unit) {
        if (busy || disposed) return
        busy = true
        buttons.forEach { it.isEnabled = false }
        refreshStatus("$label…")
        scope.launch {
            try {
                block()
                refreshStatus("$label 完成")
            } catch (e: Exception) {
                refreshStatus("$label 失败")
                message("$label 失败", errorText(e))
            } catch (e: LinkageError) {
                refreshStatus("原生库加载失败")
                message("原生库错误", "${e.message}\n请检查当前 GDAL/ArcGIS 库与设备架构。")
            } finally {
                busy = false
                if (disposed) { unload(); scope.cancel() }
                else buttons.forEach { it.isEnabled = true }
            }
        }
    }

    private fun errorText(e: Exception): String {
        android.util.Log.e("GpkgTest", "GPKG 测试失败", e)
        return generateSequence(e as Throwable?) { it.cause }.take(5)
            .joinToString("\n") { "${it.javaClass.simpleName}: ${it.message}" }
    }

    private fun message(title: String, body: String, after: (() -> Unit)? = null) {
        if (disposed || activity.isFinishing || activity.isDestroyed) return
        val text = TextView(activity).apply {
            setPadding(24, 20, 24, 20); setTextIsSelectable(true); this.text = body
        }
        AlertDialog.Builder(activity).setTitle(title)
            .setView(ScrollView(activity).apply { addView(text) })
            .setPositiveButton("确定") { _, _ -> after?.invoke() }.show()
    }

    private fun refreshStatus(extra: String = "") {
        if (disposed) return
        status.text = "${currentFile?.name ?: "尚未选择 GPKG"} | ${selectedDisplayLayer?.name ?: "未选图层"}" +
            " | ${if (selectedGraphic != null) "已选要素" else "未选要素"}" +
            (if (sketch != null || attributes.isNotEmpty()) " | 有未保存编辑" else "") + "\n$extra"
    }

    private fun requireFile(): File = currentFile?.takeIf { it.isFile } ?: error("请先创建或导入 GPKG")
    private fun setFile(file: File) {
        currentFile = file
        prefs.edit().putString("file", file.absolutePath).apply()
        refreshStatus()
    }

    private fun ensureClean(action: () -> Unit) {
        if (sketch == null && attributes.isEmpty() && !newFeature) action()
        else message("有未保存编辑", "请先点击“保存修改”或“取消编辑”，再切换文件、图层或导出。")
    }

    private suspend fun loadMap(zoom: Boolean) {
        check(!disposed) { "页面已关闭" }
        val file = requireFile()
        unload()
        val contents = withContext(Dispatchers.IO) { readDisplayLayers(file) }
        check(contents.isNotEmpty()) { "GPKG 中没有可显示的矢量图层" }
        contents.forEach { content ->
            val overlay = GraphicsOverlay()
            val symbol = when (content.geometryType) {
                GeometryType.POINT, GeometryType.MULTIPOINT -> SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 12f)
                GeometryType.POLYLINE -> SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.MAGENTA, 3f)
                else -> SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x4433CC66,
                    SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(0, 150, 60), 2f))
            }
            content.features.forEach { (geometry, attributes) -> overlay.graphics.add(Graphic(geometry, attributes, symbol)) }
            map.graphicsOverlays.add(overlay)
            displayLayers.add(DisplayLayer(content.name, overlay, content.extent, content.fields, content.geometryType))
        }
        selectedDisplayLayer = displayLayers.first()
        if (zoom) {
            val extent = displayLayers.mapNotNull { it.extent }.firstOrNull { !it.isEmpty }
            if (extent != null) {
                if (extent.width == 0.0 && extent.height == 0.0) map.setViewpointCenterAsync(extent.center, 10000.0)
                else map.setViewpointGeometryAsync(extent, 80.0)
            }
        }
        refreshStatus("已通过 GDAL 绘制 ${displayLayers.size} 个图层；点击“选择要素”选图层，再点地图。当前模式为显示/属性查看。")
    }

    private data class DisplayContent(
        val name: String,
        val geometryType: GeometryType,
        val extent: Envelope?,
        val fields: List<GpkgTestData.EditableField>,
        val features: List<Pair<Geometry, Map<String, Any>>>
    )

    /** 只用 GDAL 读 GPKG，转换为 ArcGIS GraphicsOverlay 图形，避开受授权限制的 GeoPackageFeatureTable。 */
    private fun readDisplayLayers(file: File): List<DisplayContent> {
        check(ShpLoader.initGDAL()) { "GDAL 初始化失败" }
        val source = org.gdal.ogr.ogr.Open(file.absolutePath, 0)
            ?: error("GDAL 无法打开 GPKG：${org.gdal.gdal.gdal.GetLastErrorMsg()}")
        try {
            val result = mutableListOf<DisplayContent>()
            for (i in 0 until source.GetLayerCount()) {
                val layer = source.GetLayer(i) ?: continue
                val srs = layer.GetSpatialRef() ?: error("图层 ${layer.GetName()} 缺少坐标系，无法安全定位")
                val geoJsonSrs = srs.GetAuthorityCode(null)?.toIntOrNull()?.let { "\"wkid\":$it" }
                    ?: "\"wkt\":${com.google.gson.Gson().toJson(srs.ExportToWkt())}"
                val extentValues = layer.GetExtent()
                val extent = if (extentValues != null && extentValues.size >= 4) {
                    val esriJson = "{\"xmin\":${extentValues[0]},\"ymin\":${extentValues[2]},\"xmax\":${extentValues[1]},\"ymax\":${extentValues[3]},\"spatialReference\":{$geoJsonSrs}}"
                    Geometry.fromJson(esriJson) as? Envelope
                } else null
                val definition = layer.GetLayerDefn()
                val fields = (0 until definition.GetFieldCount()).mapNotNull { fieldIndex ->
                    val field = definition.GetFieldDefn(fieldIndex)
                    if (field.GetFieldType() in listOf(
                            org.gdal.ogr.ogr.OFTString, org.gdal.ogr.ogr.OFTInteger,
                            org.gdal.ogr.ogr.OFTInteger64, org.gdal.ogr.ogr.OFTReal
                        )) GpkgTestData.EditableField(field.GetName(), field.GetFieldType(), field.GetTypeName(),
                            field.IsNullable() != 0, field.GetWidth()) else null
                }
                val features = mutableListOf<Pair<Geometry, Map<String, Any>>>()
                layer.ResetReading()
                var feature = layer.GetNextFeature()
                while (feature != null) {
                    try {
                        val ogrGeometry = feature.GetGeometryRef()
                        if (ogrGeometry != null) {
                            val geo = JsonParser.parseString(ogrGeometry.ExportToJson()).asJsonObject
                            val coords = geo.get("coordinates") ?: error("图层 ${layer.GetName()} 存在空几何")
                            val esriType = when (geo.get("type").asString) {
                                "Point" -> "\"x\":${coords.asJsonArray[0]},\"y\":${coords.asJsonArray[1]}"
                                "MultiPoint" -> "\"points\":$coords"
                                "LineString" -> "\"paths\":[${coords}]"
                                "MultiLineString" -> "\"paths\":$coords"
                                "Polygon" -> "\"rings\":$coords"
                                "MultiPolygon" -> {
                                    val rings = coords.asJsonArray.flatMap { polygon -> polygon.asJsonArray.map { it } }
                                    "\"rings\":${com.google.gson.Gson().toJsonTree(rings)}"
                                }
                                else -> error("暂不支持 ${geo.get("type").asString} 几何")
                            }
                            val geometryJson = "{$esriType,\"spatialReference\":{$geoJsonSrs}}"
                            val geometry = Geometry.fromJson(geometryJson)
                            val attrs = linkedMapOf<String, Any>(GRAPHIC_FID to feature.GetFID())
                            for (fieldIndex in 0 until definition.GetFieldCount()) {
                                val field = definition.GetFieldDefn(fieldIndex)
                                attrs[field.GetName()] = if (feature.IsFieldSetAndNotNull(fieldIndex))
                                    feature.GetFieldAsString(fieldIndex) else "NULL"
                            }
                            features.add(geometry to attrs)
                        }
                    } finally { feature.delete() }
                    feature = layer.GetNextFeature()
                }
                val geometryType = when (layer.GetGeomType() and 0xff) {
                    org.gdal.ogr.ogr.wkbPoint -> GeometryType.POINT
                    org.gdal.ogr.ogr.wkbMultiPoint -> GeometryType.MULTIPOINT
                    org.gdal.ogr.ogr.wkbLineString, org.gdal.ogr.ogr.wkbMultiLineString -> GeometryType.POLYLINE
                    org.gdal.ogr.ogr.wkbPolygon, org.gdal.ogr.ogr.wkbMultiPolygon -> GeometryType.POLYGON
                    else -> error("暂不支持图层 ${layer.GetName()} 的几何类型 ${org.gdal.ogr.ogr.GeometryTypeToName(layer.GetGeomType())}")
                }
                if (features.isNotEmpty()) result.add(DisplayContent(layer.GetName(), geometryType, extent, fields, features))
            }
            return result
        } finally { source.delete() }
    }

    private fun chooseLayer() {
        check(displayLayers.isNotEmpty()) { "请先加载地图" }
        AlertDialog.Builder(activity).setTitle("选择地图图层")
            .setItems(displayLayers.map { it.name }.toTypedArray()) { _, index ->
                selectedGraphic = null
                selectedDisplayLayer = displayLayers[index]
                val extent = displayLayers[index].extent
                if (extent != null && !extent.isEmpty) {
                    if (extent.width == 0.0 && extent.height == 0.0) map.setViewpointCenterAsync(extent.center, 10000.0)
                    else map.setViewpointGeometryAsync(extent, 80.0)
                }
                refreshStatus("点击地图要素可查看属性")
            }.setNegativeButton("取消", null).show()
    }

    /** 由原有 tapInterceptor 转发，避免另设触摸监听器破坏业务地图。 */
    fun onTap(event: MotionEvent): Boolean {
        val displayLayer = selectedDisplayLayer
        if (displayLayer != null) {
            if (busy || disposed || sketch != null) return true
            if (attributes.isNotEmpty()) {
                message("有未保存属性", "请先保存或取消编辑，再选择其他要素。")
                return true
            }
            task("选择要素") {
                val future = map.identifyGraphicsOverlayAsync(displayLayer.overlay,
                    android.graphics.Point(event.x.toInt(), event.y.toInt()), 16.0, false, 20)
                val result = withContext(Dispatchers.IO) { future.get() }
                val graphics = result.graphics
                check(graphics.isNotEmpty()) { "没有点中当前图层要素，请放大后重试" }
                if (graphics.size == 1) {
                    selectedGraphic = graphics.first()
                    refreshStatus("已选择 FID ${graphics.first().attributes[GRAPHIC_FID]}；可查看属性。")
                } else {
                    AlertDialog.Builder(activity).setTitle("选择重叠要素")
                        .setItems(graphics.map { "FID ${it.attributes[GRAPHIC_FID]}" }.toTypedArray()) { _, index ->
                            selectedGraphic = graphics[index]
                            refreshStatus("已选择 FID ${graphics[index].attributes[GRAPHIC_FID]}；可查看属性。")
                        }.setNegativeButton("取消", null).show()
                }
            }
            return true
        }
        return false
    }

    private fun editGeometry() {
        val graphic = selectedGraphic ?: error("请先在地图选择要素")
        val layer = selectedDisplayLayer ?: error("请先选择图层")
        check(sketch == null) { "已经在编辑形状，可直接操作节点后保存" }
        val geometry = graphic.geometry ?: error("该要素没有几何")
        check(!geometry.hasZ() && !geometry.hasM()) { "本测试暂只编辑二维要素，避免丢失 Z/M" }
        check(!geometry.hasCurves()) { "本测试暂不编辑真曲线，避免改变曲线结构" }
        check(geometry is Point || geometry.geometryType == GeometryType.POLYLINE || geometry.geometryType == GeometryType.POLYGON) {
            "本测试支持点、线、面编辑；多点暂只展示"
        }
        check(layer.extent?.spatialReference != null) { "图层缺少坐标系" }
        previousSketch = map.sketchEditor
        previousSketch?.stop()
        val editor = SketchEditor()
        map.sketchEditor = editor
        try {
            editor.start(geometry)
            sketch = editor
        } catch (e: Exception) {
            map.sketchEditor = previousSketch
            previousSketch = null
            throw e
        }
        refreshStatus("拖动节点修改形状；完成后点击“保存修改”，将由 GDAL 写回 GPKG")
    }

    private fun addFeature() {
        val layer = selectedDisplayLayer ?: error("请先加载地图并选择目标图层")
        check(layer.extent?.spatialReference != null) { "图层缺少坐标系" }
        val mode = when (layer.geometryType) {
            GeometryType.POINT -> SketchCreationMode.POINT
            GeometryType.POLYLINE -> SketchCreationMode.POLYLINE
            GeometryType.POLYGON -> SketchCreationMode.POLYGON
            else -> error("新增测试仅支持点、线、面图层")
        }
        previousSketch = map.sketchEditor
        previousSketch?.stop()
        val editor = SketchEditor()
        map.sketchEditor = editor
        try {
            editor.start(mode)
            selectedGraphic = null
            attributes.clear()
            newFeature = true
            sketch = editor
        } catch (e: Exception) {
            map.sketchEditor = previousSketch
            previousSketch = null
            throw e
        }
        refreshStatus("在地图绘制新要素；可编辑属性，再点击保存写入 GPKG。取消不会写文件")
    }

    private fun deleteFeature() {
        val graphic = selectedGraphic ?: error("请先选择要删除的要素")
        val layerName = selectedDisplayLayer?.name ?: error("请先选择图层")
        val fid = (graphic.attributes[GRAPHIC_FID] as? Number)?.toLong()
            ?: graphic.attributes[GRAPHIC_FID]?.toString()?.toLongOrNull() ?: error("无法读取 FID")
        val file = requireFile()
        AlertDialog.Builder(activity).setTitle("删除测试副本中的要素？")
            .setMessage("图层 $layerName，FID $fid。只修改当前工作副本；删除后不可撤销。")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ -> task("删除要素") {
                withContext(Dispatchers.IO) { GpkgTestData.deleteFeature(file, layerName, fid) }
                unload()
                try {
                    withContext(Dispatchers.IO) { GpkgTestData.verifyDeleted(file, layerName, fid) }
                    if (!disposed) loadMap(false)
                    message("删除成功", "GDAL 关闭重读核验：FID $fid 已不存在。")
                } catch (e: Exception) {
                    throw IllegalStateException("GDAL 已完成删除，但后续核验/刷新失败，请使用重开核验检查。", e)
                }
            } }.show()
    }

    private data class Input(val field: GpkgTestData.EditableField, val edit: EditText, val nullBox: CheckBox, val old: Any?)

    private fun editAttributes() {
        val layer = selectedDisplayLayer ?: error("请先加载地图并选择图层")
        val graphic = selectedGraphic
        check(graphic != null || newFeature) { "请先在地图选择要素，或先新增要素" }
        val fields = layer.fields
        check(fields.isNotEmpty()) { "没有可编辑的文本/整数/小数属性字段" }
        val form = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 12, 24, 12) }
        val inputs = fields.map { field ->
            val value = if (attributes.containsKey(field.name)) attributes[field.name]
                else graphic?.attributes?.get(field.name)?.takeUnless { it == "NULL" }
            form.addView(TextView(activity).apply { text = "${field.name} (${field.typeName})" })
            val edit = EditText(activity).apply { setText(value?.toString() ?: ""); isSingleLine = true }
            val nullBox = CheckBox(activity).apply {
                text = "空值 NULL"; isChecked = value == null && field.nullable; isEnabled = field.nullable
                setOnCheckedChangeListener { _, checked -> edit.isEnabled = !checked }
            }
            edit.isEnabled = !nullBox.isChecked
            form.addView(edit); form.addView(nullBox)
            Input(field, edit, nullBox, value)
        }
        val dialog = AlertDialog.Builder(activity).setTitle("编辑属性（暂存草稿）")
            .setView(ScrollView(activity).apply { addView(form) })
            .setPositiveButton("暂存", null).setNegativeButton("取消", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                try {
                    val changes = linkedMapOf<String, Any?>()
                    inputs.forEach { input ->
                        val text = input.edit.text.toString()
                        // 未改变的字段完全保留，避免对旧值做无意义类型转换。
                        if (!newFeature && ((input.nullBox.isChecked && input.old == null) ||
                                (!input.nullBox.isChecked && input.old != null && text == input.old.toString()))) {
                            return@forEach
                        }
                        val value: Any? = if (input.nullBox.isChecked) {
                            check(input.field.nullable) { "${input.field.name} 不允许 NULL" }; null
                        } else when (input.field.type) {
                            org.gdal.ogr.ogr.OFTString -> {
                                check(input.field.width <= 0 || text.length <= input.field.width) { "${input.field.name} 长度超限" }; text
                            }
                            org.gdal.ogr.ogr.OFTInteger -> text.toInt()
                            org.gdal.ogr.ogr.OFTInteger64 -> text.toLong()
                            org.gdal.ogr.ogr.OFTReal -> text.toDouble().also { check(it.isFinite()) { "数值必须有限" } }
                            else -> error("不支持该字段类型")
                        }
                        changes[input.field.name] = value
                    }
                    attributes.putAll(changes)
                    dialog.dismiss()
                    refreshStatus("属性已暂存，点击“保存修改”写入文件")
                } catch (e: Exception) { inputError(e) }
            }
        }
        dialog.show()
    }

    private fun inputError(e: Exception) { Toast.makeText(activity, "输入有误：${e.message}", Toast.LENGTH_LONG).show() }

    private fun save() {
        val layer = selectedDisplayLayer ?: error("请先加载地图并选择图层")
        val graphic = selectedGraphic
        check((graphic != null || newFeature) && (sketch != null || attributes.isNotEmpty())) { "没有待保存的编辑" }
        val file = requireFile()
        val adding = newFeature
        val layerName = layer.name
        val changes = LinkedHashMap(attributes)
        var geometryJson: String? = null
        sketch?.let { editor ->
            check(editor.isSketchValid) { "几何不完整，请继续编辑" }
            val shape = editor.geometry ?: error("几何为空")
            check(!shape.isEmpty) { "不允许保存空几何" }
            check(!shape.hasZ() && !shape.hasM() && !shape.hasCurves()) { "当前测试只保存二维直线几何" }
            val targetSrs = graphic?.geometry?.spatialReference ?: layer.extent?.spatialReference
                ?: error("图层缺少坐标系")
            // SketchEditor 输出的面环可能尚未完成方向/闭合等拓扑规范化；先 simplify，
            // 再投影到 GPKG 图层坐标系后检查，避免把正常绘制的三角形误判为无效。
            val normalized = GeometryEngine.simplify(shape)
            val projected = GeometryEngine.project(normalized, targetSrs)
            check(GeometryEngine.isSimple(projected)) { "几何规范化后仍不合法，可能存在自相交或重复节点，请调整后保存" }
            geometryJson = arcGisGeometryToGeoJson(projected)
        }
        task("保存修改") {
            sketch?.stop()
            try {
                val fid = withContext(Dispatchers.IO) {
                    if (adding) {
                        layer.fields.filter { !it.nullable }.forEach { field ->
                            check(changes.containsKey(field.name)) { "新增要素前请编辑必填字段：${field.name}" }
                        }
                        val geometry = geometryJson ?: error("新增要素需要先绘制几何")
                        GpkgTestData.insertFeature(file, layerName, geometry, changes)
                    } else {
                        val id = (graphic?.attributes?.get(GRAPHIC_FID) as? Number)?.toLong()
                            ?: graphic?.attributes?.get(GRAPHIC_FID)?.toString()?.toLongOrNull() ?: error("无法读取 FID")
                        GpkgTestData.updateFeature(file, layerName, id, geometryJson, changes)
                        id
                    }
                }
                cancelEdit()
                unload()
                val report = withContext(Dispatchers.IO) { GpkgTestData.verifySaved(file, layerName, fid, changes) }
                if (!disposed) {
                    loadMap(false)
                    val reloadedLayer = displayLayers.first { it.name == layerName }
                    val reloadedGraphic = reloadedLayer.overlay.graphics.firstOrNull {
                        it.attributes[GRAPHIC_FID]?.toString()?.toLongOrNull() == fid
                    } ?: error("GDAL 写入后重读未找到 FID=$fid")
                    selectedDisplayLayer = reloadedLayer
                    selectedGraphic = reloadedGraphic
                    message("保存成功并已重开", "$report\n\n已由 GDAL 重新读取并在地图上选中该要素。")
                }
            } catch (e: Exception) {
                unload()
                throw IllegalStateException("GDAL 写入已启动，但后续重读/地图刷新失败；请点击“重开核验”检查文件状态。", e)
            }
        }
    }

    private fun arcGisGeometryToGeoJson(geometry: Geometry): String {
        val esri = JsonParser.parseString(geometry.toJson()).asJsonObject
        val json = com.google.gson.JsonObject()
        when (geometry.geometryType) {
            GeometryType.POINT -> {
                json.addProperty("type", "Point")
                json.add("coordinates", com.google.gson.JsonArray().apply {
                    add(esri.get("x")); add(esri.get("y"))
                })
            }
            GeometryType.MULTIPOINT -> {
                json.addProperty("type", "MultiPoint"); json.add("coordinates", esri.get("points"))
            }
            GeometryType.POLYLINE -> {
                val paths = esri.getAsJsonArray("paths")
                if (paths.size() == 1) {
                    json.addProperty("type", "LineString"); json.add("coordinates", paths[0])
                } else {
                    json.addProperty("type", "MultiLineString"); json.add("coordinates", paths)
                }
            }
            GeometryType.POLYGON -> {
                json.addProperty("type", "Polygon"); json.add("coordinates", esri.get("rings"))
            }
            else -> error("当前仅支持点、线、面写回")
        }
        return json.toString()
    }

    private fun cancelEdit() {
        sketch?.stop()
        if (sketch != null) map.sketchEditor = previousSketch
        sketch = null
        previousSketch = null
        attributes.clear()
        if (newFeature) selectedGraphic = null
        newFeature = false
    }

    private fun unload() {
        cancelEdit()
        selectedGraphic = null
        selectedDisplayLayer = null
        displayLayers.forEach { map.graphicsOverlays.remove(it.overlay) }
        displayLayers.clear()
    }

    fun onActivityResult(request: Int, result: Int, data: Intent?): Boolean {
        if (request !in listOf(PICK_GPKG, PICK_SHP_FOLDER, SAVE_ZIP)) return false
        if (result != Activity.RESULT_OK) return true
        val uri = data?.data ?: return true
        when (request) {
            PICK_GPKG -> task("复制并检查 GPKG") {
                val file = withContext(Dispatchers.IO) {
                    val output = File(GpkgTestData.newDirectory(root, "selected"), "work.gpkg")
                    activity.contentResolver.openInputStream(uri)?.use { input -> output.outputStream().use { input.copyTo(it) } }
                        ?: error("无法读取所选文件")
                    GpkgTestData.inspect(output) // 先验证，成功才切换当前文件。
                    output
                }
                unload(); setFile(file)
                message("已导入工作副本", "$file\n后续编辑只修改此副本。点击“加载地图”继续。")
            }
            PICK_SHP_FOLDER -> task("读取 SHP 目录") {
                val documents = withContext(Dispatchers.IO) {
                    DocumentFile.fromTreeUri(activity, uri)?.listFiles()?.filter {
                        it.isFile && it.name?.endsWith(".shp", true) == true
                    }.orEmpty()
                }
                check(documents.isNotEmpty()) { "所选目录没有 SHP，请选择文件直接所在目录" }
                AlertDialog.Builder(activity).setTitle("选择要转换的 SHP")
                    .setItems(documents.map { it.name ?: "SHP" }.toTypedArray()) { _, index -> importShp(uri, documents[index]) }
                    .setNegativeButton("取消", null).show()
            }
            SAVE_ZIP -> task("另存 ZIP") {
                val zip = exportZip?.takeIf { it.isFile } ?: error("导出文件已失效，请重新导出")
                withContext(Dispatchers.IO) {
                    activity.contentResolver.openOutputStream(uri, "wt")?.use { output -> zip.inputStream().use { it.copyTo(output) } }
                        ?: error("无法写入所选位置")
                }
                message("另存成功", "SHP ZIP 已保存至你选择的位置。")
            }
        }
        return true
    }

    private fun importShp(folderUri: Uri, selectedDocument: DocumentFile) = task("SHP 转 GPKG") {
        val output = withContext(Dispatchers.IO) {
            val directory = GpkgTestData.newDirectory(root, "shp_source")
            val base = selectedDocument.name!!.substringBeforeLast('.')
            val siblings = DocumentFile.fromTreeUri(activity, folderUri)?.listFiles().orEmpty()
            siblings.filter { it.isFile && it.name?.substringBeforeLast('.')?.equals(base, true) == true }.forEach { doc ->
                val ext = doc.name!!.substringAfterLast('.').lowercase()
                if (ext in listOf("shp", "shx", "dbf", "prj", "cpg")) {
                    val target = File(directory, "source.$ext")
                    activity.contentResolver.openInputStream(doc.uri)?.use { input -> target.outputStream().use { input.copyTo(it) } }
                        ?: error("无法读取 ${doc.name}")
                }
            }
            GpkgTestData.importShp(File(directory, "source.shp"), root)
        }
        unload(); setFile(output)
        message("转换完成", "$output\n已保留原坐标系并核对要素、字段数量。点击“读取信息”或“加载地图”继续。")
    }

    fun dispose() {
        disposed = true
        // 先从地图解绑；Activity 随后 dispose MapView，后续后台清理不得再访问地图。
        cancelEdit()
        // 正在进行的持久化/复制任务完成后再关闭句柄，避免中途损坏测试文件。
        if (!busy) { unload(); scope.cancel() }
    }
}
