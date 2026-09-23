package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.location.Poi
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.Graphic
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.isNotNull
import com.jameni.allutillib.common.PrintUtil
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.Info.PoisInfo
import com.jwch.gwyt_project.Info.ThemesInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.ThemeCalloutAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewThemeCalloutBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.util.OperationLogger
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.selfTempNo
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.showSingleDialog
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.KVData
import com.jwch.gwyt_project.model.NavigationPointModel
import com.jwch.gwyt_project.util.Base64Utils
import com.jwch.gwyt_project.util.HandleMapUtil
import com.jwch.gwyt_project.util.MapConverter
import com.jwch.gwyt_project.util.NavigationUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

class ThemeCallOut : RelativeLayout {

    open fun getLayoutId(): Int = R.layout.view_theme_callout
    var vb: ViewThemeCalloutBinding? = null

    private var activity: Activity? = null

    private var callout: Callout? = null//弹窗对象
    private var poiInfo: PoisInfo? = null //兴趣点对象
    var feature: Feature? = null//图斑的特征对象
    var graphic: Graphic? = null//图斑的特征对象 （画的）
    var selectThemes: ThemesInfo? = null//专题图对象（数据库）
    private var callOutType = 0//0 兴趣点，1 图斑
    private var linkId = "" //兴趣点id 或者 图斑id 作为关联id
    private var displayName = "" //展示 兴趣点类名称 或 图斑类型名称

    var dataList = mutableListOf<KVData>()
    private var adapter: ThemeCalloutAdapter? = null

    val keyList = mutableListOf<String>() //配置callout展示的字段
    lateinit var navigationUtil: NavigationUtil

    fun setCallout(callout: Callout?) {
        this.callout = callout
    }

