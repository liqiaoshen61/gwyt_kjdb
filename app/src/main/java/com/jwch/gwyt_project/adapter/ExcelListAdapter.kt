package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.model.FileModel

class ExcelListAdapter : BrvahAdapter<FileModel>(R.layout.item_excel_list) {

    var imageWidth = 0
    var imageHeight = 0

    override fun convert(helper: BaseViewHolder?, item: FileModel?) {

        helper?.let {
            item?.apply {

                helper.setText(R.id.tvName,item.name.self())


            }
        }
    }
}