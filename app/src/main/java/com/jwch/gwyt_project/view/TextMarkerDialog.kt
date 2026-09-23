package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.jameni.allutillib.common.SoftKey
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewMarkerTextDialogBinding
import com.jwch.gwyt_project.ext.checkEmpty
import com.qmuiteam.qmui.kotlin.onClick

class TextMarkerDialog(context: Context, block: (String) -> Unit = {}) : JameniBaseDialog(context) {


    var vb: ViewMarkerTextDialogBinding? = null
    val actionBlock = block


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_marker_text_dialog, null)
        vb = ViewMarkerTextDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)


        vb!!.tvOk.onClick {
            var result = vb!!.etText.checkEmpty(vb!!.etText.hint.toString()) ?: return@onClick
            SoftKey.closeSoftKeyboard(vb!!.etText, context)
            actionBlock(result)
            vb!!.tvCancle.performClick()

        }
        vb!!.tvCancle.onClick {
            dismiss()
        }

    }




}
