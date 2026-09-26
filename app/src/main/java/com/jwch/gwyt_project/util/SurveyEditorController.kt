package com.jwch.gwyt_project.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.MotionEvent
import android.widget.*
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

/** SHP 编辑面板；GDAL 在每个数据集的 GPKG 工作副本上读写。 */
class SurveyEditorController(
    private val activity: Activity,
    private val map: MapView,
    private val actions: LinearLayout,
    private val status: TextView,
    private val primaryActions: LinearLayout,
    private val titleView: TextView
) {
    companion object {
        private const val SAVE_ZIP = 28703
        private const val GRAPHIC_FID = "__gdal_internal_fid__"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val root = File(activity.getExternalFilesDir(null) ?: activity.filesDir, "shp_workspace")
    private val prefs = activity.getSharedPreferences("shp_editor", 0)
    private var currentFile: File? = prefs.getString("file", null)?.let { File(it) }?.takeIf {
        it.isFile && it.canonicalPath.startsWith(root.canonicalPath + File.separator)
    }
    private val displayLayers = mutableListOf<DisplayLayer>()
    private val selectionOverlay = GraphicsOverlay()
    private var selectedDisplayLayer: DisplayLayer? = null
    private var selectedGraphic: Graphic? = null
    private var newFeature = false
    private val attributes = linkedMapOf<String, Any?>()
    private var sketch: SketchEditor? = null
    private var previousSketch: SketchEditor? = null
    private var busy = false
    private var disposed = false
    private var exportZip: File? = null
    private var sourceName: String = prefs.getString("source_name", null) ?: "SHP"
    private val buttons = mutableListOf<Button>()
    var onOpened: (() -> Unit)? = null
    var onSaved: ((File) -> Unit)? = null
    private var workspaceActive = false
    val isWorkspaceOpen: Boolean get() = workspaceActive
    private var selectionMode = false
    private var drawingFinished = false
    private var pendingLeave: (() -> Unit)? = null
    private var queuedExport = false
    private var note = ""

    private data class DisplayLayer(
        val name: String,
        val overlay: GraphicsOverlay,
        val extent: Envelope?,
        val fields: List<GpkgTestData.EditableField>,
        val geometryType: GeometryType
    )

    init { refreshStatus() }

    fun exportShp() = ensureClean {
        if (!busy) AlertDialog.Builder(activity).setTitle("导出成果")
            .setMessage("作业：$sourceName\n要素数量：${displayLayers.sumOf { it.overlay.graphics.size }}\n格式：SHP ZIP\n\n导出前检查数量、字段名和坐标系。")
            .setNegativeButton("取消", null)
            .setPositiveButton("导出并保存") { _, _ -> task("导出 SHP") {
            val file = requireFile()
            unload()
            exportZip = withContext(Dispatchers.IO) { GpkgTestData.exportShp(file, root, sourceName) }
            loadMap(false)
            activity.startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "application/zip"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, exportZip!!.name)
                }, SAVE_ZIP)
            } }.show()
    }

    fun openWorkspace(file: File, name: String, exportAfter: Boolean = false) = ensureClean {
        task("打开 $name") {
            check(file.isFile && file.canonicalPath.startsWith(root.canonicalPath + File.separator)) { "作业数据已失效，请重新导入" }
            unload()
            sourceName = name
            workspaceActive = true
            selectionMode = false
            setFile(file)
            loadMap(true)
            onOpened?.invoke()
            queuedExport = exportAfter
        }
    }

    fun selectLayer() = ensureClean { if (!busy) chooseLayer() }

    fun showMore() = ensureClean {
        if (!busy) AlertDialog.Builder(activity).setTitle("作业工具")
            .setItems(arrayOf("数据详情", "重新读取")) { _, index ->
                task(if (index == 0) "数据详情" else "重新读取") {
                    val file = requireFile()
                    if (index == 0) message("数据详情", withContext(Dispatchers.IO) { GpkgTestData.inspect(file) })
                    else loadMap(false)
                }
            }.show()
    }

    fun requestLeave(after: () -> Unit) {
        if (busy) { Toast.makeText(activity, "正在处理数据，请稍候", Toast.LENGTH_SHORT).show(); return }
        fun leave() { unload(); workspaceActive = false; selectionMode = false; pendingLeave = null; after() }
        if (sketch == null && attributes.isEmpty() && !newFeature) leave()
        else AlertDialog.Builder(activity).setTitle("有未保存修改")
            .setMessage("保存当前要素后返回作业列表？")
            .setPositiveButton("保存") { _, _ ->
                pendingLeave = { leave() }
                try { save() } catch (e: Exception) { pendingLeave = null; message("无法保存", errorText(e)) }
            }.setNegativeButton("放弃修改") { _, _ -> leave() }
            .setNeutralButton("继续编辑", null).show()
    }

    private fun button(title: String, host: LinearLayout = actions, action: () -> Unit) {
        val button = Button(activity).apply {
            text = title
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundResource(com.jwch.gwyt_project.R.drawable.bg_import_btn)
            val margin = (activity.resources.displayMetrics.density * 4).toInt()
            layoutParams = LinearLayout.LayoutParams(if (host === primaryActions) -2 else -1, (activity.resources.displayMetrics.density * 46).toInt()).apply {
                setMargins(margin, margin, margin, margin)
            }
            isEnabled = !busy
            setOnClickListener {
                if (!busy && !disposed) {
                    try { action() } catch (e: Exception) { message("操作失败", errorText(e)) }
                }
            }
        }
        host.addView(button)
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
                pendingLeave = null
                refreshStatus("$label 失败")
                message("$label 失败", errorText(e))
            } catch (e: LinkageError) {
                pendingLeave = null
                refreshStatus("原生库加载失败")
                message("原生库错误", "${e.message}\n请检查当前 GDAL/ArcGIS 库与设备架构。")
            } finally {
                busy = false
                if (disposed) { unload(); scope.cancel() }
                else {
                    refreshStatus()
                    if (queuedExport) { queuedExport = false; exportShp() }
                    pendingLeave?.let { callback -> pendingLeave = null; callback() }
                }
            }
        }
    }

    private fun errorText(e: Exception): String {
        android.util.Log.e("ShpEditor", "SHP 编辑失败", e)
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
        if (extra.isNotBlank()) note = extra
        val dirty = sketch != null || attributes.isNotEmpty() || newFeature
        titleView.text = "$sourceName  ·  ${if (busy) "处理中" else if (dirty) "未保存" else "已保存"}"
        status.text = "${selectedDisplayLayer?.name ?: "图层未加载"}\n" +
            when {
                sketch != null -> if (drawingFinished) "绘制已完成，请填写属性并保存" else "正在${if (newFeature) "绘制新要素" else "修改形状"}"
                selectedGraphic != null -> "已选择要素 ${selectedGraphic!!.attributes[GRAPHIC_FID]}"
                selectionMode -> "点击地图选择一个要素"
                else -> "浏览地图 · 拖动或缩放查看界线"
            } + "\n$note"
        refreshSelectionHighlight()
        renderActions()
    }

    private fun refreshSelectionHighlight() {
        selectionOverlay.graphics.clear()
        // identify 返回的 Graphic 可能是不同的包装对象，用图层和 FID 匹配原图形。
        val selectedFid = selectedGraphic?.attributes?.get(GRAPHIC_FID)?.toString()
        displayLayers.forEach { layer ->
            layer.overlay.graphics.forEach featureLoop@ { graphic ->
                graphic.isSelected = false
                if (selectedFid != null && layer === selectedDisplayLayer &&
                    graphic.attributes[GRAPHIC_FID]?.toString() == selectedFid) {
                    val geometry = graphic.geometry ?: return@featureLoop
                    val color = Color.rgb(255, 215, 64)
                    val symbol = when (geometry.geometryType) {
                        GeometryType.POINT, GeometryType.MULTIPOINT ->
                            SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, color, 20f)
                        GeometryType.POLYLINE ->
                            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, 6f)
                        else -> SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x55FFD740,
                            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, 4f))
                    }
                    selectionOverlay.graphics.add(Graphic(geometry, symbol))
                }
            }
        }
    }

    private fun renderActions() {
        actions.removeAllViews(); primaryActions.removeAllViews(); buttons.clear()
        (actions.parent as? ScrollView)?.visibility = android.view.View.GONE
        if (sketch != null) {
            button("撤销节点", primaryActions) { sketch?.undo() }
            button("取消", primaryActions) { cancelEdit(); refreshStatus("已取消修改") }
            button(if (drawingFinished) "填写属性并保存" else "完成绘制", primaryActions) {
                check(sketch?.isSketchValid == true) { "几何不完整，请继续绘制" }
                drawingFinished = true
                refreshStatus("确认属性后保存要素")
                if (newFeature && selectedDisplayLayer?.fields?.isNotEmpty() == true) editAttributes(true)
                else save()
            }
            return
        }
        button(if (!selectionMode) "● 浏览" else "浏览", primaryActions) { ensureClean {
            selectionMode = false; selectedGraphic = null; refreshStatus("浏览地图")
        } }
        button(if (selectionMode) "● 选择要素" else "选择要素", primaryActions) { ensureClean {
            selectionMode = true; refreshStatus("点击地图选择一个要素")
        } }
        button("＋ 新增要素", primaryActions) { ensureClean { addFeature() } }
        val graphic = selectedGraphic
        if (graphic != null) {
            actions.addView(TextView(activity).apply {
                text = graphic.attributes.entries.filter { it.key != GRAPHIC_FID }
                    .joinToString("\n") { "${it.key}：${it.value ?: "空"}" }
                setTextColor(Color.WHITE); textSize = 14f; setPadding(8, 8, 8, 8)
            })
            button("编辑属性") { editAttributes(true) }
            button("编辑形状") { editGeometry() }
            button("更多操作") {
                AlertDialog.Builder(activity).setTitle("要素操作").setItems(arrayOf("删除要素")) { _, _ -> deleteFeature() }.show()
            }
        }
        if (attributes.isNotEmpty()) {
            button("保存要素") { save() }
            button("取消修改") { cancelEdit(); refreshStatus("已取消修改") }
        }
        (actions.parent as? ScrollView)?.visibility = if (actions.childCount == 0) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun requireFile(): File = currentFile?.takeIf { it.isFile } ?: error("请先从勘界作业列表进入作业")
    private fun setFile(file: File) {
        currentFile = file
        prefs.edit().putString("file", file.absolutePath).apply()
        refreshStatus()
    }

    private fun ensureClean(action: () -> Unit) {
        if (sketch == null && attributes.isEmpty() && !newFeature) action()
        else message("有未保存修改", "请先完成绘制并保存要素，或取消当前修改，再切换图层或导出。")
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
        map.graphicsOverlays.add(selectionOverlay)
        selectedDisplayLayer = displayLayers.first()
        if (zoom && displayLayers.any { it.overlay.graphics.isNotEmpty() }) {
            val extent = displayLayers.mapNotNull { it.extent }.firstOrNull { !it.isEmpty }
            if (extent != null) {
                if (extent.width == 0.0 && extent.height == 0.0) map.setViewpointCenterAsync(extent.center, 10000.0)
                else map.setViewpointGeometryAsync(extent, 80.0)
            }
        }
        refreshStatus("${displayLayers.sumOf { it.overlay.graphics.size }} 个要素 · 点击底部工具开始作业")
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
                val extentValues = if (layer.GetFeatureCount().toLong() == 0L) doubleArrayOf(0.0, 0.0, 0.0, 0.0) else layer.GetExtent()
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
                result.add(DisplayContent(layer.GetName(), geometryType, extent, fields, features))
            }
            return result
        } finally { source.delete() }
    }

    private fun chooseLayer() {
        check(displayLayers.isNotEmpty()) { "请先加载地图" }
        AlertDialog.Builder(activity).setTitle("选择地图图层")
            .setItems(displayLayers.map { it.name }.toTypedArray()) { _, index ->
                selectedGraphic = null
                displayLayers.forEach { it.overlay.graphics.forEach { graphic -> graphic.isSelected = false } }
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
        if (!workspaceActive) return false
        if (!selectionMode && sketch == null) return true
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
        return true
    }

    private fun editGeometry() {
        val graphic = selectedGraphic ?: error("请先在地图选择要素")
        val layer = selectedDisplayLayer ?: error("请先选择图层")
        check(sketch == null) { "已经在编辑形状，可直接操作节点后保存" }
        val geometry = graphic.geometry ?: error("该要素没有几何")
        check(!geometry.hasZ() && !geometry.hasM()) { "当前只支持二维要素编辑" }
        check(!geometry.hasCurves()) { "当前暂不支持真曲线编辑" }
        check(geometry is Point || geometry.geometryType == GeometryType.POLYLINE || geometry.geometryType == GeometryType.POLYGON) {
            "支持点、线、面编辑；多点要素暂只展示"
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
        refreshStatus("拖动节点修改形状，完成后点击底部“完成绘制”")
    }

    private fun addFeature() {
        val layer = selectedDisplayLayer ?: error("请先加载地图并选择目标图层")
        check(layer.extent?.spatialReference != null) { "图层缺少坐标系" }
        val mode = when (layer.geometryType) {
            GeometryType.POINT -> SketchCreationMode.POINT
            GeometryType.POLYLINE -> SketchCreationMode.POLYLINE
            GeometryType.POLYGON -> SketchCreationMode.POLYGON
            else -> error("新增要素支持点、线、面图层")
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
        refreshStatus("在地图绘制要素，完成后填写属性并保存")
    }

    private fun deleteFeature() {
        val graphic = selectedGraphic ?: error("请先选择要删除的要素")
        val layerName = selectedDisplayLayer?.name ?: error("请先选择图层")
        val fid = (graphic.attributes[GRAPHIC_FID] as? Number)?.toLong()
            ?: graphic.attributes[GRAPHIC_FID]?.toString()?.toLongOrNull() ?: error("无法读取 FID")
        val file = requireFile()
        AlertDialog.Builder(activity).setTitle("删除所选要素？")
            .setMessage("图层 $layerName，FID $fid。只修改当前工作副本；删除后不可撤销。")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ -> task("删除要素") {
                withContext(Dispatchers.IO) { GpkgTestData.deleteFeature(file, layerName, fid) }
                onSaved?.invoke(file)
                unload()
                try {
                    withContext(Dispatchers.IO) { GpkgTestData.verifyDeleted(file, layerName, fid) }
                    if (!disposed) loadMap(false)
                    Toast.makeText(activity, "要素已删除", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    throw IllegalStateException("GDAL 已完成删除，但后续核验/刷新失败，请使用重开核验检查。", e)
                }
            } }.show()
    }

    private data class Input(val field: GpkgTestData.EditableField, val edit: EditText, val nullBox: CheckBox, val old: Any?)

    private fun editAttributes(saveAfter: Boolean = false) {
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
        val dialog = AlertDialog.Builder(activity).setTitle(if (newFeature) "填写新要素属性" else "编辑要素属性")
            .setView(ScrollView(activity).apply { addView(form) })
            .setPositiveButton(if (saveAfter) "保存要素" else "暂存", null).setNegativeButton("取消", null).create()
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
                    if (saveAfter && (sketch != null || attributes.isNotEmpty())) save()
                    else refreshStatus("属性没有变化")
                    dialog.dismiss()
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
            check(!shape.hasZ() && !shape.hasM() && !shape.hasCurves()) { "当前只支持二维直线几何" }
            val targetSrs = graphic?.geometry?.spatialReference ?: layer.extent?.spatialReference
                ?: error("图层缺少坐标系")
            // SketchEditor 输出的面环可能尚未完成方向/闭合等拓扑规范化；先 simplify，
            // 再投影到 GPKG 图层坐标系后检查，避免把正常绘制的三角形误判为无效。
            val normalized = GeometryEngine.simplify(shape)
            val projected = GeometryEngine.project(normalized, targetSrs)
            check(GeometryEngine.isSimple(projected)) { "几何规范化后仍不合法，可能存在自相交或重复节点，请调整后保存" }
            geometryJson = arcGisGeometryToGeoJson(projected)
        }
        if (adding) layer.fields.filter { !it.nullable }.forEach { field ->
            check(changes.containsKey(field.name) && changes[field.name] != null) { "请填写必填字段：${field.name}" }
        }
        task("保存修改") {
            var written = false
            try {
                val fid = withContext(Dispatchers.IO) {
                    if (adding) {
                        val geometry = geometryJson ?: error("新增要素需要先绘制几何")
                        GpkgTestData.insertFeature(file, layerName, geometry, changes)
                    } else {
                        val id = (graphic?.attributes?.get(GRAPHIC_FID) as? Number)?.toLong()
                            ?: graphic?.attributes?.get(GRAPHIC_FID)?.toString()?.toLongOrNull() ?: error("无法读取 FID")
                        GpkgTestData.updateFeature(file, layerName, id, geometryJson, changes)
                        id
                    }
                }
                written = true
                cancelEdit()
                unload()
                withContext(Dispatchers.IO) { GpkgTestData.verifySaved(file, layerName, fid, changes) }
                if (!disposed) {
                    loadMap(false)
                    val reloadedLayer = displayLayers.first { it.name == layerName }
                    val reloadedGraphic = reloadedLayer.overlay.graphics.firstOrNull {
                        it.attributes[GRAPHIC_FID]?.toString()?.toLongOrNull() == fid
                    } ?: error("GDAL 写入后重读未找到 FID=$fid")
                    selectedDisplayLayer = reloadedLayer
                    selectedGraphic = reloadedGraphic
                    selectionMode = true
                    onSaved?.invoke(file)
                    Toast.makeText(activity, "已保存并核验", Toast.LENGTH_SHORT).show()
                    refreshStatus("要素已保存")
                }
            } catch (e: Exception) {
                if (written) {
                    unload()
                    throw IllegalStateException("数据已写入，但重读核验失败。请从“更多 → 重新读取”检查数据。", e)
                }
                throw e
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
        drawingFinished = false
    }

    private fun unload() {
        cancelEdit()
        selectionOverlay.graphics.clear()
        map.graphicsOverlays.remove(selectionOverlay)
        selectedGraphic = null
        selectedDisplayLayer = null
        displayLayers.forEach { map.graphicsOverlays.remove(it.overlay) }
        displayLayers.clear()
    }

    fun onActivityResult(request: Int, result: Int, data: Intent?): Boolean {
        if (request != SAVE_ZIP) return false
        if (result != Activity.RESULT_OK) return true
        val uri = data?.data ?: return true
        task("另存 ZIP") {
                val zip = exportZip?.takeIf { it.isFile } ?: error("导出文件已失效，请重新导出")
                withContext(Dispatchers.IO) {
                    activity.contentResolver.openOutputStream(uri, "wt")?.use { output -> zip.inputStream().use { it.copyTo(output) } }
                        ?: error("无法写入所选位置")
                }
                message("另存成功", "SHP ZIP 已保存至你选择的位置。")
        }
        return true
    }

    fun dispose() {
        disposed = true
        // 先从地图解绑；Activity 随后 dispose MapView，后续后台清理不得再访问地图。
        cancelEdit()
        // 正在进行的持久化/复制任务完成后再关闭句柄，避免中途损坏工作文件。
        if (!busy) { unload(); scope.cancel() }
    }
}
