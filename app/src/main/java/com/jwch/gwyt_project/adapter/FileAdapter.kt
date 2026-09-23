package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.model.FileModel

//详情附件的list
class FileAdapter : BrvahAdapter<FileModel>(R.layout.item_file) {

    init { }

    override fun convert(helper: BaseViewHolder?, item: FileModel?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvName1, name)

            }
        }
    }
}