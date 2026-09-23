package com.jwch.gwyt_project.adapter

import android.graphics.Color
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.value
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.KVData
import com.jwch.gwyt_project.model.KVModel
import org.jetbrains.anko.backgroundResource

//
class DetailParamsAdapter : BrvahAdapter<KVData>(R.layout.item_detail_params) {

    init {}

    override fun convert(helper: BaseViewHolder?, item: KVData?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvKey, name)
                    .setText(R.id.tvValue, value)

                val tvValue = it.getView<TextView>(R.id.tvValue)

                when (value) {
                    "已办结","是问题" -> {
                        tvValue.setTextColor((Color.parseColor("#5D9760")))
                        tvValue.backgroundResource = R.drawable.bg_event_done
                    }

                    "未办结","不是问题" -> {
                        tvValue.setTextColor((Color.parseColor("#EF6C00")))
                        tvValue.backgroundResource = R.drawable.bg_event_undo
                    }

                    else -> {
                        tvValue.setTextColor((Color.parseColor("#343434")))
                        tvValue.backgroundResource = R.drawable.trans
                    }
                }

            }
        }
    }
}