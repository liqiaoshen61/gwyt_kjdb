package com.jwch.gwyt_project.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.allutillib.common.CommonUtil.matchList
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.Info.ReviewRecordInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.getListFromJson
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.view.GridAddImage
import org.xutils.ex.DbException

//
class ReviewAdapter : BrvahAdapter<ReviewRecordInfo>(R.layout.item_review_record) {

    init {
       addChildClickViewIds(R.id.imgDelete)
       addChildClickViewIds(R.id.imgEdit)
    }

    var linkId: String = ""//数据关联id
    var dataType = ImageInfo.REVIEW_RECORD
    var photoList: MutableList<ImageModel>? = null

    override fun convert(helper: BaseViewHolder?, item: ReviewRecordInfo?) {

        helper?.let {
            item?.apply {
                val viewImage = it.getView<GridAddImage>(R.id.viewImage)

                it.setText(R.id.tvQuestionDes, "问题描述：${description}")
                    .setText(R.id.tvTime, "复核时间：${TimeUtil.getDateToString(checkTime.self(), Config.timeFormat1)}")


                //移除之前展示的图片
                viewImage.datalist.removeIf {
                    !it.imgUrl.equals("addPic")
                }

                if(pics.isNotBlank()){
                    photoList = getListFromJson(pics)
                    viewImage.setShowOnly()
                    viewImage.showGrid(photoList)
                    viewImage.visiable(matchList(photoList))
                }

                it.setGone(R.id.viewImage,true)

            }
        }
    }


}