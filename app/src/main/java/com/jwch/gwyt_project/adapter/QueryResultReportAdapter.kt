package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.model.KVData


class QueryResultReportAdapter : BrvahAdapter<KVData>(R.layout.item_report) {


    override fun convert(helper: BaseViewHolder?, item: KVData?) {
        helper?.let {
            item?.apply {
                it.setText(R.id.tvLayerName, name.self())
                    .setText(R.id.tvTotalArea,value)

            }
        }
    }
}