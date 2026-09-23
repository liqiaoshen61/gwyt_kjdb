package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.MapView
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.PrintUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewCoordinateInputPointDialogBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.isEmpty
import com.jwch.gwyt_project.ext.isShow
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.SoftUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus


//
class CoordinateInputPointDialog(context: Context, val mapView: MapView, var listener: ActionListener) :
    JameniBaseDialog(context) {

    var lng = 0.0
    var lat = 0.0

    var mActivity: Activity? = null
    var softUtil: SoftUtil? = null

    var vb: ViewCoordinateInputPointDialogBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_coordinate_input_point_dialog, null)
        vb = ViewCoordinateInputPointDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)
        softUtil = SoftUtil()

//        if (mapView.map.basemap.baseLayers.size == 0) return
//        val layer = mapView.map.basemap.baseLayers[0]
//        val a = layer.fullExtent.xMax
//        val b = layer.fullExtent.xMin
//        val c = layer.fullExtent.yMax
//        val d = layer.fullExtent.yMin
//        val sta = a.toString()
//        val stb = b.toString()
//        val stc = c.toString()
//        val std = d.toString()
//        vb!!.tvLimit.setText("经度" + sta + "到" + stb + "\n纬度" + stc + "到" + std)


        vb!!.tvOk.onClick {
            getPoint()
        }

        vb!!.tvCancle.onClick {
            dismiss()
        }

    }

    fun getPoint() {
        //球面坐标
        vb!!.llSphere.isShow().yes {
            (vb!!.etLongitude.isEmpty() || vb!!.etLatitude.isEmpty()).yes {
                ToastUtils.show("输入不得为空")
                return@getPoint
            }

            lng = vb!!.etLongitude.text.toString().toDouble()
            lat = vb!!.etLatitude.text.toString().toDouble()

            isContains(lng, lat).yes {
                val point = Point(lng, lat, SpatialReference.create(Config.sp4490Int))
                EventBus.getDefault().post(MapEvent(MapEvent.DO_BUFFER_ANALYSIS_CUSTOM_POINT, point))
                dismiss()
            }.no {
                ToastUtils.show("此位置没在地图范围内")
                return@getPoint
            }
        }
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
