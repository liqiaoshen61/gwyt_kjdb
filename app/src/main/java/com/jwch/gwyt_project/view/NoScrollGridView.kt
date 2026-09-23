package com.jwch.gwyt_project.view

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class NoScrollGridView : GridView {
    var isFOUCSABLE = false

    constructor(context: Context?) : super(context) {
        initData()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context,attrs) {
        initData()
    }

    constructor( context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initData()
    }

    fun initData() {
        isFocusable = isFOUCSABLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec =  MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }
}