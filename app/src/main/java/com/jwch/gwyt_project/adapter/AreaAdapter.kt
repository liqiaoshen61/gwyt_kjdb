package com.jwch.gwyt_project.adapter

import android.graphics.Color
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes

//
class AreaAdapter : BrvahAdapter<DistrictsInfo>(R.layout.item_area_select) {

    init { }

    override fun convert(helper: BaseViewHolder?, item: DistrictsInfo?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvAreaTown, distName)

            }
        }
    }
}