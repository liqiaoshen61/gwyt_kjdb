package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.model.AnalysisListModel

class QueryResultListAdapter : BrvahAdapter<AnalysisListModel>(R.layout.item_query_result) {


    override fun convert(helper: BaseViewHolder?, item: AnalysisListModel?) {
        helper?.let {
            item?.apply {
                it.setText(R.id.tv_name, layerName.self())
                    .setText(R.id.tv_percentage, "占比:${percentage}%")
            }
        }
    }
}