package com.jwch.gwyt_project.util

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol

/**
 * 几何编辑器工具 —— 封装 ArcGIS SketchEditor，提供点/线/面绘制、选择编辑、撤销、清除功能
 */
class GeometryEditorHelper(private val mapView: MapView) {

    val sketchEditor = SketchEditor()
    val graphicsOverlay = GraphicsOverlay()

    private var currentMode: SketchCreationMode? = null
    private var isSelecting = false
    private var editingGraphic: Graphic? = null

    /** 外部回调：测量信息文本 */
    var onMeasureInfo: ((String) -> Unit)? = null
    /** 外部回调：浮动完成按钮的屏幕位置 (x, y)，null 时隐藏 */
    var onFloatingBtnPos: ((android.graphics.Point?) -> Unit)? = null

    private val caculationUtil = CaculationUtil()
    private val measurementTextOverlay = GraphicsOverlay()

    private var lastSketchGeom: Geometry? = null

    init {
        mapView.sketchEditor = sketchEditor
        mapView.graphicsOverlays.add(graphicsOverlay)
        mapView.graphicsOverlays.add(measurementTextOverlay)

        // 监听几何变化：统一由浮动按钮完成，几何变化时更新按钮位置
        sketchEditor.addGeometryChangedListener {
            val geom = sketchEditor.geometry
            lastSketchGeom = geom
            if (currentMode != null) {
                updateFloatingBtn(geom)
            }
        }

        // 地图视角变化时重新计算浮动按钮位置
        mapView.addViewpointChangedListener {
            if (currentMode != null) {
                updateFloatingBtn(lastSketchGeom)
            }
        }
    }

    /** 更新浮动完成按钮到几何最后一个顶点的屏幕位置 */
    private fun updateFloatingBtn(geom: Geometry?) {
        if (geom == null || geom.isEmpty) {
            onFloatingBtnPos?.invoke(null)
            return
        }
        val lastPoint: Point?
        val isValid: Boolean
        when (geom) {
            is Point -> {
                lastPoint = geom
                isValid = true
            }
            is Polyline -> {
                val parts = geom.parts
                if (parts.size > 0) {
                    val part = parts.get(parts.size - 1)
                    lastPoint = part.endPoint
                    var count = 0
                    for (p in part.points) { count++ }
                    isValid = count >= 2
                } else {
                    lastPoint = null
                    isValid = false
                }
            }
            is Polygon -> {
                val parts = geom.parts
                if (parts.size > 0) {
                    val part = parts.get(parts.size - 1)
                    lastPoint = part.endPoint
                    var count = 0
                    for (p in part.points) { count++ }
                    isValid = count >= 3
                } else {
                    lastPoint = null
                    isValid = false
                }
            }
            else -> {
                lastPoint = null
                isValid = false
            }
        }
        if (lastPoint != null && isValid) {
            val screenPoint = mapView.locationToScreen(lastPoint)
            screenPoint.offset(30, -50)
            onFloatingBtnPos?.invoke(screenPoint)
        } else {
            onFloatingBtnPos?.invoke(null)
        }
    }

    /** 开始绘制点 */
    fun startPoint() {
        exitSelectMode()
        switchMode(SketchCreationMode.POINT)
    }

    /** 开始绘制线 */
    fun startPolyline() {
        exitSelectMode()
        switchMode(SketchCreationMode.POLYLINE)
    }

    /** 开始绘制面 */
    fun startPolygon() {
        exitSelectMode()
        switchMode(SketchCreationMode.POLYGON)
    }

    /** 进入选择编辑模式 */
    fun startSelect() {
        saveCurrentGeometry()
        sketchEditor.stop()
        currentMode = null
        isSelecting = true
                onFloatingBtnPos?.invoke(null)
    }

    /** 完成当前绘制，保存图形并自动重启同模式 */
    fun finish() {
        if (currentMode != null) {
            saveAndRestart()
        }
    }

    private fun saveAndRestart() {
        saveCurrentGeometry()
        val mode = currentMode ?: return
        sketchEditor.stop()
        sketchEditor.start(mode)
    }

    private fun exitSelectMode() {
        if (isSelecting) {
            isSelecting = false
        }
    }

    /** 点击地图选择图形进行编辑 */
    fun onMapTapped(screenX: Double, screenY: Double) {
        if (!isSelecting) return

        val tolerance = 22.0
        val future = mapView.identifyGraphicsOverlayAsync(
            graphicsOverlay, android.graphics.Point(screenX.toInt(), screenY.toInt()), tolerance, false
        )
        future.addDoneListener {
            val result = future.get()
            val graphics = result.graphics
            if (graphics.isNotEmpty()) {
                startEditGraphic(graphics[0])
            }
        }
    }

