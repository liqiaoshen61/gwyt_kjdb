package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.R

import com.jwch.gwyt_project.adapter.geo.GeoExportLevel1
import com.jwch.gwyt_project.adapter.geo.GeoExportLevel2
import com.jwch.gwyt_project.model.GeoCollectionModel


class GeoCollectionAdapter: BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(GeoExportLevel1(GeoCollectionModel.LEVEL1, R.layout.item_marker_folder_export))
        addNodeProvider(GeoExportLevel2(GeoCollectionModel.LEVEL2, R.layout.item_marker_collect_export))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is GeoCollectionModel) {
            return node.itemLevel
        }
        return -1
    }


}