package com.jwch.gwyt_project.ext

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.viewpager.widget.ViewPager


fun TextView.onTextChange(block: (editable: Editable) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable) = block(editable)
        override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun EditText.onTextChange(block: (editable: Editable) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable) = block(editable)
        override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun ViewPager.onPageSelect(block: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            block(position)
        }
    })


    //拖动条进度监听
    fun SeekBar.onProgressChange(block: (Int, Boolean) -> Unit) {

        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = block(progress, fromUser)

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


}
