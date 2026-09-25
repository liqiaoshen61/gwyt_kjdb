package com.jwch.gwyt_project.adapter.shp

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.R

class ShpTreeAdapter : BaseNodeAdapter() {

    var onDeleteItem: ((ShpItem) -> Unit)? = null
    var onEditItem: ((ShpItem) -> Unit)? = null

    init {
        addNodeProvider(
            ShpFolderProvider(
                ShpItem.TYPE_FOLDER,
                R.layout.item_shp_folder
            )
        )
        addNodeProvider(
            ShpFileProvider(
                ShpItem.TYPE_FILE,
                R.layout.item_shp_file
            )
        )
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is ShpItem) {
            return node.itemType
        }
        return -1
    }
}