    /** 开始编辑选中的图形 */
    private fun startEditGraphic(graphic: Graphic) {
        val geometry = graphic.geometry ?: return
        graphicsOverlay.graphics.remove(graphic)
        editingGraphic = graphic
        isSelecting = false
        // 刷新标注（清除旧的，重算剩余图形的）
        rebuildMeasurementTexts()

        currentMode = when (geometry) {
            is Point -> SketchCreationMode.POINT
            is Polyline -> SketchCreationMode.POLYLINE
            is Polygon -> SketchCreationMode.POLYGON
            else -> return
        }
        sketchEditor.start(geometry)
    }

    private fun switchMode(newMode: SketchCreationMode) {
        sketchEditor.stop()
        currentMode = newMode
        sketchEditor.start(newMode)
    }

    /** 保存当前 sketch 中的几何图形到图层 */
    private fun saveCurrentGeometry() {
        val geom = sketchEditor.geometry
        if (geom != null && !geom.isEmpty) {
            addGeometryWithLabel(geom)
        }
    }

    /** 撤销最后一步 */
    fun undo() {
        if (currentMode != null) {
            sketchEditor.undo()
        }
    }

    /** 清除所有已绘制图形并停止绘制 */
    fun clearAll() {
        val mode = currentMode
        sketchEditor.stop()
        currentMode = null
        isSelecting = false
        editingGraphic = null
        graphicsOverlay.graphics.clear()
        measurementTextOverlay.graphics.clear()
        onMeasureInfo?.invoke("")
        onFloatingBtnPos?.invoke(null)
        // 清除后自动重启当前模式
        if (mode != null) {
            currentMode = mode
            sketchEditor.start(mode)
        }
    }

    /** 停止当前绘制（丢弃未完成图形） */
    fun stop() {
        sketchEditor.stop()
        currentMode = null
        isSelecting = false
        editingGraphic = null
                onFloatingBtnPos?.invoke(null)
    }

    /** 是否处于选择模式 */
    fun isInSelectMode(): Boolean = isSelecting

    /** 销毁，移除图层并停止 */
    fun dispose() {
        stop()
        mapView.graphicsOverlays.remove(graphicsOverlay)
        mapView.graphicsOverlays.remove(measurementTextOverlay)
    }

    /** 将几何图形添加到图层，并显示长度/面积标注 */
    private fun addGeometryWithLabel(geometry: Geometry) {
        if (geometry.isEmpty) return

        val graphic = when (geometry) {
            is Point -> Graphic(geometry, pointSymbol)
            is Polyline -> {
                val g = Graphic(geometry, lineSymbol)
                showMeasurementText(geometry)
                g
            }
            is Polygon -> {
                val g = Graphic(geometry, fillSymbol)
                showMeasurementText(geometry)
                g
            }
            else -> Graphic(geometry, lineSymbol)
        }
        graphicsOverlay.graphics.add(graphic)
    }

    /** 在线/面图形上显示长度/面积标注 */
    private fun showMeasurementText(geometry: Geometry) {
        val text: String
        val labelPoint: Point

        when (geometry) {
            is Polyline -> {
                val length = caculationUtil.caculateLengthUnit(geometry)
                text = length.trim()
                // 标注放在折线最后一个点上
                val parts = geometry.parts
                val lastPart = parts.get(parts.size - 1)
                labelPoint = lastPart.endPoint
            }
            is Polygon -> {
                val area = caculationUtil.caculateAreaSizeUnit(geometry, CaculationUtil.UNIT_DEFAULT)
                text = area.trim()
                // 标注放在面的第一个顶点附近
                val parts = geometry.parts
                val firstPart = parts.get(0)
                labelPoint = firstPart.startPoint
            }
            else -> return
        }

        val textSymbol = TextSymbol(12f, text, 0xFFFFFFFF.toInt(),
            TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.BOTTOM)
        val textGraphic = Graphic(labelPoint, textSymbol)
        measurementTextOverlay.graphics.add(textGraphic)

        // 同时通知工具栏
        onMeasureInfo?.invoke(text)
    }

    /** 清除并重建所有图形的测量标注 */
    private fun rebuildMeasurementTexts() {
        measurementTextOverlay.graphics.clear()
        for (graphic in graphicsOverlay.graphics) {
            val geom = graphic.geometry
            if (geom is Polyline || geom is Polygon) {
                showMeasurementText(geom)
            }
        }
    }

    companion object {
        /** 默认点符号：蓝色圆形 */
        val pointSymbol = SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.CIRCLE, 0xFF2196F3.toInt(), 10f
        )

        /** 默认线符号：蓝色实线 */
        val lineSymbol = SimpleLineSymbol(
            SimpleLineSymbol.Style.SOLID, 0xFF2196F3.toInt(), 3f
        )

        /** 默认面符号：蓝色半透明填充 */
        val fillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID,
            0x442196F3.toInt(),
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF2196F3.toInt(), 2f)
        )
    }
}