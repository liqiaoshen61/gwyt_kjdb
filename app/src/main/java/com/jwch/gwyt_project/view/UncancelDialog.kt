package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Window
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.UncancelDialogBinding

/**
 * 无法取消的dialog
 */
class UncancelDialog(context: Context) : JameniBaseDialog(context) {


    var vb: UncancelDialogBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.uncancel_dialog, null)
        vb = UncancelDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)
        initData()
        dialogFitScreen()

        vb!!.tv1.text ="此设备未授权\n${Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)}"

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