package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.CompoundButton
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewUnitChoiceDialogBinding
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.util.CaculationUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

/**
 * 测量的面积单位选择dialog
 */

class UnitChooiceDialog(context: Context, var actionListener: ActionListener) :
    JameniBaseDialog(context) {


    var unitType = CaculationUtil.UNIT_DEFAULT

    var vb: ViewUnitChoiceDialogBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_unit_choice_dialog, null)
        vb = ViewUnitChoiceDialogBinding.bind(view)
        setContentView(vb!!.root)

        vb!!.cbUnitType.setSelection(0)

        initViews()
        initDialogSize()
    }

    fun initDialogSize() {
        val windowSize = GetWindowSize(context)
        //设置宽高
        val width = windowSize.windowWidth * 45 / 100
        val height = windowSize.windowHeight * 30 / 100
        vb!!.llDialog.layoutParams.width = width
        vb!!.llDialog.layoutParams.height = height
    }

    fun initViews() {
        setCanceledOnTouchOutside(false)

        vb!!.imgClose.onClick {
            dismiss()
        }

        //确定
        vb!!.tvOk.onClick {

            actionListener.onAction(null, unitType)
            vb!!.tvCancel.performClick()
        }

        //取消
        vb!!.tvCancel.onClick {
//            EventBus.getDefault().post(DataEvent(DataEvent.REFRESH_MARKER))
            dismiss()
        }

        vb!!.cbUnitType.selectionActionBlock2 = { pos, item ->
            when(item.value){
                "默认" ->{
                    unitType = CaculationUtil.UNIT_DEFAULT
                }
                "平方米" ->{
                    unitType = CaculationUtil.UNIT_M2
                }
                "平方千米" ->{
                    unitType = CaculationUtil.UNIT_KM2
                }
                "亩" ->{
                    unitType = CaculationUtil.UNIT_MU
                }


            }
        }
    }


}
