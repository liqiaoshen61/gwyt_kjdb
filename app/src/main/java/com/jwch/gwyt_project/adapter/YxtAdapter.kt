package com.jwch.gwyt_project.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.ImageLayerItem


class YxtAdapter : BrvahAdapter<ImageLayerItem>(R.layout.item_map_yxt) {

    init {
        addChildClickViewIds(R.id.ivLayerReplace)
        addChildClickViewIds(R.id.ivLayerAdd)
    }

    override fun convert(helper: BaseViewHolder?, item: ImageLayerItem?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvLayerName,name)
                val ivLayerReplace = it.getView<ImageView>(R.id.ivLayerReplace)
                val ivLayerAdd = it.getView<ImageView>(R.id.ivLayerAdd)

                (selectReplaceAction).yes{ ivLayerReplace.setImageResource(R.mipmap.icon_square_select)}
                    .no{ivLayerReplace.setImageResource(R.mipmap.icon_square_unselect)}

                (selectAddAction).yes{ ivLayerAdd.setImageResource(R.mipmap.icon_square_select)}
                .no{ivLayerAdd.setImageResource(R.mipmap.icon_square_unselect)}

            }
        }
    }
}