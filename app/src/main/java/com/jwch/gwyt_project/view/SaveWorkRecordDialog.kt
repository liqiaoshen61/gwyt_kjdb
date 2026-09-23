package com.jwch.gwyt_project.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.AccessoryInfo
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.Info.MarkerInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewSaveWorkRecordDialogBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.util.OperationLogger
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.model.SelectionListModel
import com.jwch.gwyt_project.util.MarkerUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

/**
 * 保存工作记录 (也就是之前的标绘保存)
 */

class SaveWorkRecordDialog(context: Context, listener: ActionListener, markerUtil: MarkerUtil?, drawType: Int,
    _markerInfo: MarkerInfo? = null) : JameniBaseDialog(context) {

    var vb: ViewSaveWorkRecordDialogBinding? = null

    private var listener1: ActionListener? = null
    var isEdit: Boolean = false //编辑模式
    var drawType = 0//标绘的图形类型

    private var markerUtil: MarkerUtil? = null//标绘工具对象
    private var geometry: Geometry? = null//标绘的图形对象（从markerUtil中获取）
    private var centerPoint: Point? = null//标绘的图形的中心点（从geometry中获取）

    private var markerInfo: MarkerInfo? = null


    init {
        this.context = context
        this.listener1 = listener1
        this.markerUtil = markerUtil
        this.drawType = drawType

        if (_markerInfo != null) {
            isEdit = true
            this.markerInfo = _markerInfo
            geometry =  Geometry.fromJson(markerInfo!!.geometryJson)
        } else {
            isEdit = false
            geometry = markerUtil!!.getGeometry()
        }

        centerPoint = geometry!!.extent.center
        EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_BY_POINT, centerPoint))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_save_work_record_dialog, null)
        vb = ViewSaveWorkRecordDialogBinding.bind(view)
        setContentView(vb!!.root)


        setCanceledOnTouchOutside(false)
