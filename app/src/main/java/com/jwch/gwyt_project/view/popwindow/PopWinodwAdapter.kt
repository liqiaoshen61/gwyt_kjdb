package com.jwch.gwyt_project.view.popwindow

import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes
import org.jetbrains.anko.textColorResource

class PopWinodwAdapter : BrvahAdapter<PopWindowModel>(R.layout.item_popwindow_menu) {


    override fun convert(helper: BaseViewHolder?, item: PopWindowModel?) {

        helper?.let {
            item?.apply {

                val tvTxt = it.getView<TextView>(R.id.tvTxt)

                it.setText(R.id.tvTxt, txt)

                icon?.apply {
                    it.setImageResource(R.id.ivIcon, this)
                }
                it.setGone(R.id.ivIcon, icon == null)

                select.yes {
                    tvTxt.textColorResource = com.jameni.basepage_lib.R.color.main_color
                }.no {
                    tvTxt.textColorResource = com.jameni.basepage_lib.R.color.whiteColor
                }


            }
        }
    }
}