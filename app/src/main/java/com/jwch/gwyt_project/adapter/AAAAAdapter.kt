package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.model.ImageModel

class AAAAAdapter : BrvahAdapter<ImageModel>(R.layout.aaaa) {

    init {
//        addChildClickViewIds(R.id.imgDelect)
    }

    override fun convert(helper: BaseViewHolder?, item: ImageModel?) {

        helper?.let {
            item?.apply {

            }
        }
    }
}