//        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        EventBus.getDefault().register(this)

        initViewListener()
        initDialogSize()
        initData()

        if(markerInfo == null){
            vb!!.tvCheckTime.setCurrentTime()
        }

        markerInfo?.let {
            isEdit.yes {
                //赋值
                vb!!.tvRiverName.setValueText(it.riverName.self())
                vb!!.tvCity.setValueText(it.city.self())
                vb!!.tvCounty.setValueText(it.county.self())
                vb!!.tvTown.setValueText(it.town.self())
                vb!!.tvVillage.setValueText(it.village.self())
                vb!!.cbQuestionType.setSelectionByKey(it.questionType.self())
                vb!!.tvQuestionAttr.setValueText(it.questionAttr.self())
                vb!!.cbSeverity.setSelectionByKey(it.severity.self())
                vb!!.tvDescription.setValueText(it.description.self())
                vb!!.tvLocation.setValueText(it.location.self())
                vb!!.tvLatLng.setValueText(it.latLng.self())
                vb!!.tvCheckTime.setValueText(TimeUtil.getDateToString(it.checkTime.self(), Config.timeFormat1))
                vb!!.cbIsFinish.setSelectionByKey(it.isFinish.self())
                vb!!.tvSpotRectificationSituation.visiable(it.isFinish.self() == "已整改")
                vb!!.tvSpotRectificationSituation.setValueText(it.spotRectificationSituation.self())

                vb!!.tvCity.setSelection(it.city)

                val cityItem =  AppContext.app.districtHelper.getDistrictByName(it.city)
                countyList = getChildDistrictList(cityItem!!)
                countySelectionList = countyList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
                vb!!.tvCounty.updateList(countySelectionList!!)

                vb!!.tvCounty.setSelection(it.county)
                val countyItem = AppContext.app.districtHelper.getDistrictByName(it.city)
                townList = getChildDistrictList(countyItem!!)
                townSelectionList = townList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
                vb!!.tvTown.updateList(townSelectionList!!)

                vb!!.tvTown.setSelection(it.town)

                when(it.questionType){
                    "乱占" -> {
                        vb!!.tvQuestionAttr.setNewLabelArray("1、围垦湖泊,2、未经依法批准围垦河道,3、种植碍洪作物,4、非法占用水域滩地")
                    }
                    "乱采" -> {
                        vb!!.tvQuestionAttr.setNewLabelArray("5、非法采砂")
                    }
                    "乱堆" -> {
                        vb!!.tvQuestionAttr.setNewLabelArray("6、乱堆垃圾,7、废物废水倾倒、填埋等,8、堆放碍洪物体")
                    }
                    "乱建" -> {
                        vb!!.tvQuestionAttr.setNewLabelArray("9、岸线长期占而不用、多占少用、滥占滥用,10、涉河违法违规建设项")
                    }
                    "其他" -> {
                        vb!!.tvQuestionAttr.setNewLabelArray("11、其他违法违规问题")
                    }
                }
                vb!!.tvQuestionAttr.setSelection(it.questionAttr)



            }
        }

    }

    var cityList: MutableList<DistrictsInfo>? = null//行政区划数据
    var citySelectionList: MutableList<SelectionListModel>? = null//行政区划数据
    var countyList: MutableList<DistrictsInfo>? = null//行政区划数据
    var countySelectionList: MutableList<SelectionListModel>? = null//行政区划数据
    var townList: MutableList<DistrictsInfo>? = null//行政区划数据
    var townSelectionList: MutableList<SelectionListModel>? = null//行政区划数据
    var villageList: MutableList<DistrictsInfo>? = null//行政区划数据
    var villageSelectionList: MutableList<SelectionListModel>? = null//行政区划数据

    fun initData(){

//        AppContext.app.districtHelper.queryDistrictByCode("350000")
        val provinceItem =  AppContext.app.districtHelper.queryDistrictByCode("35")
        cityList = getChildDistrictList(provinceItem!!)
        citySelectionList = cityList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
        vb!!.tvCity.updateList(citySelectionList!!)

        //选择市后 获取县级数据
        vb!!.tvCity.selectionActionBlock2 = { position, data ->
            vb!!.tvCounty.reset()
            vb!!.tvTown.reset()
            vb!!.tvVillage.reset()

            val cityItem = data.data as DistrictsInfo
            countyList =  getChildDistrictList(cityItem)
            countySelectionList = countyList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
            vb!!.tvCounty.updateList(countySelectionList!!)

            updateLocationText()
        }

        vb!!.tvCounty.actionBlock = {
            if(vb!!.tvCity.getValueData().isNotBlank()){
                vb!!.tvCounty.showSelectionList()
            }else{
                tip(context, "请先选择所在市")
            }
        }

        vb!!.tvCounty.selectionActionBlock2 = { position, data ->
            vb!!.tvTown.reset()
            vb!!.tvVillage.reset()

            val countyItem = data.data as DistrictsInfo
            townList =  getChildDistrictList(countyItem)
            townSelectionList = townList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
            vb!!.tvTown.updateList(townSelectionList!!)
            updateLocationText()
        }

        vb!!.tvTown.actionBlock = {
            if(vb!!.tvCounty.getValueData().isNotBlank()){
                vb!!.tvTown.showSelectionList()
            }else{
                tip(context, "请先选择所在县")
            }
        }

        vb!!.tvTown.selectionActionBlock2 = { position, data ->
//            vb!!.tvVillage.reset()

//            val townItem = data.data as DistrictsInfo
//            villageList =  getChildDistrictList(townItem)
//            villageSelectionList = villageList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
//            vb!!.tvVillage.updateList(villageSelectionList!!)
            updateLocationText()
        }

//        vb!!.tvVillage.actionBlock = {
//            if(vb!!.tvTown.getValueData().isNotBlank()){
//                vb!!.tvVillage.showSelectionList()
//            }else{
//                tip(context, "请先选择所在镇")
//            }
//        }

        vb!!.cbQuestionType.selectionActionBlock2 = { pos, item ->
            vb!!.tvQuestionAttr.reset()
            when(item.value){
                "乱占" -> {
                    vb!!.tvQuestionAttr.setNewLabelArray("1、围垦湖泊,2、未经依法批准围垦河道,3、种植碍洪作物,4、非法占用水域滩地")
                }
                "乱采" -> {
                    vb!!.tvQuestionAttr.setNewLabelArray("5、非法采砂")
                }
                "乱堆" -> {
                    vb!!.tvQuestionAttr.setNewLabelArray("6、乱堆垃圾,7、废物废水倾倒、填埋等,8、堆放碍洪物体")
                }
                "乱建" -> {
                    vb!!.tvQuestionAttr.setNewLabelArray("9、岸线长期占而不用、多占少用、滥占滥用,10、涉河违法违规建设项")
                }
                "其他" -> {
                    vb!!.tvQuestionAttr.setNewLabelArray("11、其他违法违规问题")
                }
            }
        }



        vb!!.tvQuestionAttr.actionBlock = {
            if(vb!!.cbQuestionType.getSelectValue().isNotBlank()){
                vb!!.tvQuestionAttr.showSelectionList()
            }else{
                tip(context, "请先选择问题分类")
            }
        }

        vb!!.cbIsFinish.selectionActionBlock2 = { pos, item ->
           vb!!.tvSpotRectificationSituation.visiable(item.value == "已整改")
        }


        val poi = GeometryEngine.project(centerPoint, Config.sp4490) as Point
        vb!!.tvLatLng.setValueText("${poi.x},${poi.y}")


        vb!!.tvRiverName.onTextChangedBlock = {
            updateLocationText()
        }

}

    @SuppressLint("SuspiciousIndentation")
    fun updateLocationText(){
        val riverName = vb!!.tvRiverName.getValueData()
        val city = vb!!.tvCity.getValueData()
        val conty = vb!!.tvCounty.getValueData()
        val town = vb!!.tvTown.getValueData()
        val village = vb!!.tvVillage.getValueData()
        if(riverName.isNotBlank() && city.isNotBlank() && conty.isNotBlank())

        vb!!.tvLocation.setValueText("${city}${conty}${town}${village}${riverName}")
    }


    /**
     * 规范化 行政区code
     */
    private fun formatDistrictCode(model: DistrictsInfo): String {
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

    fun getChildDistrictList(model: DistrictsInfo): MutableList<DistrictsInfo>? {
       return AppContext.app.districtHelper.getNextLevelDistricts(model.distCode)
    }



    fun initDialogSize() {
        val windowSize = GetWindowSize(context)
        //设置宽高
        val width = windowSize.windowWidth * 70 / 100
        val height = windowSize.windowHeight * 80 / 100
        vb!!.llDialog.layoutParams.width = width
        vb!!.llDialog.layoutParams.height = height
    }

    fun initViewListener(){
        //相册
        vb!!.btnCamera.onClick {
            createSaveInfo()
            saveKV("linkId", markerInfo?.id.toString())
            saveKV("dataType", ImageInfo.IMAGE_MARKER)
            saveKV("enableSelection", false)
            OperationLogger.logOperation(context, "[PhotoDialogActivity] SaveWorkRecordDialog 发送GO_PHOTO事件 linkId=${markerInfo?.id}, dataType=${ImageInfo.IMAGE_MARKER}")
            EventBus.getDefault().post(DataEvent(DataEvent.GO_PHOTO))//打开相册页面对话框
        }

        //取消保存
        vb!!.tvCancle.onClick {
            cancelSave()
            dismiss()
        }
        vb!!.imgClose.onClick {
          vb!!.tvCancle.performClick()
        }

        //确认保存
        vb!!.tvOk.onClick {
            confirmSave()
        }

//        vb!!.tvLayerName.onClick {
//            EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_BY_POINT, centerPoint))
//        }
    }

    private fun confirmSave() {

        val riverName1 = vb!!.tvRiverName.getValueData()
//        if (riverName1.isBlank()) {
//            tip(context, "${vb!!.tvRiverName.key}不得为空")
//            return
//        }

        val city1 = vb!!.tvCity.getValueData()
        if (city1.isBlank()) {
            tip(context, "${vb!!.tvCity.key}不得为空")
            return
        }

        val county1 = vb!!.tvCounty.getValueData()
        if (county1.isBlank()) {
            tip(context, "${vb!!.tvCounty.key}不得为空")
            return
        }

        val town1 = vb!!.tvTown.getValueData()
        if (town1.isBlank()) {
            tip(context, "${vb!!.tvTown.key}不得为空")
            return
        }

        val village1 = vb!!.tvVillage.getValueData()
//        if (village1.isBlank()) {
//            tip(context, "${vb!!.tvVillage.key}不得为空")
//            return
//        }

        val questionType1 = vb!!.cbQuestionType.getSelectValue()
        if (questionType1.isBlank()) {
            tip(context, "${vb!!.cbQuestionType.key}不得为空")
            return
        }

        val questionAttr1 = vb!!.tvQuestionAttr.getValueData()
        if (questionAttr1.isBlank()) {
            tip(context, "${vb!!.tvQuestionAttr.key}不得为空")
            return
        }


        val severity1 = vb!!.cbSeverity.getSelectValue()
        if (severity1.isBlank()) {
            tip(context, "${vb!!.cbSeverity.key}不得为空")
            return
        }

        val description1 = vb!!.tvDescription.getValueData()
        if (description1.isBlank()) {
            tip(context, "${vb!!.tvDescription.key}不得为空")
            return
        }

        val location1 = vb!!.tvLocation.getValueData()
        if (location1.isBlank()) {
            tip(context, "${vb!!.tvLocation.key}不得为空")
            return
        }

        val latLng1 = vb!!.tvLatLng.getValueData()
        if (latLng1.isBlank()) {
            tip(context, "${vb!!.tvLatLng.key}不得为空")
            return
        }

        val checkTime1 = vb!!.tvCheckTime.getValueData()
        if (checkTime1.isBlank()) {
            tip(context, "${vb!!.tvCheckTime.key}不得为空")
            return
        }

        val isFinish1 = vb!!.cbIsFinish.getSelectValue()
        if (isFinish1.isBlank()) {
            tip(context, "${vb!!.cbIsFinish.key}不得为空")
            return
        }

        val spotRectificationSituation1 = vb!!.tvSpotRectificationSituation.getValueData()


        val strGeometryJson = geometry?.toJson()
        val strCenterPointJson = centerPoint?.toJson()

        createSaveInfo()

        markerInfo?.apply {
            folderId = 0
            geometryJson = strGeometryJson.self()
            centerPointJson = strCenterPointJson.self()

            riverName = riverName1
            city = city1
            county =county1
            town =town1
            village =village1
            questionType = questionType1
            questionAttr = questionAttr1
            severity = severity1
            description = description1
            location = location1
            latLng = latLng1
            checkTime = TimeUtil.getStringToDate(checkTime1, Config.timeFormat1)
            isFinish = isFinish1
            spotRectificationSituation = spotRectificationSituation1

            isEdit.no {
                //新增情况
                geoType = drawType
                createTimeStamp = TimeUtil.getCurrentStamp()
            }

        }

        //更新标绘信息
        updateMarkerInfo()

        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
        EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_MARKER_DATA))

        OperationLogger.logOperation(context, "更新工作记录：${markerInfo?.questionType} 坐标：${markerInfo?.latLng}")
        dismiss()
    }

    //取消保存
    private fun cancelSave() {
        if (!isEdit) {
            markerInfo?.let {
                //删除已添加的附件
                db.deleteAccessoryInfoByLinkIdAndDataType(it.id.toString(), AccessoryInfo.ACCESSORY_MARKER)
//            val accessoryList = db.queryAccessoryInfoByLinkIdAndDataType(it.id.toString(), AccessoryInfo.ACCESSORY_MARKER)
//            if (CommonUtil.matchList(accessoryList))
//                "附件还没删干净".printMsg()
//            else
//                "附件还删干净了".printMsg()

                //删除已添加的图片
                //先查出来 图片列表，然后遍历列表删除图片文件，文件删完后，再删除数据库数据
                val imagelist = db.queryImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)
                CommonUtil.matchList(imagelist).yes {
                    imagelist!!.forEach { img ->
                        val file = File(img.filePath)
                        if (file.exists() && file.isFile) {
                            file.delete()
                        }
                    }
                }

                //删除数据库中的图片记录
                db.deleteImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)

//            if (CommonUtil.matchList(db.queryImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)))
//                "图片还没删干净".printMsg()
//            else
//                "图片还删干净了".printMsg()
                //删掉当前这条数据
                db.deleteMarker(it)


            }
        }

    }


    override fun dismiss() {
        super.dismiss()
        isEdit.no { markerInfo = null }
    }

    /**
     * 创建一个待保存对象
     */
    private fun createSaveInfo() {
        if (markerInfo == null) {
            markerInfo = MarkerInfo()
            db.saveMarker(markerInfo!!)
            markerInfo = db.queryLastMarkerInfo()
        }
    }

    /**
     * 更新保存标绘信息
     */
    private fun updateMarkerInfo() = markerInfo?.let { db.updateMarkerInfo(it) }

    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlePageView(event: MapEvent?) {
        if (event == null) return
        when (event.actionType) {
            //更新收藏信息
            MapEvent.QUERY_AREA_CALLBACK -> {

                event.data?.apply {
                    val value = event.data as String
                    val areaType = event.data2 as String


                     when(areaType){
                        "city" -> {
                            vb!!.tvCity.setSelection(value)

                            val cityItem = AppContext.app.districtHelper.getDistrictByName(value)
                            countyList = getChildDistrictList(cityItem!!)
                            countySelectionList = countyList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
                            vb!!.tvCounty.updateList(countySelectionList!!)
                        }
                        "county" ->{
                            vb!!.tvCounty.setSelection(value)
                            val countyItem = AppContext.app.districtHelper.getDistrictByName(value)
                            townList = getChildDistrictList(countyItem!!)
                            townSelectionList = townList?.map { SelectionListModel(it.distName, it.distName, it) } as MutableList
                            vb!!.tvTown.updateList(townSelectionList!!)
                        }
                         "town" ->{
                             vb!!.tvTown.setSelection(value)
                         }
                        else -> null
                    }

                }

            }

        }
    }


}
