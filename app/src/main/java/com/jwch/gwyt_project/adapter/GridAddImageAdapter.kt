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

class GridAddImageAdapter : BrvahAdapter<ImageModel>(R.layout.item_grid_add_image) {

    init {
        addChildClickViewIds(R.id.imgDelect)
        addChildClickViewIds(R.id.cbSelect)
    }

    var isDelete = true
    var isLoadFromMemory = true

    var imageWidth = 0
    var imageHeight = 0

    var enableSelection = false //启用选择模式

    var nameColor = 0

    override fun convert(helper: BaseViewHolder?, item: ImageModel?) {

        helper?.let {
            item?.apply {

                var rlImg: RelativeLayout = it.getView(R.id.rlImg)
                var imgView: ImageView = it.getView(R.id.imgPic)
                var bacView: ImageView = it.getView(R.id.imgbac)
                var imgDelete: ImageView = it.getView(R.id.imgDelect)
                var tvName: TextView = it.getView(R.id.tvName)
                var cbSelect: TextView = it.getView(R.id.cbSelect)
                imgUrl.printMsg()

                if (imageWidth != 0 && imageHeight != 0) {
                    rlImg.layoutParams.width = imageWidth
                    rlImg.layoutParams.height = imageHeight
                }


                when (imgUrl) {

                    "addPic" -> {

                        bacView.gone()
                        imgDelete.gone()
                        imgView.load(R.mipmap.addpic)
                        tvName.hide()
//                        Glide.with(context).load(R.mipmap.addpic).into(imgView)
                        cbSelect.gone()

                    }

                    else -> {

                        bacView.show()
                        imgView.show()

//                        imgView.clearFindViewByIdCache()
                        isDelete.yes { imgDelete.show() }.no { imgDelete.gone() }

                        if (isLoadFromMemory) {
                            imgView.load(imgUrl)
                        } else {
                            Glide.with(context).load(imgUrl).skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE).into(imgView)
                        }

                        tvName.show()
                        tvName.text = name
//                        GlideUtil.displayImage(context, imgUrl, imgView)

                        when(nameColor){
                            0 ->{
                                tvName.setTextColor(findColor(context, R.color.black))
                            }
                            1 ->{
                                tvName.setTextColor(findColor(context, R.color.white))
                            }
                            else -> {
                                tvName.setTextColor(findColor(context, R.color.black))
                            }
                        }



                        cbSelect.visiable(enableSelection)

                    }
                }
            }
        }
    }
}