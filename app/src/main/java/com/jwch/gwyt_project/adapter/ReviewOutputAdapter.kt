package com.jwch.gwyt_project.adapter

import android.widget.ImageView
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
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.view.GridAddImage
import org.xutils.ex.DbException

//
class ReviewOutputAdapter : BrvahAdapter<ReviewRecordInfo>(R.layout.item_review_output_record) {


    var linkId: String = ""//数据关联id
    var dataType = ImageInfo.REVIEW_RECORD


    override fun convert(helper: BaseViewHolder?, item: ReviewRecordInfo?) {

        helper?.let {
            item?.apply {
                val imgSelect = it.getView<ImageView>(R.id.imgSelect)

                it.setText(R.id.tvName, "${"${city}${county}${town}"}")
                    .setText(R.id.tvTime, "${TimeUtil.getDateToString(createTimeStamp.self(), Config.timeFormat1)}")

                imgSelect.setImageResource(isSelect.getOne(R.mipmap.icon_square_select, R.mipmap.icon_square_unselect))

            }
        }
    }


}