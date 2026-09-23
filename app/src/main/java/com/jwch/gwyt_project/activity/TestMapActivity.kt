package com.jwch.gwyt_project.activity


import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.location.SimulatedLocationDataSource
import com.esri.arcgisruntime.location.SimulationParameters
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.google.gson.Gson
import com.jameni.allutillib.common.NumUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.PageTestMapBinding
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.i.DoneAction
import com.jwch.gwyt_project.util.GetJsonUtil
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class TestMapActivity : BaseActivity<PageTestMapBinding>() {


    var point :Point? = null
    lateinit var bufferLayer: GraphicsOverlay
    var distance = 3500.0
    val pointList = mutableListOf<Point>()
    var index = 0

    lateinit var mLocationDisplay : LocationDisplay

    lateinit var mSimulatedLocationDataSource : SimulatedLocationDataSource//定义一个DataSource的成员变量

    var polyline : Polyline?= null

    override fun initView() {
        val map = ArcGISMap(Basemap.createImagery())
        vb.mapView.map = map
        bufferLayer = GraphicsOverlay()
        vb.mapView.graphicsOverlays.add(bufferLayer)

        initPointList()
        mLocationDisplay = vb.mapView.locationDisplay

        point = Point(119.765582, 25.513567, Config.sp4490)
        vb.mapView.setViewpointCenterAsync(point,5000.0)

        //自定义数据集
//        dataSource= CustomDataSource()
//        mLocationDisplay.locationDataSource = dataSource
//        mLocationDisplay.startAsync()
//        dataSource.startAsync()
    }


    override fun initViewListener() {

        vb.tvBtn1.onClick {

//            drawRings(point!!,200.0, 7)


        }


        vb.tvBtnOk.onClick{
            bufferLayer.graphics.clear()
            val radius = NumUtil.getDoubleVale(vb.etRadius.text.toString().self())
            val num = NumUtil.getIntegerVale(vb.etNum.text.toString().self())
            drawRings(point!!, radius, num)
        }


    }


    fun createCycle(centerPoint : Point, radius : Double) : Geometry{
        return GeometryEngine.bufferGeodetic(centerPoint, radius, LinearUnit(LinearUnitId.METERS),
            1.0, GeodeticCurveType.GEODESIC)
    }

    fun drawRings(centerPoint : Point, minRadius : Double, cycleNumber : Int){
        val currentRadius = minRadius * cycleNumber
        val bigCycleGeo = createCycle(centerPoint, currentRadius)

        doo(centerPoint, currentRadius, minRadius, bigCycleGeo, cycleNumber)


    }

    fun doo(centerPoint : Point, currentRadius : Double,
                    minRadius : Double, bigCycleGeo :Geometry, cycleNumber : Int){
        val radius = currentRadius - minRadius
        val index = cycleNumber - 1
        "index = $index  radius = $radius ".printMsg()

        if (index > 0) {
            val samllCycleGeo = createCycle(centerPoint, radius)
            val ringGeo = GeometryEngine.difference(bigCycleGeo, samllCycleGeo)
            drawBufferGeomety(bufferLayer, ringGeo, index, false)
            doo(centerPoint, radius, minRadius, samllCycleGeo, index)
        }else {
            val samllCycleGeo = createCycle(centerPoint, minRadius)
            drawBufferGeomety(bufferLayer, samllCycleGeo, index, false)
        }



    }



    fun initPointList(){
        pointList.add(Point(119.765582, 25.513567, Config.sp4490))
        pointList.add(Point(119.735582, 25.513567, Config.sp4490))
        pointList.add(Point(119.725582, 25.543567, Config.sp4490))
    }





    fun drawPoint(layer: GraphicsOverlay, geometry: Geometry?, size: Float, color: Int, isRedraw: Boolean) {
        var graphic: Graphic? = null
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, color, size)
        graphic = Graphic(geometry, symbol)
        if (isRedraw) {
            layer.graphics.clear()
        }
        layer.graphics.add(graphic)
    }

    fun drawBufferGeomety(layer: GraphicsOverlay, geometry: Geometry?, index : Int, isRedraw: Boolean) {
        if (geometry == null) return
        var graphic: Graphic? = null
        val type = geometry.geometryType

        //面对象
        graphic = Graphic(geometry, getPolygonSymbolBuffer(index))
        if (isRedraw) {
            layer.graphics.clear()
        }
        if (graphic == null) return
        //添加绘制对象
        layer.graphics.add(graphic)
    }



    private var polygonSymbol: SimpleFillSymbol? = null //多边形符号
    private var polylineSymbol: SimpleLineSymbol? = null //线符号
    var polylineStyle = SimpleLineSymbol.Style.SOLID
    var polygonFillStyle = SimpleFillSymbol.Style.SOLID

    var fillColrList = mutableListOf(
        Color.parseColor("#66FB3805"),
        Color.parseColor("#66FC5C03"),
        Color.parseColor("#66FC8901"),
        Color.parseColor("#66FDD227"),
        Color.parseColor("#6669CF05"),
        Color.parseColor("#6609CCA7"),
        Color.parseColor("#66038DD0"),
    )

    var lineColorList = mutableListOf(
        Color.parseColor("#FB3805"),
        Color.parseColor("#FC5C03"),
        Color.parseColor("#FC8901"),
        Color.parseColor("#FDD227"),
        Color.parseColor("#69CF05"),
        Color.parseColor("#09CCA7"),
        Color.parseColor("#038DD0"),
    )

    var lineWidth = 2f //线的粗细

    fun getPolygonSymbolBuffer(index : Int): SimpleFillSymbol? {

        polygonSymbol = SimpleFillSymbol(polygonFillStyle, fillColrList[index], getPolylineSymbol(index))

        return polygonSymbol
    }

    fun getPolylineSymbol(index : Int): SimpleLineSymbol? {

        polylineSymbol = SimpleLineSymbol(polylineStyle, lineColorList[index], lineWidth)

        return polylineSymbol
    }

    override fun onResume() {
        super.onResume()
        vb.mapView.resume()
    }

    override fun onPause() {
        vb.mapView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        vb.mapView.dispose()
        super.onDestroy()
    }

}
