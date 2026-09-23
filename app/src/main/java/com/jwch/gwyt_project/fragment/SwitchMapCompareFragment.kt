package com.jwch.gwyt_project.fragment

import com.chad.library.adapter.base.entity.node.BaseNode
import com.jwch.gwyt_project.Info.CatTypeInfo

import com.jwch.gwyt_project.adapter.ztt.ZttCompareAdapter
import com.jwch.gwyt_project.adapter.ztt.ZttItem
import com.jwch.gwyt_project.databinding.FragSwitchMapBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.db.DbUtil
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 切换地图图层 对比地图
 */

class SwitchMapCompareFragment : BaseFragment<FragSwitchMapBinding>() {

    var dataListZtt: MutableList<BaseNode> = mutableListOf()
    private lateinit var adapterZtt: ZttCompareAdapter

    override fun initView() {
        //专题图
        //延迟8秒加载
        GlobalScope.launch(Dispatchers.Main) {
            delay(8 * 1000)
            "loader layer ".printMsg()
            initZttLayerData()

        }

        //只要专题图列表，不需要地图册
        vb.rgSwitch.gone()
        EventBus.getDefault().register(this)
    }

    override fun initViewListener() {

        vb.ivMapsClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_LAYER_COMPARE))
        }
        vb.llClearData.onClick {
            adapterZtt.clearData() //清空选中的数据
            EventBus.getDefault().post(MapEvent(MapEvent.CLEAR_COMPARE_OPERATION_LAYERS))
        }

    }


    /**
     * 初始化专题图列表数据
     */
    private fun initZttLayerData() {

        val themeTypeId = DbUtil.db.queryThemeMapTypeId()
        var listLevel = DbUtil.db.queryCatTypeListLevel(themeTypeId)
        queryList(listLevel, 0)
//        listLevel.toJson().printMsg()
        arrangeTypeListData(null, listLevel!!)

        arrangeZttListData(dataListZtt)

        initZttList()
    }

    /**
     * 通过分类数据获取到该分类下的专题图层数据
     */
    private fun arrangeZttListData(list: MutableList<BaseNode>) {

        list.forEach {
            val data = it as ZttItem

            val list = DbUtil.db.queryThemesByTypeId(data.id.self())
            val newThemeItems = mutableListOf<BaseNode>()
            matchList(list).yes {
                //查出来有数据
                list!!.forEach { themeItem ->
                    val themeInfoItem = ZttItem(themeItem, data)
                    newThemeItems.add(themeInfoItem)
                }
            }
            // 按 JSON 顺序排列 childNode：
            // 1. 基础主题（prepend）  2. 子分类  3. 延迟注入的 region 直连主题（append）
            if (newThemeItems.isNotEmpty() || matchList(data.childNode)) {
                val savedCategories = data.childNode?.toList() ?: emptyList()
                data.childNode = mutableListOf()
                data.childNode!!.addAll(newThemeItems)
                data.childNode!!.addAll(savedCategories)
                // 延迟注入的 region 直连 themes 追加到末尾
                val deferredThemes = DbUtil.db.queryInjectedDirectThemes(data.id.self())
                matchList(deferredThemes).yes {
                    deferredThemes!!.forEach { dt ->
                        data.childNode!!.add(ZttItem(dt, data))
                    }
                }
            }

            matchList(data.childNode).yes { arrangeZttListData(data.childNode!!) }
        }
    }

    /**
     * 整理分类列表数据
     */
    private fun arrangeTypeListData(itemData: ZttItem?, list: MutableList<CatTypeInfo>) {

        list.forEach {
            val item = ZttItem(it)

            if (itemData == null) {
                dataListZtt.add(item)
            } else {
                itemData?.addChildNode(item)
            }
            it.isHasNext.yes {
                arrangeTypeListData(item, it.nextList)
            }
        }
    }

    /**
     * 查询分类列表数据
     */
    private fun queryList(list: MutableList<CatTypeInfo>?, index: Int) {

        matchList(list).yes {
            list!!.forEach {

                it.levelIndex = index
                val themeId = it.id
                val childList = DbUtil.db.queryCatTypeListLevel(themeId)
                it.nextList = childList
                it.isHasNext = matchList(childList)

                val nextIndex = it.levelIndex.plus(1)
                queryList(childList, nextIndex)

            }
        }


    }


    private fun initZttList() {
        vb.lvZtt.setLinearManager()
        adapterZtt = ZttCompareAdapter()
        vb.lvZtt.adapter = adapterZtt
        vb.lvZtt.disableLoadMoreIfNotFullPage()
        adapterZtt.setNewData(dataListZtt)
    }


    //操作地图
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: DataEvent) {
        when (event.actionType) {
            //清空所有专题图图层
            DataEvent.CLEAR_ALL_COMPARE_ZZT_LAYER -> {
                //清空所有选中的专题图
                vb.llClearData.performClick()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}