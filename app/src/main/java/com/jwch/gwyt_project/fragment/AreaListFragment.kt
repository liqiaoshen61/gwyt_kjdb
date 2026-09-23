package com.jwch.gwyt_project.fragment

import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.adapter.area.AreaAdapter
import com.jwch.gwyt_project.adapter.area.AreaItem
import com.jwch.gwyt_project.databinding.FragAreaListBinding
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus


/**
 * 行政区划--树形结构
 * 该页面已弃用
 */
class AreaListFragment : BaseFragment<FragAreaListBinding>() {

    //areaList 用于存放数据库查询到的数据
    var areaList :MutableList<DistrictsInfo>? = null
    //一级数据 县
    private var dataList : MutableList<BaseNode>? = null
    //二级数据 乡/镇
    private var dataList2 : MutableList<BaseNode>? = null
    //三级数据 村
    private var dataList3 : MutableList<BaseNode>? = null
    private var adapter: AreaAdapter? = null


    override fun initView() {
        initAreaList()
    }

    override fun initViewListener() {

        vb.ivAreaClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_AREA_FRAG))
        }
    }

    private fun initAreaList() {
        areaList = mutableListOf()
        dataList = mutableListOf()
        dataList2 = mutableListOf()
        dataList3 = mutableListOf()
        try {
            areaList = db.appDb.selector(DistrictsInfo::class.java).findAll()
            //行政区划数据的三个级别在同一张表里 先分别取出来存入 dataList、dataList2、dataList3
            for (item in areaList!!) {
                when (item.distLevel) {
                    3 -> {
                        val area = AreaItem(AreaItem.LEVEL1)
                        area.name = item.distName
                        area.id = item.distCode.toLong()
                        area.fid = item.distCode.substring(0, item.distCode.length - 3).toLong()
                        area.isExpanded = false
                        dataList?.add(area)
                    }

                    4 -> {
                        val area2 = AreaItem(AreaItem.LEVEL2)
                        area2.name = item.distName
                        area2.id = item.distCode.toLong()
                        area2.fid = item.distCode.substring(0, item.distCode.length - 3).toLong()
                        area2.isExpanded = false
                        dataList2?.add(area2)
                    }

                    5 -> {
                        val area3 = AreaItem(AreaItem.LEVEL3)
                        area3.name = item.distName
                        area3.id = item.distCode.toLong()
                        area3.fid = item.distCode.substring(0, item.distCode.length - 3).toLong()
                        area3.isExpanded = false
                        area3.x = item.centerX
                        area3.y = item.centerY
                        dataList3!!.add(area3)
                    }

                }
            }
            //将符合条件的dataList2数据存入dataList的childNode
            dataList?.forEach { it ->
                val baseNode2:  MutableList<BaseNode> = mutableListOf()
                val v1 = it as AreaItem
                dataList2?.forEach {
                    val v2 = it as AreaItem
                    (v1.id == v2.fid).yes {
                        baseNode2.add(v2)
                    }
                }
                v1.childNode = baseNode2
            }
            //将符合条件的dataList3数据存入dataList2的childNode
            dataList2?.forEach { it ->

                val baseNode3:  MutableList<BaseNode> = mutableListOf()
                val v2 = it as AreaItem
                dataList3?.forEach {
                    val v3 = it as AreaItem
                    (v2.id == v3.fid).yes {
                        baseNode3.add(v3)
                    }
                }
                v2.childNode = baseNode3
            }


            vb.lvArea.setLinearManager()
            adapter = AreaAdapter()
            vb.lvArea.adapter = adapter
            adapter?.setNewData(dataList)
        } catch (e: Exception) {
            print("行政区划 data error：" + e.message)
        }
    }


}