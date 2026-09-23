package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.NetDialogBinding

/**
 * 连上网络提示
 */
class NetDialog(context: Context) : JameniBaseDialog(context) {


    var vb: NetDialogBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.net_dialog, null)
        vb = NetDialogBinding.bind(view)
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