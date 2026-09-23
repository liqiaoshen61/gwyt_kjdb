package com.jwch.gwyt_project.adapter

import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.PoiTypesInfo
import com.jwch.gwyt_project.R


class PlaceTypeAdapter : BrvahAdapter<PoiTypesInfo>(R.layout.item_place_type) {



    override fun convert(helper: BaseViewHolder?, item: PoiTypesInfo?) {

        helper?.let {
            item?.apply {
                val type = it.getView<TextView>(R.id.tvPlaceType)

                type.text = typeName

                if(isSelect) type.setBackgroundResource(R.drawable.bg_square_radius)  else type.setBackgroundResource(0)
            }
        }
    }
}