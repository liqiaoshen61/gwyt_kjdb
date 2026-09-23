package com.jwch.gwyt_project.adapter

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.qmuiteam.qmui.kotlin.onClick
import org.jetbrains.anko.find

class TownAdapter2<T>(context: Context) :  ABSAdapter<DistrictsInfo>(context) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var cView: View
        var holder: ViewHolder
        if (convertView == null) {
            cView = LayoutInflater.from(mContext).inflate(R.layout.item_area_town, null)
            holder = ViewHolder()
            holder.tvAreaTown = cView.find(R.id.tvAreaTown)
            holder.tvCount = cView.find(R.id.tvCount)
            cView?.tag = holder
        } else {
            cView = convertView
            holder = convertView.tag as ViewHolder
        }


        val data = getItem(position) as DistrictsInfo

        data.apply {
            holder.tvAreaTown?.text =distName
            holder.tvCount?.text = "$countFinish / $count"



        }

        return cView
    }

    internal class ViewHolder {
        var tvAreaTown: TextView? = null
        var tvCount: TextView? = null
    }



}