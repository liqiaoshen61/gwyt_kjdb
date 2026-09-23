package com.jwch.gwyt_project.fragment

import android.view.KeyEvent
import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenilistlib.i.ItemChildViewClickListener
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.Info.*
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.geo.GeoAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragGeoCollectionBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.GeoCollectionModel
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.SoftUtil
import com.jwch.gwyt_project.view.ChangeFolderDialog
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.model.SelectionListModel
import com.jwch.gwyt_project.util.FunctionControlUtil
import com.jwch.gwyt_project.util.OperationLogger
import com.jwch.gwyt_project.view.CreateFolderDialog
import com.jwch.gwyt_project.view.NavigationSelectDialog
import com.jwch.gwyt_project.view.NetDialog
import com.jwch.gwyt_project.view.UncancelDialog
import com.jwch.gwyt_project.view.WorkRecordOutPutDialog
import com.loper7.date_time_picker.DateTimeConfig
import com.loper7.date_time_picker.dialog.CardDatePickerDialog
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.text.Collator
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 *   工作记录列表 （以前的标绘收藏夹，产品把收藏夹分开了）
 */

class GeoCollectionFragment : BaseFragment<FragGeoCollectionBinding>(), ItemClickListener,
    ItemChildViewClickListener,
    ActionListener {

    val keyboard = SoftUtil()

    //用于存放数据库查询到的标绘列表
    var geoDataList: MutableList<BaseNode> = mutableListOf()
    var selectGeoList: MutableList<MarkerInfo?> = mutableListOf()  //选中的标绘列表
    var geoAdapter: GeoAdapter? = null //Adapter
    private lateinit var createFolderDialog: CreateFolderDialog

    var startTime : Long? = null //开始时间戳
    var endTime : Long? = null  //结束时间戳
    var timeBuild: CardDatePickerDialog.Builder? = null
    lateinit var timeformatList: MutableList<Int>
    var timeStamp: Long? = null //临时保存时选择的间戳

    var areaSelectionList = mutableListOf<SelectionListModel>()
    var finishSelectionList = mutableListOf<SelectionListModel>()


    var initCode = Config.AreaCode //这里需要需要设置 初始展示的区域代码
    var districtsList: MutableList<DistrictsInfo>? = null//行政区划数据
    var currentAreaData: DistrictsInfo? = null //行政区划筛选参数

    var keywordParams = ""  //关键词搜索参数
    var areaLevel : Int? = -1 //行政区划筛选参数
    var areaFilterParams = "" //行政区划筛选参数
    var isFinishFilterParams = "" //是否整改筛选参数


    lateinit var outPutDialog: WorkRecordOutPutDialog
    lateinit var navigationSelectDialog: NavigationSelectDialog

    companion object {
        const val ACTION_DRAW = 0 //绘制标绘
        const val ACTION_DELETE = 1 //删除标绘
        const val ACTION_DELETE_FOLDER = 2 //删除标绘文件夹
        const val ACTION_MOVE = 3 //移动标绘
        const val ACTION_SELECT_FOLDER = 4 //选择移动的文件夹
        const val ACTION_EDIT = 5 //编辑
    }

    override fun initView() {

        //初始化标绘
        initGeoList()
        reloadGeoCollectData()
        initSelectData()

        EventBus.getDefault().register(this)

        vb.ivOutPut.visiable(FunctionControlUtil.instances.MODULE_COLLECTION_EXPORT)

        vb.etSearch.hint = "请输入河流名称"

        vb.llGeoStatis.visiable(Config.AreaCode =="350525")

    }


    //初始化标绘收藏列表
    private fun initGeoList() {
        vb.lvGeometyList.setLinearManager()
        geoAdapter = GeoAdapter(this)
        vb.lvGeometyList.adapter = geoAdapter
        vb.lvGeometyList.disableLoadMoreIfNotFullPage()
        vb.lvGeometyList?.update(geoDataList)
    }


    fun queryGeoCollectList(
        keyword: String? = null,
        sTime: Long? = null,
        eTime: Long? = null,
        area: String? = null,
        areaLevel : Int? = null,
        isFinish: String? = null
    ): MutableList<BaseNode> {
        return geoDataList.filter { item ->
            item as GeoCollectionModel

            // 根据 areaLevel 动态选择匹配字段
            val areaMatch = when (areaLevel) {
                1 -> item.markerInfo?.city == area
                2 -> item.markerInfo?.county == area
                3 -> item.markerInfo?.town == area
                else -> true  // 默认不进行区域筛选
            }

            (keyword.isNullOrBlank() || item.markerInfo?.riverName.self().contains(keyword.self())) &&
                    (isFinish.isNullOrBlank() || item.markerInfo?.isFinish.self() == isFinish) &&
                    (sTime == null || eTime == null || item.markerInfo?.checkTime.self() in (sTime + 1) until eTime) &&
                    (area.isNullOrBlank() || areaMatch)

        } as MutableList

    }

    //加载标绘数据
    private fun reloadGeoCollectData() {
        geoDataList.clear()
        //1、查出所有文件夹
        val folderList = db.queryAllFolderList()
        "文件夹：${folderList.toJson()}".printMsg()

        folderList?.forEach {
            geoDataList.add(GeoCollectionModel(it))
        }
        //2、查出所有不在文件夹下的标绘
        val markerList_noFolder = db.queryAllMarkerListByFolderId(0)
        markerList_noFolder?.forEach {
            geoDataList.add(GeoCollectionModel(it, GeoCollectionModel.LEVEL2))
        }

        //3、查出所有文件夹下的标绘
        geoDataList.forEach {
            val item = it as GeoCollectionModel

            item.isFolder.yes {

                val folder = item.folderData!!

                //查出文件夹下标绘的列表
                val markerList = db.queryAllMarkerListByFolderId(folder.id)
                markerList?.forEach { marker ->
                    item.addChildNode(GeoCollectionModel(marker, GeoCollectionModel.LEVEL2))
                }
            }
        }

        //4‘如果有已选中的标绘，还要匹配
        matchList(selectGeoList).yes {
            selectGeoList.forEach {
                val marker = it as MarkerInfo

                geoDataList.forEach {
                    val item = it as GeoCollectionModel
                    item.isFolder.yes {
                        item.childNode?.forEach { child ->
                            val childData = child as GeoCollectionModel
                            if (childData.markerInfo?.id == marker.id) {
                                childData.isSelect = true
                            }
                        }
                    }.no {
                        if (item.markerInfo?.id == marker.id) {
                            item.isSelect = true
                        }
                    }
                }
            }

        }

        geoAdapter?.setNewData(geoDataList)
        vb.tvNodata.visiable(!matchList(geoDataList))
        if(matchList(geoDataList)){
            vb.tvCount.text = "(共${geoDataList.size}条)"
        }else{
            vb.tvCount.text = ""
        }

    }


    private fun showOutPutDialog() {

        if (!::outPutDialog.isInitialized) {
            outPutDialog = WorkRecordOutPutDialog(requireContext())
        }
        if (!outPutDialog.isShowing) {
            outPutDialog.show()
        }
    }

    private fun showNavigationSelectDialog() {

        if (!::navigationSelectDialog.isInitialized) {
            navigationSelectDialog = NavigationSelectDialog(requireContext())
        }
        if (!navigationSelectDialog.isShowing) {
            navigationSelectDialog.show()
        }
    }

    override fun initViewListener() {

        //关闭我的收藏
        vb.ivClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_GEO_COLLECTION_FRAG))
        }

        vb.llGeoStatis.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.GO_COLLECTION_STATIS_FRAG))
        }

        //  清空搜索内容
        vb.tvClear.onClick {
            vb.etSearch.setText("")
            clearLayer()
        }

        //搜索
        vb.tvSearch.onClick {
            queryData()
        }

        //  清空搜索内容
        vb.tvClear.onClick {
            vb.etSearch.setText("")
            clearLayer()
        }

        //软键盘回车
        vb.etSearch.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                keyboard.hideKeyboard(activity)
                vb.tvSearch.callOnClick()
                return@OnKeyListener true
            }
            false
        })

        vb!!.tvTitle.onClick {
//            showNavigationSelectDialog()
        }
        //导出
        vb.ivOutPut.onClick {
            showOutPutDialog()

        }
        //清空所有勾选的标绘
        vb.llClearAll.onClick {
            reloadGeoCollectData()
        }
        //时间筛选
        vb.tvTimeFilter.onClick {
            showTimeDialog("选择记录时间")
        }
        //位置筛选
        vb.tvAreaFilter.onClick {

            showSelectionDialog(requireContext(), areaSelectionList) { Any, i ->
                val item = Any as SelectionListModel

                vb.tvAreaFilter.text = item.value
                areaFilterParams = item.value
                areaLevel = currentAreaData?.distLevel
                queryData()
            }
        }
        //办结情况筛选
        vb.tvFinishFilter.onClick {
            showSelectionDialog(requireContext(), finishSelectionList) { Any, i ->
                val item = Any as SelectionListModel
                vb.tvFinishFilter.text = item.value
                isFinishFilterParams = item.value

                queryData()
            }
        }



        vb.tvFilterReset.onClick {
            vb.tvTimeFilter.text = "时间筛选"
            vb.tvAreaFilter.text = "地区筛选"
            vb.tvFinishFilter.text = "是否整改"
            geoAdapter?.setNewData(geoDataList)
            if(matchList(geoDataList)){
                vb.tvCount.text = "(共${geoDataList.size}条)"
            }else{
                vb.tvCount.text = ""
            }
            startTime = null
            endTime = null
            areaLevel = -1
            areaFilterParams = ""
            isFinishFilterParams = ""
            vb.etSearch.setText("")
            vb.tvFilterReset.hide()
        }
    }

    fun initSelectData() {

        val item = AppContext.app.districtHelper.queryDistrictByCode(initCode)
        if (item != null) {
            currentAreaData = item
            districtsList = AppContext.app.districtHelper.getNextLevelDistricts(currentAreaData!!.distCode)
            districtsList?.forEach {
                areaSelectionList.add(SelectionListModel(it.distName))
            }
        }

        finishSelectionList.add(SelectionListModel("未整改"))
        finishSelectionList.add(SelectionListModel("已整改"))

    }

    fun showTimeDialog(
        dialogTitle: String = "请选择",
        showUnit: Boolean = false,
        backNow: Boolean = false,
        chooseText: String = "选择",
        showSecond: Boolean = false
    ) {

        if (!::timeformatList.isInitialized) {
            timeformatList = mutableListOf()
            timeformatList.add(DateTimeConfig.YEAR)
            timeformatList.add(DateTimeConfig.MONTH)
        }
        timeBuild = CardDatePickerDialog.builder(requireContext()).setTitle(dialogTitle)
            .setDisplayType(timeformatList).setBackGroundModel(CardDatePickerDialog.CUBE)
            .showBackNow(false).setPickerLayout(R.layout.layout_date_picker_segmentation)
            .setWrapSelectorWheel(false)
            .setThemeColor(findColor(requireContext(), com.jameni.basepage_lib.R.color.main_color))
            .showDateLabel(showUnit).showFocusDateInfo(false)
            .setOnChoose(chooseText) {

                timeStamp = it

                getMonthStartAndEnd(timeStamp!!)
                val time = TimeUtil.getDateToString(it, Config.timeFormat4)
                vb.tvTimeFilter.text = time

                queryData()

            }.setOnCancel("取消")

        isNotNullObj(timeStamp) { timeBuild?.setDefaultTime(timeStamp!!) }
        timeBuild?.build()?.show()
    }


    fun getMonthStartAndEnd(timestamp: Long) {
        // 使用时区（此处为系统默认时区，可按需替换为ZoneOffset.UTC）
        val zoneId = ZoneId.systemDefault()
        val instant = Instant.ofEpochMilli(timestamp)
        val zonedDateTime = instant.atZone(zoneId)

        // 计算月份起始时间（当月第一天00:00:00）
        val start = zonedDateTime.withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        // 计算月份结束时间（下个月第一天00:00:00减1毫秒）
        val end = zonedDateTime.plusMonths(1)
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli() - 1

        startTime = start
        endTime = end
    }


    fun getKeyWord(): String {
        val keyWord = vb.etSearch.text.toString().toCharArray()
        var str = ""
        for (i in keyWord.indices) {
            str += "%" + keyWord[i]
        }
        str += "%"

        return str
    }

    //查询
    fun queryData() {

        keyboard.hideKeyboard(activity)
        setLoadingVisiable(true)
        vb.tvNodata.gone()

        keywordParams = vb.etSearch.text.toString()

        val filterList = queryGeoCollectList(
            keywordParams,
            startTime,
            endTime,
            areaFilterParams,
            areaLevel,
            isFinishFilterParams
        )
        geoAdapter?.setNewData(filterList)
        vb.tvNodata.visiable(!matchList(filterList))

        if(matchList(filterList)){
            vb.tvCount.text = "(共${filterList.size}条)"
        }else{
            vb.tvCount.text = ""
        }

        vb.tvFilterReset.show()


    }

    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlePageView(event: DataEvent?) {
        if (event == null) return
        when (event.actionType) {
            //更新收藏信息
            DataEvent.UPDATE_COLLECTION_DATA -> {


            }

            DataEvent.UPDATE_MARKER_DATA -> {
                reloadGeoCollectData()

               if(vb.tvFilterReset.isShow()){
                    queryData()
                }
            }

            DataEvent.UPDATE_MARKER_DATA_AND_DRAW -> {
                reloadGeoCollectData()
                drawSelectGeo(null)
            }

            DataEvent.CLEAR_ALL_GEO -> {
                vb.llClearAll.performClick()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClick(itemData: Any?, position: Int) {

    }

    fun clearLayer() {
        EventBus.getDefault().post(MapEvent(MapEvent.HIDE_CALLOUT))
        EventBus.getDefault().post(MapEvent(MapEvent.CLEAR_GRAPHIC_LAYER))
    }

    private fun setLoadingVisiable(visiable: Boolean) {
//        if (isNotNull(vb.llLoading)) {
//            vb.llLoading.visibility = if (visiable) View.VISIBLE else View.GONE
//        }
    }

    override fun onItemChildViewClick(viewId: Int, position: Int) {
    }


    //新建文件夹
    private fun addFolder() {
        if (!::createFolderDialog.isInitialized) {
            createFolderDialog = CreateFolderDialog(requireContext(), object : ActionListener {
                override fun onAction(obj: Any?, flag: Int) {
                    reloadGeoCollectData()
                }
            })
        }
        if (!createFolderDialog.isShowing) {
            createFolderDialog.show()
        }
    }

    //移动标绘所在文件夹
    private fun moveGeo(item: GeoCollectionModel) {
        val changeFolderDialog = ChangeFolderDialog(requireContext()) {
            val marker = item.markerInfo!!
            marker.folderId = it
            db.updateMarkerInfo(marker)

            geoDataList.remove(item)
            geoDataList.forEach { node ->
                val data = node as GeoCollectionModel
                (data.isFolder && data.folderData!!.id == it).yes {
                    data.addChildNode(item)
                    return@forEach
                }
            }

            geoAdapter?.setNewData(geoDataList)


        }
        if (!changeFolderDialog.isShowing) {
            changeFolderDialog.show()
        }

    }

    override fun onAction(obj: Any?, flag: Int) {
        when (flag) {
            //绘制
            ACTION_DRAW -> {
                drawSelectGeo(obj as GeoCollectionModel)
            }
            //删除标绘
            ACTION_DELETE -> {
                showNormalDialog(requireContext(), "是否确定删除") {
                    it.yes {
                        deleteGeo(obj as GeoCollectionModel)
                    }
                }
            }
            //删除标绘文件夹
            ACTION_DELETE_FOLDER -> {
                showNormalDialog(
                    requireContext(),
                    "删除文件夹的同时，该文件夹下的标绘也一起删除，是否确定删除？"
                ) {
                    it.yes { deleteFolder(obj as GeoCollectionModel) }
                }
            }
            //移动标绘
            ACTION_MOVE -> {
                moveGeo(obj as GeoCollectionModel)
            }
            //选择移动的文件夹
            ACTION_SELECT_FOLDER -> {
                val folderId = obj as Int
            }
            //编辑
            ACTION_EDIT -> {
                obj as GeoCollectionModel
                EventBus.getDefault().post(DataEvent(DataEvent.EDIT_MARKER_INFO, obj.markerInfo))
            }

        }
    }

    private fun drawSelectGeo(data: GeoCollectionModel?) {
//        data?.let {
//            it.isSelect.yes {
//                selectGeoList.add(it.markerInfo!!)
//            }.no {
//                //删除不能用对象，因为数据中心加载一次，数据对象就变了
//                selectGeoList.removeIf { item ->
//                    item?.id == it?.markerInfo?.id
//                }
//            }
//        }
//
//        EventBus.getDefault().post(DataEvent(DataEvent.DRAW_SELECT_COLLECTION_MARKER, selectGeoList))


        data?.let {
            if (data.isSelect) {
                geoDataList.forEach {
                    it as GeoCollectionModel
                    it.isSelect = false
                }
                data.isSelect = true
                EventBus.getDefault()
                    .post(DataEvent(DataEvent.DRAW_SELECT_COLLECTION_MARKER_SINGLE, it.markerInfo))
                EventBus.getDefault()
                    .post(MapEvent(MapEvent.CLICK_MARKER_RESULT_LIST_ITEM, it.markerInfo))
            } else {
                EventBus.getDefault()
                    .post(DataEvent(DataEvent.UNDRAW_SELECT_COLLECTION_MARKER_SINGLE))
            }
        }

    }

    //删除标绘
    private fun deleteGeo(item: GeoCollectionModel) {


        val marker = item.markerInfo!!

        OperationLogger.logOperation(requireContext(), "删除工作记录：${marker.questionType} 坐标：${marker.latLng}")

        //删除已添加的附件
        db.deleteAccessoryInfoByLinkIdAndDataType(
            marker.id.toString(),
            AccessoryInfo.ACCESSORY_MARKER
        )

        val imagelist =
            db.queryImageInfoByLinkIdAndDataType(marker.id.toString(), ImageInfo.IMAGE_MARKER)
        CommonUtil.matchList(imagelist).yes {
            //遍历删除图片文件
            imagelist!!.forEach { img ->
                val file = File(img.filePath)
                if (file.exists() && file.isFile) {
                    file.delete()
                }
            }
        }
        //删除数据库中的图片记录
        db.deleteImageInfoByLinkIdAndDataType(marker.id.toString(), ImageInfo.IMAGE_MARKER)
        //删除单签标绘
        db.deleteMarker(marker)
        geoDataList.remove(item)
        geoAdapter?.setNewData(geoDataList)

        item.isSelect.yes {
            //在地图上绘制
            drawSelectGeo(null)
        }
        vb.tvNodata.visiable(!matchList(geoDataList))


    }

    private fun deleteFolder(data: GeoCollectionModel) {
        val folderInfo = data.folderData!!
        //查出文件夹下所有标绘--然后删除标绘所有关联的图片和附件
        val markerList = db.queryAllMarkerListByFolderId(folderInfo.id)
        markerList?.forEach {
            //删除已添加的附件
            db.deleteAccessoryInfoByLinkIdAndDataType(
                it.id.toString(),
                AccessoryInfo.ACCESSORY_MARKER
            )

            val imagelist =
                db.queryImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)
            CommonUtil.matchList(imagelist).yes {
                //遍历删除图片文件
                imagelist!!.forEach { img ->
                    val file = File(img.filePath)
                    if (file.exists() && file.isFile) {
                        file.delete()
                    }
                }
            }
            //删除数据库中的图片记录
            db.deleteImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)
        }

        //先删除文件夹下的标绘
        db.deleteMarkerByFolderId(folderInfo.id)
        db.deleteFolder(folderInfo)

//        文件夹下，如果有已勾选的标绘，也从已选的数组中删除
        data.childNode?.forEach {

            val item = it as GeoCollectionModel
            item.isSelect.yes {
                selectGeoList.remove(item.markerInfo)
            }
        }


        geoDataList.remove(data)
        geoAdapter?.setNewData(geoDataList)
        vb.tvNodata.visiable(!matchList(geoDataList))

        //在地图上绘制
        drawSelectGeo(null)

    }

    private fun deleteFile(fileName: String): Boolean {
        val path: String = Config.FILE_CAMERA_PATH + fileName
        val file = File(path)
        return if (file.exists() && file.isFile) {
            file.delete()
        } else {
            false
        }
    }

    fun getChildDistrictList(model: DistrictsInfo): MutableList<DistrictsInfo>? {
        var districtList: MutableList<DistrictsInfo>? = null

        val nextLevel = model.distLevel + 1
        val nextList = db.queryDistrictListByLevel(nextLevel)

        val formatCode = getDistrictCode(model)
        CommonUtil.matchList(nextList).yes {
            districtList =
                nextList?.filter { it.distCode.startsWith(formatCode) } as MutableList<DistrictsInfo>
        }
        //拼音排序
        sortChineseList(districtList)
        return districtList
    }


    /**
     * 规范化 行政区code
     */
    private fun getDistrictCode(model: DistrictsInfo): String {
        var code = ""
        when (model.distLevel) {
            DistrictsInfo.LEVEL_PROVINCE -> code = model.distCode.substring(0, 2) //省 2位
            DistrictsInfo.LEVEL_CITY -> code = model.distCode.substring(0, 4) //市 4位
            DistrictsInfo.LEVEL_COUNTY -> code = model.distCode.substring(0, 6) //县 6位
            DistrictsInfo.LEVEL_TOWN -> code = model.distCode.substring(0, 9) //乡镇 9位
            DistrictsInfo.LEVEL_VILLAGE -> code = model.distCode.substring(0, 12) //村 12位
        }
        return code
    }

    // 排序函数
    fun sortChineseList(list: MutableList<DistrictsInfo>?) {
        CommonUtil.matchList(list).yes {
            val collator = Collator.getInstance(Locale.CHINA) // 使用中文排序规则
            list!!.sortWith { o1, o2 ->
                collator.compare(o1.distName, o2.distName)
            }
        }
    }

}
