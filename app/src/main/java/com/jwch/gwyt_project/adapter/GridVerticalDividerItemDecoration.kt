package com.jwch.gwyt_project.adapter

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridVerticalDividerItemDecoration(
    private val color: Int,
    private val heightPx: Int,
    private val columnCount: Int
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        color = this@GridVerticalDividerItemDecoration.color
        style = Paint.Style.FILL
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val column = position % columnCount

        // 只在列之间添加偏移（用于绘制垂直分割线）
        outRect.left = if (column == 0) 0 else heightPx / 2
        outRect.right = if (column == columnCount - 1) 0 else heightPx / 2
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        if (childCount == 0) return

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val column = position % columnCount

            // 只绘制列之间的垂直分割线
            if (column < columnCount - 1) {
                val left = child.right.toFloat()
                val right = left + heightPx
                val top = child.top.toFloat()
                val bottom = child.bottom.toFloat()

                canvas.drawRect(left, top, right, bottom, paint)
            }
        }
    }
}

