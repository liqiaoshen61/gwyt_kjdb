package com.jwch.gwyt_project.util

import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.MapView
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QueryGisUtil {
    companion object {
        var queryUtil = QueryGisUtil()
    }

    public fun printAttrbuite(feature: Feature?) {
        feature?.attributes?.forEach {
            if (!(it.key as String).contains("time")) {
                "属性 key: ${it.key}   value:${it.value}".printMsg()
            }
        }
    }


    public fun getAttrValue(attr: Map<String, Any>?, key: String): String {

        attr?.let {
            it.contains(key).yes {
                if (attr[key] == null) {
                    return ""
                }
                return attr[key].toString()
            }
        }

        return ""
    }

    public fun getAttrValue_Double(attr: Map<String, Any>?, key: String): Double {

        attr?.let {
            it.contains(key).yes {
                if (attr[key] == null) {
                    return 0.0
                }
                return attr[key].toString().toDouble()
            }
        }

        return 0.0
    }

    suspend fun queryFeatureTable(table: ServiceFeatureTable, param: QueryParameters, block: (MutableIterator<Feature>?) -> Unit) {
        val queryResult = table.queryFeaturesAsync(param, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        queryResult?.addDoneListener {

            try {
                val result = queryResult?.get()
                GlobalScope.launch(Dispatchers.Main) {
                    block(result?.iterator())
                }
            } catch (e: Exception) {
                e.message.printMsg()
                e.cause?.message.printMsg()
            }

        }
    }


    fun getEnvelope(mapView: MapView, clickPoint: Point): Envelope {
        val tolerance = 10.0
        val mapTolerace = tolerance.times(mapView.unitsPerDensityIndependentPixel)
        val envelope = Envelope(
            clickPoint.x.minus(mapTolerace), clickPoint.y.minus(mapTolerace), clickPoint.x.plus(mapTolerace), clickPoint.y.plus(mapTolerace), mapView.map.spatialReference
        )

        return envelope
    }
}