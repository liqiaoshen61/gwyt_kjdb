package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.CompoundButton
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.MapView
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.PrintUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewCoordinateMarkerDialogBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.MapEvent
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus


/**
 * 输入坐标标绘dialog
 */
open class CoordinateMarkerDialog(context: Context, val mapView: MapView, var listener: ActionListener) :
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

    var input_not_in_range = "输入坐标错误或此位置没在地图范围内"

    var singlePoint: Point? = null

    val pointCollection = PointCollection(Config.def_sp)

    //    val pointCollection2 = PointCollection(SpatialReference.create(Config.sp4548Int_longhai))
    val pointCollection2 = PointCollection(Config.def_sp)



    var vb: ViewCoordinateMarkerDialogBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_coordinate_marker_dialog, null)
        vb = ViewCoordinateMarkerDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)

        vb!!.rbPlanePoint.setOnCheckedChangeListener(this)
        vb!!.rbSpherePoint.setOnCheckedChangeListener(this)
        vb!!.rbSphere2Point.setOnCheckedChangeListener(this)

        vb!!.rbCoordinatePoint.setOnCheckedChangeListener(this)
        vb!!.rbCoordinatePolyline.setOnCheckedChangeListener(this)
        vb!!.rbCoordinatePolygon.setOnCheckedChangeListener(this)

        vb!!.rbPlaneSingle.setOnCheckedChangeListener(this)
        vb!!.rbSphereMuti.setOnCheckedChangeListener(this)
        vb!!.rbSphereSingele.setOnCheckedChangeListener(this)
        vb!!.rbPlaneMuti.setOnCheckedChangeListener(this)

        ///默认开启点标绘模式
        EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_MARKER_POINT))

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

        vb!!.tvOk.onClick {
            //确定绘制
            getPoint()
            vb!!.tvCancle.performClick()
        }
        vb!!.tvCancle.onClick {
            //关闭对话框
            dismiss()
            clearData()
            EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_MARKER_CLOSE))
        }

        //添加平面
        //线--平面坐标（添加坐标功能）  tvPlanepointList（展示坐标点）
        vb!!.tvAddPlanePoint.onClick {
            (vb!!.etXSingle.isEmpty() || vb!!.etYSingle.isEmpty()).yes {
                tip(context, input_not_in_range)
                return@onClick
            }
            x = vb!!.etXSingle.text.toString().toDouble()
            y = vb!!.etYSingle.text.toString().toDouble()

            val p = Point(x, y, Config.def_sp)
            val poi = GeometryEngine.project(p, Config.sp4490) as Point
            isContains(poi.x, poi.y).yes {
                vb!!.tvPlanepointList.text = vb!!.tvPlanepointList.text.toString() + "${x},${y}\n"
                pointCollection2.add(p)
            }.no {
                tip(context, input_not_in_range)
                return@onClick
            }

//            etXSingle.setText("")
//            etYSingle.setText("")
        }


        //添加球面点
        vb!!.tvAddSpherePoint.onClick {
            (vb!!.etLongitudeSingle.isEmpty() || vb!!.etLatitudeSingle.isEmpty()).yes {
                tip(context, input_not_in_range)
                return@onClick
            }
            lng = vb!!.etLongitudeSingle.text.toString().toDouble()
            lat = vb!!.etLatitudeSingle.text.toString().toDouble()

            isContains(lng, lat).yes {
                vb!!.tvSpherepointList.text = vb!!.tvSpherepointList.text.toString() + "${lng},${lat}\n"
                val p = Point(lng, lat, Config.sp4490)
                val poi = GeometryEngine.project(p, Config.def_sp) as Point
                pointCollection.add(poi)
            }.no {
                tip(context, input_not_in_range)
                return@onClick
            }


//            etLongitudeSingle.setText("")
//            etLatitudeSingle.setText("")
        }


