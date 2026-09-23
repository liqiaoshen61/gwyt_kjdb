package com.jwch.gwyt_project.adapter.dtc

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.BigImageActivity
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.util.Keys
import org.jetbrains.anko.startActivity

class DtcLevel3(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {
    protected var dimen = 0
    var datalist: MutableList<ImageModel> = mutableListOf()
    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as DtcItem?
        helper.let {
            item?.apply {
                helper.setText(R.id.id_treenode_label, name)
                    .setGone(R.id.id_treenode_icon, true)

                val icon = it.getView<ImageView>(R.id.id_treenode_icon)
                icon.hide()

                val parmas = icon.layoutParams as LinearLayout.LayoutParams
                val dimen  = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize10)
                parmas.leftMargin = dimen.times(index)
            }
        }

        if (dimen == 0) {
            dimen = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize5)
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        val item = data as DtcItem
        datalist.clear()
//
        datalist.add(ImageModel(item.path))
        AppContext.map[Keys.IMG_MODEL_LIST] = datalist
        AppContext.map[Keys.INDEX] = position
        AppContext.map[Keys.IMAGE_DES_VISIABLE] = false
        context.startActivity<BigImageActivity>()
    }
}