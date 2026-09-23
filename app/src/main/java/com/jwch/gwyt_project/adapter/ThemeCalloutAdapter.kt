package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.ztt.ZttItem
import com.jwch.gwyt_project.model.KVData

//专题图callout中的列表
class ThemeCalloutAdapter : BrvahAdapter<KVData>(R.layout.item_theme_callout) {

    init { }

    override fun convert(helper: BaseViewHolder?, item: KVData?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvText, "${name}：${value}")

            }
        }
    }
}