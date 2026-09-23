package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.ztt.ZttItem

//村
class AreaStatsAdapter : BrvahAdapter<ZttItem>(R.layout.item_area_stats) {

    init { }

    override fun convert(helper: BaseViewHolder?, item: ZttItem?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvText, "●  ${name}  ${dataCountArea}")
                    .setGone(R.id.tvText,dataCountArea == 0)

            }
        }
    }
}