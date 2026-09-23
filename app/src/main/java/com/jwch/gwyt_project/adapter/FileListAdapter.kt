package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.model.SelectFileModel


class FileListAdapter : BrvahAdapter<SelectFileModel>(R.layout.item_select_file) {


    override fun convert(helper: BaseViewHolder?, item: SelectFileModel?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvName, name)
                    .setImageResource(R.id.imgCheck, select.getOne(R.mipmap.logo_selectd, R.mipmap.logo_unselected))
                    .setTextColor(R.id.tvName, select.getOne(findColor(context, com.jameni.basepage_lib.R.color.main_color), findColor(context, R.color.txt_black)))
            }
        }
    }
}