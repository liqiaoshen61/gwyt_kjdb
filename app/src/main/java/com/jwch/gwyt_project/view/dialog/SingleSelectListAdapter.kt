package com.jwch.gwyt_project.view.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.ABSAdapter
import com.jwch.gwyt_project.model.SelectionItemInterface

public class SingleSelectListAdapter<E : SelectionItemInterface>(context: Context) : ABSAdapter<E>(context) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var cView: View
        val holder: ViewHolder
        if (convertView == null) {
            cView = LayoutInflater.from(mContext).inflate(com.jameni.jamenidialoglib.R.layout.item_txt, null)
            holder = ViewHolder()
            holder.tv = cView!!.findViewById(com.jameni.jamenidialoglib.R.id.tv)
            holder.rlSelectionItem = cView!!.findViewById(com.jameni.jamenidialoglib.R.id.rlSelectionItem)
            cView!!.setTag(holder)
        } else {
            cView = convertView
            holder = cView.tag as ViewHolder
        }


        if (getItem(position) is SelectionItemInterface) {
            val model = getItem(position) as SelectionItemInterface


            holder.tv!!.text = if (model.getText() == null) "" else model.getText()!!
            if (model.getTextSize() != null) {
                holder.tv!!.textSize = model.getTextSize()!!
            } else {
                holder.tv!!.textSize = 14f
            }
            if (model.getTextColorResId() != null) {
                holder.tv!!.setTextColor(mContext.resources.getColor(model.getTextColorResId()!!))
            } else {
                holder.tv!!.setTextColor(mContext.resources.getColor(R.color.txt_black))
            }
            if (model.getBackgroundColorResId() != null) {
                holder.rlSelectionItem!!.setBackgroundResource(model.getBackgroundColorResId()!!)
            } else {
                holder.rlSelectionItem!!.setBackgroundResource(R.color.white)
            }
        }
        return cView
    }


    internal class ViewHolder {
        var tv: TextView? = null
        var rlSelectionItem: RelativeLayout? = null
    }
}