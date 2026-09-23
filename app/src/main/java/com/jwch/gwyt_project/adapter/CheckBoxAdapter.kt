package com.jwch.gwyt_project.adapter

import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.SelectionListModel
import org.jetbrains.anko.*

class CheckBoxAdapter : BrvahAdapter<SelectionListModel>(R.layout.item_check_box) {

    var uiType = 0

    override fun convert(helper: BaseViewHolder?, item: SelectionListModel?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvCheckItem, value.self())

                val tvCheckItem = it.getView<TextView>(R.id.tvCheckItem)
                val rlCheckItem = it.getView<RelativeLayout>(R.id.rlCheckItem)
                val ivCheckIcon = it.getView<ImageView>(R.id.ivCheckIcon)

//                select.yes {
//                    ivCheckItem.setBackgroundResource(R.mipmap.icon_square_select)
//                }.no {
//                    ivCheckItem.setBackgroundResource(R.mipmap.icon_square_unselect)
//                }

                
                when(uiType){
                    0 ->{
                        select.yes {
                            tvCheckItem.textColorResource = R.color.white
                            rlCheckItem.setBackgroundResource(R.drawable.bg_green_gradient_border)
                        }.no {
                            tvCheckItem.textColorResource = R.color.white
                            rlCheckItem.setBackgroundResource(R.drawable.trans)
                        }
                    }
                    1 -> {
                        ivCheckIcon.setImageResource(R.mipmap.icon_check_blue)

                        select.yes {
                            rlCheckItem.setBackgroundResource(R.drawable.bg_checkbox_select_blue)
                            ivCheckIcon.show()
                        }.no {
                            rlCheckItem.setBackgroundResource(R.drawable.bg_checkbox_unselect)
                            ivCheckIcon.gone()
                        }
                    }
                    2 ->{
                        tvCheckItem.textSize = 15f

                        select.yes {
                            tvCheckItem.textColorResource = R.color.green2
                            rlCheckItem.setBackgroundResource(R.drawable.bg_square_green_border6)
                        }.no {
                            tvCheckItem.textColorResource = R.color.black
                            rlCheckItem.setBackgroundResource(R.drawable.bg_square_white_border6)
                        }
                    }

                    3 ->{
                        tvCheckItem.textSize = 16f
                        select.yes {
                            tvCheckItem.textColorResource = R.color.statis_year_select
                            rlCheckItem.setBackgroundResource(R.drawable.trans)
                        }.no {
                            tvCheckItem.textColorResource = R.color.statis_year_unselect
                            rlCheckItem.setBackgroundResource(R.drawable.trans)
                        }
                    }
                    4 ->{
                        select.yes {
                            tvCheckItem.textColorResource = R.color.white
                            rlCheckItem.setBackgroundResource(R.drawable.bg_blue_btn2)
                        }.no {
                            tvCheckItem.textColorResource = R.color.txt_black
                            rlCheckItem.setBackgroundResource(R.drawable.trans)
                        }
                    }
                }


            }
        }
    }
}