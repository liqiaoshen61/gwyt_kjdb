package com.jwch.gwyt_project.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaActionSound
import android.view.animation.DecelerateInterpolator
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureCollection
import com.esri.arcgisruntime.data.FeatureCollectionTable
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ShapefileFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.layers.WebTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.Renderer
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.symbology.Symbol
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.PrintUtil
import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.Info.EMapsInfo
import com.jwch.gwyt_project.Info.GraphicInfo
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.Info.MarkerInfo
import com.jwch.gwyt_project.Info.PoisInfo
import com.jwch.gwyt_project.Info.ThemeFieldsInfo
import com.jwch.gwyt_project.Info.ThemesInfo
import com.jwch.gwyt_project.Info.TianDiMapInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.ztt.ZttItem
import com.jwch.gwyt_project.common.Tools
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.getObjFromJson2
import com.jwch.gwyt_project.ext.isNull
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.i.DoneAction
import com.jwch.gwyt_project.i.DoneListener
import com.jwch.gwyt_project.i.SingleClickMapAction
import com.jwch.gwyt_project.i.SingleClickMapListener
import com.jwch.gwyt_project.model.AnalysisListModel
import com.jwch.gwyt_project.model.BaseLayerInfo
import com.jwch.gwyt_project.model.BufferAnalysisModel
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.GeoJsonModel
import com.jwch.gwyt_project.model.InterSectionModel
import com.jwch.gwyt_project.model.KVModel
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.model.StyleConfig
import com.jwch.gwyt_project.util.CryptoUtils.decryptGeodatabase
import com.jwch.gwyt_project.util.DrawUtil.Companion.DRAW_POINT
import com.jwch.gwyt_project.util.DrawUtil.Companion.DRAW_POLYGON
import com.jwch.gwyt_project.util.DrawUtil.Companion.DRAW_POLYLINE
import com.jwch.gwyt_project.view.MarkerCallOut
import com.jwch.gwyt_project.view.SaveGeometyDialog
import com.jwch.gwyt_project.view.SaveWorkRecordDialog
import com.jwch.gwyt_project.view.EasySearchBarView
import com.jwch.gwyt_project.view.TextMarkerDialog
import com.jwch.gwyt_project.view.ThemeCallOut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import org.gdal.gdal.gdal
import org.gdal.ogr.ogr
import org.gdal.osr.CoordinateTransformation
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import org.xutils.ex.DbException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Stack
import java.util.concurrent.ExecutionException


