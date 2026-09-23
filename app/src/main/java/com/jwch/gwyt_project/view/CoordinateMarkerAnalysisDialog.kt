package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import com.esri.arcgisruntime.mapping.view.MapView
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.i.ActionListener
import com.qmuiteam.qmui.kotlin.onClick


//坐标标绘dialog
class CoordinateMarkerAnalysisDialog(context: Context, mapView: MapView, listener: ActionListener) :
    CoordinateMarkerDialog(context, mapView, listener) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        vb!!.rgGeoType.gone()

        vb!!.rbCoordinatePolygon.performClick()

        vb!!.tvCancle.onClick {
            dismiss()
            clearData()
        }
    }
}
