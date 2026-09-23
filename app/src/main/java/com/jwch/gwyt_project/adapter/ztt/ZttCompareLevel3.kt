package com.jwch.gwyt_project.adapter.ztt

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.MapEvent
import org.greenrobot.eventbus.EventBus

class ZttCompareLevel3(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {
    var padding = 0
    var paddingLeft = 0

    init{
        addChildClickViewIds(R.id.ivLayerCheck)
    }

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val item = data as ZttItem

        if (padding == 0) {
            padding = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize8)
            paddingLeft = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize20)
        }

        helper.let {
            item.apply {
                helper.setText(R.id.id_treenode_label, name)
                    .setVisible(R.id.ivLayerCheck, true)
                    .setGone(R.id.ll_op, !isCheck)

                val id_treenode_icon = it.getView<ImageView>(R.id.id_treenode_icon)
                val ivLayerCheck = it.getView<ImageView>(R.id.ivLayerCheck)
                val bar = it.getView<SeekBar>(R.id.sb_ztt)
                bar.progress = progress

                id_treenode_icon.gone()
                (isCheck).yes{ ivLayerCheck.setImageResource(R.mipmap.icon_square_select)}
                    .no{ivLayerCheck.setImageResource(R.mipmap.icon_square_unselect)}

                bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        //拖拽结束
                        progress = seekBar.progress
                        setLayerOpacity(item)
                    }
                })


                val viewContent = helper.getView<LinearLayout>(R.id.itemContent2)
                viewContent.setPadding(paddingLeft.times(levelIndex), padding, padding, padding)
            }
        }


    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        val item = data as ZttItem
    }

    override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onChildClick(helper, view, data, position)
        val item = data as ZttItem
//        ("1type3data==" + item.themesInfo.toString()).printMsg()
        when(view.id){
            //选中
            R.id.ivLayerCheck -> {
                //反选
                item.isCheck = !item.isCheck
                //控制图层展示
                controlLayer(item)
            }

        }
        getAdapter()?.notifyDataSetChanged()
    }

    //控制图层展示
    private fun controlLayer(entity: ZttItem?) {
        //控制专题图展示
        EventBus.getDefault().post(MapEvent(MapEvent.CONTROL_COMPARE_LAYER, entity))
    }

    private fun setLayerOpacity(entity: ZttItem) {
        val info = entity.themesInfo
        if (CommonUtil.isNotNull(info)) {
            val layer = info?.tiledLayer
            val layer2 = info?.vectorTiledLayer

            if (CommonUtil.isNotNull(layer)) {
                layer?.opacity = entity.progress / 100f
            }

            if (CommonUtil.isNotNull(layer2)) {
                layer2?.opacity = entity.progress / 100f
            }

        }
    }

}