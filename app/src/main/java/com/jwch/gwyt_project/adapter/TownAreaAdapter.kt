package com.jwch.gwyt_project.adapter

import android.graphics.Color
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes

//镇
class TownAreaAdapter : BrvahAdapter<DistrictsInfo>(R.layout.item_area_town) {

    init { }

    override fun convert(helper: BaseViewHolder?, item: DistrictsInfo?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvAreaTown, distName)
                    .setText(R.id.tvCount, "$countFinish / $count")

                val tvAreaTown = it.getView<TextView>(R.id.tvAreaTown)


                (isSelect).yes{
                    tvAreaTown.setBackgroundResource(R.drawable.bg_area_town_select)
                    tvAreaTown.setTextColor((Color.parseColor("#d4e9ff")))
                }.no{
                    tvAreaTown.setBackgroundResource(R.drawable.bg_area_town)
                    tvAreaTown.setTextColor((Color.parseColor("#0664e5")))
                }



            }
        }
    }
}