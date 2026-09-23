package com.jwch.gwyt_project.ext

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide


fun ImageView.load(url: String?) {
    Glide.with(this).load(url).into(this)
}

fun ImageView.load(uri: Uri?) {
    Glide.with(this).load(uri).into(this)
}

fun ImageView.load(resId: Int) {
    Glide.with(this).load(resId).into(this)
}