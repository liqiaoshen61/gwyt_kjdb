package com.jwch.gwyt_project.adapter

import android.graphics.Color
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes

//村
class VillageAreaAdapter : BrvahAdapter<DistrictsInfo>(R.layout.item_area_village) {

    init { }

    override fun convert(helper: BaseViewHolder?, item: DistrictsInfo?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvAreaVillage, "●  $distName")

                val tvAreaVillage = it.getView<TextView>(R.id.tvAreaVillage)
                (isSelect).yes{
                    tvAreaVillage.setBackgroundResource(R.color.bg_village_select)
                    tvAreaVillage.setTextColor((Color.parseColor("#0664e5")))
                }.no{
                    tvAreaVillage.setBackgroundResource(R.color.transparent)
                    tvAreaVillage.setTextColor((Color.parseColor("#d4e9ff")))
                }

            }
        }
    }
}