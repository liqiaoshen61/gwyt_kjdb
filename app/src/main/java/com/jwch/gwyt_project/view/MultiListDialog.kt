package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import com.jameni.allutillib.common.CommonUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.MultiSelectionAdapter
import com.jwch.gwyt_project.databinding.ViewMultiDialogListBinding
import com.jwch.gwyt_project.model.SelectionListModel
import com.qmuiteam.qmui.kotlin.onClick


class MultiListDialog(
    context: Context,
    list: MutableList<SelectionListModel>,
    mAdapter: MultiSelectionAdapter<SelectionListModel>,
    isCenter: Boolean = false,
    block: (String, MutableList<SelectionListModel>) -> Unit = { _, _ -> }
) : JameniBaseDialog(context, isCenter), AdapterView.OnItemClickListener {

    var vb: ViewMultiDialogListBinding? = null
    val actionBlock = block
    val datalist = list
    var adapter = mAdapter
    val selectList = mutableListOf<SelectionListModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_multi_dialog_list, null)

        vb = ViewMultiDialogListBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(true)

        vb!!.dialogList.adapter = adapter
        adapter.update(datalist)
        vb!!.dialogList.onItemClickListener = this


        vb!!.tvOk.onClick {

            val list = datalist.filter {
                it.select
            }.toMutableList()


            if (CommonUtil.matchList(list)) {

                var sb = StringBuffer()
                list.forEachIndexed { index, selectionListModel ->

                    sb.append(selectionListModel.value)
                    if (index != list.size - 1) {
                        sb.append(",")
                    }
                }
                actionBlock(sb.toString(), list)
                dismiss()
            } else {

                Toast.makeText(context, "至少选择一项", Toast.LENGTH_SHORT).show()
            }

        }

        if (isCenter) {
        } else {
            window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window!!.setGravity(Gravity.BOTTOM or Gravity.CENTER)
        }

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val item = adapter.getItem(position) as SelectionListModel
        item.select = !item.select
        adapter.update(datalist)
    }


}
