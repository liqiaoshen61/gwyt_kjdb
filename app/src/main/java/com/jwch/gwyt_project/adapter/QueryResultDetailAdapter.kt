package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.SaType
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.yes


class QueryResultDetailAdapter : BrvahAdapter<SaType>(R.layout.item_table) {


    override fun convert(helper: BaseViewHolder?, item: SaType?) {
        helper?.let {
            item?.apply {
                it.setText(R.id.tvTableName, name.self())
                    .setText(R.id.tvTotalArea,"$totalArea")
                    .setText(R.id.tvAreaPercentage, "$totalAreaPercentage%")


                if(dataType == SaType.Mode.TOTAL){
                    it.setText(R.id.tvAreaPercentage,"100.0%")
                }


                level.equals("1").yes {
                    it.setText(R.id.tvTableName, "    $name")
                        .setText(R.id.tvTotalArea,"$totalArea")
                        .setText(R.id.tvAreaPercentage, "")
                }
            }
        }
    }
}