package com.jwch.gwyt_project.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class BigImagePagerAdapter(list: MutableList<View>) : PagerAdapter() {
    val list = list

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun getCount(): Int = list.size

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(list[position])
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = list[position]
        container.addView(itemView, 0)
        return itemView
    }
}