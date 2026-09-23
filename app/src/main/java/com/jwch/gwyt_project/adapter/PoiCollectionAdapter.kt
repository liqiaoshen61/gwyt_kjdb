package com.jwch.gwyt_project.adapter

import android.view.View
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.Info.CollectPoiInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.db.DbUtil
import org.greenrobot.eventbus.EventBus

class PoiCollectionAdapter : BrvahAdapter<CollectPoiInfo>(R.layout.item_place_collection) {

    init {
        addChildClickViewIds(R.id.ivClear)
    }

    override fun convert(helper: BaseViewHolder?, item: CollectPoiInfo?) {

        helper?.let {
            item?.apply {
                it.setText(R.id.tvName, name).setText(R.id.tvTime, time)
            }
        }
    }

    override fun setOnItemChildClick(v: View, position: Int) {
        super.setOnItemChildClick(v, position)

        showNormalDialog(context!!, "是否确定删除该图斑信息？") {
            it.yes {
                val item = getItem(position)
                DbUtil.db.deleteCollectPoi(item)
                remove(item)
                notifyDataSetChanged()

                EventBus.getDefault().post(MapEvent(MapEvent.REMOVE_ONE_COLLECT_GRAPHICS, item.linkId))
            }
        }
    }
}