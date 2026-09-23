package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.CompoundButton
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.MapView
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.PrintUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewCoordinateMarkerDialogBinding
import com.jwch.gwyt_project.databinding.ViewCoordinatePositionDialogBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.SoftUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus


//坐标定位dialog
class CoordinatePositionDialog(context: Context, val mapView: MapView, var listener: ActionListener) :
    JameniBaseDialog(context), CompoundButton.OnCheckedChangeListener {

    var x = 0.0
    var y = 0.0
    var lng = 0.0
    var lat = 0.0
    var lngDegree = ""
    var lngMinute = ""
    var lngSecond = ""
    var latDegree = ""
    var latMinute = ""
    var latSecond = ""

    var mActivity: Activity? = null
    var softUtil: SoftUtil? = null


    var vb: ViewCoordinatePositionDialogBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_coordinate_position_dialog, null)
        vb = ViewCoordinatePositionDialogBinding.bind(view)
        setContentView(vb!!.root)


        setCanceledOnTouchOutside(false)

        vb!!.rbPlane.setOnCheckedChangeListener(this)
        vb!!.rbSphere.setOnCheckedChangeListener(this)
        vb!!.rbSphere2.setOnCheckedChangeListener(this)

        if (mapView.map.basemap.baseLayers.size == 0) return
        val layer = mapView.map.basemap.baseLayers[0]
        val a = layer.fullExtent.xMax
        val b = layer.fullExtent.xMin
        val c = layer.fullExtent.yMax
        val d = layer.fullExtent.yMin
        val sta = a.toString()
        val stb = b.toString()
        val stc = c.toString()
        val std = d.toString()
        vb!!.tvLimit.setText("经度" + sta + "到" + stb + "\n纬度" + stc + "到" + std)

        //测试
//        val point1 = Point(117.82309,24.447018,SpatialReference.create(4490))
//        var p2 = GeometryEngine.project(point1, SpatialReference.create(4539)) as Point
//        "${p2.x}".printMsg()
//        "${p2.y}".printMsg()
//        etX.setText(p2.x.toString())
//        etY.setText(p2.y.toString())

        vb!!.tvOk.onClick {
            getPoint()
            dismiss()

        }
        vb!!.tvCancle.onClick {
            dismiss()
        }


        softUtil = SoftUtil()

        vb!!.tvLayerName.onClick {
//            testData()
        }

    }

    private fun testData() {
//        .setText("")
        //点的 测试数据
        vb!!.etX.setText("590104.478")
        vb!!.etY.setText("2701526.867")
        vb!!.etLongitude.setText("117.88837977847872")
        vb!!.etLatitude.setText("24.41485150511088")
        vb!!.etLongitudeDegree.setText("117")
        vb!!.etLongitudeMinute.setText("53")
        vb!!.etLongitudeSecond.setText("18")
        vb!!.etLatitudeDegree.setText("24")
        vb!!.etLatitudeMinute.setText("24")
        vb!!.etLatitudeSecond.setText("53")
    }

    fun getPoint() {
        //平面坐标
        vb!!.llPlane.isShow().yes {

            (vb!!.etX.isEmpty() || vb!!.etY.isEmpty()).yes {
                tip(context, "输入坐标错误或此位置没在地图范围内")
                return@getPoint
            }
            x = vb!!.etX.text.toString().toDouble()
            y = vb!!.etY.text.toString().toDouble()

//            val point = Point(x, y, SpatialReference.create(4539))
            val point = Point(x, y, Config.def_sp)
            val poi = GeometryEngine.project(point, Config.sp4490) as Point
            "point.x ${point.x}  point.y ${point.y}".printMsg()
            "poi.x ${poi.x}  poi.y ${poi.y}".printMsg()

            isContains(poi.x, poi.y).yes {
                EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_POSITION, point))
            }.no {
                tip(context, "输入坐标错误或此位置没在地图范围内")
                return@getPoint
            }

        }
        //球面坐标
        vb!!.llSphere.isShow().yes {
            (vb!!.etLongitude.isEmpty() || vb!!.etLatitude.isEmpty()).yes {
                tip(context, "输入坐标错误或此位置没在地图范围内")
                return@getPoint
            }

            lng = vb!!.etLongitude.text.toString().toDouble()
            lat = vb!!.etLatitude.text.toString().toDouble()

            isContains(lng, lat).yes {
                val point = Point(lng, lat, SpatialReference.create(Config.sp4490Int))
                EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_POSITION, point))
            }.no {
                tip(context, "输入坐标错误或此位置没在地图范围内")
                return@getPoint
            }
        }
        //球面坐标（度分秒）
        vb!!.llSphere2.isShow().yes {

            (vb!!.etLongitudeDegree.isEmpty() || vb!!.etLongitudeMinute.isEmpty() ||vb!!.etLongitudeSecond.isEmpty() ||
                    vb!!.etLatitudeDegree.isEmpty() || vb!!.etLatitudeMinute.isEmpty() || vb!!.etLatitudeSecond.isEmpty()).yes {
                tip(context, "输入坐标错误或此位置没在地图范围内")
                return@getPoint
            }

            lngDegree = vb!!.etLongitudeDegree.text.toString()
            lngMinute = vb!!.etLongitudeMinute.text.toString()
            lngSecond = vb!!.etLongitudeSecond.text.toString()
            latDegree = vb!!.etLatitudeDegree.text.toString()
            latMinute = vb!!.etLatitudeMinute.text.toString()
            latSecond = vb!!.etLatitudeSecond.text.toString()

            lng = tranformPos("$lngDegree°$lngMinute′$lngSecond\"")
            lat = tranformPos("$latDegree°$latMinute′$latSecond\"")

            isContains(lng, lat).yes {
                val point = Point(lng, lat, SpatialReference.create(Config.sp4490Int))
                EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_POSITION, point))
            }.no {
                tip(context, "输入坐标错误或此位置没在地图范围内")
                return@getPoint
            }

        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.rbPlane -> if (isChecked) {
                //平面坐标
                vb!!.llPlane.show()
                vb!!.llSphere.gone()
                vb!!.llSphere2.gone()
            }
            R.id.rbSphere -> if (isChecked) {
                //球面坐标
                vb!!.llPlane.gone()
                vb!!.llSphere.show()
                vb!!.llSphere2.gone()
            }
            R.id.rbSphere2 -> if (isChecked) {
                //球面坐标（度分秒）
                vb!!.llPlane.gone()
                vb!!.llSphere.gone()
                vb!!.llSphere2.show()
            }
        }
    }

    //度分秒转换经纬度
    fun tranformPos(lng: String): Double {
        val lntArr = lng
            .trim { it <= ' ' }
            .replace("°", ";")
            .replace("′", ";")
            .replace("'", ";")
            .replace("\"", "")
            .split(";").toTypedArray()
        var result = 0.0
        for (i in lntArr.size downTo 1) {
            val v = lntArr[i - 1].toDouble()
            result = if (i == 1) {
                v + result
            } else {
                (result + v) / 60
            }
        }
        result.toString().printMsg()
        return result
    }

    //判断输入坐标是否在一定范围内
    fun isContains(x: Double, y: Double): Boolean {
        val envelope = mapView.map.basemap.baseLayers[0].fullExtent
        val xmin = envelope.xMin
        val ymin = envelope.yMin
        val xmax = envelope.xMax
        val ymax = envelope.yMax
        val flag = x in xmin..xmax && y in ymin..ymax
        if (flag) {
            PrintUtil.printMsg("在点内")
        } else {
            PrintUtil.printMsg("不在点内")
        }
        return flag
    }


}
