package com.jwch.gwyt_project.adapter

import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.model.ProcessRecord

//处理流程的adapter
class HandleFlowAdapter : BrvahAdapter<ProcessRecord>(R.layout.item_handle_flow) {

    init { }

    override fun convert(helper: BaseViewHolder?, item: ProcessRecord?) {

        helper?.let {
            item?.apply {
                // 获取当前 item 的位置
                val position = helper.adapterPosition
                val llItem = it.getView<LinearLayout>(R.id.llItem)
                val viewLift = it.getView<View>(R.id.viewLift)


                it.setText(R.id.tvUser, processor)
                    .setText(R.id.tvTime, processTime)
                    .setText(R.id.tvContent, content)
                    .setText(R.id.tvStatus, status)
                    .setGone(R.id.tvContent, content.isNullOrBlank())
                    .setGone(R.id.tvStatus, status.isNullOrBlank())

                // 如果是第一个 item，设置背景色
                if (position == 0) {
                    // 设置背景颜色
                    llItem.setBackgroundResource(R.drawable.bg_handle_flow)
                    viewLift.show()

                } else {
                    // 其他 item 恢复默认背景
                    llItem.setBackgroundResource(R.color.transColor)
                    viewLift.hide()
                }

            }
        }
    }


}