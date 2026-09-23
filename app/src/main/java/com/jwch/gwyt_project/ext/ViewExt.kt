package com.jwch.gwyt_project.ext

import android.graphics.Paint
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes


///下划线
fun TextView.underLine() {
    paint.flags = paint.flags or Paint.UNDERLINE_TEXT_FLAG
    paint.isAntiAlias = true
}

fun TextView.deleteLine() {
    paint.flags = paint.flags or Paint.STRIKE_THRU_TEXT_FLAG
    paint.isAntiAlias = true
}

//粗体
fun TextView.bold(isBold: Boolean = true) {
    paint.isFakeBoldText = isBold
    paint.isAntiAlias = true
}


fun TextView.color(@ColorRes resId: Int) {
    setTextColor(findColor(context, resId))
}

fun EditText.passwordToggledVisible() {
    val selection = selectionStart
    transformationMethod = if (transformationMethod == null) PasswordTransformationMethod() else null
    setSelection(selection)
}


inline fun View.show() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

inline fun View.visiable(visiable: Boolean) {
    if (visiable) {
        show()
    } else {
        gone()
    }
}

inline fun View.gone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

inline fun View.hide() {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
}

inline fun View.isShow() = visibility == View.VISIBLE


inline fun WebView.setContent(richText: String) {
    val webSettings: WebSettings = this.getSettings()
    webSettings.javaScriptEnabled = true //允许使用js
    webSettings.useWideViewPort = true  //支持自动适配
    webSettings.setSupportZoom(false)  // 不支持屏幕缩放
    webSettings.builtInZoomControls = false
    webSettings.displayZoomControls = false  //不显示webview缩放按钮
    webSettings.setBlockNetworkImage(true)  // 把图片加载放在最后来加载渲染

    this.loadDataWithBaseURL(null, richText, "text/html", "utf-8", null)
}


