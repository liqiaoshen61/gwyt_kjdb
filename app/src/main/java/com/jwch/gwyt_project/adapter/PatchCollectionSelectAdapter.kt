package com.jwch.gwyt_project.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.CollecPatchInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.db.DbUtil
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.model.DataEvent
import org.greenrobot.eventbus.EventBus

class PatchCollectionSelectAdapter : BrvahAdapter<CollecPatchInfo>(R.layout.item_place_collection_select) {

    init {
        addChildClickViewIds(R.id.ivClear)
    }

    override fun convert(helper: BaseViewHolder?, item: CollecPatchInfo?) {

        helper?.let {
            item?.apply {
                val imgSelect = it.getView<ImageView>(R.id.imgSelect)

                it.setText(R.id.tvName, name).setText(R.id.tvTime, time)

                imgSelect.setImageResource(isSelect.getOne(R.mipmap.icon_square_select, R.mipmap.icon_square_unselect))
            }
        }
    }
}