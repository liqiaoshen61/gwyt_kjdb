package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.UncancelDialog2Binding
import com.jwch.gwyt_project.databinding.UncancelDialogBinding

/**
 * 无法取消的dialog
 */
class UncancelDialog2(context: Context) : JameniBaseDialog(context) {


    var vb: UncancelDialog2Binding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.uncancel_dialog2, null)
        vb = UncancelDialog2Binding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)
        initData()
        dialogFitScreen()

    }
    private fun dialogFitScreen() {
        val util = GetWindowSize(context)
        val width = util.windowWidth / 3 * 1
        vb!!.llDialog.layoutParams.width = width
    }


    fun initData() {
    }

    override fun onBackPressed() {}

}