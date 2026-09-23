package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R


class PoiDetailAdapter : BrvahAdapter<List<Map<String, String>>>(R.layout.item_detail) {


    override fun convert(helper: BaseViewHolder?, item: List<Map<String, String>>?) {

        helper?.let {
            item?.apply {

            }
        }
    }


}