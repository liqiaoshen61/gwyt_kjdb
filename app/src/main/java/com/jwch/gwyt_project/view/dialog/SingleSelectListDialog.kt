package com.jwch.gwyt_project.view.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.AdapterView
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jameni.jamenidialoglib.view.MaxListview
import com.jwch.gwyt_project.model.SelectionListModel


class SingleSelectListDialog : JameniBaseDialog, AdapterView.OnItemClickListener {

    lateinit var datalist: MutableList<SelectionListModel>
    lateinit var listview: MaxListview
    lateinit var adapter: SingleSelectListAdapter<SelectionListModel>
    lateinit var actionBlock: (obj: Any, postion: Int) -> Unit
    var listHeightScale = 0.7f

    constructor(context: Context, mList: MutableList<SelectionListModel>) : super(context, true) {
        this.datalist = mList
    }

    constructor(context: Context, mList: MutableList<SelectionListModel>, blcok: (Any, Int) -> Unit) : super(context, true) {
        this.datalist = mList
        this.actionBlock = blcok
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(com.jameni.jamenidialoglib.R.layout.view_jameni_dialog_list, null)
        setContentView(view)
        listview = view.findViewById(com.jameni.jamenidialoglib.R.id.dialogList)

        if (!::adapter.isInitialized) {
            adapter = SingleSelectListAdapter<SelectionListModel>(context)
            adapter.update(datalist)
        }
        listview.adapter = adapter
        listview.onItemClickListener = this

        if (1 > listHeightScale && listHeightScale > 0) {
            listview.setHeightScale(listHeightScale)
        }
        setCanceledOnTouchOutside(true)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (::actionBlock.isInitialized) {
            actionBlock(adapter.getItem(position)!!, position)
        }
        dismiss()
    }


    fun setListAdapter(mAdapter: SingleSelectListAdapter<SelectionListModel>) {
        this.adapter = mAdapter
        this.adapter.update(datalist)
    }

    fun setHeightScale(heightScale: Float) {
        this.listHeightScale = heightScale
    }
}