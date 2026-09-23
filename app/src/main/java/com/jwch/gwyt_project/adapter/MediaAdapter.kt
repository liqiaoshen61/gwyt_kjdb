package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.AccessoryInfo
import com.jwch.gwyt_project.R


class MediaAdapter : BrvahAdapter<AccessoryInfo>(R.layout.item_media_collect) {

    init {
        addChildClickViewIds(R.id.ivDeleteMedia)
    }

    override fun convert(helper: BaseViewHolder?, item: AccessoryInfo?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvName, path)
                    .setText(R.id.tvCreateTime, time)
            }
        }
    }

}