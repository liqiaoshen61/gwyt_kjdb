package com.jwch.gwyt_project.adapter.geojson

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.R

class GeoJsonTreeAdapter : BaseNodeAdapter() {

    var onDeleteItem: ((GeoJsonItem) -> Unit)? = null

    init {
        addNodeProvider(
            GeoJsonFolderProvider(
                GeoJsonItem.TYPE_FOLDER,
                R.layout.item_geojson_folder
            )
        )
        addNodeProvider(
            GeoJsonFileProvider(
                GeoJsonItem.TYPE_FILE,
                R.layout.item_geojson_file
            )
        )
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is GeoJsonItem) {
            return node.itemType
        }
        return -1
    }
}