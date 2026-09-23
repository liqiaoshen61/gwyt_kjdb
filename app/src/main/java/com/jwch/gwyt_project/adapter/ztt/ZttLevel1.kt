package com.jwch.gwyt_project.adapter.ztt

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.MapEvent
import org.greenrobot.eventbus.EventBus

class ZttLevel1(override val itemViewType: Int, override val layoutId: Int) : BaseNodeProvider() {

    var padding = 0
    init {
        addChildClickViewIds(R.id.ivLayerCheck)
    }

    override fun convert(helper: BaseViewHolder, data: BaseNode) {

        if (padding == 0) {
            padding = context.resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize10)
        }

        val item = data as ZttItem

        helper.let {
            item.apply {

                var countStr = ""
                //行政区划不统计数量
                if (name == "基础数据" || name == "水电站") {
                    countStr = ""
                }else{
                    countStr = "（$dataCount）"
                }

                helper.setText(R.id.id_treenode_label, name+countStr)
                    .setImageResource(R.id.id_treenode_icon, if (isExpanded) R.mipmap.tree_ex else R.mipmap.tree_ec_right)

                val ivLayerCheck = it.getView<ImageView>(R.id.ivLayerCheck)
                (isCheck).yes { ivLayerCheck.setImageResource(R.mipmap.icon_square_select) }.no { ivLayerCheck.setImageResource(R.mipmap.icon_square_unselect) }

                val viewContent = helper.getView<LinearLayout>(R.id.itemContent)
                viewContent.setPadding(padding.times(levelIndex), padding, padding, padding)

                val childOne = childNode?.firstOrNull() as ZttItem?
                ivLayerCheck.visiable(childOne?.themesInfo != null)



            }
        }


    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        getAdapter()!!.expandOrCollapse(position)
    }

    override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onChildClick(helper, view, data, position)
        val item = data as ZttItem

        when (view.id) {
            //选中
            R.id.ivLayerCheck -> {
                //反选
                item.isCheck = !item.isCheck

                item.childNode?.forEach { child ->
                    child as ZttItem
                    if(child.isCheck != item.isCheck){
                        child.isCheck = item.isCheck
                        //控制图层展示
                        controlLayer(child)
                    }
                }

                getAdapter()?.notifyDataSetChanged()
            }

        }
//        getAdapter()?.notifyDataSetChanged()
    }

    //控制图层展示
    private fun controlLayer(entity: ZttItem?) {
        //控制专题图展示
        EventBus.getDefault().post(MapEvent(MapEvent.CONTROL_LAYER, entity))
        //展示图例
//        EventBus.getDefault().post(DataEvent(DataEvent.SHOW_LAYER_LEGEND, entity))
    }

}