class HandleMapUtil(activity: Activity, context: Context, mapView: MapView, other: MapView) :
    SingleClickMapListener, ViewpointChangedListener {

    var activity: Activity
    var context: Context
    var mapView: MapView


    lateinit var imgLayerOnline: WebTiledLayer
    lateinit var imgNoteLayerOnline: WebTiledLayer
    lateinit var vectorLayerOnline: WebTiledLayer
    lateinit var vectorNoteLayerOnline: WebTiledLayer

    lateinit var imgLayer: ArcGISTiledLayer
    lateinit var imgNoteLayer: ArcGISTiledLayer
    lateinit var vectorLayer: ArcGISTiledLayer
    lateinit var vectorNoteLayer: ArcGISTiledLayer

    lateinit var image2018Layer: ArcGISTiledLayer
    lateinit var image2022Layer: ArcGISTiledLayer
    lateinit var image2024Layer: ArcGISTiledLayer
    lateinit var image2025Layer: ArcGISTiledLayer
    lateinit var stream2018: TPKDecryptedStream
    lateinit var stream2022: TPKDecryptedStream
    lateinit var stream2024: TPKDecryptedStream
    lateinit var stream2025: TPKDecryptedStream


    lateinit var markerUtil: MarkerUtil
    var caculationUtil = CaculationUtil()
    lateinit var baseLayerInfo: BaseLayerInfo
    private var cityLayer: ArcGISTiledLayer? = null

    lateinit var graphicsLayer: GraphicsOverlay //用来画标绘和测量图形的图解图层（GeoJSON）
    lateinit var shpGraphicsLayer: GraphicsOverlay //用来画SHP的图解图层
    lateinit var graphicsDisplayLayer: GraphicsOverlay //用来展示收藏夹中的标绘结果
    lateinit var myLocationLayer: GraphicsOverlay //我的位置图层

    /** 上一次应用到「我的位置」箭头上的角度，用于过滤无变化的重绘 */
    private var lastMyLocationRotation = -1f
    /** 「我的位置」图标平滑平移动画；持续定位每秒回调时用于把图标从当前位置平滑拉到新位置，避免突然跳位 */
    private var myLocationAnimator: ValueAnimator? = null
    lateinit var poiSearchLayer: GraphicsOverlay   //用来画点击的查询结果的图解图层
    lateinit var overlyingLayer: GraphicsOverlay   //图形叠加分析图层
    lateinit var bufferAnalysisResultLayer: GraphicsOverlay //用来展示缓冲区查询结果
    lateinit var testGraphicsOverlay: GraphicsOverlay //测试用的图解图层
    var mapUtil: MapUtil
    var mapAction: SingleClickMapAction
    lateinit var callout: Callout
    var tapInterceptor: ((MotionEvent) -> Boolean)? = null // 外部点击拦截器，返回true则拦截
    var isVector = false//是否为影像图
    val defScale: Double = 15000.0 //移动到某个特定的点的比例
    lateinit var clickPoint: Point
    var textMarkerDialog: TextMarkerDialog? = null

    var provinceFeatureLayer: FeatureLayer? = null //省级
    var cityFeatureLayer: FeatureLayer? = null //市级
    var countyFeatureLayer: FeatureLayer? = null //区县级
    var townFeatureLayer: FeatureLayer? = null //镇级
    var villageFeatureLayer: FeatureLayer? = null //村级

    //    var selectLayerList = mutableListOf<ThemesInfo>()//选中图层
    private var otherMap: MapView? = null
    private var saveGeometyDialog: SaveGeometyDialog? = null
    private var saveWorkRecordDialog: SaveWorkRecordDialog? = null

    var markerCallOut: MarkerCallOut? = null
    var themeCallOut: ThemeCallOut? = null

    // 全局搜索栏引用，用于在地图点击时隐藏筛选条件
    var easySearchBar: EasySearchBarView? = null

    var actionType = 0
    var isLoadOnlineMap = true //是否加载在线地图
    private var index = 0//手势查询标记

    var selectLayers: MutableList<ThemesInfo> //已选中的图层
    var bufferAnalysisLayer: MutableList<ThemesInfo> //要查询的缓冲区图层 默认是所有业务图层

    private var interSectionModel = mutableListOf<InterSectionModel>()//叠加分析结果数据
    private var analysisList = mutableListOf<AnalysisListModel>()//叠加分析结果数据
    private var searchLayers: MutableList<ThemesInfo> //点击查询的时候 有查到属性的图层对象 集合
    var infoCount = 0// 查询到所有专题图中 属性个数 ，该字段没什么用


    val lastAnalysisList = mutableListOf<InterSectionModel>() //最后一个叠加分析图层的结果

    var countyNameList = mutableListOf<String>() //叠加分析标绘相交的镇名列表


    private var bufferAnalysisList = mutableListOf<BufferAnalysisModel>()//缓冲区分析的结果

    var layerFilterType = 0 //图层筛选类型 默认全部 0 全部 1未办结 2 已办结
    var zttItemList = mutableListOf<ZttItem>() //
    var eventStatusFilterParams = "" //事件状态 筛选参数

    private lateinit var manager: MapViewMemoryManager //地图native内存监控
    var minScale = 4708339.6152
    var fullExtent: Envelope? = null

    var baseMapStack = Stack<String>() //这个栈 用来维护影像底图
    
    // 协程作用域
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    // 加密操作的引用，用于取消
    private var encryptJob: Job? = null


    companion object {
        const val ACTION_NOMAL = 0 //主页正常操作
        const val ACTION_MARKER = 1 //标绘
        const val ACTION_OVERLYING_ANALYSIS = 2 //叠加分析
        const val ACTION_OVERLYING_ANALYSIS_FINISH = 4 //叠加分析完成（暂停标绘）
        const val ACTION_BUFFER_ANALYSIS = 5 //自定义点位的空间查询（缓冲区分析）
        const val ACTION_IDENTIFY_LAYER = 6 //地图查询

        const val TYPE_POI = 0  //兴趣点callout
        const val TYPE_THEME = 1  //专题图callout
        const val TYPE_GEO = 2  //标绘callout
        const val TYPE_GEOJSON = 3  //geojson callout
        const val TYPE_SHP = 4  //shp callout
    }


    init {
        this.activity = activity
        this.context = context
        this.mapView = mapView
        this.otherMap = other
        mapUtil = MapUtil.getMapUtil()
        mapView.addViewpointChangedListener(this)
        mapAction = SingleClickMapAction(context, mapView, this, 0)
        mapView.onTouchListener = mapAction
        selectLayers = mutableListOf()
        searchLayers = mutableListOf()
        bufferAnalysisLayer = mutableListOf()



        initBaseLayer()
        markerUtil = MarkerUtil(graphicsLayer, mapView, context)

        mapView.addMapScaleChangedListener {
//            PrintUtil.printMsg("当前比例：${it.source.mapScale}")
        }

        mapView.interactionOptions.isMagnifierEnabled = false
        mapView.interactionOptions.isRotateEnabled = false
        mapView.isAttributionTextVisible = false
        mapView.selectionProperties.color = Color.TRANSPARENT




        loadProvinceLayer()
        loadCityLayer()
        loadAreaLayer()
        loadCountyLayer()

        loadVillageLayer()

//        ArcGISRuntimeEnvironment.setApiKey("AAPKe4b90ed8d2d3499ab13ed5297cd29b469NK6YC9YD_F_BlUGZy232nkS6lZ_V-VC0kDNSmv1-w9HPxUpOUTRpUoCUB2j--JW")
//        test()

        ArcGISRuntimeEnvironment.initialize()

        //阈值700MB 每10秒检测一次native
        manager = MapViewMemoryManager(700, 10) {
        }
        manager.startMonitoring()

    }


    override fun viewpointChanged(viewpointChangedEvent: ViewpointChangedEvent) {
        //监听地图移动
//        synchronizeViewpoints(mapView, otherMap!!)
    }

    //同步地图移动
    private fun synchronizeViewpoints(controlMap: MapView, followMap: MapView) {
        if (controlMap.isNavigating) {
            val navigatingViewpoint =
                controlMap.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)
            followMap.setViewpoint(navigatingViewpoint)
        }
    }

    private fun initBaseLayer() {
        mapUtil.initMap(mapView)
        mapUtil.setAuthorVisiable(mapView, false)
        mapUtil.deleteWaterMark(Config.licence)
        callout = mapView.callout

        //设置最大最小缩放
        mapView.map.maxScale = 600.0
//        mapView.map.minScale = 2140060.0
        mapView.map.minScale = minScale


//        //初始化图层信息--天地图

        if (isLoadOnlineMap) {
            baseLayerInfo = BaseLayerInfo()
            baseLayerInfo.initLayer()
            imgLayerOnline =
                TianDiTuMethodsClass.CreateTianDiTuTiledLayer(TianDiTuMethodsClass.LayerType.TIANDITU_IMAGE_2000)
            imgNoteLayerOnline =
                TianDiTuMethodsClass.CreateTianDiTuTiledLayer(TianDiTuMethodsClass.LayerType.TIANDITU_IMAGE_ANNOTATION_CHINESE_2000);
            vectorLayerOnline =
                TianDiTuMethodsClass.CreateTianDiTuTiledLayer(TianDiTuMethodsClass.LayerType.TIANDITU_VECTOR_2000)
            vectorNoteLayerOnline =
                TianDiTuMethodsClass.CreateTianDiTuTiledLayer(TianDiTuMethodsClass.LayerType.TIANDITU_VECTOR_ANNOTATION_CHINESE_2000) //单独的注记图层

        } else {
//            //初始化图层信息---本地离线底图
            var tdmap = TianDiMapInfo()
            //矢量切片
            tdmap.vector =
                db.appDb.selector(EMapsInfo::class.java).where("EMapName", "=", "矢量切片")
                    .findFirst()
            vectorLayer =
                ArcGISTiledLayer(Config.EMAPDATA_PATH + tdmap.vector.geteMapName() + ".tpk")
            //矢量注记
            tdmap.vectorNote =
                db.appDb.selector(EMapsInfo::class.java).where("EMapName", "=", "矢量注记")
                    .findFirst()
            vectorNoteLayer =
                ArcGISTiledLayer(Config.EMAPDATA_PATH + tdmap.vectorNote.geteMapName() + ".tpk")
            //影像切片
            tdmap.image =
                db.appDb.selector(EMapsInfo::class.java).where("EMapName", "=", "影像切片")
                    .findFirst()
            imgLayer = ArcGISTiledLayer(Config.EMAPDATA_PATH + tdmap.image.geteMapName() + ".tpk")
            //影像注记
            tdmap.imageNote =
                db.appDb.selector(EMapsInfo::class.java).where("EMapName", "=", "影像注记")
                    .findFirst()
            imgNoteLayer =
                ArcGISTiledLayer(Config.EMAPDATA_PATH + tdmap.imageNote.geteMapName() + ".tpk")

        }

        stream2018 =
            TPKDecryptedStream(Config.EMAPDATA_PATH + "影像底图2018/v101/图层/conf.cdi", 0x55)
        stream2022 =
            TPKDecryptedStream(Config.THEMEDATA_TPK_PATH + "影像底图2022/v101/图层/conf.cdi", 0x55)
        stream2024 =
            TPKDecryptedStream(Config.THEMEDATA_TPK_PATH + "影像底图2024/v101/图层/conf.cdi", 0x55)
        stream2025 =
            TPKDecryptedStream(Config.THEMEDATA_TPK_PATH + "影像底图2025/v101/图层/conf.cdi", 0x55)

        stream2018.decryptFileInPlace { boolean, string ->
            "=== $boolean $string".printMsg()
            image2018Layer = Tools.getTiledLayerByPath(Config.EMAPDATA_PATH, "影像底图2018")
            image2018Layer.loadAsync()
            image2018Layer.addDoneLoadingListener {
                fullExtent = image2018Layer.fullExtent
                switchBaseLyer(MapEvent.SET_IMAGE_MAP_2018)
            }
        }

        image2022Layer = Tools.getTiledLayerByPath(Config.THEMEDATA_TPK_PATH, "影像底图2022")
        image2024Layer = Tools.getTiledLayerByPath(Config.THEMEDATA_TPK_PATH, "影像底图2024")
        image2025Layer = Tools.getTiledLayerByPath(Config.THEMEDATA_TPK_PATH, "影像底图2025")


        // 当用拼多多底图的时候 使用这个代码就会解除范围受限的bug
//            baseMapList.forEach {
//                it.minScale = 0.0
//                it.maxScale = 0.0
//            }

//        switchBaseLyer(MapEvent.SET_IMAGE_MAP)
        baseMapStack.push("影像底图2018")
        graphicsLayer = GraphicsOverlay()
        shpGraphicsLayer = GraphicsOverlay()
        graphicsDisplayLayer = GraphicsOverlay()
        myLocationLayer = GraphicsOverlay()
        poiSearchLayer = GraphicsOverlay()
        overlyingLayer = GraphicsOverlay()
        bufferAnalysisResultLayer = GraphicsOverlay()
        testGraphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsLayer)
        mapView.graphicsOverlays.add(shpGraphicsLayer)
        mapView.graphicsOverlays.add(graphicsDisplayLayer)
        mapView.graphicsOverlays.add(myLocationLayer)
        mapView.graphicsOverlays.add(poiSearchLayer)
        mapView.graphicsOverlays.add(overlyingLayer)
        mapView.graphicsOverlays.add(bufferAnalysisResultLayer)
        mapView.graphicsOverlays.add(testGraphicsOverlay)


    }


    //显示底图
    fun switchBaseLyer(layerType: Int) {


        initBaseLayerData()
//        clearBaseLayer()
        when (layerType) {
            MapEvent.SET_IMAGE_MAP -> {
                if (isLoadOnlineMap) {
                    mapView.map.basemap.baseLayers.add(imgLayerOnline)
                    mapView.map.basemap.baseLayers.add(image2018Layer)
                } else {
                    mapView.map.basemap.baseLayers.add(image2018Layer)
                }
            }

            MapEvent.SET_VECTOR_MAP -> {
                mapView.map.basemap.baseLayers.add(if (isLoadOnlineMap) vectorLayerOnline else vectorLayer)
            }

            MapEvent.SET_IMAGE_MAP_2018 -> {

                if(mapView.map.basemap.baseLayers.contains(image2018Layer)){
                    return
                }

                // 取消之前的加密操作
                encryptJob?.cancel()

                stream2018.decryptFileInPlace { boolean, string ->
                    if (!NetUtil.isNetworkConnected(context)) {
                        mapView.map.basemap.baseLayers.add(imgLayerOnline)
                    }
                    mapView.map.basemap.baseLayers.add(image2018Layer)
                    //先加载新底图，再移除旧底图 就不会出现底图闪一下的问题！！！
                    mapView.map.basemap.baseLayers.retainAll(listOf(image2018Layer))

                    // 保存当前底图状态
                    MapStatePrefs.saveBaseMapName(context, "影像底图2018")

                    encryptJob = scope.launch(Dispatchers.IO) {
                        delay(500)
                        try {
                            stream2018.encryptFileInPlace { _, _ -> }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }

            MapEvent.SET_IMAGE_MAP_2022 -> {

                if(mapView.map.basemap.baseLayers.contains(image2022Layer)){
                    return
                }

                // 取消之前的加密操作
                encryptJob?.cancel()

                stream2022.decryptFileInPlace { boolean, string ->
                    if (!NetUtil.isNetworkConnected(context)) {
                        mapView.map.basemap.baseLayers.add(imgLayerOnline)
                    }
                    mapView.map.basemap.baseLayers.add(image2022Layer)

                    mapView.map.basemap.baseLayers.retainAll(listOf(image2022Layer))

                    // 保存当前底图状态
                    MapStatePrefs.saveBaseMapName(context, "影像底图2022")

                    encryptJob = scope.launch(Dispatchers.IO) {
                        delay(500)
                        try {
                            stream2022.encryptFileInPlace { _, _ -> }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }

            MapEvent.SET_IMAGE_MAP_2024 -> {

                if(mapView.map.basemap.baseLayers.contains(image2024Layer)){
                    return
                }

                // 取消之前的加密操作
                encryptJob?.cancel()

                stream2024.decryptFileInPlace { boolean, string ->
                    if (!NetUtil.isNetworkConnected(context)) {
                        mapView.map.basemap.baseLayers.add(imgLayerOnline)
                    }
                    mapView.map.basemap.baseLayers.add(image2024Layer)
                    mapView.map.basemap.baseLayers.retainAll(listOf(image2024Layer))

                    // 保存当前底图状态
                    MapStatePrefs.saveBaseMapName(context, "影像底图2024")

                    encryptJob = scope.launch(Dispatchers.IO) {
                        delay(500)
                        try {
                            stream2024.encryptFileInPlace { _, _ -> }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
            MapEvent.SET_IMAGE_MAP_2025 -> {

                if(mapView.map.basemap.baseLayers.contains(image2025Layer)){
                    return
                }

                // 取消之前的加密操作
                encryptJob?.cancel()

                stream2025.decryptFileInPlace { boolean, string ->
                    if (!NetUtil.isNetworkConnected(context)) {
                        mapView.map.basemap.baseLayers.add(imgLayerOnline)
                    }
                    mapView.map.basemap.baseLayers.add(image2025Layer)
                    mapView.map.basemap.baseLayers.retainAll(listOf(image2025Layer))

                    // 保存当前底图状态
                    MapStatePrefs.saveBaseMapName(context, "影像底图2025")

                    encryptJob = scope.launch(Dispatchers.IO) {
                        delay(500)
                        try {
                            stream2025.encryptFileInPlace { _, _ -> }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
        }

        initNoteOperationalLayer(layerType)

//        //加载天地图
//        if (isVector) {
//            mapView.map.basemap.baseLayers.add(imgLayer)
//            mapView.map.basemap.baseLayers.add(imgNoteLayer)
//        } else {
//            mapView.map.basemap.baseLayers.add(vectorLayer)
//            mapView.map.basemap.baseLayers.add(vectorNoteLayer)
//        }
    }


    fun handleBaseMap(layerType: Int, layer: ArcGISTiledLayer?) {
        initBaseLayerData()
        initBaseLayer()
        when (layerType) {
            MapEvent.REPLACE_BASE_MAP ->
//                替换底图
                if (layer != null) {
                    clearBaseLayer()
                    mapView.map.basemap.baseLayers.add(layer)
                }

            MapEvent.ADD_BASEMAP ->
                if (layer != null) {
                    var isContain = false
                    for (item in mapView.map.basemap.baseLayers) {
                        if (CommonUtil.getSelfValue(item.id) == CommonUtil.getSelfValue(layer.id)) {
                            isContain = true
                        }
                    }
                    if (!isContain) {
                        mapView.map.basemap.baseLayers.add(layer)
                        PrintUtil.printMsg("叠加底图成功")
                    }
                }

            MapEvent.REMOVED_ADDED_BASEMAP ->
                if (layer != null) {
                    for (item in mapView.map.basemap.baseLayers) {
                        if (CommonUtil.getSelfValue(item.id) == CommonUtil.getSelfValue(layer.id)) {
                            mapView.map.basemap.baseLayers.remove(item)
                            PrintUtil.printMsg("删除已叠加的底图成功")
                            break
                        }
                    }
                }
        }
        initNoteOperationalLayer(layerType)
    }

    private fun initNoteOperationalLayer(layerType: Int) {
//        when (layerType) {
//            MapEvent.SET_IMAGE_MAP,
        //            MapEvent.SET_IMAGE_MAP_2018,
////            MapEvent.SET_IMAGE_MAP_2022,
////            MapEvent.SET_IMAGE_MAP_2024 -> {
//                if (mapView.map.operationalLayers.contains(if (isLoadOnlineMap) imgNoteLayerOnline else imgNoteLayer)) return
//                if (mapView.map.operationalLayers.isEmpty()) {
//                    mapView.map.operationalLayers.add(if (isLoadOnlineMap) imgNoteLayerOnline else imgNoteLayer)
//                    return
//                }
//                if (mapView.map.operationalLayers.contains(if (isLoadOnlineMap) vectorNoteLayerOnline else vectorNoteLayer)) {
//                    mapView.map.operationalLayers.remove(if (isLoadOnlineMap) vectorNoteLayerOnline else vectorNoteLayer)
//                    mapView.map.operationalLayers.add(if (isLoadOnlineMap) imgNoteLayerOnline else imgNoteLayer)
//                }
//            }
//
//            MapEvent.SET_VECTOR_MAP -> {
//                if (mapView.map.operationalLayers.contains(if (isLoadOnlineMap) vectorNoteLayerOnline else vectorNoteLayer)) return
//                if (mapView.map.operationalLayers.isEmpty()) {
//                    mapView.map.operationalLayers.add(if (isLoadOnlineMap) vectorNoteLayerOnline else vectorNoteLayer)
//                    return
//                }
//                if (mapView.map.operationalLayers.contains(if (isLoadOnlineMap) imgNoteLayerOnline else imgNoteLayer)) {
//                    mapView.map.operationalLayers.remove(if (isLoadOnlineMap) imgNoteLayerOnline else imgNoteLayer)
//                    mapView.map.operationalLayers.add(if (isLoadOnlineMap) vectorNoteLayerOnline else vectorNoteLayer)
//                }
//            }
//
//        }
    }

    private fun initBaseLayerData() {

        try {
            if (cityLayer == null) {
                val list = db.queryThemesById(Config.CITY_YXT_ID)
                if (CommonUtil.matchList(list) && list?.size == 1) {
                    val cityLayerInfo = list[0]
                    cityLayer = ArcGISTiledLayer(
                        Config.EMAPDATA_PATH
                                + Tools.getTpkPath(cityLayerInfo.themeName.self())
                    )
                    cityLayer?.id = Config.CITY_YXT_ID.toString()
                }
            }
        } catch (e: DbException) {
            e.printStackTrace()
        }
    }

    lateinit var screenPoint: android.graphics.Point

    override fun onMapSingleClick(event: MotionEvent?, tag: Int) {

        // 外部拦截器（如几何编辑器选择模式）
        if (tapInterceptor?.invoke(event!!) == true) return

        // 网络检测：如果联网则拦截地图点击
        if (AppContext.hasNet) {
            ToastUtils.show("当前已连接网络，该功能不可用")
            return
        }

        // 点击地图时隐藏全局搜索栏的筛选条件
        easySearchBar?.let {
            // 若筛选条件已展开，收起它（同时更新下拉箭头方向）
            if (it.isExpanded) {
                it.vb?.llFilterContainer?.visibility = View.GONE
                it.isExpanded = false
            }
        }

        screenPoint = android.graphics.Point(Math.round(event!!.x), Math.round(event!!.y))
        clickPoint = mapUtil.transScreenPoint2MapPoint(mapView, event!!.x, event!!.y)
        "clickPoint ${clickPoint.x} ${clickPoint.y}".printMsg()
        when (actionType) {

            ACTION_NOMAL -> {
                index = 0
                searchLayerInfo(selectLayers)
                //drawMyLocaton(clickPoint)

                if (graphicsDisplayLayer.graphics.isNotEmpty()) {
                    searchWorkRecordLayer()
                }

                if (bufferAnalysisResultLayer.graphics.isNotEmpty()) {
                    searchBufferAnalysisResultLayer()
                }

                if (graphicsLayer.graphics.isNotEmpty()) {
                    searchGeoJsonLayer()
                }
                if (shpGraphicsLayer.graphics.isNotEmpty()) {
                    searchShpLayer()
                }
            }

            ACTION_OVERLYING_ANALYSIS_FINISH -> {
                index = 0
                searchLayerInfo(selectLayers)
                //drawMyLocaton(clickPoint)
            }

            //标绘
            ACTION_MARKER -> {
                //文字标绘
                if (markerUtil.drawType == DrawUtil.DRAW_TEXT) {

                    isNull(textMarkerDialog) {
                        textMarkerDialog = TextMarkerDialog(context) {
                            markerUtil.addPointText(clickPoint, it)
                        }
                    }

                    textMarkerDialog!!.show()
                    //markerUtil.addPint(point);
                } else { //点 线 面标绘
                    //markerUtil.addPint(point);
                    EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT, clickPoint))
                    //markerUtil.addPoint(clickPoint)
                }
            }

            ACTION_OVERLYING_ANALYSIS -> {
                //叠加分析
                EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT, clickPoint))
            }
            //空间查询 缓冲区分析
            ACTION_BUFFER_ANALYSIS -> {
                EventBus.getDefault()
                    .post(MapEvent(MapEvent.DO_BUFFER_ANALYSIS_CUSTOM_POINT, clickPoint))
            }

            ACTION_IDENTIFY_LAYER -> {
                identifyLayer(screenPoint)
            }
        }
    }

    private fun identifyLayer(screenPoint: android.graphics.Point) {
        val identifyLayerResultFuture = mapView.identifyLayersAsync(
            screenPoint,
            10.0,  // 容差（像素）
            false, // 不返回弹出信息
            1      // 最大返回结果数
        )

        identifyLayerResultFuture.addDoneListener {
            "identifyLayer 查询到信息。。。".printMsg()
            val resultList = identifyLayerResultFuture.get()
            resultList.forEach { result ->
                "图层名称：${result.layerContent.name}".printMsg()
                val subLayer = result.sublayerResults
                subLayer.forEachIndexed { index, subResult ->
                    "子图层${index}：${subResult.layerContent.name}".printMsg()
                    subResult.elements.forEach { geoEle ->
                        val attr = geoEle.attributes
                        "参数==  ${attr.toJson()}"

                    }
                }
            }
        }
    }


    private fun checkField(fieldName: String, table: GeodatabaseFeatureTable): Boolean {

        return table.fields.any { field ->
            field.name.equals(fieldName, ignoreCase = true)
        }
    }

    //搜索 地图图层信息
    fun searchLayerInfo(layerlist: MutableList<ThemesInfo>) {
        if (!layerlist.isNullOrEmpty()) {
            val postion = layerlist.size - 1 - index
            if (postion < 0) return
            val info = layerlist[postion]
            val layer = info.layer ?: return
            val tolerance = 12
            val mapTolerance = tolerance * mapView.unitsPerDensityIndependentPixel
            val envelope = Envelope(
                clickPoint.x - mapTolerance,
                clickPoint.y - mapTolerance,
                clickPoint.x + mapTolerance,
                clickPoint.y + mapTolerance,
                mapView.map.spatialReference
            )

            val table = layer.featureTable as GeodatabaseFeatureTable
            //查询参数
            val query = QueryParameters()
            query.geometry = envelope

            var queryDefinition = ""

            //先判断是否筛选了该参数
            if (eventStatusFilterParams.isNotBlank()) {
                //其次判断图层是否存在该字段
                if (checkField("事件状", table)) {
                    queryDefinition = "事件状 = '${eventStatusFilterParams}'"
                }
            }

            if (queryDefinition.isBlank()) {
                queryDefinition = "1 = 1"
            }
            query.whereClause = queryDefinition
            query.isReturnGeometry = true
            query.maxFeatures = 1
            info.features.clear()
            val future = layer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()
                        if (!iterator.hasNext()) {
                            index++
                            searchLayerInfo(selectLayers)
                            return
                        }
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            if (actionType == ACTION_OVERLYING_ANALYSIS_FINISH) {
                                queryFeature(feature)
                                return
                            }

                            info.features.add(feature)
                            val attr = feature.attributes
                            //处理属性

                            "参数==： + ${getParmas(attr)}".printMsg()


                            val geometry = feature.geometry
                            val centerPoint = getCenterPoint(geometry)

                            val map = HashMap<String, Any>()
                            map["info"] = info
                            map["feature"] = feature
//                            mapUtil.drawGeomety(poiSearchLayer, feature.geometry, 2, true)

//                            mapUtil.drawImage(context, poiSearchLayer, clickPoint, R.mipmap.icon_point, false)
//                            mapView.setViewpointCenterAsync(clickPoint)

                            if (info.themeName.equals("河流") || info.themeName.equals("河道")|| info.themeName.equals("河管线")|| info.themeName.equals("河道管理范围线")) {
                                showCallout(clickPoint, map, TYPE_THEME)
                            } else {
                                mapView.setViewpointGeometryAsync(geometry, 150.0)

                                if (centerPoint != null) {
                                    showCallout(centerPoint, map, TYPE_THEME)
                                } else {
                                    showCallout(clickPoint, map, TYPE_THEME)
                                }

                            }





                            PrintUtil.printMsg("======运行完成=======")
                            break
                            //
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }
    }


    fun updateLineToGdb(feature: Feature) {

        "====1111=====".printMsg()
        val featureTable = feature.featureTable
        // 更新name属性数据
        feature.attributes.put("BSM", "12345678");

        "============canUpdate=========== ${featureTable.canUpdate(feature)}".printMsg()
        "============canAdd=========== ${featureTable.canAdd()}".printMsg()
        // 更新要素
        val updateFeatureOper = featureTable.updateFeatureAsync(feature)
        updateFeatureOper.addDoneListener {
            try {
                updateFeatureOper.get()
                if (updateFeatureOper.isDone) {
                    "==== success".printMsg()
                }
            } catch (e: ExecutionException) {
                // 处理异常
                "==== fail".printMsg()
                e.printStackTrace()
            } catch (e: InterruptedException) {
                // 处理异常
                "==== fail".printMsg()
                e.printStackTrace()
            }
        }
        //如果修改的是i在线服务， 要对ServiceFeatureTable进行确认同步
//        table.applyEditsAsync()
    }

    fun queryFeature(feature: Feature) {
        val geometry = feature.geometry
        val polygon = markerUtil.getGeometry() as Polygon

        //传进来的 polygon是龙海的空间参考，查出来的是4490的，所以要转化
        val trasGeometry =
            GeometryEngine.project(geometry, SpatialReference.create(Config.sp4548Int_longhai))
        val isInterSection = GeometryEngine.intersects(trasGeometry, polygon)
        if (isInterSection) {
            "有交集".printMsg()
            val attr = feature.attributes

            EventBus.getDefault().post(DataEvent(DataEvent.SEND_ATTR, attr))
        }
    }

    fun getParmas(attr: Map<String, Any?>): String {
        val sb = StringBuffer()
        val keys = attr.keys
        keys.forEach { key ->
            var value = attr[key]
            if (value is GregorianCalendar) {
                val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                value = simpleDateFormat.format(value.time)
            }
            if (value == null) {
                value = "null"
            }
            sb.append("$key : $value").append("\n")
        }
        return sb.toString()
    }


    /**
     * 空间查询--手势查询
     */
    fun handSearchInfo(queryGeomety: Geometry?) {
        if (queryGeomety == null) return

        searchLayers.clear() //清空有查询到图层属性的专题图数据
        index = 0//查询序号标记
        infoCount = 0

        (selectLayers.size > 0).yes {
            queryLayerInfo(selectLayers, queryGeomety)
        }.no {
            tip(context, "请先加载专题图层")
            return
        }

    }

    /**
     * 空间查询代码
     */
    private fun queryLayerInfo(layerlist: List<ThemesInfo>, queryGeomety: Geometry) {

        if (CommonUtil.matchList(layerlist)) {

            if (index >= layerlist.size) return;//查询序号标记 大于 已选中的专题图数量就不再查询

            val info = layerlist[index]//按序号取出已选的 专题图层数据对象，准备查询
            "  空间查询==图层==${info.getThemeName()} ".printMsg()

            val layer = info.layer
            if (layer == null) {
                //图层是空的
                if (index == layerlist.size - 1) {
                    //如果是最后一项，查询结束
                    queryEnd(searchLayers)
                    return;
                }
                //还有数据可以查，就继续查下一个图层
                if (index < layerlist.size - 1) {
                    index++;
                    queryLayerInfo(layerlist, queryGeomety)
                }
                return
            }

            //查询参数

            //查询参数
            val query = QueryParameters()
            query.geometry = queryGeomety;
//            query.setSpatialRelationship(QueryParameters.SpatialRelationship.WITHIN);
            query.outSpatialReference = Config.sp4490;
            query.whereClause = "1=1"
//            SpatialReference sr = SpatialReference.create(102100);//设置空间参考坐标系
//            query.setOutSpatialReference(sr);
//            query.setReturnGeometry(true);

            info.features.clear()

            //开始查询数据
            val queryList = layer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            queryList.addDoneListener {
                try {

                    val result = queryList.get()
                    val iterator: Iterator<Feature> = result.iterator()
                    if (!iterator.hasNext()) {
                        //没查到数据===查询图层异常===
                        if (index == layerlist.size - 1) {
                            //已经到最后一项--查询结束
                            queryEnd(searchLayers)
                            return@addDoneListener
                        }

                        if (index < layerlist.size - 1) {
                            //还有未查询的专题图层--继续查询下一个专题图层
                            index++
                            queryLayerInfo(selectLayers, queryGeomety)
                        }
                        return@addDoneListener
                    } else {
                        //查到数据，就把这个专题图数据添加到列表中
                        searchLayers.add(info)
                    }


                    while (iterator.hasNext()) {

                        val feature = iterator.next()
                        info.features.add(feature)

//                       属性
//                       val attr = feature.attributes
//                       "手势查询参数==：${getParmas(attr)}".printMsg()
                        infoCount++
                    }

                    if (layerlist.size == 0 || index == layerlist.size - 1) {
                        //已经到最后一项--查询结束
                        queryEnd(searchLayers)
                        return@addDoneListener
                    }

                    if (index < layerlist.size - 1) {
                        //还有未查询的专题图层--继续查询下一个专题图层
                        index++
                        queryLayerInfo(selectLayers, queryGeomety)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    "手势查询异常1：${e.message}".printMsg()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    "手势查询异常2：${e.message}".printMsg()
                }
            }
        }
    }

    //空间查询结束
    fun queryEnd(list: List<ThemesInfo?>) {
        PrintUtil.printMsg("搜索到数据的专题图数量 ： " + list.size)
        PrintUtil.printMsg("信息数量 ： $infoCount")
        EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_LIST, list))
        EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
        if (infoCount == 0) {
            tip(context, "暂无查询结果")
        }
    }

    fun startHandSearch() {
        markerUtil.drawType = DRAW_POLYGON
        markerUtil.isMeasure = true
        markerUtil.reset()
        actionType = ACTION_MARKER
        "h手势查询 2 actionType = $actionType  ".printMsg()
    }


    fun endHandSearch() {
        actionType = ACTION_NOMAL
        "h正常 0 actionType = $actionType  ".printMsg()
        markerUtil.reset()
    }

    fun startMarker(drawType: Int) {
        markerUtil.drawType = drawType
        markerUtil.isMeasure = true
        markerUtil.reset()
        actionType = ACTION_MARKER
        "m标绘 1 actionType = $actionType  ".printMsg()
    }

    fun startDrawBufferPoint() {
        markerUtil.drawType = DRAW_POINT
        markerUtil.reset()
        actionType = ACTION_BUFFER_ANALYSIS
        "准备绘制自定义点位 缓冲区分析 actionType = $actionType  ".printMsg()
    }

    /**
     * 重新绘制
     */
    fun resetDraw() {
        markerUtil.reset()
    }

    /**
     * 回退上一步绘制
     */
    fun preStep() {
        markerUtil.preStep()
    }

    fun markerRefresh() {
        markerUtil.refresh()
    }

    /**
     * 结束绘制
     */
    fun endMarker() {
        actionType = ACTION_NOMAL
        "m正常 0 actionType = $actionType  ".printMsg()
        markerUtil.reset()
    }

    /**
     * 开始量测
     */
    fun startMeasure(drawType: Int) {
        markerUtil.drawType = drawType
        markerUtil.isMeasure = true
        markerUtil.reset()
        actionType = ACTION_MARKER
        "e标绘 1 actionType = $actionType  ".printMsg()
    }

    /**
     * 结束量测
     */
    fun endMeasure() {
        actionType = ACTION_NOMAL
        markerUtil.reset()
        "e正常 0 actionType = $actionType  ".printMsg()
    }

    /**
     * 开始叠加分析
     */
    fun startOverlyingAnalysis(): Boolean {
        markerUtil.drawType = DRAW_POLYGON
        markerUtil.isMeasure = true
        markerUtil.reset()
        actionType = ACTION_OVERLYING_ANALYSIS
        "e标绘 1 actionType = $actionType  ".printMsg()

        return selectLayers.isNotEmpty()

    }

    //重新开始（继续）叠加分析
    fun restartOverlyingAnalysis() {
        actionType = ACTION_OVERLYING_ANALYSIS
        "h继续叠加分析 2 actionType = $actionType  ".printMsg()
    }

    /**
     * 结束叠加分析
     */
    fun endOverlyingAnalysis() {
        actionType = ACTION_NOMAL
        markerUtil.reset()

    }

    /**
     * 画出/移动我的定位点。
     * 若地图上尚未有定位点：首次画入箭头图标。
     * 若已有定位点：复用同一个 Graphic，把图标从当前位置平滑平移到新位置，
     * 避免持续定位（约每秒回调一次）时图标突然跳位。
     */
    fun drawMyLocaton(point: Point, rotation: Float) {
        lastMyLocationRotation = rotation
        val existing = myLocationLayer.graphics.firstOrNull()
        if (existing != null && existing.symbol is PictureMarkerSymbol) {
            (existing.symbol as PictureMarkerSymbol).setAngle(rotation)
            startMyLocationAnimator(existing, point)
        } else {
            // 还没有定位点：取消可能残留的动画，新画一个
            cancelMyLocationMove()
            mapUtil.drawImageWithRotation(
                context, myLocationLayer, point, R.mipmap.icon_location_arrow_up2,
                null, rotation, true
            )
        }
    }

    /**
     * 把「我的位置」图标从当前几何平滑过渡到 [target]，逐帧更新 Graphic.geometry。
     * 坐标系不一致或距离过近时直接落点；动画期间若有新定位点进来，会从头一条重定向到最新目标。
     */
    private fun startMyLocationAnimator(graphic: Graphic, target: Point) {
        myLocationAnimator?.cancel()

        val start = graphic.geometry as? Point ?: target
        val startSr = start.spatialReference
        val targetSr = target.spatialReference
        // 坐标系不一致时不插值，直接落到终点（正常应为同一地图坐标系）
        if (startSr != targetSr || Math.abs(start.x - target.x) + Math.abs(start.y - target.y) < 5e-7) {
            graphic.geometry = target
            return
        }

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 650L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                graphic.geometry = Point(
                    start.x + (target.x - start.x) * t,
                    start.y + (target.y - start.y) * t,
                    startSr
                )
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    graphic.geometry = target // 收尾保证精确落在目标点
                    if (myLocationAnimator === animation) myLocationAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    if (myLocationAnimator === animation) myLocationAnimator = null
                }
            })
        }
        myLocationAnimator = animator
        animator.start()
    }

    /** 取消「我的位置」平滑平移动画（定位点被清除/重画时调用） */
    private fun cancelMyLocationMove() {
        myLocationAnimator?.cancel()
        myLocationAnimator = null
    }

    /**
     * 只更新「我的位置」箭头的旋转角度，不重建符号。
     * 供罗盘（CompassHelper）按 10Hz 左右调用，用于实现箭头随设备转动。
     * 若地图上还没有定位点，或角度变化不足 1°，则不做任何事。
     */
    fun updateMyLocationRotation(rotation: Float) {
        if (!hasMyLocation()) return
        if (rotation == lastMyLocationRotation) return
        lastMyLocationRotation = rotation
        mapUtil.updateMarkerAngle(myLocationLayer, rotation)
    }

    /** 地图上是否已经画出了「我的位置」图标 */
    fun hasMyLocation(): Boolean =
        ::myLocationLayer.isInitialized && myLocationLayer.graphics.isNotEmpty()


    /**
     * 画出行政区域位置
     */
    fun drawArea(point: Point, sacle: Double = defScale) {
        mapUtil.drawImage(context, graphicsLayer, point, R.mipmap.icon_location, true)
        mapView.setViewpointCenterAsync(point, sacle);
    }


    fun clearAreaPoint() {
        graphicsLayer.graphics.clear()
    }

    fun cleaBufferLayer() {
        // 只清缓冲区相关图层，不清「我的位置」定位点（缓冲分析锚点画在 graphicsLayer 上）
        overlyingLayer.graphics.clear()
        bufferAnalysisResultLayer.graphics.clear()
    }


    /**
     * 绘制兴趣点搜搜索结果
     */
    fun drawSearchResult(list: MutableList<PoisInfo>?) {
        if (list == null) return
        val size = if (list.size >= 10) 10 else list.size
        for (i in 0 until size) {
            val point = Point(list[i].x, list[i].y)
            val resId =
                context.resources.getIdentifier("number_$i", "mipmap", "com.jwch.gwyt_project")
            mapUtil.drawImage(context, poiSearchLayer, point, resId, list[i], i == 0)
        }
    }

    /**
     * 点击了兴趣点搜索结果列表item
     */
    fun clickSearchResultItem(data: PoisInfo?) {
        if (data == null) return
        val point = mapUtil.get_change_geometry_point(
            data.getY(),
            data.getX(),
            mapView.spatialReference.wkid
        )
        showCallout(point, data, TYPE_POI)
        mapView.setViewpointCenterAsync(point, defScale)
    }

    /**
     * 点击了 标绘收藏 列表item
     */
    fun clickMarkerResultItem(data: MarkerInfo?) {
        if (data == null || !CommonUtil.isNotEmpty(data.geometryJson)) return
        val geometry = Geometry.fromJson(data.geometryJson)
        val centerPoint = geometry.extent.center //几何图形的中心点
        showCallout(centerPoint, data, TYPE_GEO)
    }

    /**
     * 绘制 被收藏的兴趣点 标记位置（点击item事件触发）
     */
    fun drawClickCollectResult(data: PoisInfo?) {
        if (data == null) return
        val point = mapUtil.get_change_geometry_point(data.y, data.x, mapView.spatialReference.wkid)
        val resId = context.resources.getIdentifier("icon_point", "mipmap", "com.jwch.gwyt_project")
        mapUtil.drawImage(context, poiSearchLayer, point, resId, data, false)
        showCallout(point, data, 0)
        mapView.setViewpointCenterAsync(point, defScale)
    }

    /**
     * 删除一个收藏的poi点
     */
    fun removeOneCollectGraphics(poid: String) {
        PrintUtil.printMsg("poid==$poid")
        if (CommonUtil.isNotNull(poiSearchLayer)) {
            for (item in poiSearchLayer.getGraphics()) {
                val strData = item.attributes["data"] as String
                val itemData: PoisInfo = getObjFromJson2(strData, PoisInfo::class.java)
                //收藏的是兴趣点
                if (CommonUtil.isNotNull(itemData)) {
                    if (CommonUtil.isNotEmpty(poid) && itemData.id == poid) {
                        poiSearchLayer.graphics.remove(item)
                        if (mapView.callout != null && mapView.callout.isShowing) {
                            mapView.callout.dismiss()
                        }
                        break
                    }
                } else {
                    //收藏的是专题图查询结果数据
                    poiSearchLayer.getGraphics().clear()
                    if (mapView.callout != null && mapView.callout.isShowing) {
                        mapView.callout.dismiss()
                    }
                }
            }
        }
    }

    /**
     * 展示地图弹窗
     */
    private fun showCallout(centerPoint: Point, obj: Any, tag: Int) {
        val isGeoJson = tag == TYPE_GEOJSON
        val isShp = tag == TYPE_SHP
        val maxWidth = if (isGeoJson || isShp) 400 else 500
        val maxHeight = 500
        val minWidth = 100
        val minHeight = 100

        if (tag == TYPE_POI) {

        } else if (tag == TYPE_THEME) {
            if (themeCallOut == null) {
                themeCallOut = ThemeCallOut(context)
                themeCallOut?.setCallout(callout)
            }
            themeCallOut?.setLayerInfo(obj as HashMap<String?, Any?>)
            callout.content = themeCallOut
        } else if (tag == TYPE_GEO) {
//            //点击标绘收藏  展示 图层参数
            val markerInfo = obj as MarkerInfo
            if (markerCallOut == null) {
                markerCallOut = MarkerCallOut(context)
                markerCallOut?.setCallout(callout)
            }
            markerCallOut?.setCalloutData(markerInfo)
            callout.content = markerCallOut

        } else if (tag == TYPE_GEOJSON) {
            @Suppress("UNCHECKED_CAST")
            val attrs = obj as Map<String, Any>
            callout.content = createGeoJsonCalloutView(attrs, callout)
        } else if (tag == TYPE_SHP) {
            @Suppress("UNCHECKED_CAST")
            val attrs = obj as Map<String, Any>
            callout.content = createGeoJsonCalloutView(attrs, callout)
        }

        //设置Callout样式--加载方式一
        val style = Callout.Style(context)
        style.maxWidth = maxWidth //设置最大宽度
        style.maxHeight = maxHeight //设置最大高度
        style.minWidth = minWidth //设置最小宽度
        style.minHeight = minHeight //设置最小高度
        //style.borderWidth = 2 //设置边框宽度
        style.borderColor = context.resources.getColor(R.color.transparent) //设置边框颜色
        style.backgroundColor = context.resources.getColor(R.color.transparent) //设置背景颜色
        style.cornerRadius = 4 //设置圆角半径
        //style.setLeaderLength(50); //设置指示性长度
        //style.setLeaderWidth(5); //设置指示性宽度
        style.leaderPosition = Callout.Style.LeaderPosition.LOWER_MIDDLE //设置指示性位置
        callout.style = style
        callout.location = centerPoint //设置弹窗展示的位置
        callout.show()
    }

    /**
     * 样式属性 key 集合，这些字段不在弹窗中展示
     */
    private val styleKeys = setOf(
        "fillColor", "fillOpacity", "strokeColor", "strokeWidth", "radius"
    )

    /**
     * 创建 GeoJSON 属性弹窗 View，带标题栏和关闭按钮
     */
    private fun createGeoJsonCalloutView(attrs: Map<String, Any>, callout: Callout): View {
        val view = LayoutInflater.from(context).inflate(R.layout.view_geojson_callout, null)

        // 标题
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val nameValue = attrs["name"]
        if (nameValue != null && nameValue.toString().isNotEmpty()) {
            tvTitle.text = nameValue.toString()
        }

        // 关闭按钮
        val imgClose = view.findViewById<ImageView>(R.id.imgClose)
        imgClose.setOnClickListener { callout.dismiss() }

        // 属性内容
        val llContent = view.findViewById<LinearLayout>(R.id.llContent)
        val density = context.resources.displayMetrics.density
        val textSize = 15f

        attrs.forEach { (key, value) ->
            if (key.startsWith("_") || value.toString().isEmpty() || key in styleKeys) return@forEach
            val label = when (key) {
                "name" -> "名称"
                "id" -> "编号"
                else -> key
            }
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                val p2 = (4 * density).toInt()
                setPadding(0, p2, 0, p2)
            }
            row.addView(TextView(context).apply {
                text = "$label: "
                setTextColor(Color.parseColor("#aaccee"))
                this.textSize = textSize
            })
            row.addView(TextView(context).apply {
                text = value.toString()
                setTextColor(Color.parseColor("#d4e9ff"))
                this.textSize = textSize
                maxWidth = (320 * density).toInt()
            })
            llContent.addView(row)
        }

        return view
    }

    /**
     * 清除所有GraphicsOverlay图层 和callout
     * @param clearMyLocation 是否连「我的位置」定位点一起清除。
     *    默认 false（保留定位点）——翻页/开关面板等流程不应误清定位点；
     *    仅用户主动「清除地图」时传 true。
     */
    fun clearDrawLayerAndDismissCallout(clearMyLocation: Boolean = false) {
        if (clearMyLocation && CommonUtil.isNotNull(myLocationLayer)) {
            cancelMyLocationMove()
            myLocationLayer.graphics.clear()
        }
        if (CommonUtil.isNotNull(poiSearchLayer)) {
            poiSearchLayer.graphics.clear()
        }
        if (CommonUtil.isNotNull(graphicsLayer)) {
            graphicsLayer.graphics.clear()
        }
        if (CommonUtil.isNotNull(shpGraphicsLayer)) {
            shpGraphicsLayer.graphics.clear()
        }
        if (mapView != null && mapView.callout != null) {
            mapView.callout.dismiss()
        }
        if (CommonUtil.isNotNull(bufferAnalysisResultLayer)) {
            bufferAnalysisResultLayer.graphics.clear()
        }
        if (CommonUtil.isNotNull(overlyingLayer)) {
            overlyingLayer.graphics.clear()
        }
        if (CommonUtil.isNotNull(graphicsDisplayLayer)) {
            graphicsDisplayLayer.graphics.clear()
        }

    }

    fun clearPoiSearchLayer() {
        if (CommonUtil.isNotNull(poiSearchLayer)) {
            poiSearchLayer.graphics.clear()
        }
    }

    /**
     * 清除所有收藏的展示标绘GraphicsOverlay
     */
    fun clearCollectionGrapicslayer() {
        if (CommonUtil.isNotNull(graphicsDisplayLayer)) {
            graphicsDisplayLayer.graphics.clear()
        }
    }


    /**
     * 保存标绘
     */
    fun saveGeometyMarker(info: GraphicInfo?) {
        var saveEnable = true //是否能保存
        when (markerUtil.drawType) {
            DRAW_POINT -> saveEnable = markerUtil.getPointCount() > 0
            DRAW_POLYLINE -> saveEnable = markerUtil.getPointCount() > 1
            DRAW_POLYGON -> saveEnable = markerUtil.getPointCount() > 2
        }
        if (saveEnable) {
            //弹出保存对话框
            saveGeometyDialog = SaveGeometyDialog(context, object : ActionListener {
                override fun onAction(obj: Any?, flag: Int) {}
            }, markerUtil, markerUtil.drawType)
            saveGeometyDialog?.show()

        } else {
            Toast.makeText(context, "未构成几何图形，请重新绘图", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 保存工作记录
     */
    fun saveWorkRecord(info: GraphicInfo?) {
        var saveEnable = true //是否能保存
        when (markerUtil.drawType) {
            DRAW_POINT -> saveEnable = markerUtil.getPointCount() > 0
            DRAW_POLYLINE -> saveEnable = markerUtil.getPointCount() > 1
            DRAW_POLYGON -> saveEnable = markerUtil.getPointCount() > 2
        }
        if (saveEnable) {
            //弹出保存对话框
            saveWorkRecordDialog = SaveWorkRecordDialog(context, object : ActionListener {
                override fun onAction(obj: Any?, flag: Int) {}
            }, markerUtil, markerUtil.drawType)
            saveWorkRecordDialog?.show()

        } else {
            Toast.makeText(context, "未构成几何图形，请重新绘图", Toast.LENGTH_LONG).show()
        }
    }


    /**
     * 编辑工作纪律
     */
    @SuppressLint("SuspiciousIndentation")
    fun editWorkRecord(info: MarkerInfo?) {
        saveWorkRecordDialog = SaveWorkRecordDialog(context, object : ActionListener {
            override fun onAction(obj: Any?, flag: Int) {

            }
        }, markerUtil, markerUtil.drawType, info)
        saveWorkRecordDialog?.show()

    }

    /**
     * 编辑标绘
     */
    @SuppressLint("SuspiciousIndentation")
    fun editGeometyMarker(info: MarkerInfo?) {
        saveGeometyDialog = SaveGeometyDialog(context, object : ActionListener {
            override fun onAction(obj: Any?, flag: Int) {}
        }, markerUtil, markerUtil.drawType, info)
        saveGeometyDialog?.show()

    }

    /**
     * 绘制选中收藏的标绘
     */
    fun drawSelectCollectionMarker(list: MutableList<MarkerInfo>) {
        graphicsDisplayLayer.graphics.clear()
        list.forEach {
            val geometry = Geometry.fromJson(it.geometryJson)
            if (geometry != null) {
                when (it.geoType) {
                    DRAW_POINT -> {
                        mapUtil.drawCollectionMarker(
                            graphicsDisplayLayer,
                            geometry,
                            DRAW_POINT,
                            false
                        )
                        val newPoint = GeometryEngine.buffer(geometry, 10.0)
                        mapView.setViewpointGeometryAsync(newPoint, 150.0)
                    }

                    DRAW_POLYLINE -> {
                        mapUtil.drawCollectionMarker(
                            graphicsDisplayLayer,
                            geometry,
                            DRAW_POLYLINE,
                            false
                        )
                        mapView.setViewpointGeometryAsync(geometry, 150.0)
                    }

                    DRAW_POLYGON -> {
                        mapUtil.drawCollectionMarker(
                            graphicsDisplayLayer,
                            geometry,
                            DRAW_POLYGON,
                            false
                        )
                        mapView.setViewpointGeometryAsync(geometry, 150.0)
                    }
                }
            }

        }
    }


    /**
     * 绘制选中收藏的标绘 单个
     */
    fun drawSelectCollectionMarkerSingle(data: MarkerInfo) {
        graphicsDisplayLayer.graphics.clear()

        val geometry = Geometry.fromJson(data.geometryJson)
        if (geometry != null) {
            when (data.geoType) {
                DRAW_POINT -> {
                    mapUtil.drawCollectionMarkerWithAttr(
                        graphicsDisplayLayer,
                        geometry,
                        DRAW_POINT,
                        data,
                        true,
                    )
                    val newPoint = GeometryEngine.buffer(geometry, 10.0)
                    mapView.setViewpointGeometryAsync(newPoint, 150.0)
                }

                DRAW_POLYLINE -> {
                    mapUtil.drawCollectionMarkerWithAttr(
                        graphicsDisplayLayer,
                        geometry,
                        DRAW_POLYLINE,
                        data,
                        true
                    )
                    mapView.setViewpointGeometryAsync(geometry, 150.0)
                }

                DRAW_POLYGON -> {
                    mapUtil.drawCollectionMarkerWithAttr(
                        graphicsDisplayLayer,
                        geometry,
                        DRAW_POLYGON,
                        data,
                        true
                    )
                    mapView.setViewpointGeometryAsync(geometry, 150.0)
                }
            }
        }


    }

    fun darwCollectionMarkerAnalysis(data: MarkerInfo) {
        markerUtil.reset()
        data.let {
            val geometry = Geometry.fromJson(it.geometryJson)
            markerUtil.drawType = it.geoType
            // point:0  polyline:1  polygon:2
            when (markerUtil.drawType) {
                0 -> {
                    val point = geometry as Point
                    markerUtil.addPoint(point)

                }

                1 -> {
                    val polyline = geometry as Polyline
                    polyline.parts.partsAsPoints.forEach {
                        markerUtil.addPointOnly(it)
                    }
                    markerUtil.refresh()
                }

                2 -> {
                    val polygon = geometry as Polygon
                    polygon.parts.partsAsPoints.forEach {
                        markerUtil.addPointOnly(it)
                    }
                    markerUtil.refresh()
                }
            }

            if (geometry != null) {
                val point = geometry.extent.center
                mapView.setViewpointGeometryAsync(point)
            }

        }

    }


    fun sortTiledLayerList() {
        val originalList = mapView.map.operationalLayers.toList()
        val sortedList = originalList.sortedWith(compareBy<Layer> {
            when (it.name) {
                "影像底图2022" -> 0    // 最高优先级
                "影像底图2024" -> 0    // 最高优先级
                "影像底图2025" -> 0    // 最高优先级
                "无人机影像" -> 0    // 最高优先级
                "行政区划" -> 1        // 第三优先级
                "河道" -> 2           // 第二优先级
                "河管线" -> 2           // 第二优先级
                "河道管理范围线" -> 2           // 第二优先级
                "河流" -> 2           // 第二优先级
                else -> 3            // 其他保持原顺序
            }
        })
        mapView.map.operationalLayers.clear()
        mapView.map.operationalLayers.addAll(sortedList)
    }


    fun layerFilterRefresh(layerFilterType: Int, eventStatusFilterParams: String) {

        if (this.layerFilterType == layerFilterType) {
            return
        }
        this.eventStatusFilterParams = eventStatusFilterParams

        this.layerFilterType = layerFilterType
        zttItemList.forEach {
            val info = it.themesInfo!!
            val tiledLayer = info.tiledLayer


            for (item in mapView.map.operationalLayers) {
                if (info.themeName.startsWith("乱") || info.themeName.startsWith("其他")) {
                    if (item.id == "${tiledLayer?.id}_point") {
                        mapView.map.operationalLayers.remove(item)
                    }
                }

                if (item.id.isNotBlank() && tiledLayer?.id.self()
                        .isNotBlank() && tiledLayer?.id == item.id
                ) {
                    mapView.map.operationalLayers.remove(item)
                    break
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(1 * 50)
                mapView.invalidate()

                "=====123=====1".printMsg()

                val tiledLayerNew = if (info.vectorTiledLayer != null) {
                    when (layerFilterType) {
                        0 -> {
                            info.vectorTiledLayer
                        }

                        1 -> {
                            info.vectorTiledLayer_1
                        }

                        2 -> {
                            info.vectorTiledLayer_2
                        }

                        else -> {
                            info.vectorTiledLayer
                        }
                    }
                } else {
                    info.tiledLayer
                }
                if (tiledLayerNew != null) {
                    "=====123=====2".printMsg()
                    mapView.map.operationalLayers.add(tiledLayerNew)
                }
            }

        }


    }

    /**
     * 控制图层展示
     */
    fun controlZttLayer(data: ZttItem?) {
        if (data == null) return

        //单个图层的展示和隐藏
        val info = data.themesInfo!!
        if (CommonUtil.isNotNull(info)) {
            //如果一个业务图层没有gdb 就不加载，不然点击查询的功能会失效 (影像和行政区划除外)
            if (!info.themeName.contains("影像") && !info.themeName.contains("行政区划") && !info.isGdbFileNotEmpty) {
                return
            }


            val featureLayer = info.layer

            var tiledLayer = if (info.vectorTiledLayer != null) {
                when (layerFilterType) {
                    0 -> {
                        info.vectorTiledLayer
                    }

                    1 -> {
                        info.vectorTiledLayer_1
                    }

                    2 -> {
                        info.vectorTiledLayer_2
                    }

                    else -> {
                        info.vectorTiledLayer
                    }
                }
            } else {
                info.tiledLayer
            }


            if (info.themeName.contains("影像")
                || info.themeName.contains("河道")
                || info.themeName.contains("河管线")
                || info.themeName.contains("河道管理范围线")
                || info.themeName.contains("蓝线")
                || info.themeName.contains("红线")
                || info.themeName.contains("河流")
                || info.themeName.contains("行政区划")
            ) {
                tiledLayer = if (info.vectorTiledLayer != null) {
                    info.vectorTiledLayer
                } else {
                    info.tiledLayer
                }
            }


            //选中
            if (data.isCheck) {
                //如果操作的是影像底图，要通知首页更新地图左下角的文字
                if (info.themeName.contains("影像底图")) {
                    baseMapStack.push(info.themeName)
                    EventBus.getDefault().post(DataEvent(DataEvent.SELECT_BASE_IMAGE_LAYER_NMAE))

                    when (info.themeName) {
                        "影像底图2022" -> {
                            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2022)
                        }

                        "影像底图2024" -> {
                            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2024)
                        }
                        "影像底图2025" -> {
                            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2025)
                        }
                    }

                } else {
                    //先往地图最上层add进去
                    tiledLayer?.let {
                        //赋值的目的是 能按照正确的名称顺序进行排序 （如果读取是图层读文件夹，不是tpk，名称就会变成文件夹里面的“图层”）
                        tiledLayer.name = info.themeName

//                        mapView.setViewpointAsync(Viewpoint(tiledLayer.fullExtent))

                        mapView.map.operationalLayers.add(tiledLayer)

                        if (info.themeName.contains("影像")
                            || info.themeName.contains("河道")
                            || info.themeName.contains("河管线")
                            || info.themeName.contains("蓝线")
                            || info.themeName.contains("红线")
                            || info.themeName.contains("河道管理范围线")
                            || info.themeName.contains("河流")
                            || info.themeName.contains("行政区划")
                        ) {
                            sortTiledLayerList()
                        } else {
                            zttItemList.add(data)
                        }


//                        if (info.themeName.contains("影像底图")
//                            || info.themeName.contains("河道")
//                            || info.themeName.contains("河流")
//                            || info.themeName.contains("行政区划")
//                        ) {
//                            //如果是基础数据 则加载tpk
//                            mapView.map.operationalLayers.add(
//                                tiledLayer
//                            )
//                            sortTiledLayerList()
//                        } else {
//                            //如果是业务数据 则加载featureLayer 自己渲染样式
//                            drawLayerStyle(info.themeName, featureLayer)
////                            if (info.themeName.startsWith("乱") || info.themeName.startsWith("其他")) {
////                                createPointFeature(info, featureLayer)
////                            }
//                            mapView.map.operationalLayers.add(
//                                mapView.map.operationalLayers.size - 1,
//                                featureLayer
//                            )
//                        }
                    }


                    selectLayers.add(info)
                    val tempList = selectLayers.sortedWith(compareBy<ThemesInfo> {
                        when (it.themeName) {
                            "影像底图2022" -> 0    // 最高优先级
                            "影像底图2024" -> 0    // 最高优先级
                            "影像底图2025" -> 0    // 最高优先级
                            "无人机影像" -> 0    // 最高优先级
                            "行政区划" -> 1           // 第二优先级
                            "河道" -> 2           // 第三优先级
                            "河管线" -> 2           // 第三优先级
                            "河道管理范围线" -> 2           // 第三优先级
                            "河流" -> 2           // 第三优先级
                            else -> 3            // 其他保持原顺序
                        }
                    })
                    selectLayers.clear()
                    selectLayers.addAll(tempList)


//                    mapView.map.operationalLayers.add(mapView.map.operationalLayers.size - 1, tiledLayer)
//                    selectLayers.add(info)

                    OperationLogger.logOperation(context, "加载图层：${info.themeName}")
                }
            } else {
                //反选
                if (!info.themeName.contains("影像") && !info.themeName.contains("行政区划") && !info.isGdbFileNotEmpty) {
                    return
                }

                zttItemList.remove(data)

                if (info.themeName.contains("影像底图")) {
                    baseMapStack.popFirst(info.themeName)
                    EventBus.getDefault().post(DataEvent(DataEvent.UNSELECT_BASE_IMAGE_LAYER_NMAE))
                    when (baseMapStack.peek()) {
                        "影像底图2018" -> {
                            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2018)
                        }

                        "影像底图2022" -> {
                            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2022)
                        }

                        "影像底图2024" -> {
                            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2024)
                        }
                        "影像底图2025" -> {
                            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2025)
                        }
                    }
                } else {


                    if (!selectLayers.isNullOrEmpty()) {
                        if (selectLayers.contains(info)) {
                            //含有该对象则删除
                            selectLayers.remove(info)
                        }
                    }

                    //遍历找到对应的图层
                    for (item in mapView.map.operationalLayers) {
                        if (info.themeName.startsWith("乱") || info.themeName.startsWith("其他")) {
                            if (item.id == "${tiledLayer?.id}_point") {
                                mapView.map.operationalLayers.remove(item)
                            }
                        }
                        if (item.id.isNotBlank() && tiledLayer?.id.self()
                                .isNotBlank() && tiledLayer?.id == item.id
                        ) {
                            mapView.map.operationalLayers.remove(item)
//                        break
                        }
                    }
                }


            }

            dismissCallout()
        }
    }


    fun Stack<String>.popFirst(element: String): Boolean {
        // 使用 ArrayDeque 作为临时存储，性能比 Stack 更好
        val tempStorage = java.util.ArrayDeque<String>()
        var found = false

        // 从栈顶开始查找（LIFO顺序）
        while (this.isNotEmpty()) {
            val item = this.pop()
            if (item == element && !found) {
                found = true  // 找到并移除第一个匹配项
                break  // 立即跳出循环，提高性能
            } else {
                tempStorage.push(item)  // 暂时保存非目标元素
            }
        }

        // 将临时存储的元素按原顺序放回栈中
        while (tempStorage.isNotEmpty()) {
            this.push(tempStorage.pop())
        }

        return found
    }

    fun drawLayerStyle(name: String, featureLayer: FeatureLayer) {

//        featureLayer.isLabelsEnabled = true

        var symbol: Symbol? = null
        when (featureLayer.featureTable.geometryType.name) {
            "POINT" -> {
                //根据不同类型数据，获取到图标
                val bitmapDrawable = CustomLayerDisplayUtils.getBitmapDrawableIcon(context, name)
                val pictureMarkerSymbol = PictureMarkerSymbol(bitmapDrawable)
                symbol = pictureMarkerSymbol
            }

            "MULTIPOINT" -> {
                symbol = SimpleMarkerSymbol(
                    SimpleMarkerSymbol.Style.CIRCLE,
                    Color.parseColor("#B03060"),
                    10f
                )
            }

            "POLYLINE" -> {
                symbol =
                    SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#FFFF00"), 2f)
            }

            "POLYGON" -> {
                val color = CustomLayerDisplayUtils.getColor(name)
                val line = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, 7f)
                symbol =
                    SimpleFillSymbol(SimpleFillSymbol.Style.NULL, Color.parseColor("#000000"), line)
            }

            else -> {
                symbol = SimpleMarkerSymbol(
                    SimpleMarkerSymbol.Style.CIRCLE,
                    Color.parseColor("#B03060"),
                    10f
                )
            }
        }

        val renderer: Renderer = SimpleRenderer(symbol)
        featureLayer.renderer = renderer
    }

    fun createPointFeature(info: ThemesInfo, featureLayer: FeatureLayer) {

        val queryParams = QueryParameters().apply { whereClause = "1=1" }
        // 异步查询要素
        val featureTable = featureLayer.featureTable
        val featureFuture: ListenableFuture<FeatureQueryResult> =
            featureTable.queryFeaturesAsync(queryParams)
        featureFuture.addDoneListener {
            try {
                val featureQueryResult = featureFuture.get()

                // 1. 创建字段定义
                val fields = listOf(Field.createString("id", "ID", 10))

                // 2. 创建FeatureCollectionTable
                val pointFeatureTable = FeatureCollectionTable(
                    fields,
                    GeometryType.POINT,
                    featureLayer.spatialReference
                )

                // 3. 在表格上设置渲染器（关键步骤）

                //根据不同类型数据，获取到图标
                val bitmapDrawable =
                    CustomLayerDisplayUtils.getBitmapDrawableIcon(context, info.themeName)
                val pictureMarkerSymbol = PictureMarkerSymbol(bitmapDrawable)
                pointFeatureTable.renderer = SimpleRenderer(pictureMarkerSymbol)

                // 4. 计算中心点并添加到表格
                var index = 0
                for (feature in featureQueryResult) {
                    val polygon = feature.geometry as? Polygon
                    polygon?.let {
                        val centroid = polygon.extent.center
                        val pointFeature = pointFeatureTable.createFeature().apply {
                            geometry = centroid
                            attributes["id"] = "point_${index++}"
                        }
                        pointFeatureTable.addFeatureAsync(pointFeature)
                    }
                }

                // 5. 创建FeatureCollection并添加表格
                val featureCollection = FeatureCollection().apply {
                    tables.add(pointFeatureTable)
                }

                // 6. 创建FeatureCollectionLayer
                val centerPointsLayer = FeatureCollectionLayer(featureCollection)
                centerPointsLayer.id = "${info.tiledLayer.id}_point"

                mapView.map.operationalLayers.add(centerPointsLayer)


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }


    /**
     * 关闭地图弹窗
     */
    fun dismissCallout() {
        if (mapView.callout != null) {
            mapView.callout.dismiss()
        }
    }

    /**
     * 清空地图底图
     */
    fun clearBaseLayer() {
        if (mapView.map.basemap.baseLayers.size > 0) {
            mapView.map.basemap.baseLayers.clear()
        }
    }

    /**
     * 清空所有专题图
     */
    fun clearOperationLayers() {
        if (CommonUtil.isNotNull(mapView)) {
            mapView.map.operationalLayers.clear()
            selectLayers.clear()
        }
    }

    /**
     * 获取图形的中心点
     * @param geometry 图形对象
     * @return
     */
    fun getCenterPoint(geometry: Geometry): Point? {
        var point: Point? = null
        val type = geometry.geometryType
        if (type == GeometryType.POINT) {
            point = geometry as Point
        } else if (type == GeometryType.POLYLINE) {
            point = (geometry as Polyline).extent.center
        } else if (type == GeometryType.POLYGON) {
            point = (geometry as Polygon).extent.center
        }
        return point
    }


    /**
     * 点击空间--手势查询的 第三个页面的 feature 列表
     */
    fun showQueryResultFeature(map: HashMap<String?, Any?>?) {

        if (map == null) return

        val feature = map["feature"] as Feature? ?: return
        val info = map["info"] as ThemesInfo?

        val attr = feature.attributes

        try {
            //参数1
            val list: MutableList<KVModel> = ArrayList()
            val themeid = info!!.id
            val popFiled = info.popupInfoField1
            val popFiled1 = info.popupInfoField2
            val list1: List<ThemeFieldsInfo> = db.queryAllThemeFieldsByThemeIdAndFieldName(
                themeid.toString(),
                popFiled
            )


            if (CommonUtil.matchList(list1)) {
                val key1: String = list1[0].getDispName()
                val value1 = if (attr[popFiled] == null) "" else attr[popFiled].toString()
                list.add(KVModel(key1, value1))
            }

            //参数2
            if (popFiled1.isNotBlank()) {
                val key2: String = db.queryThemeFieldsByThemeIdAndFieldName(themeid, popFiled1)
                val value2 = if (attr[popFiled1] == null) "" else attr[popFiled1].toString()
                list.add(KVModel(key2, value2))
            }


//            MyApplication.ma.getGson().toJson(list);
            //            MyApplication.ma.getGson().toJson(list);
            map["valueList"] = list


            val geometry = feature.geometry
            val centerPoint = geometry.extent.center //几何图形的中心点

            showCallout(centerPoint, map, 1)

            mapUtil.drawGeomety(poiSearchLayer, feature.geometry, 2, true, null, true)
            drawAreaPoint(centerPoint, false)
        } catch (e: Exception) {
            ("======查询图层异常=======" + e.message).printMsg()
        }
    }

    //绘制地区标点
    fun drawAreaPoint(point: Point?, isRedraw: Boolean) {
        if (point == null) return
        mapUtil.drawImage(context, poiSearchLayer, point, R.mipmap.icon_point_big, isRedraw)
        mapView.setViewpointCenterAsync(point, 2200.0)
    }

    /**
     * 开始分析叠加的图层数据
     */
    fun startAnalysisOverly() {

        if (markerUtil.getPointCount() < 3) {
            context.toast("未形成面状图形")
            return
        }

        //得到已经画好的图形
        val polygon = markerUtil.getGeometry() as Polygon

        searchLayers.clear()
        analysisList.clear()

        index = 0 //查询序号重置
        infoCount = 0//专题图中查到的所有属性数量重置
        lastAnalysisList.clear()//清空上一次的分写结果数据
        clearAnalysisResultPatch()//清空已标绘过的叠加图斑

        (selectLayers.size > 0).yes {
            EventBus.getDefault().post(DataEvent(DataEvent.FINISH_ANALYSIS))
            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING))

            //叠加分析的区域所属行政区
            analysisdistrict(polygon)
            //叠加分析
            analysisLayer(selectLayers, polygon)

        }.no {
            tip(context, "暂无查询结果")
            return
        }
        finishAnalysis()
    }

    //暂停标绘
    fun finishAnalysis() {
        actionType = ACTION_OVERLYING_ANALYSIS_FINISH
        "h暂停标绘 2 actionType = $actionType  ".printMsg()
    }

    /**
     * 查询图层，看看有几个图层和图形相交，一个图层中，有几个图形是相交的
     */
    fun analysisLayer(layerlist: MutableList<ThemesInfo>, polygon: Polygon) {

        if (CommonUtil.matchList(layerlist)) {
            if (index >= layerlist.size) return;
            val info = layerlist[index]
            val layer = info.layer
            if (layer == null) {
                //图层是空的
                if (index == layerlist.size - 1) {
                    //如果是最后一项，查询结束
                    analysisEnd(analysisList)
                    return;
                }
                //还有数据可以查，就继续查下一个图层
                if (index < layerlist.size - 1) {
                    index++;
                    analysisLayer(selectLayers, polygon)
                }
                return
            }


            //查询参数
            val query = QueryParameters()
            query.geometry = polygon;
            val geomterySizeMu = caculationUtil.caculateAreaSizeMu(polygon, true) //标绘出的多边形大小（亩）
//            query.setSpatialRelationship(QueryParameters.SpatialRelationship.WITHIN);
//            query.outSpatialReference = Config.sp4490;
            query.outSpatialReference = Config.def_sp;
            query.whereClause = "1=1"

            info.features.clear()
            val queryList = layer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            queryList.addDoneListener {
                try {
                    val result = queryList.get()
                    val iterator: Iterator<Feature> = result.iterator()
                    if (!iterator.hasNext()) {
                        //没查到数据===查询图层异常===
                        if (index == layerlist.size - 1) {
                            //查询结束
                            analysisEnd(analysisList)
                            return@addDoneListener
                        }
                        if (index < layerlist.size - 1) {
                            index++
                            analysisLayer(selectLayers, polygon)
                        }
                        return@addDoneListener
                    } else {
                        analysisList.add(
                            AnalysisListModel(
                                info.themeName,
                                info.groupField,
                                geomterySizeMu
                            )
                        )
                    }
                    while (iterator.hasNext()) {

                        val feature = iterator.next() ?: continue
                        val attr = feature.attributes
//                        "attrs: ${attr.toJson()}".printMsg()
                        infoCount++
                        info.features.add(feature)
                        val geometry = feature.geometry

                        //传进来的 polygon是龙海的空间参考，查出来的是4490的，所以要转化
                        val trasGeometry = GeometryEngine.project(
                            geometry,
                            SpatialReference.create(Config.sp4490Int)
                        )
                        val isInterSection = GeometryEngine.intersects(trasGeometry, polygon)

                        if (isInterSection) {
                            //有交集
                            "有交集".printMsg()
                            val geoSize = GeometryEngine.intersection(trasGeometry, polygon)
                            when (geoSize) {
                                is Polygon -> {
                                    //是面状图形才能计算交集
                                    val size = caculationUtil.caculateAreaSizeUnit(
                                        geoSize,
                                        CaculationUtil.UNIT_DEFAULT,
                                        true
                                    )
                                    val sizeValue = caculationUtil.caculateAreaSize(geoSize)
//                            info.interSectionList.add(InterSectionModel(geoSize, size, attr))

                                    val data = InterSectionModel(geoSize, size, sizeValue, attr)
                                    data.layerName = info.themeName

                                    when (info.themeName) {
                                        "2018年地类图斑", "南安市控制性详细规划", "南安市城市总体规划" -> {
                                            data.geometryName =
                                                attr[info.getPopupInfoField(1)].toString()
                                        }

                                        else -> {
                                            data.geometryName =
                                                attr[info.getPopupInfoField(0)].toString()
                                        }
                                    }

                                    val item = analysisList.first { it.layerName == info.themeName }
                                    item.interSectionModel.add(data)

                                    lastAnalysisList.add(data)
                                }

                                else -> {

                                }
                            }
                        }
                        infoCount++
                    }
                    if (layerlist.size == 0 || index == layerlist.size - 1) {
                        //查询结束
                        analysisEnd(analysisList)
                        lastAnalysisList.forEach {
                            drawAnalysisResult(it, false)
                        }
                        return@addDoneListener
                    }
                    if (index < layerlist.size - 1) {
                        index++
                        lastAnalysisList.clear()
                        analysisLayer(selectLayers, polygon)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }


    /**
     * 叠加分析结束
     */
    fun analysisEnd(list: MutableList<AnalysisListModel>) {
        PrintUtil.printMsg("分析结果数量 ： " + list.size)
        PrintUtil.printMsg("信息数量 ： $infoCount")
        EventBus.getDefault().post(DataEvent(DataEvent.SHOW_QUERY_RESULT_LIST, list))
        EventBus.getDefault().post(DataEvent(DataEvent.COUNTY_NAME_LIST, countyNameList))
        EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
        if (infoCount == 0) {
            tip(context, "暂无分析结果")
        }
    }

    /**2.4一抽 50抽 保底10片
     * 将叠加分析的交集部分 标红12
     */
    fun drawAnalysisResult(item: InterSectionModel, isRedraw: Boolean = true) {

        mapUtil.drawOverlap(poiSearchLayer, item.geometry, isRedraw)
        //如果是画单个 设置Viewpoint
        isRedraw.yes {
            mapView.setViewpointGeometryAsync(item.geometry, 1000.0)
        }
    }

    /**
     * 将叠加分析 有交集的图斑清空
     */
    fun clearAnalysisResultPatch() {
        poiSearchLayer?.let { it.graphics.clear() }
    }


    /**
     * 加载省 行政区的gdb
     */
    fun loadProvinceLayer() {

        val filePath = "${Config.GDB_PATH}省级.geodatabase"
        val realPath = decryptGeodatabase(filePath)

        val geodatabase = Geodatabase(realPath)
        geodatabase.loadAsync()
        geodatabase.addDoneLoadingListener(Runnable {

            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                val tableList: List<GeodatabaseFeatureTable> = geodatabase.geodatabaseFeatureTables
                if (CommonUtil.matchList(tableList)) {
                    val table = tableList[0]
                    table.loadAsync()
                    //创建要素图层
                    val featureLayer = FeatureLayer(table)

                    featureLayer.addDoneLoadingListener {
                        if (featureLayer.loadStatus == LoadStatus.LOADED) {
                            "省级gdb加载成功".printMsg()
                            provinceFeatureLayer = featureLayer
//
                            //===查询===
//                            val query = QueryParameters()
//                            val queryList = provinceFeatureLayer!!.selectFeaturesAsync(
//                                query,
//                                FeatureLayer.SelectionMode.NEW
//                            )
//                            queryList.addDoneListener {
//                                try {
//                                    val result = queryList.get()
//                                    val iterator: Iterator<Feature> = result.iterator()
//                                    while (iterator.hasNext()) {
//
//                                        val feature = iterator.next() ?: continue
//                                        val attr = feature.attributes
//                                        "attrs: ${attr.toJson()}".printMsg()
//                                    }
//                                } catch (e: ExecutionException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                            //===查询结束===

                        }
                    }
                }
            }
        })

    }

    /**
     * 加载区县行政区的gdb
     */
    fun loadCityLayer() {

        val filePath = "${Config.GDB_PATH}市级.geodatabase"
        val realPath = decryptGeodatabase(filePath)

        val geodatabase = Geodatabase(realPath)
        geodatabase.loadAsync()
        geodatabase.addDoneLoadingListener(Runnable {

            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                val tableList: List<GeodatabaseFeatureTable> = geodatabase.geodatabaseFeatureTables
                if (CommonUtil.matchList(tableList)) {
                    val table = tableList[0]
                    table.loadAsync()
                    //创建要素图层
                    val featureLayer = FeatureLayer(table)

                    featureLayer.addDoneLoadingListener {
                        if (featureLayer.loadStatus == LoadStatus.LOADED) {
                            "市级gdb加载成功".printMsg()
                            cityFeatureLayer = featureLayer
//
//                            //===查询===
//                            val query = QueryParameters()
//                            val queryList = cityFeatureLayer!!.selectFeaturesAsync(
//                                query,
//                                FeatureLayer.SelectionMode.NEW
//                            )
//                            queryList.addDoneListener {
//                                try {
//                                    val result = queryList.get()
//                                    val iterator: Iterator<Feature> = result.iterator()
//                                    while (iterator.hasNext()) {
//
//                                        val feature = iterator.next() ?: continue
//                                        val attr = feature.attributes
//                                        "attrs: ${attr.toJson()}".printMsg()
//                                    }
//                                } catch (e: ExecutionException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                            //===查询结束===

                        }
                    }
                }
            }
        })

    }

    /**
     * 加载区县行政区的gdb
     */
    fun loadAreaLayer() {

        val filePath = "${Config.GDB_PATH}区县.geodatabase"
        val realPath = decryptGeodatabase(filePath)
        val geodatabase = Geodatabase(realPath)

        geodatabase.loadAsync()
        geodatabase.addDoneLoadingListener(Runnable {

            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                val tableList: List<GeodatabaseFeatureTable> = geodatabase.geodatabaseFeatureTables
                if (CommonUtil.matchList(tableList)) {
                    val table = tableList[0]
                    table.loadAsync()
                    //创建要素图层
                    val featureLayer = FeatureLayer(table)

                    featureLayer.addDoneLoadingListener {
                        if (featureLayer.loadStatus == LoadStatus.LOADED) {
                            "区县gdb加载成功".printMsg()
                            countyFeatureLayer = featureLayer
//
                            //===查询===
//                            val query = QueryParameters()
//                            val queryList = areaFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
//                            queryList.addDoneListener {
//                                try {
//                                    val result = queryList.get()
//                                    val iterator: Iterator<Feature> = result.iterator()
//                                    while (iterator.hasNext()) {
//
//                                        val feature = iterator.next() ?: continue
//                                        val attr = feature.attributes
//                                        "attrs: ${attr.toJson()}".printMsg()
//                                    }
//                                }catch ( e : ExecutionException){
//                                    e.printStackTrace()
//                                }
//                            }
                            //===查询结束===

                        }
                    }
                }
            }
        })

    }

    /**
     * 加载镇级行政区的gdb
     */
    fun loadCountyLayer() {

        val filePath = "${Config.GDB_PATH}乡镇.geodatabase"
        val realPath = decryptGeodatabase(filePath)

        val geodatabase = Geodatabase(realPath)
        geodatabase.loadAsync()
        geodatabase.addDoneLoadingListener(Runnable {

            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                val tableList: List<GeodatabaseFeatureTable> = geodatabase.geodatabaseFeatureTables
                if (CommonUtil.matchList(tableList)) {
                    val table = tableList[0]
                    table.loadAsync()
                    //创建要素图层
                    val featureLayer = FeatureLayer(table)

                    featureLayer.addDoneLoadingListener {
                        if (featureLayer.loadStatus == LoadStatus.LOADED) {
                            "gdb加载成功 乡镇".printMsg()
                            townFeatureLayer = featureLayer
//
//                            //===查询===
//                            val query = QueryParameters()
//                            val queryList = countyFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
//                            queryList.addDoneListener {
//                                try {
//                                    val result = queryList.get()
//                                    val iterator: Iterator<Feature> = result.iterator()
//                                    while (iterator.hasNext()) {
//
//                                        val feature = iterator.next() ?: continue
//                                        val attr = feature.attributes
//                                        "attrs: ${attr.toJson()}".printMsg()
//                                    }
//                                }catch ( e : ExecutionException){
//                                    e.printStackTrace()
//                                }
//                            }
//                            //===查询结束===

                        }
                    }
                }
            }
        })

    }


    /**
     * 加载村级行政区划的gdb
     */
    fun loadVillageLayer() {
        val filePath = "${Config.GDB_PATH}村级.geodatabase"
        val realPath = decryptGeodatabase(filePath)


        val geodatabase = Geodatabase(realPath)
        geodatabase.loadAsync()
        geodatabase.addDoneLoadingListener(Runnable {

            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                val tableList: List<GeodatabaseFeatureTable> = geodatabase.geodatabaseFeatureTables
                if (CommonUtil.matchList(tableList)) {
                    val table = tableList[0]
                    table.loadAsync()
                    //创建要素图层
                    val featureLayer = FeatureLayer(table)

                    featureLayer.addDoneLoadingListener {
                        "1234567".printMsg()
                        featureLayer.loadStatus.toJson().printMsg()
                        if (featureLayer.loadStatus == LoadStatus.LOADED) {
                            "villageFeatureLayer gdb加载成功".printMsg()
                            villageFeatureLayer = featureLayer

//                            //===查询===
//                            val query = QueryParameters()
//                            val queryList = villageFeatureLayer!!.selectFeaturesAsync(
//                                query,
//                                FeatureLayer.SelectionMode.NEW
//                            )
//                            queryList.addDoneListener {
//                                try {
//                                    val result = queryList.get()
//                                    val iterator: Iterator<Feature> = result.iterator()
//                                    while (iterator.hasNext()) {
//
//                                        val feature = iterator.next() ?: continue
//                                        val attr = feature.attributes
//                                        "attrs: ${attr.toJson()}".printMsg()
//                                    }
//                                } catch (e: ExecutionException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                            //===查询===

                        }
                    }
                }
            }
        })
    }


    var geometryList: MutableList<Geometry> = mutableListOf()

    //查询 行政区划图层 区县级
    fun searchAreaLayer(
        areaName: String,
        areaCode: String,
        data: DistrictsInfo,
        isStatis: Boolean = true
    ) {

        val areaCodeKey = "XZQDM"//行政区代码
//        val areaCodeKey = "区级区划编号"


        geometryList.clear()

        if (countyFeatureLayer != null && areaName.isNotBlank()) {
            //查询参数·
            val query = QueryParameters()
            query.isReturnGeometry = true
//            query.whereClause = "$areaCodeKey = '$areaCode'"//行政区code匹配
            query.whereClause = "$areaCodeKey like '$areaCode%'"//行政区code匹配
            val future =
                countyFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()

                        if (!iterator.hasNext()) {
//                            showSingleDialog(context, "暂无数据")
                            tip(context, "暂无定位数据")
                        }
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==： + ${getParmas(attr)}".printMsg()

//                            val geometry = feature.geometry
                            geometryList.add(feature.geometry)

//                            mapUtil.drawGeometyOutline(poiSearchLayer, geometry, 2, true)
//                            mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
//                            mapView.setViewpointGeometryAsync(geometry, 2000.0)
                            PrintUtil.printMsg("======运行完成=======")

//                            break
                            //
                        }
                        if (!iterator.hasNext() && geometryList.isNotEmpty()) {
                            geometryList.forEachIndexed { index, geometry ->
                                when (index) {
                                    0 -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, true)
                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                        mapView.setViewpointGeometryAsync(geometry, 4000.0)
                                    }

                                    else -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, false)
                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                    }
                                }

                            }

                        }
                        isStatis.yes {
                            EventBus.getDefault().post(MapEvent(MapEvent.GO_STRAT))
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }


    //查询 行政区划图层  省级
    fun searchProvinceLayer(
        areaName: String,
        areaCode: String,
        data: DistrictsInfo,
        isStatis: Boolean = true
    ) {

        val areaCodeKey = "XZQDM"//行政区代码
//        val areaCodeKey = "区级区划编号"

        geometryList.clear()

        if (provinceFeatureLayer != null && areaName.isNotBlank()) {
            //查询参数
            val query = QueryParameters()
            query.isReturnGeometry = true

//            query.whereClause = "$areaCodeKey = '$areaCode'"//行政区code匹配
            query.whereClause = "$areaCodeKey like '$areaCode%'"//行政区code匹配
            val future =
                provinceFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()

                        if (!iterator.hasNext()) {
//                            showSingleDialog(context, "暂无数据")
                            tip(context, "暂无定位数据")
                        }
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==： + ${getParmas(attr)}".printMsg()

//                            val geometry = feature.geometry
                            geometryList.add(feature.geometry)

//                            mapUtil.drawGeometyOutline(poiSearchLayer, geometry, 2, true)
//                            mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
//                            mapView.setViewpointGeometryAsync(geometry, 2000.0)
                            PrintUtil.printMsg("======运行完成=======")

//                            break
                            //
                        }
                        if (!iterator.hasNext() && geometryList.isNotEmpty()) {

                            geometryList.forEachIndexed { index, geometry ->
                                when (index) {
                                    0 -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, true)
//                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                        mapView.setViewpointGeometryAsync(geometry, 4000.0)
                                    }

                                    else -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, false)
//                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                    }
                                }

                            }

                        }
                        isStatis.yes {
                            EventBus.getDefault().post(MapEvent(MapEvent.GO_STRAT))
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }


    //查询 行政区划图层  市级
    fun searchCityLayer(
        areaName: String,
        areaCode: String,
        data: DistrictsInfo,
        isStatis: Boolean = true
    ) {

        val areaCodeKey = "XZQDM"//行政区代码
//        val areaCodeKey = "区级区划编号"

        geometryList.clear()

        if (cityFeatureLayer != null && areaName.isNotBlank()) {
            //查询参数
            val query = QueryParameters()
            query.isReturnGeometry = true
//            query.whereClause = "$areaKey = '$areaName'"//行政区名称匹配
//            query.whereClause = "$areaCodeKey = '$areaCode'"//行政区code匹配
            query.whereClause = "$areaCodeKey like '$areaCode%'"//行政区code匹配
            val future =
                cityFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()

                        if (!iterator.hasNext()) {
//                            showSingleDialog(context, "暂无数据")
                            tip(context, "暂无定位数据")
                        }
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==： + ${getParmas(attr)}".printMsg()

//                            val geometry = feature.geometry
                            geometryList.add(feature.geometry)

//                            mapUtil.drawGeometyOutline(poiSearchLayer, geometry, 2, true)
//                            mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
//                            mapView.setViewpointGeometryAsync(geometry, 2000.0)
                            PrintUtil.printMsg("======运行完成=======")

//                            break
                            //
                        }
                        if (!iterator.hasNext() && geometryList.isNotEmpty()) {
                            geometryList.forEachIndexed { index, geometry ->
                                when (index) {
                                    0 -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, true)
//                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                        mapView.setViewpointGeometryAsync(geometry, 4000.0)
                                    }

                                    else -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, false)
//                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                    }
                                }

                            }
                        }
                        isStatis.yes {
                            EventBus.getDefault().post(MapEvent(MapEvent.GO_STRAT))
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }

    //查询 行政区划图层 镇级
    fun searchCountyLayer(
        areaName: String,
        areaCode: String,
        data: DistrictsInfo,
        isStatis: Boolean = true
    ) {

        val areaCodeKey = "XZQDM"//行政区代码
//        val areaCodeKey = "area_code"

        geometryList.clear()

        if (townFeatureLayer != null && areaName.isNotBlank()) {
            //查询参数
            val query = QueryParameters()
            query.isReturnGeometry = true
//            query.whereClause = "$areaCodeKey = '$areaCode'"//行政区code匹配
            query.whereClause = "$areaCodeKey like '$areaCode%'"//行政区code匹配
            val future =
                townFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()

                        if (!iterator.hasNext()) {
//                            showSingleDialog(context, "暂无数据")
                            tip(context, "暂无定位数据")
                        }
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==： + ${getParmas(attr)}".printMsg()

//                            val geometry = feature.geometry
                            geometryList.add(feature.geometry)

//                            mapUtil.drawGeometyOutline(poiSearchLayer, geometry, 2, true)
//                            mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
//                            mapView.setViewpointGeometryAsync(geometry, 2000.0)
                            PrintUtil.printMsg("======运行完成=======")
//                            break
                            //
                        }
                        if (!iterator.hasNext() && geometryList.isNotEmpty()) {

                            geometryList.forEachIndexed { index, geometry ->
                                when (index) {
                                    0 -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, true)
                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                        mapView.setViewpointGeometryAsync(geometry, 4000.0)
                                    }

                                    else -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, false)
                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                    }
                                }
                            }
                        }
                        isStatis.yes {
                            EventBus.getDefault().post(MapEvent(MapEvent.GO_STRAT))
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }

    //查询 行政区划图层 村级
    fun searchVillageLayer(
        areaName: String,
        areaCode: String,
        data: DistrictsInfo,
        isStatis: Boolean = true
    ) {
//        val areaNameKey = "XZQMC"//行政村名称key
        val areaCodeKey = "XZQDM"//行政村Code的key

        geometryList.clear()

        if (villageFeatureLayer != null && areaName?.isNotBlank() == true) {
            //查询参数
            val query = QueryParameters()
            query.isReturnGeometry = true
//            query.whereClause = "$areaCodeKey = '$areaCode'"//行政区code匹配

            query.whereClause = "$areaCodeKey like '${areaCode}%'"//行政区code匹配

            val future =
                villageFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()
                        while (iterator.hasNext()) {
                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==： + ${getParmas(attr)}".printMsg()
                            geometryList.add(feature.geometry)

//                            val geometry = feature.geometry

                            PrintUtil.printMsg("======运行完成=======")
//                            break
                        }
                        if (!iterator.hasNext() && geometryList.isNotEmpty()) {
                            data.geometryList = geometryList
                            isStatis.yes {
                                EventBus.getDefault().post(MapEvent(MapEvent.GO_STRAT))
                            }
                            geometryList.forEachIndexed { index, geometry ->
                                when (index) {
                                    0 -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, true)
                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                        mapView.setViewpointGeometryAsync(geometry, 4000.0)
                                    }

                                    else -> {
                                        mapUtil.drawPolygonArea(poiSearchLayer, geometry, false)
                                        mapUtil.drawText(poiSearchLayer, areaName, geometry, false)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }


    //查询 行政区划图层 镇级
    fun analysisdistrict(polygon: Polygon) {
        countyNameList.clear()
        //查询参数
        val query = QueryParameters()
        query.geometry = polygon;
        query.outSpatialReference = Config.def_sp;
        query.whereClause = "1=1"

        if (townFeatureLayer != null) {

            val future =
                townFeatureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==============： + ${getParmas(attr)}".printMsg()

                            val countyName = attr.getOrDefault("XZQMC", "").toString()
                            countyName.isBlank().no {
                                countyNameList.add(countyName)
                            }

                            PrintUtil.printMsg("======运行完成=======")
                            //
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }


    fun drawSelectGeoJson(data: GeoJsonModel) {
        scope.launch(Dispatchers.IO) {
            graphicsLayer.graphics.clear()
            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING))
            val featureList = data.getData()
            featureList?.forEachIndexed { index, feature ->
                mapUtil.drawGeomety(graphicsLayer, feature.geometry, feature.styleConfig, false)
                // 设置属性用于点击查询
                if (feature.properties.isNotEmpty()) {
                    graphicsLayer.graphics.lastOrNull()?.attributes?.putAll(
                        feature.properties.mapValues { it.value ?: "" })
                }
                (index == featureList.size - 1).yes {
                    val point = feature.geometry.extent.center
                    withContext(Dispatchers.Main) {
                        mapView.setViewpointGeometryAsync(point)
                    }
                }
            }

            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
        }

    }

    /**
     * 绘制多个 GeoJSON（多选模式），先清空再绘制所有选中的
     */
    fun drawSelectGeoJsonList(dataList: List<GeoJsonModel>) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { graphicsLayer.graphics.clear() }
            if (dataList.isEmpty()) {
                return@launch
            }
            var loadingShown = false
            val loadingJob = launch(Dispatchers.Main) {
                delay(300)
                loadingShown = true
                EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING))
            }
            try {
                dataList.forEach { model ->
                    val featureList = model.getData()
                    featureList?.forEach { feature ->
                        mapUtil.drawGeomety(graphicsLayer, feature.geometry, feature.styleConfig, false)
                        if (feature.properties.isNotEmpty()) {
                            graphicsLayer.graphics.lastOrNull()?.attributes?.putAll(
                                feature.properties.mapValues { it.value ?: "" })
                        }
                    }
                }
                val lastFeature = dataList.lastOrNull()?.getData()?.lastOrNull()
                if (lastFeature != null) {
                    val point = lastFeature.geometry.extent.center
                    withContext(Dispatchers.Main) {
                        mapView.setViewpointGeometryAsync(point)
                    }
                }
            } finally {
                loadingJob.cancel()
                if (loadingShown) {
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
                }
            }
        }
    }

    /**
     * 绘制多个 SHP（多选模式），先清空再绘制所有选中的
     */
    fun drawShpList(dataList: List<com.jwch.gwyt_project.model.ShpModel>) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { shpGraphicsLayer.graphics.clear() }
            if (dataList.isEmpty()) {
                return@launch
            }
            // 延迟显示 loading，避免快速加载时的闪烁
            var loadingShown = false
            val loadingJob = launch(Dispatchers.Main) {
                delay(300)
                loadingShown = true
                EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING))
            }
            try {
                dataList.forEach { model ->
                    val featureList = model.getData()
                    featureList?.forEach { feature ->
                        val projectedGeom = try {
                            GeometryEngine.project(feature.geometry, Config.sp4490)
                        } catch (e: Exception) {
                            feature.geometry
                        }
                        mapUtil.drawGeomety(shpGraphicsLayer, projectedGeom, feature.styleConfig, false)
                        if (feature.properties.isNotEmpty()) {
                            shpGraphicsLayer.graphics.lastOrNull()?.attributes?.putAll(
                                feature.properties.mapValues { it.value ?: "" })
                        }
                    }
                }
                val lastFeature = dataList.lastOrNull()?.getData()?.lastOrNull()
                if (lastFeature != null) {
                    try {
                        val point = lastFeature.geometry.extent.center
                        withContext(Dispatchers.Main) {
                            mapView.setViewpointGeometryAsync(point)
                        }
                    } catch (_: Exception) {}
                }
            } finally {
                loadingJob.cancel()
                if (loadingShown) {
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
                }
            }
        }
    }

    /**
     * 捕捉控件作为图片 (type: 0 主页截屏按钮 1现场拍照自动截屏)
     */
    fun captureScreenshotAsync(type: Int = 0, data: ImageInfo? = null) {

        // export the image from the mMapView
        val export: ListenableFuture<Bitmap> = mapView.exportImageAsync()
        export.addDoneListener {
            try {
                val currentMapImage = export.get()
                // play the camera shutter sound
                val sound = MediaActionSound()
                sound.play(MediaActionSound.SHUTTER_CLICK)
                // save the exported bitmap to an image file
                val saveImageTask = SaveImageTask(type, data)
                saveImageTask.execute(currentMapImage)
            } catch (e: Exception) {
                Toast.makeText(activity, "截图失败" + e.message, Toast.LENGTH_SHORT).show()
                Log.e("TakeScreenshotActivity", "截图失败" + e.message)
            }
        }
    }


    /**
     * AsyncTask class to save the bitmap as an image
     */
    inner class SaveImageTask(var type: Int, var data: ImageInfo?) :
        AsyncTask<Bitmap?, Void?, File?>() {
        override fun onPreExecute() {
            // display a toast message to inform saving the map as an image
            Toast.makeText(activity, "正在截图，请稍后", Toast.LENGTH_SHORT).show()
        }

        /**
         * save the file using a worker thread
         */
        protected override fun doInBackground(vararg mapBitmap: Bitmap?): File? {
            try {
                return saveToFile(type, mapBitmap[0]!!, data)
            } catch (e: java.lang.Exception) {
                Log.e("TakeScreenshotActivity", "截图失败" + e.message)
            }
            return null
        }

        /**
         * Perform the work on UI thread to open the exported map image
         */
        override fun onPostExecute(file: File?) {
            when (type) {
                0 -> {
                    val i = Intent()
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    i.action = Intent.ACTION_VIEW
                    i.setDataAndType(
                        FileProvider.getUriForFile(
                            activity, activity.applicationContext.packageName + ".provider", file!!
                        ),
                        "image/png"
                    )
                    activity.startActivity(i)
                }

                1 -> {
                    if (data != null) {
                        data!!.createTimeStamp = TimeUtil.getCurrentStamp()
                        data!!.filePath = file?.absolutePath
                        data!!.name = "${data!!.name}(截图)"
                        db.saveImageInfo(data!!)//存入数据库
                    }

                    EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_PHOTO_LIST, data))
                }

                else -> {

                }
            }

        }
    }

    private fun saveToFile(type: Int, bitmap: Bitmap, data: ImageInfo?): File? {

        // create a directory ArcGIS to save the file
        var file: File? = null
        var fileName = "" //文件名
        var fileDir: File? = null //文件路径

        when (type) {
            -1 -> {
                fileDir = File("/sdcard/screenshot/")

            }

            0 -> {
                fileDir = File(Config.SCREEN_SHOTS_PAHT)

                var layerName = ""
                CommonUtil.matchList(selectLayers).yes {
                    layerName = selectLayers.map { it.themeName }.joinToString("_")
                }
                val time =
                    TimeUtil.getDateToString(System.currentTimeMillis(), "yyyy-MM-dd HH_mm_ss")
                fileName = "${time} ${layerName}.png"

            }

            1 -> {
                if (data != null) {
                    fileDir = File(Config.FILE_CAMERA_PATH)
                    fileName = "${getFileNameWithoutExtension(data.filePath)}(截图).png"
                }
            }

            else -> {

            }
        }

        var isDirectoryCreated = fileDir!!.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = fileDir.mkdirs()
        }
        if (isDirectoryCreated) {
            file = File(fileDir, fileName)
            // write the bitmap to PNG file
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

            // close the stream
            fos.flush()
            fos.close()

            if (type == 0) {
                //发送广播 更新图片库 否则打开相册找不到图片
                activity.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)
                    )
                )
            }
        }


        return file
    }

    fun getFileNameWithoutExtension(filePath: String): String {
        val file = File(filePath)
        val fileNameWithExtension = file.name
        val fileNameWithoutExtension = fileNameWithExtension.substringBeforeLast('.')
        return fileNameWithoutExtension
    }

    class ScreenshotFileProvider : FileProvider()


    fun convertGdalToArcGISGeometry(gdalGeometry: org.gdal.ogr.Geometry): Geometry {
        return when (gdalGeometry.GetGeometryType()) {
            ogr.wkbPoint -> {
                // 点
                Point(
                    gdalGeometry.GetX(),
                    gdalGeometry.GetY(),
                    SpatialReferences.getWgs84()
                )
            }

            ogr.wkbLineString -> {
                // 线
                val lineBuilder = PolylineBuilder(SpatialReferences.getWgs84())
                val pointCount = gdalGeometry.GetPointCount()
                for (i in 0 until pointCount) {
                    val pt = gdalGeometry.GetPoint(i)
                    lineBuilder.addPoint(Point(pt[0], pt[1]))
                }
                lineBuilder.toGeometry()
            }

            ogr.wkbPolygon -> {
                // 面
                val polygonBuilder = PolygonBuilder(SpatialReferences.getWgs84())
                val ringCount = gdalGeometry.GetGeometryCount()
                for (i in 0 until ringCount) {
                    val ring = gdalGeometry.GetGeometryRef(i)
                    val pointCount = ring.GetPointCount()
                    for (j in 0 until pointCount) {
                        val pt = ring.GetPoint(j)
                        polygonBuilder.addPoint(Point(pt[0], pt[1]))
                    }
                }
                polygonBuilder.toGeometry()
            }

            else -> throw IllegalArgumentException("Unsupported geometry type: ${gdalGeometry.GetGeometryName()}")
        }
    }


    fun transformGeometry(
        geometry: org.gdal.ogr.Geometry,
        sourceSRID: Int,
        targetSRID: Int
    ): org.gdal.ogr.Geometry {
        val sourceSR = org.gdal.osr.SpatialReference()
        sourceSR.ImportFromEPSG(sourceSRID)

        val targetSR = org.gdal.osr.SpatialReference()
        targetSR.ImportFromEPSG(targetSRID)

        val transform = CoordinateTransformation(sourceSR, targetSR)
        geometry.Transform(transform)
        return geometry
    }




    //查询 工作记录
    fun searchWorkRecordLayer() {

        val tolerance = 5.0
        val mapTolerace = tolerance.times(mapView.unitsPerDensityIndependentPixel)
        val envelope = Envelope(
            clickPoint.x.minus(mapTolerace),
            clickPoint.y.minus(mapTolerace),
            clickPoint.x.plus(mapTolerace),
            clickPoint.y.plus(mapTolerace),
            mapView.map.spatialReference
        )
        val queryParmas = QueryParameters()
        queryParmas.geometry = envelope
//        queryParmas.spatialRelationship = QueryParameters.SpatialRelationship.WITHIN
        queryParmas.outSpatialReference = Config.sp4490
        queryParmas.isReturnGeometry = true


        //identifyGraphicsOverlayAsync查询  参数 1 要查询的图层 2 屏幕点 3 以screenPoint为中心的圆的密度无关像素（dp）中的半径
        // 4 如果包含弹出窗口，但识别结果中没有地理元素，则为true; false表示包含地理元素和弹出窗口
        val identifyResultsFuture: ListenableFuture<IdentifyGraphicsOverlayResult> =
            mapView.identifyGraphicsOverlayAsync(
                graphicsDisplayLayer,
                screenPoint,
                tolerance,
                false
            )

        identifyResultsFuture.addDoneListener {
            val identifyGraphicsOverlayResult = identifyResultsFuture.get()
            val graphics = identifyGraphicsOverlayResult.graphics
            val iterator = graphics.iterator()

            iterator?.let {

                if (it.hasNext()) {
                    var attr: Map<String, Any>? = null
                    while (it.hasNext()) {

                        val feature = it.next()
                        val geometry = feature.geometry
                        PrintUtil.printMsg("返回图形类型: ${geometry.geometryType}")
                        attr = feature.attributes
//                        attr?.forEach {
//                            PrintUtil.printMsg("属性 key: ${it.key}   value:${it.value}")
//                        }

                        val itemData =
                            getObjFromJson2(attr["data"] as String, MarkerInfo::class.java)
//                        EventBus.getDefault().post(EventBusModel(EventBusModel.QUERY_SHIP_RESULT, attr))
                        showCallout(clickPoint, itemData, TYPE_GEO)


                    }
                } else {
                    PrintUtil.printMsg("identifyGraphicsOverlayAsync  查询工作记录 未查到数据")

                }
            }
        }
    }


    /**
     * 查询 graphicsLayer 上被点击的 GeoJSON 图形，展示属性弹窗
     */
    private fun searchGeoJsonLayer() {
        val tolerance = 10.0
        val identifyFuture = mapView.identifyGraphicsOverlayAsync(
            graphicsLayer,
            screenPoint,
            tolerance,
            false
        )
        identifyFuture.addDoneListener {
            try {
                val result = identifyFuture.get()
                val graphics = result.graphics
                if (graphics.isNotEmpty()) {
                    val graphic = graphics.first()
                    val attrs = graphic.attributes
                    if (attrs.isNotEmpty()) {
                        showCallout(graphic.geometry.extent.center, attrs, TYPE_GEOJSON)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * 查询 shpGraphicsLayer 上被点击的 SHP 图形，展示属性弹窗
     */
    private fun searchShpLayer() {
        val tolerance = 10.0
        val identifyFuture = mapView.identifyGraphicsOverlayAsync(
            shpGraphicsLayer,
            screenPoint,
            tolerance,
            false
        )
        identifyFuture.addDoneListener {
            try {
                val result = identifyFuture.get()
                val graphics = result.graphics
                if (graphics.isNotEmpty()) {
                    val graphic = graphics.first()
                    val attrs = graphic.attributes
                    if (attrs.isNotEmpty()) {
                        showCallout(graphic.geometry.extent.center, attrs, TYPE_SHP)
                    }
                }
            } catch (_: Exception) {}
        }
    }


    //绘制一个缓冲区
    fun getBufferRange(distance: Double, lat: Double?, lng: Double?): Geometry? {

        if (lat == null || lng == null) return null

        val point = MapUtil.getMapUtil().get_change_geometry_point(lat, lng, Config.sp4490Int)
        val geometry = GeometryEngine.bufferGeodetic(
            point, distance, LinearUnit(LinearUnitId.METERS), 3.0, GeodeticCurveType.LOXODROME
        )
        geometry.toJson().printMsg()
        return geometry
    }


    /**
     * 执行缓冲区分析
     * bufferAnalyisQueryType 是缓冲区分析的查询方式  0定位点查询  1自定位点位
     */
    fun executeBufferAnalysis(dataList: MutableList<ThemesInfo>, point: Point, distance: Double) {


        overlyingLayer.graphics.clear()
        bufferAnalysisResultLayer.graphics.clear()
        cancelMyLocationMove()
        myLocationLayer.graphics.clear()


        bufferAnalysisLayer.clear()
        bufferAnalysisLayer.addAll(dataList)

        mapUtil.drawImage(context, graphicsLayer, point, R.mipmap.icon_location, false)
        val bufferPolygon = getBufferRange(distance, point.y, point.x) as Polygon
        mapUtil.drawBuffer(overlyingLayer, bufferPolygon, false)

        mapView.setViewpointGeometryAsync(bufferPolygon, 150.0)

        searchLayers.clear()
        bufferAnalysisList.clear()

        index = 0 //查询序号重置
        clearAnalysisResultPatch()//清空已标绘过的叠加图斑

        (dataList.size > 0).yes {
            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING));
            //叠加分析
            doBufferAnalysisLayer(bufferAnalysisLayer, bufferPolygon)

        }.no {
            tip(context, "暂无查询结果")
            return
        }
    }

    fun doBufferAnalysisLayer(layerlist: MutableList<ThemesInfo>, polygon: Polygon) {

        if (CommonUtil.matchList(layerlist)) {
            if (index >= layerlist.size) return;
            val info = layerlist[index]
            val layer = info.layer
            if (layer == null) {
                //图层是空的
                if (index == layerlist.size - 1) {
                    //如果是最后一项，查询结束
                    bufferAnalysisEnd(bufferAnalysisList)
                    return;
                }
                //还有数据可以查，就继续查下一个图层
                if (index < layerlist.size - 1) {
                    index++;
                    doBufferAnalysisLayer(bufferAnalysisLayer, polygon)
                }
                return
            }

            //查询参数
            val query = QueryParameters()
            query.geometry = polygon;
            val geomterySizeMu = caculationUtil.caculateAreaSizeMu(polygon, true) //标绘出的多边形大小（亩）
//            query.setSpatialRelationship(QueryParameters.SpatialRelationship.WITHIN);
//            query.outSpatialReference = Config.sp4490;
            query.outSpatialReference = Config.def_sp;
            query.whereClause = "1=1"

            info.features.clear()
            val queryList = layer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            queryList.addDoneListener {
                try {
                    val result = queryList.get()
                    val iterator: Iterator<Feature> = result.iterator()
                    if (!iterator.hasNext()) {
                        //没查到数据===查询图层异常===
                        if (index == layerlist.size - 1) {
                            //查询结束
                            bufferAnalysisEnd(bufferAnalysisList)
                            return@addDoneListener
                        } else if (index < layerlist.size - 1) {
                            index++
                            doBufferAnalysisLayer(bufferAnalysisLayer, polygon)
                        }
                        return@addDoneListener
                    }
                    while (iterator.hasNext()) {

                        val feature = iterator.next() ?: continue
                        val attr = feature.attributes
                        "attrs: ${attr.toJson()}".printMsg()

                        info.features.add(feature)
                        val geometry = feature.geometry

                        val map = HashMap<String, Any>()
                        map["info"] = info
                        map["feature"] = feature

                        val model = BufferAnalysisModel()
//                        model.themesInfo = info
//                        model.feature = feature
                        model.map = map
                        model.layerName = info.themeName
                        model.geometryJson = geometry.toJson()
                        model.centerPointJson = geometry.extent.center.toJson()


                        bufferAnalysisList.add(model)

                    }
                    if (layerlist.size == 0 || index == layerlist.size - 1) {
                        //查询结束
                        bufferAnalysisEnd(bufferAnalysisList)
                        return@addDoneListener
                    }
                    if (index < layerlist.size - 1) {
                        index++
                        doBufferAnalysisLayer(bufferAnalysisLayer, polygon)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun executeKeywordAnalysis(dataList: MutableList<ThemesInfo>, keyword: String, isProblem: Int = 0, isFinished: Int = 0, year: Int = 0, cityName: String = "", countyName: String = "", townName: String = "", problemType: String = "") {

        overlyingLayer.graphics.clear()
        bufferAnalysisResultLayer.graphics.clear()
        cancelMyLocationMove()
        myLocationLayer.graphics.clear()

        bufferAnalysisLayer.clear()
        bufferAnalysisLayer.addAll(dataList)


        searchLayers.clear()
        bufferAnalysisList.clear()

        index = 0 //查询序号重置
        clearAnalysisResultPatch()//清空已标绘过的叠加图斑

        (dataList.size > 0).yes {
            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING));
            //叠加分析
            KeywordAnalysisLayer(bufferAnalysisLayer, keyword, isProblem, isFinished, year, cityName, countyName, townName, problemType)

        }.no {
            tip(context, "暂无查询结果")
            return
        }
    }

    /**
     * 关键字查询图层（并行优化版本）
     * 将原来的串行递归查询改为并行查询所有图层，大幅提升查询效率
     */
    fun KeywordAnalysisLayer(layerlist: MutableList<ThemesInfo>, keyword: String, isProblem: Int = 0, isFinished: Int = 0, year: Int = 0, cityName: String = "", countyName: String = "", townName: String = "", problemType: String = "") {

        if (!CommonUtil.matchList(layerlist)) {
            tip(context, "暂无查询结果")
            return
        }

        EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LOADING))

        // 使用协程并行查询所有图层
        scope.launch {
            try {
                // 创建所有图层的异步查询任务
                val deferredList = layerlist.map { info ->
                    async(Dispatchers.IO) {
                        querySingleLayerForKeyword(
                            info, keyword, isProblem, isFinished, year,
                            cityName, countyName, townName, problemType
                        )
                    }
                }

                // 并行等待所有查询完成，并合并结果
                val allResults = deferredList.awaitAll().flatten()

                withContext(Dispatchers.Main) {
                    bufferAnalysisList.clear()
                    bufferAnalysisList.addAll(allResults)
                    bufferAnalysisEnd(bufferAnalysisList, 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
                    tip(context, "查询出错：${e.message}")
                }
            }
        }
    }

    /**
     * 查询单个图层（用于并行查询）
     * @return 返回该图层查询到的结果列表
     */
    private suspend fun querySingleLayerForKeyword(
        info: ThemesInfo,
        keyword: String,
        isProblem: Int,
        isFinished: Int,
        year: Int,
        cityName: String,
        countyName: String,
        townName: String,
        problemType: String
    ): List<BufferAnalysisModel> = suspendCancellableCoroutine { continuation ->
        try {
            val layer = info.layer

            // 图层为空，返回空列表
            if (layer == null) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            // 判断是否有"问题编"字段，没有则跳过
            val hasPlatformIdField = layer.featureTable?.fields?.any { it.name == "问题编" } ?: false
            if (!hasPlatformIdField) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            // 检查筛选字段是否存在
            val hasIsProblemField = layer.featureTable?.fields?.any { it.name == "是否问" } ?: false
            val hasEventStatusField = layer.featureTable?.fields?.any { it.name == "事件状" } ?: false
            val hasYearField = layer.featureTable?.fields?.any { it.name == "年份" } ?: false
            val hasCityField = layer.featureTable?.fields?.any { it.name == "所在市" } ?: false
            val hasCountyField = layer.featureTable?.fields?.any { it.name == "所在县" } ?: false
            val hasTownField = layer.featureTable?.fields?.any { it.name == "所在镇" } ?: false
            val hasProblemTypeField = layer.featureTable?.fields?.any { it.name == "问题类" } ?: false

            // 该图层专题名称是否携带所选年份（如"暗访-乱占2026"）
            val yearMatchesName = year != 0 && (info.themeName?.contains(year.toString()) ?: false)

            // 如果设置了筛选条件但图层没有对应字段，跳过该图层
            if (isProblem != 0 && !hasIsProblemField) {
                "${info.themeName} 没有 是否问 字段，跳过查询".printMsg()
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }
            if (isFinished != 0 && !hasEventStatusField) {
                "${info.themeName} 没有 事件状 字段，跳过查询".printMsg()
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }
            if (year != 0 && !hasYearField) {
                // 该图层没有年份字段，但专题名称可能携带年份（如"暗访-乱占2026"）
                if (yearMatchesName) {
                    // 整个图层均属于该年份，正常查询该图层全部数据
                    "${info.themeName} 没有 年份 字段，但专题名称含年份$year，仍查询全部数据".printMsg()
                } else {
                    "${info.themeName} 没有 年份 字段，跳过查询".printMsg()
                    continuation.resume(emptyList())
                    return@suspendCancellableCoroutine
                }
            }
            if (cityName.isNotBlank() && !hasCityField) {
                "${info.themeName} 没有 所在市 字段，跳过查询".printMsg()
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }
            if (countyName.isNotBlank() && !hasCountyField) {
                "${info.themeName} 没有 所在县 字段，跳过查询".printMsg()
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }
            if (townName.isNotBlank() && !hasTownField) {
                "${info.themeName} 没有 所在镇 字段，跳过查询".printMsg()
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }
            if (problemType.isNotBlank() && !hasProblemTypeField) {
                "${info.themeName} 没有 问题类 字段，跳过查询".printMsg()
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            // 构建查询条件
            val queryDefinition = buildKeywordQueryDefinition(
                keyword, isProblem, isFinished, year,
                cityName, countyName, townName, problemType,
                hasYearField, yearMatchesName
            )
            "查询筛选条件 :${queryDefinition}".printMsg()

            // 构建查询参数
            val query = QueryParameters().apply {
                whereClause = queryDefinition
                outSpatialReference = Config.def_sp
                spatialRelationship = QueryParameters.SpatialRelationship.WITHIN
                maxFeatures = 2000  // 限制单图层最大返回数量，防止数据过多
            }

            info.features.clear()

            // 执行异步查询
            val queryFuture = layer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            queryFuture.addDoneListener {
                try {
                    val result = queryFuture.get()
                    val models = mutableListOf<BufferAnalysisModel>()
                    val iterator = result.iterator()

                    while (iterator.hasNext()) {
                        val feature = iterator.next() ?: continue
                        val geometry = feature.geometry ?: continue

                        info.features.add(feature)

                        val map = HashMap<String, Any>()
                        map["info"] = info
                        map["feature"] = feature

                        val model = BufferAnalysisModel()
                        model.map = map
                        model.layerName = info.themeName
                        model.geometryJson = geometry.toJson()
                        model.centerPointJson = geometry.extent.center.toJson()
                        models.add(model)
                    }

                    continuation.resume(models)
                } catch (e: Exception) {
                    e.printStackTrace()
                    continuation.resume(emptyList())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resume(emptyList())
        }
    }

    /**
     * 构建关键字查询的 SQL 条件
     */
    private fun buildKeywordQueryDefinition(
        keyword: String,
        isProblem: Int,
        isFinished: Int,
        year: Int,
        cityName: String,
        countyName: String,
        townName: String,
        problemType: String,
        hasYearField: Boolean = true,
        yearMatchesName: Boolean = false
    ): String {
        val conditions = mutableListOf<String>()

        // 关键字查询（问题编模糊匹配）
        conditions.add("问题编 like '%${keyword}%'")

        // 是否问题筛选：0-全部，1-是，2-否，3-待复核
        when (isProblem) {
            1 -> conditions.add("是否问 = '是问题'")
            2 -> conditions.add("是否问 = '不是问题'")
            3 -> conditions.add("是否问 = '待复核'")
        }

        // 是否办结筛选：0-全部，1-已办结，2-未办结
        when (isFinished) {
            1 -> conditions.add("事件状 = '已办结'")
            2 -> conditions.add("事件状 = '未办结'")
        }

        // 年份筛选
        if (year != 0) {
            if (!hasYearField) {
                // 图层无年份字段，但专题名称携带该年份（如"暗访-乱占2026"），
                // 整个图层数据均属于该年份，不加年份条件（全部查询）
            } else if (yearMatchesName) {
                // 图层有年份字段，但部分数据年份为空；其专题名称携带该年份，
                // 这些空年份数据应一并纳入查找范围
                conditions.add("(年份 = $year OR 年份 IS NULL)")
            } else {
                conditions.add("年份 = $year")
            }
        }

        // 行政区划筛选
        if (cityName.isNotBlank()) {
            conditions.add("所在市 = '$cityName'")
        }
        if (countyName.isNotBlank()) {
            conditions.add("所在县 = '$countyName'")
        }
        if (townName.isNotBlank()) {
            conditions.add("所在镇 = '$townName'")
        }

        // 问题类型筛选
        if (problemType.isNotBlank()) {
            conditions.add("问题类 = '$problemType'")
        }

        return conditions.joinToString(" AND ")
    }

    /**
     * 缓冲区查询结束（不再自动绘制所有结果，改为分页绘制）
     */
    fun bufferAnalysisEnd(list: MutableList<BufferAnalysisModel>, queryType: Int = 0) {
        PrintUtil.printMsg("分析结果数量1 ： " + list.size)
        EventBus.getDefault()
            .post(DataEvent(DataEvent.SHOW_QUERY_BUFFER_RESULT_LIST, list, queryType))
        EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))

        // 不再自动绘制所有结果，改为分页绘制
        // drawAnalysisResult(list)

    }

    /**
     * 绘制当前页的缓冲区查询结果（分页绘制优化）
     * @param pageData 当前页的数据列表
     */
    fun drawBufferResultForPage(pageData: MutableList<BufferAnalysisModel>) {
        // 使用协程在IO线程执行绘制，避免阻塞UI
        scope.launch {
            try {
                // 在主线程清空之前的绘制结果
                withContext(Dispatchers.Main) {
                    bufferAnalysisResultLayer.graphics.clear()
                }

                // 在IO线程准备数据（解析geometry等耗时操作）
                val graphicsData = pageData.mapNotNull { item ->
                    try {
                        val geometry = Geometry.fromJson(item.geometryJson)
                        val bitmapDrawable = CustomLayerDisplayUtils.getBitmapDrawableIcon(context, item.layerName)
                        val color = CustomLayerDisplayUtils.getColor(item.layerName)
                        val line = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, 4f)
                        val symbol = SimpleFillSymbol(SimpleFillSymbol.Style.NULL, Color.parseColor("#000000"), line)
                        Quadruple(geometry, getGeoType(geometry), item.centerPointJson, Pair(bitmapDrawable, symbol))
                    } catch (e: Exception) {
                        null
                    }
                }

                // 在主线程执行绘制
                withContext(Dispatchers.Main) {
                    graphicsData.forEach { (geometry, geoType, centerPointJson, symbols) ->
                        val (bitmapDrawable, symbol) = symbols
                        mapUtil.drawBufferResult(
                            bufferAnalysisResultLayer,
                            geometry,
                            geoType,
                            centerPointJson,
                            false,
                            bitmapDrawable,
                            symbol
                        )
                    }

                    // 绘制完成后隐藏loading
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 出错时也要隐藏loading
                withContext(Dispatchers.Main) {
                    EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LOADING))
                }
            }
        }
    }

    // 辅助数据类，用于存储绘制所需的参数
    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    /**
     * 清空缓冲区查询结果的绘制
     */
    fun clearBufferResultDraw() {
        bufferAnalysisResultLayer.graphics.clear()
    }


    //绘制1111
    fun drawAnalysisResult(list: MutableList<BufferAnalysisModel>, isRedraw: Boolean = true) {
        list.forEach { item ->
            val geometry = Geometry.fromJson(item.geometryJson)

            val bitmapDrawable =
                CustomLayerDisplayUtils.getBitmapDrawableIcon(context, item.layerName)
            val color = CustomLayerDisplayUtils.getColor(item.layerName)
            val line = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, 4f)
            val symbol =
                SimpleFillSymbol(SimpleFillSymbol.Style.NULL, Color.parseColor("#000000"), line)


            mapUtil.drawBufferResult(
                bufferAnalysisResultLayer,
                geometry,
                getGeoType(geometry),
                item.centerPointJson,
                false,
                bitmapDrawable,
                symbol
            )
        }

    }

    fun showBufferResult(item: BufferAnalysisModel) {
        val centerPoint = Geometry.fromJson(item.centerPointJson) as Point
        mapView.setViewpointCenterAsync(centerPoint, defScale)
        showCallout(centerPoint, item.map, TYPE_THEME)
    }


    fun getGeoType(geo: Geometry?): Int {
        var type = DRAW_POINT
        when (geo?.geometryType) {
            GeometryType.POINT -> type = DRAW_POINT
            GeometryType.POLYLINE -> type = DRAW_POLYLINE
            GeometryType.POLYGON -> type = DRAW_POLYGON
            else -> type = DRAW_POINT
        }
        return type
    }

    //查询 缓冲区结果
    fun searchBufferAnalysisResultLayer() {

        val tolerance = 5.0
        val mapTolerace = tolerance.times(mapView.unitsPerDensityIndependentPixel)
        val envelope = Envelope(
            clickPoint.x.minus(mapTolerace),
            clickPoint.y.minus(mapTolerace),
            clickPoint.x.plus(mapTolerace),
            clickPoint.y.plus(mapTolerace),
            mapView.map.spatialReference
        )
        val queryParmas = QueryParameters()
        queryParmas.geometry = envelope
//        queryParmas.spatialRelationship = QueryParameters.SpatialRelationship.WITHIN
        queryParmas.outSpatialReference = Config.sp4490
        queryParmas.isReturnGeometry = true


        //identifyGraphicsOverlayAsync查询  参数 1 要查询的图层 2 屏幕点 3 以screenPoint为中心的圆的密度无关像素（dp）中的半径
        // 4 如果包含弹出窗口，但识别结果中没有地理元素，则为true; false表示包含地理元素和弹出窗口
        val identifyResultsFuture: ListenableFuture<IdentifyGraphicsOverlayResult> =
            mapView.identifyGraphicsOverlayAsync(
                bufferAnalysisResultLayer,
                screenPoint,
                tolerance,
                false
            )

        identifyResultsFuture.addDoneListener {
            val identifyGraphicsOverlayResult = identifyResultsFuture.get()
            val graphics = identifyGraphicsOverlayResult.graphics
            val iterator = graphics.iterator()

            iterator?.let {

                if (it.hasNext()) {
                    var attr: Map<String, Any>? = null
                    while (it.hasNext()) {

                        val feature = it.next()
                        val geometry = feature.geometry
                        PrintUtil.printMsg("返回图形类型: ${geometry.geometryType}")
                        attr = feature.attributes

                        if (!attr.isNullOrEmpty()) {
                            //用图斑中心点的json串当做 唯一表示 进行匹配 （不同专题图斑里面的attr 编号字段可能不一样 所以这里没有使用）
                            val centerPoint =
                                getObjFromJson2(attr["data"] as String, String::class.java)
                            val find =
                                bufferAnalysisList.firstOrNull { it.centerPointJson == centerPoint }

                            if (find != null) {
                                showCallout(geometry.extent.center, find.map, TYPE_THEME)
                            }
                        }
                        break
                    }
                } else {
                    PrintUtil.printMsg("identifyGraphicsOverlayAsync  查询缓冲区 未查到数据")
                }
            }
        }
    }


    //根据某个点 来查询所在市 所在县 (用于表单自动填充信息)
    fun searchAreaLayerByPoint(centerpoint: Point, areaType: String = "city") {

        val featureLayer = when (areaType) {
            "city" -> cityFeatureLayer
            "county" -> countyFeatureLayer
            "town" -> townFeatureLayer
            else -> null
        }

        if (featureLayer != null) {
            //查询参数
            val query = QueryParameters()
            query.geometry = centerpoint

            val future = featureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()

                        if (!iterator.hasNext()) {
                            return
                        }
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==： + ${getParmas(attr)}".printMsg()

//                            val geometry = feature.geometry
                            val value = attr["XZQMC"].toString()

                            PrintUtil.printMsg("======运行完成=======")

                            EventBus.getDefault()
                                .post(MapEvent(MapEvent.QUERY_AREA_CALLBACK, value, areaType))

                            if (areaType == "city") {
                                searchAreaLayerByPoint(centerpoint, "county")
                            } else if (areaType == "county") {
                                searchAreaLayerByPoint(centerpoint, "town")
                            }

                            break
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }

    fun drawPoint(point: Point) {
        mapUtil.drawImage(context, testGraphicsOverlay, point, R.mipmap.icon_luanzhan, true)
    }


    fun screenshotTool(pointList: MutableList<Point>) {
        scope.launch(Dispatchers.Main) {
            pointList.forEach { point ->
                mapView.setViewpointCenterAsync(point, 200.0)
                drawPoint(point)
                withContext(Dispatchers.Default) {
                    delay(2000)
                }
                captureScreenshotAsync(-1)
                // 切换到 IO 或 Default 调度器等待，释放主线程
                withContext(Dispatchers.Default) {
                    delay(3000)
                }
            }
        }
    }

    fun clearCollcetion() {
        clearAnalysisResultPatch()
        if (mapView.callout != null && mapView.callout.isShowing) {
            mapView.callout.dismiss()
        }

    }


    fun queryLocationArea() {
        val centerPoint = mapView.visibleArea.extent.center
        searchAreaLayerByPoint2(centerPoint, "city")
    }

    //根据某个点 来查询所在市 所在县 (用于表单自动填充信息)
    fun searchAreaLayerByPoint2(centerpoint: Point, areaType: String = "city") {

        val featureLayer = when (areaType) {
            "city" -> cityFeatureLayer
            "county" -> countyFeatureLayer
            "town" -> townFeatureLayer
            else -> null
        }

        if (featureLayer != null) {
            //查询参数
            val query = QueryParameters()
            query.geometry = centerpoint

            val future = featureLayer!!.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
            future.addDoneListener(DoneAction(object : DoneListener {
                override fun onDone(o: Any, tag: Int) {
                    try {
                        val featureQueryResults = o as ListenableFuture<FeatureQueryResult>
                        val result = featureQueryResults.get()
                        val iterator: Iterator<Feature> = result.iterator()

                        if (!iterator.hasNext()) {
                            return
                        }
                        while (iterator.hasNext()) {

                            val feature = iterator.next() ?: continue

                            val attr = feature.attributes
                            //处理属性
                            "参数==： + ${getParmas(attr)}".printMsg()

//                            val geometry = feature.geometry
                            val value = attr["XZQMC"].toString()

                            PrintUtil.printMsg("======运行完成=======")

                            EventBus.getDefault().post(
                                MapEvent(
                                    MapEvent.QUERY_LOCATION_AREAINFO_BACK,
                                    value,
                                    areaType
                                )
                            )
                            if (areaType == "city") {
                                searchAreaLayerByPoint2(centerpoint, "county")
                            } else if (areaType == "county") {
                                searchAreaLayerByPoint2(centerpoint, "town")
                            }

                            break
                        }
                    } catch (e: Exception) {
                        PrintUtil.printMsg("======查询图层异常1=======" + e.message)
                    }
                }
            }, future, 0))
        }

    }

    fun relocation() {
        fullExtent?.apply {
            val fullExtent = fullExtent
            val scaleFactor = 0.5

            // 计算新的范围
            val newWidth = fullExtent!!.width * scaleFactor
            val newHeight = fullExtent.height * scaleFactor
            val center = fullExtent.center

            val extent75Percent = Envelope(
                center.x - newWidth / 2,
                center.y - newHeight / 2,
                center.x + newWidth / 2,
                center.y + newHeight / 2,
                fullExtent.spatialReference
            )

            mapView.setViewpointAsync(Viewpoint(extent75Percent))
        }

    }

    fun checkBaseMap() {
        if (baseMapStack.peek() !== "影像底图2018") {
            baseMapStack.pop()
            EventBus.getDefault().post(DataEvent(DataEvent.UNSELECT_BASE_IMAGE_LAYER_NMAE))
            switchBaseLyer(MapEvent.SET_IMAGE_MAP_2018)
        }
    }
    
    // 释放资源
    fun release() {
        // 取消协程
        scope.cancel()
        // 取消加密操作
        encryptJob?.cancel()
    }
}