//        testData()

        vb!!.tvLayerName.onClick {
//            testData()
//            testData2()
        }
    }

    private fun testData() {
//        .setText("")
        //点的 测试数据
        vb!!.etXPoint.setText("590104.478")
        vb!!.etYPoint.setText("2701526.867")

        vb!!.etLongitudePoint.setText("117.88837977847872")
        vb!!.etLatitudePoint.setText("24.41485150511088")

        vb!!.etLongitudeDegree.setText("117")
        vb!!.etLongitudeMinute.setText("53")
        vb!!.etLongitudeSecond.setText("18")

        vb!!.etLatitudeDegree.setText("24")
        vb!!.etLatitudeMinute.setText("24")
        vb!!.etLatitudeSecond.setText("53")

        //线和面的 测试数据
        //线--平面坐标（单点添加）
        vb!!.etXSingle.setText("590104.478")
        vb!!.etYSingle.setText("2701526.867")

        //线--平面坐标（多点）
        vb!!.etPlaneMuti.setText("590104.478,2701526.867;590510.578,2701527.967;590510.578,2701597.967;")

        //线--球面坐标（单点）--添加点
        vb!!.etLongitudeSingle.setText("117.88837977847872")
        vb!!.etLatitudeSingle.setText("24.41485150511088")
        //线--球面坐标（多点）
        vb!!.etSphereMuti.setText(
            "117.88837977847872,24.41485150511088;117.88937977847872,24.41485150511088;" +
                    "117.8837977847872,24.4485150511088;117.89837977847872,24.94485150511088;"
        )

    }

    private fun testData2() {

        vb!!.etPlaneMuti.setText(
            "586633.607,2700859.785;" +
                    "586633.607,2700759.785;" +
                    "586733.607,2700759.785;" +
                    "586733.607,2700859.785;"
        )

        vb!!.etSphereMuti.setText(
            "117.85412144908717,24.409026589229043;" +
                    "117.85411537578261,24.408123870878399;" +
                    "117.85510118361346,24.408118306394488;" +
                    "117.85510726392562,24.409021024513034;"
        )


    }

    fun clearData() {
        pointCollection.clear()
        vb!!.tvSpherepointList.text = ""
        pointCollection2.clear()
        vb!!.tvPlanepointList.text = ""
        x = 0.0
        y = 0.0
        lng = 0.0
        lat = 0.0
        lngDegree = ""
        lngMinute = ""
        lngSecond = ""
        latDegree = ""
        latMinute = ""
        latSecond = ""
        singlePoint = null

        vb!!.etXPoint.setText("")
        vb!!.etYPoint.setText("")
        vb!!.etLongitudePoint.setText("")
        vb!!.etLatitudePoint.setText("")
        vb!!.etLongitudeDegree.setText("")
        vb!!.etLongitudeMinute.setText("")
        vb!!.etLongitudeSecond.setText("")
        vb!!.etLatitudeDegree.setText("")
        vb!!.etLatitudeMinute.setText("")
        vb!!.etLatitudeSecond.setText("")
        vb!!.etXSingle.setText("")
        vb!!.etYSingle.setText("")
        vb!!.etPlaneMuti.setText("")
        vb!!.etLongitudeSingle.setText("")
        vb!!.etLatitudeSingle.setText("")
        vb!!.etSphereMuti.setText("")

    }

    fun getPoint() {

        //点
        vb!!.rbCoordinatePoint.isChecked.yes {

            //平面坐标
            vb!!.llPlanePoint.isShow().yes {

                (vb!!.etXPoint.isEmpty() || vb!!.etYPoint.isEmpty()).yes {
                    tip(context, input_not_in_range)
                    return@getPoint
                }
                x = vb!!.etXPoint.text.toString().toDouble()
                y = vb!!.etYPoint.text.toString().toDouble()

                singlePoint = Point(x, y, Config.def_sp)
                val poi = GeometryEngine.project(singlePoint, SpatialReference.create(Config.sp4490Int)) as Point

                isContains(poi.x, poi.y).yes {
                    //添加点
                    "dfaaaaaaaaaaaaaaaa".printMsg()
                    EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT_COORDINATE, singlePoint))
                }.no {
                    tip(context, input_not_in_range)
                    return@getPoint
                }

            }
            //球面坐标
            vb!!.llSpherePoint.isShow().yes {
                (vb!!.etLongitudePoint.isEmpty() || vb!!.etLatitudePoint.isEmpty()).yes {
                    tip(context, input_not_in_range)
                    return@getPoint
                }

                lng = vb!!.etLongitudePoint.text.toString().toDouble()
                lat = vb!!.etLatitudePoint.text.toString().toDouble()

                isContains(lng, lat).yes {
                    singlePoint = Point(lng, lat, SpatialReference.create(Config.sp4490Int))
                    //添加点
                    EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT_COORDINATE, singlePoint))
                }.no {
                    tip(context, input_not_in_range)
                    return@getPoint
                }
            }
            //球面坐标 度分秒
            vb!!.llSphere2Point.isShow().yes {
                (vb!!.etLongitudeDegree.isEmpty() || vb!!.etLongitudeMinute.isEmpty() || vb!!.etLongitudeSecond.isEmpty() ||
                        vb!!.etLatitudeDegree.isEmpty() || vb!!.etLatitudeMinute.isEmpty() || vb!!.etLatitudeSecond.isEmpty()).yes {
                    tip(context, "请输入正确的坐标")
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
                    singlePoint = Point(lng, lat, SpatialReference.create(Config.sp4490Int))
                    //添加点
                    EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT_COORDINATE, singlePoint))
                }.no {
                    tip(context, input_not_in_range)
                    return@getPoint
                }


            }

        }

        //线 或 面
        (vb!!.rbCoordinatePolyline.isChecked || vb!!.rbCoordinatePolygon.isChecked).yes {
            //平面单点
            vb!!.llPlaneSingle.isShow().yes {
                pointCollection2.isEmpty().yes {
                    return@getPoint
                }
                //添加点集合
                EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT_LIST_COORDINATE, pointCollection2))
            }
            //平面多点
            vb!!.llPlaneMuti.isShow().yes {
                vb!!.etPlaneMuti.isEmpty().yes {
                    tip(context, input_not_in_range)
                    return@getPoint
                }
                val str = vb!!.etPlaneMuti.text.toString()
                //解析坐标
                str.replace(" ", "")
                val list = str.split(";")
                pointCollection.clear()
                list.forEach {
                    if (CommonUtil.isNotEmpty(it) && it.contains(",")) {

                        val lnglat = it.split(",")
                        var x = 0.0
                        var y = 0.0
                        try {
                            x = lnglat[0].toDouble()
                            y = lnglat[1].toDouble()
                        } catch (e: Exception) {
                            tip(context, input_not_in_range)
                            return@getPoint
                        }
                        val p = Point(x, y, Config.def_sp)
                        val poi = GeometryEngine.project(p, Config.sp4490) as Point

                        isContains(poi.x, poi.y).yes {
                            pointCollection.add(p)
                        }.no {
                            tip(context, input_not_in_range)
                            return@getPoint
                        }
                    }


                }
                //添加点集合
                EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT_LIST_COORDINATE, pointCollection))
            }
            //球面单点
            vb!!.llSphereSingle.isShow().yes {
                pointCollection.isEmpty().yes {
                    return@getPoint
                }
                //添加点集合
                EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT_LIST_COORDINATE, pointCollection))

            }
            //球面多点
            vb!!.llSphereMuti.isShow().yes {
                vb!!.etSphereMuti.isEmpty().yes {
                    tip(context, input_not_in_range)
                    return@getPoint
                }

                val str = vb!!.etSphereMuti.text.toString()
                //解析坐标
                str.replace(" ", "")
                val list = str.split(";")
                pointCollection.clear()
                list.forEach {
                    if (CommonUtil.isNotEmpty(it) && it.contains(",")) {
                        val lnglat = it.split(",")
                        var x = 0.0
                        var y = 0.0
                        try {
                            x = lnglat[0].toDouble()
                            y = lnglat[1].toDouble()
                        } catch (e: Exception) {
                            tip(context, input_not_in_range)
                            return@getPoint
                        }

                        isContains(x, y).yes {
                            val p = Point(x, y, Config.sp4490)
                            val poi = GeometryEngine.project(p, Config.def_sp) as Point
                            pointCollection.add(poi)
                        }.no {
                            tip(context, input_not_in_range)
                            return@getPoint
                        }
                    }
                }
                //添加点集合
                EventBus.getDefault().post(MapEvent(MapEvent.ADD_POINT_LIST_COORDINATE, pointCollection))
            }
        }


    }


    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            //点
            R.id.rbCoordinatePoint -> if (isChecked) {
                //开启标绘点模式
                EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_MARKER_POINT))
                clearData()
                vb!!.llPointInput.show()
                vb!!.llPolyInput.gone()
            }
            //线
            R.id.rbCoordinatePolyline -> if (isChecked) {
                //开启标绘点模式
                EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_MARKER_PLOYLINE))
                clearData()
                vb!!.llPointInput.gone()
                vb!!.llPolyInput.show()
            }
            //面
            R.id.rbCoordinatePolygon -> if (isChecked) {
                //开启标绘面模式
                EventBus.getDefault().post(MapEvent(MapEvent.COORDINATE_MARKER_POLYGON))
                clearData()
                vb!!.llPointInput.gone()
                vb!!.llPolyInput.show()
            }

            //点三种
            R.id.rbPlanePoint -> if (isChecked) {
                //平面坐标
                vb!!.llPlanePoint.show()
                vb!!.llSpherePoint.gone()
                vb!!.llSphere2Point.gone()
            }
            R.id.rbSpherePoint -> if (isChecked) {
                //球面坐标
                vb!!.llPlanePoint.gone()
                vb!!.llSpherePoint.show()
                vb!!.llSphere2Point.gone()
            }
            R.id.rbSphere2Point -> if (isChecked) {
                //球面坐标（度分秒）
                vb!!.llPlanePoint.gone()
                vb!!.llSpherePoint.gone()
                vb!!.llSphere2Point.show()
            }

            //线和面一样 四种
            R.id.rbPlaneSingle -> if (isChecked) {
               vb!!.llPlaneSingle.show()
               vb!!.llPlaneMuti.gone()
               vb!!.llSphereSingle.gone()
               vb!!.llSphereMuti.gone()
            }
            R.id.rbPlaneMuti -> if (isChecked) {
                vb!!.llPlaneSingle.gone()
                vb!!.llPlaneMuti.show()
                vb!!.llSphereSingle.gone()
                vb!!.llSphereMuti.gone()
            }
            R.id.rbSphereSingele -> if (isChecked) {
                vb!!.llPlaneSingle.gone()
                vb!!.llPlaneMuti.gone()
                vb!!.llSphereSingle.show()
                vb!!.llSphereMuti.gone()
            }
            R.id.rbSphereMuti -> if (isChecked) {
                vb!!.llPlaneSingle.gone()
                vb!!.llPlaneMuti.gone()
                vb!!.llSphereSingle.gone()
                vb!!.llSphereMuti.show()
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

        "envelope:${envelope.toString()}".printMsg()
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
