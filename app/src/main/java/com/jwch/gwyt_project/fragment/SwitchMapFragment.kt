package com.jwch.gwyt_project.fragment

import android.graphics.Typeface
import android.widget.CompoundButton
import com.chad.library.adapter.base.entity.node.BaseNode
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.loadable.LoadStatus
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenilistlib.i.ItemChildViewClickListener
import com.jwch.gwyt_project.Info.CatType2Info
import com.jwch.gwyt_project.Info.CatTypeInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.YxtAdapter
import com.jwch.gwyt_project.adapter.dtc.DtcAdapter
import com.jwch.gwyt_project.adapter.dtc.DtcItem
import com.jwch.gwyt_project.adapter.ztt.ZttAdapter
import com.jwch.gwyt_project.adapter.ztt.ZttItem
import com.jwch.gwyt_project.common.Tools
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragSwitchMapBinding
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.ImageLayerItem
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.model.SelectionListModel
import com.jwch.gwyt_project.util.FunctionControlUtil
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import kotlin.collections.isNotEmpty


/**
 * 切换地图图层
 */
class SwitchMapFragment : BaseFragment<FragSwitchMapBinding>(),
    CompoundButton.OnCheckedChangeListener, ItemChildViewClickListener {

    //zztList 用于存放数据库查询到的专题图数据
    var zztList: MutableList<CatType2Info>? = null
    var dataListZtt: MutableList<BaseNode> = mutableListOf()
    private lateinit var adapterZtt: ZttAdapter

    private var dataListYxt: MutableList<ImageLayerItem>? = null
    private lateinit var adapterYxt: YxtAdapter

    private lateinit var dataListDtc: MutableList<BaseNode>
    private lateinit var adapterDtc: DtcAdapter


    var dataQueue = mutableListOf<ZttItem>()
    var zttData = mutableListOf<ZttItem>()

    var eventStatusSelectionList = mutableListOf<SelectionListModel>()
    var eventStatusFilterParams = ""
    var isQuestionFilterSelectionList = mutableListOf<SelectionListModel>()
    var isQuestionFilterParams = ""

    var definition = ""

    private var loadGeojsonFragment: LoadGeojsonFragment? = null
    private var loadShpFragment: LoadShpFragment? = null

    override fun initView() {
        vb.rgSwitch.visiable(FunctionControlUtil.instances.MODULE_LAYERS_ALBUM) //因此地图册

        // 数据资源 / 外部导入 tab 控制
        val showDataResource = FunctionControlUtil.instances.TAB_DATA_RESOURCE
        val showExternalImport = FunctionControlUtil.instances.TAB_EXTERNAL_IMPORT
        val showShpImport = FunctionControlUtil.instances.TAB_IMPORT_SHP
        val showAnyExternal = showExternalImport || showShpImport

        // 控制顶部 Tab 可见性
        vb.rbDataResource.visiable(showDataResource)
        vb.rbExternalImport.visiable(showAnyExternal)

        val visibleTabCount = listOf(showDataResource, showAnyExternal).count { it }

        // 二级 sub-tab: geojson / shp
        val showSubTabs = showExternalImport && showShpImport
        vb.rgImportSubTab.visiable(showSubTabs)
        vb.rbSubGeojson.visiable(showExternalImport)
        vb.rbSubShp.visiable(showShpImport)

        if (visibleTabCount > 1) {
            vb.rgTopTab.show()
            // 预创建 geojson fragment，避免首次切换到"外部导入"时列表为空
            if (showAnyExternal) {
                createGeojsonFragment()
                if (showShpImport) createShpFragment()
            }
        } else if (showDataResource) {
            vb.rgTopTab.gone()
            vb.rbDataResource.isChecked = true
            vb.llDataResource.show()
        } else if (showAnyExternal) {
            vb.rgTopTab.gone()
            vb.llDataResource.gone()
            vb.llExternalImportPanel.show()
            if (!showSubTabs) {
                // 只有一个子类型时直接显示对应内容
                if (showExternalImport) {
                    vb.flExternalImport.visiable(true)
                    createGeojsonFragment()
                } else {
                    vb.flShpImport.visiable(true)
                    createShpFragment()
                }
            } else {
                vb.flExternalImport.visiable(true)
                createGeojsonFragment()
            }
        } else {
            vb.rgTopTab.gone()
        }

        initSelectionList()
        //专题图
        initZttLayerData()
        //地图册
        initDtcLayerData()
        initDtcList()

        vb.rbZtt.setOnCheckedChangeListener(this)
        vb.rbYxt.setOnCheckedChangeListener(this)
        vb.rbDtc.setOnCheckedChangeListener(this)
        vb.rbDataResource.setOnCheckedChangeListener(this)
        vb.rbExternalImport.setOnCheckedChangeListener(this)
        vb.rbSubGeojson.setOnCheckedChangeListener(this)
        vb.rbSubShp.setOnCheckedChangeListener(this)

        // 二级 tab 初始加粗
        if (showSubTabs) {
            vb.rbSubGeojson.setTypeface(null, Typeface.BOLD)
        }

        EventBus.getDefault().register(this)

        vb.tvSwitchMapTitle.onClick {

        }
    }

    fun initSelectionList(){
        eventStatusSelectionList.add(SelectionListModel("","全部"))
        eventStatusSelectionList.add(SelectionListModel("未办结","未办结"))
        eventStatusSelectionList.add(SelectionListModel("已办结","已办结"))


        isQuestionFilterSelectionList.add(SelectionListModel("","全部"))
        isQuestionFilterSelectionList.add(SelectionListModel("是问题","是问题"))
        isQuestionFilterSelectionList.add(SelectionListModel("不是问题","不是问题"))
        isQuestionFilterSelectionList.add(SelectionListModel("待复核","待复核"))
    }

    override fun initViewListener() {

        vb.ivMapsClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_SWITCH_MAPS))
        }
        vb.llClearData.onClick {
            adapterZtt.clearData() //清空选中的数据
            EventBus.getDefault().post(MapEvent(MapEvent.CLEAR_OPERATION_LAYERS))
        }

        //办结情况筛选
        vb.tvEventStatusFilter.onClick {
            showSelectionDialog(requireContext(), eventStatusSelectionList) { Any, i ->
                val item = Any as SelectionListModel
                eventStatusFilterParams = item.key.self()
                if(eventStatusFilterParams.isBlank()){
                    vb.tvEventStatusFilter.text = "事件状态"

                }else{
                    vb.tvEventStatusFilter.text = item.value
                }


                doFilterData()
            }
        }

        //是否问题筛选
        vb.tvIsQuestionFilter.onClick {
            showSelectionDialog(requireContext(), isQuestionFilterSelectionList) { Any, i ->
                val item = Any as SelectionListModel
                isQuestionFilterParams = item.key.self()
                if(isQuestionFilterParams.isBlank()){
                    vb.tvIsQuestionFilter.text = "是否问题"
                }else{
                    vb.tvIsQuestionFilter.text = item.value
                }
                doFilterData()
            }
        }


    }

    fun doFilterData(){

        definition = ""

        if (eventStatusFilterParams.isNotBlank()) {
            definition ="事件状 = '${eventStatusFilterParams}'"
        }


        if ((isQuestionFilterParams.isNotBlank())) {
            if (definition.isNotBlank()) {
                definition += " and 是否问  = '${isQuestionFilterParams}'"
            } else {
                definition = "是否问  = '${isQuestionFilterParams}'"
            }
        }

        "definition=== $definition".printMsg()


        val filterList = zttData.filter {
            val name = it.themesInfo?.themeName.self()
            !name.contains("影像")
                    && !name.contains("河管线")
                    && !name.contains("河道")
                    && !name.contains("河流")
                    && !name.contains("行政区划")
                    && !name.contains("厂房")
                    && !name.contains("拦河建筑")
                    && !name.contains("水库")
        }

        filterList.forEach {
            it.themesInfo?.layer?.definitionExpression = definition

        }


        when(vb.tvEventStatusFilter.text){
            "事件状态" ->{ EventBus.getDefault().postSticky(DataEvent(DataEvent.LAYER_FILTER_REFRESH, 0, eventStatusFilterParams))}
            "未办结" ->{EventBus.getDefault().postSticky(DataEvent(DataEvent.LAYER_FILTER_REFRESH, 1, eventStatusFilterParams))}
            "已办结" ->{EventBus.getDefault().postSticky(DataEvent(DataEvent.LAYER_FILTER_REFRESH, 2, eventStatusFilterParams))}
        }


        GlobalScope.launch(Dispatchers.Main) {
            dataQueue.addAll(filterList)
            if (matchList(dataQueue)) {
                loadThemeDataCount(dataQueue.last())
            }
        }

    }

    /**
     * 初始化专题图列表数据
     */
    private fun initZttLayerData() {

        val themeTypeId = db.queryThemeMapTypeId()//专题类型id
        var listLevel = db.queryCatTypeListLevel(themeTypeId)

//        得到数据、处理数据
        queryList(listLevel, 0)//尾递归
//        listLevel.toJson().printMsg()
        arrangeTypeListData(null, listLevel!!)
        arrangeZttListData(dataListZtt)


        EventBus.getDefault().postSticky(DataEvent(DataEvent.ZTT_DATA_TO_AREA_STAT, zttData))
        EventBus.getDefault().postSticky(DataEvent(DataEvent.ZTT_DATA_TO_BUFFER_ANALYSIS, zttData))
        "开始时间  ${TimeUtil.getCurrentStamp()}".printMsg()

        GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            if (matchList(dataQueue)) {
                loadThemeDataCount(dataQueue.last())
            }
        }


        //展示数据
        initZttList()

    }

    private fun loadThemeDataCount(item:ZttItem) {
        item.queryThemeDataCount(eventStatusFilterParams, isQuestionFilterParams) { success->
            success.yes {
                if(CommonUtil.matchList(dataQueue)){
                    dataQueue.removeLast()
                    if (dataQueue.size > 0) {
                        loadThemeDataCount(dataQueue.last())
                    } else {
                        "结束时间  ${TimeUtil.getCurrentStamp()}".printMsg()

                        calculateCounts(dataListZtt)

                        adapterZtt.notifyDataSetChanged()
                    }
                }


            }.no {
                "${item.name} 没有gdb数据".printMsg()
            }
        }
    }

    fun <T> List<T>.moveToEnd(predicate: (T) -> Boolean) =
        sortedBy { predicate(it) }
    /**
     * 尾递归 方法
     * 通过分类数据获取到该分类下的专题图层数据
     */
    private fun arrangeZttListData(list: MutableList<BaseNode>) {

        list.forEach {
            val data = it as ZttItem


            var list = db.queryThemesByTypeId(data.id.self())
            val newThemeItems = mutableListOf<BaseNode>()
            matchList(list).yes {
                list = list?.moveToEnd { it.themeName == "2021年以来已许可涉河建设项目" } as MutableList
                //查出来有数据
                list?.forEach { themeItem ->

                    //影像底图专题：若本地找不到对应的tpk文件夹或.tpk文件，则隐藏该图层
                    if (themeItem.themeName.startsWith("影像底图")) {
                        val tpkPath = Tools.getTpkFilePath(Config.THEMEDATA_TPK_PATH, themeItem.themeName)
                        if (tpkPath.isEmpty()) return@forEach
                    }

                    val themeInfoItem = ZttItem(themeItem, data)
                    newThemeItems.add(themeInfoItem)
                    dataQueue.add(themeInfoItem)
                    zttData.add(themeInfoItem)

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
                val deferredThemes = db.queryInjectedDirectThemes(data.id.self())
                matchList(deferredThemes).yes {
                    deferredThemes!!.forEach { dt ->
                        val deferredItem = ZttItem(dt, data)
                        data.childNode!!.add(deferredItem)
                        dataQueue.add(deferredItem)
                        zttData.add(deferredItem)
                    }
                }
            }
            matchList(data.childNode).yes { arrangeZttListData(data.childNode!!) }
        }
    }


    /**
     * 尾递归 方法
     * 整理分类列表数据--把数据变成列表能用的数据
     */
    private fun arrangeTypeListData(itemData: ZttItem?, list: MutableList<CatTypeInfo>) {

        list.forEach {
            val item = ZttItem(it)
            //只有省级级用户才能查看[督办]和[区县自查]数据
            if(Config.userLevel > Config.USER_LEVEL_PROVINCE){
                if(item.name.self().contains("督办事件")
                    || item.name.self().contains("县区自查")){
                    return@forEach
                }
            }

            if (itemData == null) {
                item.isExpanded= true
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
     * 尾递归 方法
     * 查询分类列表数据
     */
    private fun queryList(list: MutableList<CatTypeInfo>?, index: Int) {

        matchList(list).yes {
            list!!.forEach {

                it.levelIndex = index
                val themeId = it.id
                val childList = db.queryCatTypeListLevel(themeId)
                it.nextList = childList
                it.isHasNext = matchList(childList)

                val nextIndex = it.levelIndex.plus(1)
                queryList(childList, nextIndex)

            }
        }
    }

    private fun initYxtList() {
        vb.lvYxt.setLinearManager()
        adapterYxt = YxtAdapter()
        vb.lvYxt.adapter = adapterYxt
        vb.lvYxt.update(dataListYxt)
        vb.lvYxt.disableLoadMoreIfNotFullPage()
        vb.lvYxt.itemChildViewClickListener = this
    }

    /**
     * 初始化列表视图
     */
    private fun initZttList() {
        vb.lvZtt.setLinearManager()
        adapterZtt = ZttAdapter()
        vb.lvZtt.adapter = adapterZtt
        vb.lvZtt.disableLoadMoreIfNotFullPage()
        adapterZtt.setNewData(dataListZtt)
    }


    /**
     * 初始化地图册数据
     */
    private fun initDtcLayerData() {
        dataListDtc = mutableListOf()

        val file = File(Config.ATLASDATA_PATH)
        var data = DtcItem(DtcItem.LEVEL1)
        getImageFiles2(file, data, 1)
    }


    /**
     * 尾递归 方法
     * 获取SD卡中，地图册目录下的 目录和文件
     */
    private fun getImageFiles2(file: File, data: DtcItem, index: Int) {
//        "文件目录  ${file.path}".printMsg()
        if (file.isDirectory) {
            val fs = file.listFiles()

            if (fs.isNotEmpty()) {

                var tempList = mutableListOf<BaseNode>()

                fs.forEach {
//
                    "文件目录2  ${it.path}".printMsg()
                    var level = DtcItem.LEVEL1
                    if (it.isFile) {
                        level = DtcItem.LEVEL3
                    }
                    val item = DtcItem(level)
                    item.index = index
                    item.path = it.path
                    item.parentPath = file.path
                    item.isExpanded = false
                    item.name = it.name.self()
                    tempList.add(item)
                    if (it.isDirectory) {
                        getImageFiles2(File(it.path), item, index + 1)
                    }
                }

                data.childNode = tempList

                (index == 1).yes { dataListDtc.addAll(tempList) }
            }
        }
    }

    fun calculateCounts(list: MutableList<BaseNode>): Int {
        var totalCount = 0
        for (item in list) {
            item as ZttItem
            if (item.childNode.isNullOrEmpty()) {

                // 如果是叶子节点，直接累加它的 count
                totalCount += item.dataCount

            } else
                CommonUtil.matchList(item.childNode).yes {
                    // 如果不是叶子节点，递归计算子节点的 count 总和
                    val childrenCount = calculateCounts(item.childNode!!)
                    item.dataCount = childrenCount // 更新当前节点的 count
                    totalCount += childrenCount
                }
            }
        return totalCount
    }


    private fun initDtcList() {
        vb.lvDtc.setLinearManager()
        adapterDtc = DtcAdapter()
        vb.lvDtc.adapter = adapterDtc
        vb.lvDtc.disableLoadMoreIfNotFullPage()
        adapterDtc.setNewData(dataListDtc)
    }

    private fun createGeojsonFragment() {
        if (loadGeojsonFragment == null) {
            loadGeojsonFragment = LoadGeojsonFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.flExternalImport, loadGeojsonFragment!!)
                .commitNow()
            loadGeojsonFragment!!.onEmbedded()
        }
    }

    private fun createShpFragment() {
        if (loadShpFragment == null) {
            loadShpFragment = LoadShpFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.flShpImport, loadShpFragment!!)
                .commitNow()
            loadShpFragment!!.onEmbedded()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.rbDataResource -> if (isChecked) {
                vb.llDataResource.show()
                vb.llExternalImportPanel.gone()
            }
            R.id.rbExternalImport -> if (isChecked) {
                vb.llDataResource.gone()
                vb.llExternalImportPanel.show()
                val showSubTabs = FunctionControlUtil.instances.TAB_EXTERNAL_IMPORT
                        && FunctionControlUtil.instances.TAB_IMPORT_SHP
                if (!showSubTabs) {
                    // 只有一个子类型，直接显示
                    if (FunctionControlUtil.instances.TAB_EXTERNAL_IMPORT) {
                        vb.flExternalImport.visiable(true)
                        createGeojsonFragment()
                    } else {
                        vb.flShpImport.visiable(true)
                        createShpFragment()
                    }
                } else {
                    // 默认选中 geojson（由于 XML 中 rbSubGeojson 已 checked，手动置 true 不触发 listener）
                    vb.rbSubGeojson.setTypeface(null, Typeface.BOLD)
                    vb.rbSubShp.setTypeface(null, Typeface.NORMAL)
                    vb.flExternalImport.visiable(true)
                    vb.flShpImport.gone()
                    createGeojsonFragment()
                    vb.rbSubGeojson.isChecked = true
                }
            }
            R.id.rbSubGeojson -> if (isChecked) {
                vb.rbSubGeojson.setTypeface(null, Typeface.BOLD)
                vb.rbSubShp.setTypeface(null, Typeface.NORMAL)
                vb.flExternalImport.visiable(true)
                vb.flShpImport.gone()
                createGeojsonFragment()
            }
            R.id.rbSubShp -> if (isChecked) {
                vb.rbSubGeojson.setTypeface(null, Typeface.NORMAL)
                vb.rbSubShp.setTypeface(null, Typeface.BOLD)
                vb.flExternalImport.gone()
                vb.flShpImport.visiable(true)
                createShpFragment()
            }
            R.id.rbZtt -> if (isChecked) {
                //专题图
                vb.lvZtt.show()
                vb.lvYxt.gone()
                vb.lvDtc.gone()
                vb.tvNodata.visiable(dataListZtt.isNullOrEmpty())
            }
            R.id.rbYxt -> if (isChecked) {
                //历年影像图
                vb.lvZtt.gone()
                vb.lvYxt.show()
                vb.lvDtc.gone()
            }
            R.id.rbDtc -> if (isChecked) {
                //地图册
                vb.lvZtt.gone()
                vb.lvYxt.gone()
                vb.lvDtc.show()
                vb.tvNodata.visiable(dataListDtc.isNullOrEmpty())
            }
        }
    }

    override fun onItemChildViewClick(viewId: Int, position: Int) {
        val item: ImageLayerItem = dataListYxt?.get(position) ?: return
        when (viewId) {
            R.id.ivLayerReplace -> {
                tip("替换底图")
                item.isSelectReplaceAction = !item.isSelectReplaceAction
                EventBus.getDefault().post(MapEvent(MapEvent.REPLACE_BASE_MAP, item))

                //替换底图的时候，应该把其他选中的底图全部反选
                if (item.isSelectReplaceAction) {
                    dataListYxt?.forEach { data ->
                        if (data.id != item.id) {
                            data.isSelectReplaceAction = false
                        }
                        //叠加的也去掉
                        data.isSelectAddAction = false
                    }
                }
                adapterYxt.notifyDataSetChanged()
            }
            R.id.ivLayerAdd -> {
                tip("叠加底图")
                //这时候被选中了
                //判断 底图是否已经被替换
                if (item.isSelectReplaceAction) {
                    tip("当前地图已经是：" + item.getName().toString() + "，无需叠加。")
                    return
                }
                item.isSelectAddAction = !item.isSelectAddAction
                adapterYxt.notifyDataSetChanged()
                EventBus.getDefault().post(
                    MapEvent(
                        if (item.isSelectAddAction) MapEvent.ADD_BASEMAP else MapEvent.REMOVED_ADDED_BASEMAP,
                        item
                    )
                )
            }
        }
    }

    //操作地图
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: DataEvent) {
        when (event.actionType) {
            //清空所有专题图图层
            DataEvent.CLEAR_ALL_ZZT_LAYER -> {
                vb.llClearData.performClick()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}