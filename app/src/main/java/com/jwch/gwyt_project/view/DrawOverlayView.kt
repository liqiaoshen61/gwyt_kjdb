package com.jwch.gwyt_project.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class DrawOverlayView(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.RED                // ⭐ 红色方便看
        strokeWidth = 8f                 // ⭐ 粗一点才看得清
        style = Paint.Style.STROKE
        isAntiAlias = true               // ⭐ 防锯齿
    }


    private var points = mutableListOf<PointF>()
    var onMeasureFinish: ((pxDistance: Float) -> Unit)? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e("DrawView", "onTouchEvent: x=${event.x}, y=${event.y}")

        if (event.action == MotionEvent.ACTION_DOWN) {
            points.add(PointF(event.x, event.y))

            if (points.size == 2) {
                // 计算像素距离
                val dx = points[1].x - points[0].x
                val dy = points[1].y - points[0].y
                val distance = sqrt(dx * dx + dy * dy)

                onMeasureFinish?.invoke(distance)

                // ⭐ 添加下面一行：允许继续测量
                postDelayed({
                    points.clear()
                    invalidate()
                }, 1000)

            }
            invalidate()
        }
        return true
    }




    override fun onDraw(canvas: Canvas) {
        if (points.size == 2) {
            canvas.drawLine(
                points[0].x, points[0].y,
                points[1].x, points[1].y,
                paint
            )
        }
    }

    fun reset() {
        points.clear()
        invalidate()
    }
}
