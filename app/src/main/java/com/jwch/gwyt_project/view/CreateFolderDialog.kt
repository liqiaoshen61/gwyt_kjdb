package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewCreateFolderDialogBinding
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.db.DbUtil
import com.qmuiteam.qmui.kotlin.onClick

/**
 * 新建文件夹对话框
 */
class CreateFolderDialog(context: Context, var listener1: ActionListener) : JameniBaseDialog(context) {


    var vb: ViewCreateFolderDialogBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_create_folder_dialog, null)
        vb = ViewCreateFolderDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)
        //取消
        vb!!.tvCancel.onClick {
            vb!!.etFolder.setText("")
            dismiss()
        }
        //确定
        vb!!.tvOk.onClick {
            val folderName =  vb!!.etFolder.text.toString()

            if (folderName.length > 10) {
                tip(context, "文件夹名称不要超过10个字")
                return@onClick
            }

            val result = DbUtil.db.craeteFolder(folderName)
            when (result) {
                0 -> {
                    tip(context, "文件夹添加成功")

                    val folder = DbUtil.db.queryLastFolder()
                    "${folder?.toJson()}".printMsg()
                    listener1.onAction(folder, 0)
                    vb!!.tvCancel.performClick()
                }
                -1 -> tip(context, "请输入文件夹名称")
                -2 -> tip(context, "该文件夹名称已存在")
            }
//            "${DbUtil.db.queryAllFolderList()?.toJson()}".printMsg()

//            val folder = GraphicInfo()
//            val folderName = etFolder.text.toString()
//            if (folderName.isNotBlank()) {
//                val result: Int = folder.createFolder(folderName)
//                when (result) {
//                    0 -> {
//                        tip(context, "文件夹添加成功")
//                        listener.onAction(folder, 0)
//                        tvCancel.performClick()
//                    }
//                    -1 -> {
//                        tip(context, "该文件夹名称已存在")
//                        Toast.makeText(context, "该文件夹已存在", Toast.LENGTH_LONG).show()
//                    }
//                    -2 -> tip(context, "添加失败")
//                }
//            } else {
//                tip(context, "请输入文件夹名称")
//            }
        }
    }

}
