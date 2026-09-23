package com.jwch.gwyt_project.adapter

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.PoisInfo
import com.jwch.gwyt_project.R


class PlaceQueryAdapter : BrvahAdapter<PoisInfo>(R.layout.item_place_query_result) {

    init {
//        addChildClickViewIds(R.id.imgDelect)
    }

    override fun convert(helper: BaseViewHolder?, item: PoisInfo?) {

        helper?.let {
            item?.apply {
                val point = it.getView<ImageView>(R.id.iv_point)
                it.setText(R.id.tv_name,poiName)
                        .setText(R.id.tv_two_name,townName)
                point.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier("number_" + index, "mipmap", "com.jwch.gwyt_project")));
            }
        }
    }


}