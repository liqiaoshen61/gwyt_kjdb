package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jameni.jamenidialoglib.dialog.JameniSimpleDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.PoltSpinnerAdapter
import com.jwch.gwyt_project.databinding.ViewChangeGeoFolderBinding
import com.jwch.gwyt_project.databinding.ViewMarkerTextDialogBinding
import com.qmuiteam.qmui.kotlin.onClick

/**
 * 改变标绘文件夹
 */

class ChangeFolderDialog(context: Context, listener: (Int) -> Unit) : JameniBaseDialog(context) {

    var vb: ViewChangeGeoFolderBinding? = null

    var mListener = listener

    private lateinit var adapter: PoltSpinnerAdapter

    init {
        this.context =context
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_change_geo_folder, null)
        vb = ViewChangeGeoFolderBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)

        adapter = PoltSpinnerAdapter(context)
        vb!!.spType.adapter = adapter

        //取消
        vb!!.tvCancle.onClick{
            dismiss()
        }

        vb!!.tvOk.onClick{
            val selectFolderId = adapter.list[vb!!.spType.selectedItemPosition].id
            mListener(selectFolderId)

            Toast.makeText(context, "移动成功", Toast.LENGTH_LONG).show()
//            EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_MARKER_DATA_AND_DRAW))
            dismiss()
        }
        dialogFitScreen()
    }

    private fun dialogFitScreen() {
        val util = GetWindowSize(context)
        val width = util.windowWidth / 2 * 1
        val height = util.windowHeight / 5 * 1
        vb!!.llDialog.layoutParams.width = width
        vb!!.llDialog.layoutParams.height = height
    }

}