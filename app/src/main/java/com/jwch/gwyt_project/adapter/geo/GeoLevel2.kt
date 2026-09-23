package com.jwch.gwyt_project.adapter.geo

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.selfTempString
import com.jwch.gwyt_project.fragment.CollectionFragment
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.GeoCollectionModel

class GeoLevel2(override val itemViewType: Int, override val layoutId: Int, val listener: ActionListener) : BaseNodeProvider() {
    protected var dimen = 0

    init {
        addChildClickViewIds(R.id.imgSelect)
        addChildClickViewIds(R.id.imgMoveFolder)
        addChildClickViewIds(R.id.imgDelete)
        addChildClickViewIds(R.id.imgEdit)
    }


    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as GeoCollectionModel?

        helper.let {
            item?.apply {
                it.setText(R.id.tvName, "${markerInfo?.city}${markerInfo?.county}${markerInfo?.town}")
                    .setText(R.id.tvTime, "${TimeUtil.getDateToString(markerInfo?.checkTime.self(), Config.timeFormat1)}")
//                    .setVisible(R.id.imgMoveFolder, true)
                val imgSelect = it.getView<ImageView>(R.id.imgSelect)
                imgSelect.setImageResource(isSelect.getOne(R.mipmap.icon_square_select, R.mipmap.icon_square_unselect))
            }
        }

        if (dimen == 0) {
            dimen = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize5)
        }

        var llMarkerItem = helper.getView<LinearLayout>(R.id.llMarkerItem)
        llMarkerItem.setPadding(if (item?.markerInfo?.folderId == 0) dimen else dimen.times(4), 0, 0, 0)
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
//        getAdapter()!!.expandOrCollapse(position)
        val item = data as GeoCollectionModel
        item.isSelect = !item.isSelect
        listener.onAction(item, CollectionFragment.ACTION_DRAW)
        getAdapter()?.notifyDataSetChanged()

//        if (item.isSelect) { //如果是选择状态 在地图上绘制该标绘
//            EventBus.getDefault().post(MapEvent(MapEvent.CLICK_MARKER_RESULT_LIST_ITEM, item.markerInfo))
//        }

    }

    override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onChildClick(helper, view, data, position)
        val item = data as GeoCollectionModel
        when (view.id) {
            //选中
            R.id.imgSelect -> {
                //反选
                item.isSelect = !item.isSelect
                listener.onAction(item, CollectionFragment.ACTION_DRAW)
            }
            //删除
            R.id.imgDelete -> {
                listener.onAction(item, CollectionFragment.ACTION_DELETE)
            }
            //移动到相应文件夹
            R.id.imgMoveFolder -> {
                listener.onAction(item, CollectionFragment.ACTION_MOVE)
            }
            //编辑标绘
            R.id.imgEdit -> {
                listener.onAction(item, CollectionFragment.ACTION_EDIT)
            }
        }
        getAdapter()?.notifyDataSetChanged()
    }

}