    constructor(context: Context?) : super(context) {
        initViewData(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewData(context, attrs)
    }

    fun initViewData(context: Context?, attrs: AttributeSet?) {
        initView(context)
        initViewListener()
        initList()
        initShowKey()
    }


    fun initList() {
        vb!!.lvMain.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ThemeCalloutAdapter()
        vb!!.lvMain.adapter = adapter
    }

    fun initShowKey() {
        keyList.clear()
        val siluanKeyList = mutableListOf("问题描述", "位置", "下发时", "问题属性","问题编号") //四乱需要展示的属性
        val hedaoKeyList = mutableListOf("河湖名称")  //河道展示的属性
        val shuihuluList = mutableListOf("河流名","面积")  //水葫芦展示的属性
        val shuilibutuban = mutableListOf("编号","地物对象","图斑大类","河段(湖片)名称")  //水利部图斑的属性
        val heliuList = mutableListOf("河流名称","河流编号")  //河流
        val yanshi = mutableListOf("自然保护地面名称","自然保护地面名称","面积","立标类型")  //河流

        keyList.addAll(siluanKeyList)
        keyList.addAll(hedaoKeyList)
        keyList.addAll(shuihuluList)
        keyList.addAll(shuilibutuban)
        keyList.addAll(heliuList)
        keyList.addAll(yanshi)
    }

    fun initView(context: Context?) {
        context?.let {

            val contentView: View = LayoutInflater.from(it).inflate(getLayoutId(), null)
            contentView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            vb = ViewThemeCalloutBinding.bind(contentView)
            vb?.apply { addView(root) }
            navigationUtil = NavigationUtil(context)
        }
    }

    fun getCenterPoint() : Point?{
        if(feature !=null && feature!!.geometry.extent.center != null){
            return feature!!.geometry.extent.center
        }else if(graphic !=null && graphic!!.geometry.extent.center != null){
            return graphic!!.geometry.extent.center
        }else{
            return null
        }
    }

    fun initViewListener() {
        vb?.apply {
            //导航按钮
            llNavigation.onClick {

                if (getCenterPoint() == null) {
                    ToastUtils.show("暂无位置信息，导航失败")
                    return@onClick
                }

                showNormalDialog(context, "确定跳转到导航软件吗？") {
                    it.yes {
                        context.packageManager.getInstalledPackages(0)  //华为手机在获取所有包名时会弹窗询问，所有这里先调用一次，保证导航功能正常

                        val poi = GeometryEngine.project(getCenterPoint(), Config.sp4490) as Point
                        val point = NavigationPointModel(displayName, poi.y,poi.x)
                        navigationUtil.startNavigation(point)

                    }
                }
            }
            //关闭
            imgClose.onClick {
                if (CommonUtil.isNotNull(callout)) {
                    callout!!.dismiss()
                }
            }
            //打开相册
            llCamera.onClick {
                saveKV("linkId", linkId) //兴趣点id 或者 图斑id 作为关联id
                if (callOutType == 0) {
                    //兴趣点
                    saveKV("dataType", ImageInfo.IMAGE_POI)
                } else if (callOutType == 1) {
                    //图斑
                    saveKV("dataType", ImageInfo.IMAGE_PATCH)
                }
                saveKV("enableSelection", false)
                if (checkKeyword(linkId)) {
                    OperationLogger.logOperation(context, "[PhotoDialogActivity] ThemeCallOut 发送GO_PHOTO事件 linkId=$linkId, callOutType=$callOutType")
                    EventBus.getDefault().post(DataEvent(DataEvent.GO_PHOTO))
                }else{
                    showNormalDialog(context, "拍照前请先记录图斑") {
                        it.yes {
                            vb!!.llCollect.performClick()
                        }
                    }
                }


            }
            //收藏
            llCollect.onClick {

                if (checkKeyword(linkId)) {
                    if (callOutType == 0) {
                        ToastUtils.show("该兴趣点已收藏")
                    } else if (callOutType == 1) {
                        showNormalDialog(context, "是否取消收藏？"){
                            it.yes {
                                db.deleteCollectPatchById(linkId)
                                CommonUtil.tip(context, "操作成功")

                                EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_COLLECTION_DATA, 1))
                                EventBus.getDefault().post(DataEvent(DataEvent.CANCEL_COLLECT_PATCH))
                                OperationLogger.logOperation(context, "取消收藏图斑：${displayName}")
                            }
                        }
                    }
                    return@onClick
                }
                val dialog = CollectDialog(context)
                dialog.show()

                if (callOutType == 0) {
                    dialog.setPoiData(poiInfo!!.poiName, linkId, CommonUtil.getSelfValue(poiInfo!!.typeName))
                } else if (callOutType == 1) {


                    if (getCenterPoint() == null) {
                        dialog.setPatchData(displayName, linkId, CommonUtil.getSelfValue(selectThemes!!.id), CommonUtil.getSelfValue(selectThemes!!.themeName))
                    }else{
                        val centerPoint = GeometryEngine.project(getCenterPoint(), Config.sp4490) as Point
                        dialog.setPatchData2(displayName, linkId, CommonUtil.getSelfValue(selectThemes!!.id), CommonUtil.getSelfValue(selectThemes!!.themeName), centerPoint)
                    }

                }

            }

            //事件处理状态
            llHandleStaus.onClick {
                if (callOutType == HandleMapUtil.TYPE_THEME) {
                    //图斑
                    val map = java.util.HashMap<String, Any>()
                    map["theme"] = selectThemes!!
                    map["feature"] = feature!!
                    map["linkId"] = linkId
                    map["displayName"] = displayName
                    EventBus.getDefault().post(DataEvent(DataEvent.SHOW_GEOMETRY_DETAIL2, map))
                }
            }
            //更多详情
            llMore.onClick {
                //更多详情
                if (callOutType == HandleMapUtil.TYPE_POI) {
                    //兴趣点
                    val map = java.util.HashMap<String, Any>()
                    map["info"] = poiInfo!!
                    EventBus.getDefault().post(DataEvent(DataEvent.SHOW_POI_DETAIL, map))
                } else if (callOutType == HandleMapUtil.TYPE_THEME) {
                    //图斑
                    val map = java.util.HashMap<String, Any>()
                    map["theme"] = selectThemes!!
                    map["feature"] = feature!!
                    map["linkId"] = linkId
                    map["displayName"] = displayName
                    EventBus.getDefault().post(DataEvent(DataEvent.SHOW_GEOMETRY_DETAIL, map))
                }
            }
        }
    }


    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    fun setLayerInfo(map: HashMap<String?, Any?>) {
        callOutType = 1

        if (map["feature"] is Feature?) {
            feature = map["feature"] as Feature?
        }else if(map["feature"] is Graphic?){
            graphic = map["feature"] as Graphic
        }

        selectThemes = map["info"] as ThemesInfo?
        setListData()


        if (feature?.attributes?.get(Config.primaryKey) != null) {
            linkId = feature!!.attributes[Config.primaryKey].toString()
        } else if (feature!!.attributes[Config.primaryKey2] != null) {
            linkId = feature!!.attributes[Config.primaryKey2].toString()
        } else if (graphic?.attributes?.get(Config.primaryKey) != null) {
            linkId = graphic!!.attributes[Config.primaryKey].toString()
        }  else if (graphic?.attributes?.get(Config.primaryKey2) != null) {
            linkId = graphic!!.attributes[Config.primaryKey2].toString()
        }else {
            linkId = "0"
        }

        PrintUtil.printMsg("图斑的linkid:$linkId")

        val code = if (feature!!.attributes[Config.primaryKey] == null) "" else feature!!.attributes[Config.primaryKey].toString()
        val code2 = if (feature!!.attributes[Config.primaryKey2] == null) "" else feature!!.attributes[Config.primaryKey2].toString()

        if(code.isNotBlank()){
            displayName = "${selectThemes!!.themeName}-${code}"
            vb!!.llHandleStaus.show()
        }else if(code2.isNotBlank()){
            displayName = "${selectThemes!!.themeName}-${code2}"
            vb!!.llHandleStaus.gone()
        }else {
            displayName = "${selectThemes!!.themeName}-${linkId}"
            vb!!.llHandleStaus.gone()
        }

        if(selectThemes!!.themeName.contains("乱占")
            ||  selectThemes!!.themeName.contains("乱堆")
            ||  selectThemes!!.themeName.contains("乱建")
            ||  selectThemes!!.themeName.contains("乱采")
            ||  selectThemes!!.themeName.contains("其他")
            || selectThemes!!.themeName.contains("水利部")
            || selectThemes!!.themeName.contains("自查自纠")
        ){
            vb!!.llHandleStaus.show()
            vb!!.llCamera.show()
            vb!!.llCollect.show()
            vb!!.topCard.show()
        }else{
            vb!!.llHandleStaus.gone()
            vb!!.llCamera.gone()
            vb!!.llCollect.gone()
            vb!!.topCard.gone()
        }

        if(selectThemes!!.themeName.contains("水利部")||selectThemes!!.themeName.contains("自查自纠")){
            vb!!.llHandleStaus.gone()
        }

        updateCollectUI()

        OperationLogger.logOperation(context, "查看图斑：${displayName}")

        //河流河道 不可以导航
        vb!!.llNavigation.visiable(selectThemes?.themeName != "河流" && selectThemes?.themeName != "河道" && selectThemes?.themeName != "河管线"&& selectThemes?.themeName != "河道管理范围线")
    }

    //模糊查询字段
    fun filterMapByPartialKeys(originalMap: Map<String, Any>, priorityPatterns: List<String>): Map<String, Any> {
        val regex = priorityPatterns.joinToString("|") { "($it)" }.toRegex()
        return originalMap.filterKeys { key ->
            regex.containsMatchIn(key)
        }
    }

    //精确查询字段
    fun filterMapByPriorityKeys(originalMap: Map<String, Any>, priorityKeys: MutableList<String>): Map<String, Any> {
        // 将 List 转换为 Set 以提高查询效率（O(1) 时间复杂度）
        val priorityKeySet = priorityKeys.toSet()
        // 过滤出 key 存在于优先级列表中的条目
        return originalMap.filterKeys { it in priorityKeySet }
    }

    fun setListData() {


        isNotNull(feature).yes {
            dataList.clear()

            if (!selectThemes?.themeName.isNullOrBlank()) {
                vb!!.tvTitle.text = selectThemes?.themeName
            } else {
                vb!!.tvTitle.text = feature!!.featureTable.tableName
            }

            val attrs = feature!!.attributes //图斑属性

            // 设置统计字段值
            updateStatFields(attrs)

            val prioritySet = keyList.toSet()
            val isRiverTheme = selectThemes?.themeName.self() == "河道"
                    || selectThemes?.themeName.self() == "河管线"

            MapConverter.convert(attrs).forEach { item ->
                if (item.key !in prioritySet) return@forEach

                if (item.value is GregorianCalendar) {
                    val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.US)
                    val v = simpleDateFormat.format((item.value as GregorianCalendar).time)
                    dataList.add(KVData(item.key, v))
                } else {
                    var value = "${item.value}"

                    //河道字段值是加密的 需要解码再展示
                    if (isRiverTheme) {
                        if (Base64Utils.isBase64(value)) {
                            value = Base64Utils.decode(value) // 解码
                        }
                    }
                    dataList.add(KVData(item.key, value.selfTempNo()))
                }
            }

//            val filterMap = filterMapByPartialKeys(attrs, keyList)
//
//            QueryGisUtil.queryUtil.printAttrbuite(feature)`
//            filterMap.let {
//                it.forEach { item ->
//                    if (item.value is GregorianCalendar) {
//                        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.US)
//                        val v = simpleDateFormat.format((item.value as GregorianCalendar).time)
//
//                        dataList.add(KVData(item.key, v))
//                    } else {
//                        dataList.add(KVData(item.key, "${item.value}"))
//                    }
//
//                }
//            }


            adapter!!.update(dataList)
        }
    }

    /**
     * 更新统计字段：是否问题、是否办结、问题类型
     */
    private fun updateStatFields(attrs: Map<String, Any>) {
        // 是否问题：原始字段名"是否问"
        val isProblemValue = attrs["是否问"]?.toString() ?: "--"
        vb!!.tvStatIsProblem.text = when (isProblemValue) {
            "是问题" -> "是"
            "不是问题" -> "否"
            "待复核" -> "待复核"
            else -> isProblemValue.ifBlank { "--" }
        }

        // 是否办结：原始字段名"事件状"
        val isFinishedValue = attrs["事件状"]?.toString() ?: "--"
        vb!!.tvStatIsFinished.text = when (isFinishedValue) {
            "已办结" -> "是"
            "未办结" -> "否"
            else -> isFinishedValue.ifBlank { "--" }
        }

        // 问题类型：原始字段名"问题类"
        val problemTypeValue = attrs["问题类"]?.toString() ?: "--"
        vb!!.tvStatProblemType.text = problemTypeValue.ifBlank { "--" }
    }

    /**
     * 判断
     *
     * @param mLink
     * @return
     */
    fun checkKeyword(mLink: String?): Boolean {
        //判断是否已经收藏过了
        if (!CommonUtil.isNotEmpty(mLink)) return false
        return if (callOutType == 0) {
            val count = db.queryPoiCollectionCountByLinkId(linkId)
            count != null && count > 0
        } else {
            val count = db.queryPatchCollectionCountByLinkId(linkId)
            count != null && count > 0
        }
    }

    fun updateCollectUI(){
        if (checkKeyword(linkId)) {
            vb!!.tvCollect.text = "已收藏"
            vb!!.ivCollect.setImageResource(R.mipmap.icon_star_yellow)
        }else{
            vb!!.tvCollect.text = "收 藏"
            vb!!.ivCollect.setImageResource(R.mipmap.icon_star)
        }
    }


}