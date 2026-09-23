package com.jwch.gwyt_project.fragment

import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.jamenilistlib.i.ItemChildViewClickListener
import com.jwch.gwyt_project.adapter.overlayanalysis.OverlayAnalysisAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragOverlayAnalysisBinding
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.InterSectionModel
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.CaculationUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 图层叠加分析页面
 */

class OverlayAnalysisFragment : BaseFragment<FragOverlayAnalysisBinding>(), ItemChildViewClickListener {

    //zztList 用于存放数据库查询到的专题图数据
    var datalist = mutableListOf<InterSectionModel>()
    var datalist2 = mutableListOf<BaseNode>()
    private lateinit var adapter: OverlayAnalysisAdapter
    val caculationUtil = CaculationUtil()

    override fun initView() {

        EventBus.getDefault().register(this)

        vb.lvMain.setLinearManager()
        adapter = OverlayAnalysisAdapter()
        vb.lvMain.adapter = adapter
        vb.lvMain.disableLoadMoreIfNotFullPage()
        adapter.setNewData(datalist2)

    }

    override fun initViewListener() {

        vb.imgClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_OVERLY_ANALYSIS))
        }
    }

    override fun onItemChildViewClick(viewId: Int, position: Int) {


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: DataEvent) {
        when (event.actionType) {
            DataEvent.SHOW_OVERLY_ANALYSIS -> {

                event.data?.let {
                    datalist = it as MutableList<InterSectionModel>
                    datalist.toJson()
                    val templist = mutableListOf<String>()
                    val tempdatalist = mutableListOf<BaseNode>()
                    datalist.forEach { item ->

                        val isNotContain = templist.none { s ->
                            item.layerName == s
                        }

                        isNotContain.yes {
                            templist.add(item.layerName)
                            val data = InterSectionModel(InterSectionModel.LEVEL1, item.layerName)
                            data.isExpanded = false
                            data.listType = 0
                            tempdatalist.add(data)
                        }

                    }

                    tempdatalist.forEach { node ->

                        val item = node as InterSectionModel

                        datalist.forEach { item2 ->

                            if (item2.layerName == item.layerName) {
                                val data = InterSectionModel(InterSectionModel.LEVEL2, item.layerName)
                                data.areaSize = item2.areaSize
                                data.attr = item2.attr
                                data.geometry = item2.geometry
                                data.geometryName = item2.geometryName
                                item.addChildNode(data)
                            }
                        }
                    }

                    datalist2.clear()
                    datalist2.addAll(tempdatalist)
                    datalist2.toJson().printMsg()

//                    lvMain.update(datalist2)

                    addGroupData()

                    adapter.setNewData(datalist2)
                }
            }
            DataEvent.SEND_ATTR -> {

                var idFromEventBus = 1
                var idFromList = -1

                event.data?.let {
                    val attr = it as MutableMap<*, *>
                    if(attr.containsKey(Config.primaryKey)){
                        idFromEventBus = attr.get(Config.primaryKey) as Int
                    }else if (attr.containsKey("FID")){
                        idFromEventBus = attr.get("FID") as Int
                    }


                    datalist2.forEach {
                        it.childNode?.forEachIndexed { index, data ->
                            data as InterSectionModel
                            data.isSecletd = false
                            if(data.attr?.contains(Config.primaryKey) == true){
                                idFromList = data.attr!!.get(Config.primaryKey) as Int
                            }else if(data.attr?.contains("FID") == true){
                                idFromList = data.attr!!.get("FID") as Int
                            }

                            if(idFromEventBus == idFromList){
                                data.isSecletd = true

                                vb.lvMain.recycleview.scrollToPosition(index)
                                adapter.notifyDataSetChanged()


                                EventBus.getDefault().post(MapEvent(MapEvent.DRAW_ANALYSIS_RESULT, data))

                            }


                        }
                    }
                }
            }
            DataEvent.SELECT_ITEM ->{
                datalist2.forEach {
                    it.childNode?.forEach {
                        it as InterSectionModel
                        it.isSecletd = false
                    }
                }
                event.data?.let {
                    it as InterSectionModel
                    it.isSecletd = true
                }
                adapter.notifyDataSetChanged()
            }

        }
    }

    fun addGroupData(){
        val map = datalist.groupBy { it.layerName }

        "分组结果:::${map.toJson()}".printMsg()
        val datalistLevel1 = mutableListOf<BaseNode>()

        map.forEach { key, list ->

            if (isNotEmpty(key) && matchList(list)) {

                val data = InterSectionModel(InterSectionModel.LEVEL1, key)
                data.isExpanded = false
                data.listType = 1

                datalistLevel1.add(data)

                var totalSize: Double = 0.0
                //添加子项和计算总面积
                list.forEach {

                    totalSize += it.areaSizeValue
                }


                //按照图斑类型 分组  同一个图斑组成一个item，并且计算同类型的图斑面积总和
                val typeMap = list.groupBy { it.geometryName }

                typeMap.forEach { typeKey, typeList ->

                    if (isNotNull(typeKey) && matchList(typeList)) {
                        val childData = InterSectionModel(InterSectionModel.LEVEL2, key)
                        childData.geometryName = typeKey
                        var childTotalSize: Double = 0.0
                        typeList.forEach {
                            childTotalSize += it.areaSizeValue
                        }
                        childData.childTotalSizeValue = childTotalSize
                        childData.childTotalSize = caculationUtil.getAreaStringUnitMu(childTotalSize,true)
                        childData.areaSize = childData.childTotalSize
                        data.addChildNode(childData)
                    }

                }


                val strTotalSize = caculationUtil.getAreaStringUnit(totalSize,CaculationUtil.UNIT_DEFAULT,true)
                "$key  total size  $totalSize   strTotalSize  $strTotalSize ".printMsg()
                data.childTotalSize = strTotalSize
                data.childTotalSizeValue = totalSize

            }
        }

        "level1  datalist ${datalistLevel1.toJson()}".printMsg()

        datalist2.addAll(datalistLevel1)
        datalist2.toJson().printMsg()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}