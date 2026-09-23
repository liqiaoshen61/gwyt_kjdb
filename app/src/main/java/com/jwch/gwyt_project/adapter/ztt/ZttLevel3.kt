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
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import org.greenrobot.eventbus.EventBus


class ZttLevel3(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {
    var padding = 0
    var paddingLeft = 0

    init {
        addChildClickViewIds(R.id.ivLayerCheck)
        addChildClickViewIds(R.id.ivLayerOpactiy)
    }

    override fun convert(helper: BaseViewHolder, data: BaseNode) {

        if (padding == 0) {
            padding = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize8)
            paddingLeft = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize18)
        }

        val item = data as ZttItem

        helper.let {
            item.apply {
                var count = "（0）"
                if (dataCount > 0) {
                    count = "（$dataCount）"
                }

                //行政区划不统计数量
                if (name == "行政区划" || name!!.contains("影像")) {
                    count = ""
                }


                helper.setText(R.id.id_treenode_label, name + count).setVisible(R.id.ivLayerCheck, true).setGone(R.id.ll_op, !isCheck)

                val id_treenode_icon = it.getView<ImageView>(R.id.id_treenode_icon)
                val ivLayerCheck = it.getView<ImageView>(R.id.ivLayerCheck)
                val ivLayerOpactiy = it.getView<ImageView>(R.id.ivLayerOpactiy)
                val bar = it.getView<SeekBar>(R.id.sb_ztt)
                val iv_legend = it.getView<ImageView>(R.id.iv_legend)
                val ll_op = it.getView<LinearLayout>(R.id.ll_op)


                bar.progress = progress

                id_treenode_icon.gone()
                (isCheck).yes {
                    ivLayerCheck.setImageResource(R.mipmap.icon_square_select)
                    ivLayerOpactiy.visiable(!name!!.contains("影像"))
                }.no {
                    ivLayerCheck.setImageResource(R.mipmap.icon_square_unselect)
                    ivLayerOpactiy.gone()
                    showOpactiy = false
                }

                (showOpactiy).yes {
                    ivLayerOpactiy.setImageResource(R.mipmap.icon_layer_opacity_o)
                    ll_op.show()
                }.no {
                    ivLayerOpactiy.setImageResource(R.mipmap.icon_layer_opacity_c)
                    ll_op.gone()
                }



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



                when {
                    name.self().contains("乱建") -> {
                        iv_legend.setImageResource(R.mipmap.icon_luanjian)
                        iv_legend.show()
                    }

                    name.self().contains( "乱占") -> {
                        iv_legend.setImageResource(R.mipmap.icon_luanzhan)
                        iv_legend.show()
                    }

                    name.self().contains("乱采") -> {
                        iv_legend.setImageResource(R.mipmap.icon_luancai)
                        iv_legend.show()
                    }

                    name.self().contains("乱堆") -> {
                        iv_legend.setImageResource(R.mipmap.icon_luandui)
                        iv_legend.show()
                    }

                    name.self().contains("其他") -> {
                        iv_legend.setImageResource(R.mipmap.icon_qita)
                        iv_legend.show()
                    }
                    name.self().contains("水葫芦") -> {
                        iv_legend.setImageResource(R.mipmap.icon_shl)
                        iv_legend.show()
                    }
                    name.self().contains("水利部图斑") -> {
                        iv_legend.setImageResource(R.mipmap.icon_shuilibu)
                        iv_legend.show()
                    }
                    name.self().contains("自查自纠") -> {
                        iv_legend.setImageResource(R.mipmap.icon_zichazijiu)
                        iv_legend.show()
                    }


                    else -> {
                        iv_legend.gone()
                    }
                }

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
        when (view.id) {
            //选中
            R.id.ivLayerCheck -> {
                //反选
                item.isCheck = !item.isCheck
                //控制图层展示
                controlLayer(item)
            }
            R.id.ivLayerOpactiy -> {
                //反选
                item.showOpactiy = !item.showOpactiy
            }

        }
        getAdapter()?.notifyDataSetChanged()
    }

    //控制图层展示
    private fun controlLayer(entity: ZttItem?) {
        //控制专题图展示
        EventBus.getDefault().post(MapEvent(MapEvent.CONTROL_LAYER, entity))
        //展示图例
        EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LAYER_LEGEND, entity))
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