package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.LicenseExpiredDialogBinding

/**
 * 授权失败弹窗
 */
class LicenseExpiredDialog(context: Context, private val message: String = "授权失败") : JameniBaseDialog(context) {

    private var vb: LicenseExpiredDialogBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.license_expired_dialog, null)
        vb = LicenseExpiredDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)
        setCancelable(false)
        dialogFitScreen()

        vb?.tvMessage?.text = message
    }

    private fun dialogFitScreen() {
        val util = GetWindowSize(context)
        val width = util.windowWidth / 3 * 1
        vb?.llDialog?.layoutParams?.width = width
    }

    override fun onBackPressed() {
        // 禁止返回键关闭
    }
}
