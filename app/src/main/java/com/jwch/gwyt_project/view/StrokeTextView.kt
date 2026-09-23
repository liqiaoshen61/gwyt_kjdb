package com.jwch.gwyt_project.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import com.jwch.gwyt_project.R


class StrokeTextView(context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int) :
    androidx.appcompat.widget.AppCompatTextView(context!!, attrs, defStyleAttr) {
    private var outlineTextView: TextView? = null

    constructor(context: Context?) : this(context, null) {}
    constructor(context: Context?, @Nullable attrs: AttributeSet?) : this(context, attrs, 0) {}

    private fun init(attrs: AttributeSet?) {
        //1.获取参数
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)
        val stroke_color = ta.getColor(R.styleable.StrokeTextView_stroke_color,  resources.getColor(com.jameni.basepage_lib.R.color.whiteColor))
        val stroke_width = ta.getDimension(R.styleable.StrokeTextView_stroke_width, 2f)

        //2.初始化TextPaint
        val paint = outlineTextView!!.paint
        paint.strokeWidth = stroke_width
        paint.style = Paint.Style.STROKE
        outlineTextView!!.setTextColor(stroke_color)
        outlineTextView!!.gravity = gravity
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        super.setLayoutParams(params)
        outlineTextView!!.layoutParams = params
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //设置轮廓文字
        val outlineText = outlineTextView!!.text
        if (outlineText == null || outlineText != text) {
            outlineTextView!!.text = text
            postInvalidate()
        }
        outlineTextView!!.measure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        outlineTextView!!.layout(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        outlineTextView!!.draw(canvas)
        super.onDraw(canvas)
    }

    init {
        outlineTextView = TextView(context, attrs, defStyleAttr)
        init(attrs)
    }
}