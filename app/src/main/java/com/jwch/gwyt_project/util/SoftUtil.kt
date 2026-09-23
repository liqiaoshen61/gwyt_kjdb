package com.jwch.gwyt_project.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

class SoftUtil {

    fun hideKeyboard(activity: Activity?) {
        activity?.run {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive && currentFocus != null && currentFocus?.windowToken != null) {
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }


    fun hideKeyboard2(activity: Activity?) {
        activity?.run {
        val view =  window.peekDecorView()
            view?.let {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken,0)
            }
        }
    }


}