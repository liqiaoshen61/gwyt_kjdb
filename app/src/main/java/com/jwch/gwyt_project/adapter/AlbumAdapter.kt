package com.jwch.gwyt_project.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.load
import com.jwch.gwyt_project.model.ImageModel

class AlbumAdapter : BrvahAdapter<ImageModel>(R.layout.item_album) {

    var imageWidth = 0
    var imageHeight = 0

    override fun convert(helper: BaseViewHolder?, item: ImageModel?) {

        helper?.let {
            item?.apply {

                var imgView: ImageView = it.getView(R.id.imgPic)

                var params = imgView.layoutParams
                params.width  = imageWidth
                params.height = imageHeight

                imgView.load(imgUrl)
            }
        }
    }
}