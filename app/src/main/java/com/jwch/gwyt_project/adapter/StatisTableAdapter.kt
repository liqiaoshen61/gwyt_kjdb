package com.jwch.gwyt_project.adapter

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.FileModel
import com.jwch.gwyt_project.util.excel.TableDataModel

//统计表格Adapter
class StatisTableAdapter : BrvahAdapter<TableDataModel>(R.layout.item_statis_table) {

    init {
        addChildClickViewIds(R.id.llItem)
    }

    override fun convert(helper: BaseViewHolder?, item: TableDataModel?) {


        helper?.let {
            item?.apply {

                val ivSelect = it.getView<ImageView>(R.id.ivSelect)
                val llItem = it.getView<LinearLayout>(R.id.llItem)

                it.setText(R.id.tvKey, key)
                    .setText(R.id.tvValue, value)
                    .setText(R.id.tvValue2, value2)

                isSelect.yes {
                    ivSelect.show()
                    llItem.setBackgroundColor(findColor(context, R.color.statis_table_select))
                }.no {
                    ivSelect.hide()
                    llItem.setBackgroundColor(findColor(context, R.color.transColor))
                }

                if(key == "专题类型"){
                    llItem.setBackgroundColor(findColor(context, R.color.statis_table_title))
                }

            }
        }
    }
}