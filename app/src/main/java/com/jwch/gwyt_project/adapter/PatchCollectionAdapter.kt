package com.jwch.gwyt_project.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.CollecPatchInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.db.DbUtil
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.model.DataEvent
import org.greenrobot.eventbus.EventBus

class PatchCollectionAdapter : BrvahAdapter<CollecPatchInfo>(R.layout.item_place_collection) {

    init {
        addChildClickViewIds(R.id.ivClear)
        addChildClickViewIds(R.id.imgSelect)
    }

    var selectMode = false

    override fun convert(helper: BaseViewHolder?, item: CollecPatchInfo?) {

        helper?.let {
            item?.apply {
                val imgSelect = it.getView<ImageView>(R.id.imgSelect)

                it.setText(R.id.tvName, name).setText(R.id.tvTime, time)
                    .setGone(R.id.ivClear, selectMode)
                    .setGone(R.id.imgSelect, !selectMode)

                imgSelect.setImageResource(
                    isSelect.getOne(
                        R.mipmap.icon_square_select,
                        R.mipmap.icon_square_unselect
                    )
                )
            }
        }
    }


    override fun setOnItemChildClick(v: View, position: Int) {
        super.setOnItemChildClick(v, position)
        val item = getItem(position)
        when (v.id) {
            R.id.ivClear -> {
                showNormalDialog(context!!, "是否取消收藏？") {
                    it.yes {
                        DbUtil.db.deleteCollectPatch(item)
                        EventBus.getDefault().post(DataEvent(DataEvent.CANCEL_COLLECT_PATCH))
                        EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_COLLECTION_DATA, 1))
                        remove(item)
                    }
                }
            }

            R.id.imgSelect -> {
                item.isSelect = !item.isSelect
                EventBus.getDefault().post(DataEvent(DataEvent.SELECT_NAVIGATION_LIST_CHANGE))
            }
        }
        notifyDataSetChanged()

    }
}