package com.jwch.gwyt_project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.model.SelectionItemInterface
import com.jwch.gwyt_project.model.SelectionListModel
import org.jetbrains.anko.find

class MultiSelectionAdapter<T : SelectionItemInterface>(context: Context) : ABSAdapter<T>(context) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var cView: View
        var holder: ViewHolder
        if (convertView == null) {
            cView = LayoutInflater.from(mContext).inflate(com.jameni.jamenidialoglib.R.layout.item_txt, null)
            holder = ViewHolder()
            holder.tv = cView.find(com.jameni.jamenidialoglib.R.id.tv)
            holder.rlSelectionItem = cView.find(com.jameni.jamenidialoglib.R.id.rlSelectionItem)

            cView?.tag = holder
        } else {
            cView = convertView
            holder = convertView.tag as ViewHolder
        }

        if (getItem(position) is SelectionListModel) {

            val data = getItem(position) as SelectionListModel
            holder.tv?.text = data.getText().self()
            holder.tv!!.setTextColor(
                data.select.getOne(
                    findColor(mContext, com.jameni.basepage_lib.R.color.main_color),
                    findColor(mContext, com.jameni.basepage_lib.R.color.txt_black)
                )
            )
        }
        return cView
    }


    internal class ViewHolder {
        var tv: TextView? = null
        var rlSelectionItem: RelativeLayout? = null
    }
}