package com.jwch.gwyt_project.adapter

import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.model.ImageModel

class GridSelectImageAdapter : BrvahAdapter<ImageModel>(R.layout.item_grid_select_image) {

    init {
        addChildClickViewIds(R.id.imgPic)
    }

    var imageWidth = 0
    var imageHeight = 0

    override fun convert(helper: BaseViewHolder?, item: ImageModel?) {

        helper?.let {
            item?.apply {

                var imgPic: ImageView = it.getView(R.id.imgPic)
                var ivSelect: ImageView = it.getView(R.id.ivSelect)
                var tvName: TextView = it.getView(R.id.tvName)

                tvName.text = name
                imgPic.show()

                isSelect.yes {
                    ivSelect.setImageResource(R.mipmap.select)
                }.no {
                    ivSelect.setImageResource(R.mipmap.not_select)
                }

//                Glide.with(context).load(imgUrl).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(imgPic)
                Glide.with(context).load(imgUrl).into(imgPic)


            }
        }
    }
}