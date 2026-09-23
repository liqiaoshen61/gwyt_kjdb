package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.MapView
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.PrintUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewCoordinateInputPointDialogBinding
import com.jwch.gwyt_project.databinding.ViewLicenseInputDialogBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.isEmpty
import com.jwch.gwyt_project.ext.isShow
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.CollectionFragment
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.SoftUtil
import com.jwch.gwyt_project.util.TextFileReader
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus


//输入授权码
class LicenseInputDialog(context: Context,var isExpire :Boolean, var listener1: ActionListener) :
    JameniBaseDialog(context) {

    var mActivity: Activity? = null
    var softUtil: SoftUtil? = null

    var vb: ViewLicenseInputDialogBinding? = null
    private lateinit var textFileReader: TextFileReader


    companion object {
        const val ACTION_OK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_license_input_dialog, null)
        vb = ViewLicenseInputDialogBinding.bind(view)
        setContentView(vb!!.root)


        setCanceledOnTouchOutside(false)
        softUtil = SoftUtil()

        //没有过期 才能手动关闭窗口
        vb!!.imgClose.visiable(!isExpire)


        vb!!.tvOk.onClick {
            val str = vb!!.etCode.text.toString().self()
            if(str.isNotBlank()){
                listener1.onAction(str, ACTION_OK)
            }else{
                ToastUtils.show("输入不得为空")
            }
        }

        vb!!.imgClose.onClick {
            dismiss()
        }

        textFileReader = TextFileReader(context)
        val content = textFileReader.readExternalStorageFile("/sdcard/gwyt_license")
        if(!content.isNullOrBlank()){
            vb!!.etCode.setText(content)
        }
        
    }

    override fun onBackPressed() {

    }


}
