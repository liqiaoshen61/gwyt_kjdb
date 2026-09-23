package com.jwch.gwyt_project.adapter

import android.content.Context
import android.widget.BaseAdapter

abstract class ABSAdapter<T>(context: Context) : BaseAdapter() {

    var listData: MutableList<T>? = null
    var mContext: Context = context

    fun update(list: MutableList<T>?) {
        this.listData = list
        notifyDataSetChanged()
    }

    override fun getCount(): Int = if (listData == null) 0 else listData!!.size

    override fun getItem(position: Int): Any? = if (listData != null) listData!![position] else null

    override fun getItemId(position: Int): Long = position.toLong()


}