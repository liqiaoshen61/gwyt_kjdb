package com.jwch.gwyt_project.model

import com.esri.arcgisruntime.geometry.Geometry

data class ShpFeature(
    val geometry: Geometry,
    val styleConfig: StyleConfig,
    val properties: Map<String, Any?> = emptyMap()
)