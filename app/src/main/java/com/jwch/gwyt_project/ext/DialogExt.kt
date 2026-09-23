package com.jwch.gwyt_project.ext

import android.content.Context
import com.jameni.jamenidialoglib.JameniDialog
import com.jameni.jamenidialoglib.dialog.ListDialog
import com.jameni.jamenidialoglib.i.NormalDialogListener
import com.jwch.gwyt_project.model.SelectionListModel
import com.jwch.gwyt_project.view.dialog.SingleSelectListDialog


fun showNormalDialog(context: Context, msg: String, rightText: String = "确定", leftText: String = "取消", block: (Boolean) -> Unit) {

    JameniDialog.Builder(context).setMsg(msg).setRightText(rightText).setLeftText(leftText).setNormalDialogListener(object : NormalDialogListener {
        override fun onLeftClick(obj: Any?, actionTag: Int) {
            block(false)
        }

        override fun onRightClick(obj: Any?, actionTag: Int) {
            block(true)
        }

    }).showNormalDialog()

}

fun showSingleDialog(context: Context, msg: String, btnText: String = "确定", block: (Any?, Int) -> Unit = { _, _ -> }) {
    JameniDialog.Builder(context)
        .setMsg(msg)
        .setSingleBtnText(btnText)
        .setSingleDialogListener { obj, actionTag -> block(obj, actionTag) }
        .showSingleDialog()
}


fun showSelectionDialog(context: Context, list: MutableList<SelectionListModel>, outsizeCancleAble: Boolean = true, block: (Any, Int) -> Unit) {
    val dialog = SingleSelectListDialog(context, list) { obj, position ->
        block(obj, position)
    }
    dialog.setCanceledOnTouchOutside(outsizeCancleAble)
    dialog.show()
}

fun showSelectionDialog(context: Context, strItems: String, outsizeCancleAble: Boolean = true, block: (Any, Int) -> Unit) {

    val list = mutableListOf<SelectionListModel>()
    val array = strItems.cutString(",")
    array.forEach { list.add(SelectionListModel(it)) }

    val dialog = SingleSelectListDialog(context, list) { obj, position ->
        block(obj, position)
    }
    dialog.setCanceledOnTouchOutside(outsizeCancleAble)
    dialog.show()

}