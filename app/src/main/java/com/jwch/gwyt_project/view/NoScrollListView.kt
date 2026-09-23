package com.jwch.gwyt_project.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView

class NoScrollListView : ListView {
    constructor(context: Context?) : super(context) {
        initData()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context,attrs) {
        initData()
    }

    constructor( context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initData()
    }

    private fun initData() {
        isFocusable = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }
}