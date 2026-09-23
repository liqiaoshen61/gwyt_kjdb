package com.jwch.gwyt_project.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.CompoundButton
import android.widget.RadioButton
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.basepage_lib.util.PageManager
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.Info.GraphicInfo
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.Info.MarkerInfo
import com.jwch.gwyt_project.Info.PoisInfo
import com.jwch.gwyt_project.Info.ThemesInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.adapter.ztt.ZttItem
import com.jwch.gwyt_project.common.Tools
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.PageMainBinding
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.isShow
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.showSingleDialog
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.BufferAnalysisModel
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.GeoJsonModel
import com.jwch.gwyt_project.model.ShpModel
import com.jwch.gwyt_project.model.ImageLayerItem
import com.jwch.gwyt_project.model.InterSectionModel
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.ArcgisUtils
import com.jwch.gwyt_project.util.CompassHelper
import com.jwch.gwyt_project.util.DeviceIdMatcher
import com.jwch.gwyt_project.util.DrawUtil
import com.jwch.gwyt_project.util.FunctionControlUtil
import com.jwch.gwyt_project.util.GetJsonUtil
import com.jwch.gwyt_project.util.GeometryEditorHelper
import com.jwch.gwyt_project.util.HandleMapUtil
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.util.LicenseGenerator
import com.jwch.gwyt_project.util.LocationCacheManager
import com.jwch.gwyt_project.util.MapStatePrefs
import com.jwch.gwyt_project.util.MapUtil
import com.jwch.gwyt_project.util.OperationLogger
import com.jwch.gwyt_project.util.PositionUtil
import com.jwch.gwyt_project.util.UnifiedLocationManager
import com.jwch.gwyt_project.util.download.DownloadTaskListener
import com.jwch.gwyt_project.util.download.DownloadTaskUtil
import com.jwch.gwyt_project.view.AreaPopWindow
import com.jwch.gwyt_project.view.CoordinateInputPointDialog
import com.jwch.gwyt_project.view.CoordinateMarkerAnalysisDialog
import com.jwch.gwyt_project.view.CoordinateMarkerDialog
import com.jwch.gwyt_project.view.CoordinatePositionDialog
import com.jwch.gwyt_project.view.ExcelListDialog
import com.jwch.gwyt_project.view.LicenseInputDialog
import com.jwch.gwyt_project.view.MarkerCollectionListDialog
import com.jwch.gwyt_project.view.NetDialog
import com.jwch.gwyt_project.view.NetworkLockDialog
import com.jwch.gwyt_project.view.NumLockDialog
import com.jwch.gwyt_project.view.NumLockDialog.Companion.ACTION_INPUT_PASSWORD_UNLOCK
import com.jwch.gwyt_project.view.SettingDialog
import com.jwch.gwyt_project.view.UncancelDialog
import com.jwch.gwyt_project.view.UncancelDialog2
import com.jwch.gwyt_project.view.UnitChooiceDialog
import com.jwch.gwyt_project.view.popwindow.ToolPopWindow
import com.liulishuo.filedownloader.BaseDownloadTask
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.Calendar


class MainActivity : FullScreenActivity<PageMainBinding>(), CompoundButton.OnCheckedChangeListener {

    companion object {
        private const val KEY_BASE_MAP_NAME = "key_base_map_name"
        private const val KEY_MAP_CENTER_X = "key_map_center_x"
        private const val KEY_MAP_CENTER_Y = "key_map_center_y"
        private const val KEY_MAP_SCALE = "key_map_scale"

        /** 用定位 bearing 校正罗盘时，缓存方位允许的最大年龄（毫秒）。更新的视为陈旧值不采用。 */
        private const val SYNC_BEARING_MAX_AGE_MS = 4000L
    }

    // 保存 Activity 状态，用于进程重建后恢复
    private var savedInstanceState: Bundle? = null

    lateinit var handleMapUtil: HandleMapUtil   //原地图
    lateinit var mapCompareUtil: HandleMapUtil  //对比地图
    lateinit var geometryEditorHelper: GeometryEditorHelper  //几何编辑器
    lateinit var locationManager: UnifiedLocationManager

    /** 设备朝向提供器：驱动地图上「我的位置」箭头随设备转动（懒加载，首次用到时才建） */
    private val compassHelper: CompassHelper by lazy { CompassHelper(applicationContext) }

    /** 单次定位等待期的进度提示更新（10s/20s 改底部标签文案） */
    private val locatingHandler = Handler(Looper.getMainLooper())
    private var locateDotsCount = 0 // 动画点号计数
    private var locateDotsBaseText = "正在定位，请稍候" // 动画基础文案（10s/20s 会切换）
    private var locateDotsRunnable: Runnable? = null
    var settingDialog: SettingDialog? = null
    private var networkLockDialog: NetworkLockDialog? = null
    private lateinit var unitChooiceDialog: UnitChooiceDialog

    private val editGraphicInfo: GraphicInfo? = null//准备编辑的图形对象
    private var coordinatePositionDialog: CoordinatePositionDialog? = null//坐标定位对话框
    private var coordinateInputPointDialog: CoordinateInputPointDialog? = null//坐标定位对话框
    private var coordinateMarkerDialog: CoordinateMarkerDialog? = null//坐标标绘对话框

    private var legendList: MutableList<ThemesInfo>? = null  //图例数组
    var numLockPanelUtil: NumLockDialog? = null
    lateinit var netDialog: NetDialog
    lateinit var uncancelDialog: UncancelDialog
    lateinit var uncancelDialog2: UncancelDialog2
    lateinit var mActivity: MainActivity
    private var coordinateMarkerAnalysisDialog: CoordinateMarkerAnalysisDialog? = null
    lateinit var toolPopWindow: ToolPopWindow
    lateinit var areaPopWindow: AreaPopWindow

    var queryThemeList = mutableListOf<ThemesInfo>()//需要查询的图层 （默认查所有的业务数据）

    var downloadUtil: DownloadTaskUtil? = null

    var distance = 100.0 //缓冲区范围
    var queryLocationPoint: Point? = null
    var queryInputPoint: Point? = null

    var imageScreenShot: ImageInfo? = null

    val excelDialog: ExcelListDialog by lazy {
        ExcelListDialog(context) {
            //选中excel回调
            handleMapUtil.markerUtil.addPointList(it)

            val polygon = Polygon(handleMapUtil.markerUtil.pointCollection)
            handleMapUtil.mapView.setViewpointGeometryAsync(polygon, 100.0)
//            handleMapUtil.mapView.setViewpointCenterAsync(polygon.extent.center)
        }
    }

    var markerDialog: MarkerCollectionListDialog? = null
    var isMarkerAnalysisAfterSave = false //是否标绘模式下 保存标绘后叠加分析

    var bufferAnalyisQueryType = 0 //缓冲区分析的查询方式  0 定位点查询   1 自定位点位  2 坐标输入

    private var isFirstContinuousLocation = true // 持续定位第一次回调标志
    private var licenseInputDialog: LicenseInputDialog? = null//坐标定位对话框


    override fun initView() {

        OperationLogger.logOperation(this, "软件启动")

        initDeviceId()

        mActivity = this
        EventBus.getDefault().register(this)

        // 同步网络状态 - 在 EventBus 注册后立即同步当前网络状态
        "initView: 当前网络状态 hasNet=${AppContext.hasNet}".printMsg()
        if (AppContext.hasNet) {
            showNetworkLock()
        } else {
            hideNetworkLock()
        }


        initMap()
        initLocation()
        initCompass()

        legendList = mutableListOf()
        initLockPane()
        initToolPopWindow()
        initAreaPopWindow()

//        vb.includeViewOverlyAnalysisActions.llAnalysisActions.show()

        fitFragWidth()
        initFunctionControll()

        vb.tvArea.onClick {
            areaPopWindow.isShowing.yes {
                showAreaPopWindow(false)
            }.no {
                showAreaPopWindow(true)
            }
        }

        vb.easySearchBar.initView(this, "请输入问题编号")

        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
//            vb.includeViewRightTools.ivRelocation.performClick()
            handleMapUtil.relocation()
            vb.tvBaseMapInfo.text = "当前影像：${handleMapUtil.baseMapStack.peek()}"

        }

        //验证授权码
        verifyLicense()
        //生成授权文件
//        LicenseGenerator.main()

        // 恢复地图状态（处理进程重建的情况）
        restoreMapState()

    }

    /** 联网锁定：显示全屏锁定弹窗，拦截用户一切操作 */
    private fun showNetworkLock() {
        if (networkLockDialog == null) {
            networkLockDialog = NetworkLockDialog(this)
        }
        if (networkLockDialog?.isShowing != true) {
            networkLockDialog?.show()
        }
    }

    /** 联网锁定：解除锁定，移除弹窗 */
    private fun hideNetworkLock() {
        networkLockDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        networkLockDialog = null
    }

    fun verifyLicense(){
        val regionInfoJson = GetJsonUtil.getJsonFromFile(Config.APPDB_PATH +"region_info.txt").self().trim()
        val regionInfo = try {
            org.json.JSONObject(regionInfoJson).optString("region", "")
        } catch (e: Exception) {
            regionInfoJson
        }

        // 验证License
        AppContext.app.verifyLicense(regionInfoJson)
    }


    //根据deviceId 授权的设备才能使用
    fun initDeviceId() {

        DeviceIdMatcher.isAuthorizedDevice(this).no {
            if (!::uncancelDialog.isInitialized) {
                uncancelDialog = UncancelDialog(this)
            }
            if (!uncancelDialog.isShowing && FunctionControlUtil.instances.AUTHORIZE_DEVICE) {
                uncancelDialog.show()
            }
        }
    }

    //批量删除未加密的文件
    fun deleteFileByPath(filePath: String) {
        val file = File(filePath)
        // 检查并删除未加密文件
        if (file.exists() && file.isFile) {
            file.delete()
        }
    }

    fun initFunctionControll() {
        val fcUtil = FunctionControlUtil.instances

        vb.includeMenuList.llMenuArea.visiable(fcUtil.BUTTON_AREA)
        vb.includeMenuList.llMenuSearch.visiable(fcUtil.BUTTON_SERACH)
        vb.includeMenuList.llMenuMaps.visiable(fcUtil.BUTTON_THEME_LAYER)
        vb.includeMenuList.llMenuCollection.visiable(fcUtil.BUTTON_COLLECTION)
        vb.includeMenuList.llMenuTools.visiable(fcUtil.BUTTON_TOOL_BOX)
        vb.includeMenuList.llMenuSetting.visiable(fcUtil.BUTTON_SETTING)

        vb.includeViewRightTools.ivScreenshots.visiable(fcUtil.BUTTON_CHANGE_SCREEN_SHOTS)
        vb.includeViewRightTools.ivMyLocation.visiable(fcUtil.BUTTON_LOCATION)
        vb.includeViewRightTools.ivClearMap.visiable(fcUtil.BUTTON_CLEAR_MAP)
        vb.includeViewRightTools.ivRelocation.visiable(fcUtil.BUTTON_RELOCATION)
        vb.includeViewRightTools.ivChangeBaseMap.visiable(fcUtil.BUTTON_CHANGE_BASE_MAP)

    }

    fun fitFragWidth() {
        val windowWidth = GetWindowSize(context).windowWidth
        val fragWidth = windowWidth * 30 / 100
        vb.apply {
            includeViewAreaList.llAreaList.layoutParams.width = fragWidth
            includeViewSearch.llSearch.layoutParams.width = fragWidth
            includeViewSwitchMap.llSwitchMap.layoutParams.width = fragWidth
            includeViewCollection.llCollection.layoutParams.width = fragWidth
            includeViewQueryResultList.llQueryResultList.layoutParams.width = fragWidth
            includeViewQueryResultDetail.llQueryResultDetail.layoutParams.width = fragWidth
            includeViewQueryResultDetailList.llQueryResultDetailList.layoutParams.width = fragWidth
            includeViewQueryBufferResultList.llQueryBufferResultList.layoutParams.width = fragWidth
            includeViewGeoCollection.llCollection.layoutParams.width = fragWidth
            includeViewCollectionStatis.llCollectionStatis.layoutParams.width = fragWidth
        }

    }

    fun initToolPopWindow() {
        if (!::toolPopWindow.isInitialized) {
            toolPopWindow = ToolPopWindow(this)
            toolPopWindow.actionBlock = { model, i ->
                toolPopWindow.dismiss()
                when (model.txt) {
                    "叠加分析" -> {
                        handleMapUtil.startOverlyingAnalysis().yes {
                        }
                        vb.includeViewOverlyAnalysisActions.llAnalysisActions.show()

                        vb.includeViewMarkerActions.llMarkerActions.gone()//隐藏标绘
                        vb.includeViewHandSearchActions.llHandSearchActions.gone()//隐藏手势查询
                        vb.includeViewMeasureActions.llMeasureActions.gone()//隐藏测量
                        vb.includeViewOverlyAnalysisActions.llAnalysisActions.show() //展示叠加分析
                    }

                    "标绘" -> {
                        vb.includeViewMarkerActions.llMarkerActions.show()
                        vb.includeViewHandSearchActions.llHandSearchActions.gone()//隐藏手势查询
                        vb.includeViewMeasureActions.llMeasureActions.gone()//隐藏测量
                        vb.includeViewOverlyAnalysisActions.llAnalysisActions.gone()//隐藏叠加分析
                    }

                    "测量" -> {
                        vb.includeViewMeasureActions.llMeasureActions.show()
                        vb.includeViewHandSearchActions.llHandSearchActions.gone()//隐藏手势查询
                        vb.includeViewMarkerActions.llMarkerActions.gone()//隐藏标绘
                        vb.includeViewOverlyAnalysisActions.llAnalysisActions.gone()//隐藏叠加分析
                    }

                    "对比地图" -> {
                        vb.llMap2.show()
                        vb.llMap2.requestLayout() //重新绘制布局，不然会有问题
                        //这一句是为了让两边同步，第二个是让主地图界面不会被压缩展示
                        MapUtil.getMapUtil()
                            .moveToCenter(vb.mapView, ArcgisUtils.getCenterPoint(vb.mapView))
                    }
                }
            }
        }
    }

    fun initAreaPopWindow() {
        if (!::areaPopWindow.isInitialized) {
            areaPopWindow = AreaPopWindow(this)
            areaPopWindow.actionBlock = { model ->
                vb.tvArea.text = model.distName
            }
        }
    }


    /**
     * 初始化锁屏
     */
    fun initLockPane() {

        if (Tools.getIsOpenLock()) {
            //防止重复展示dialog
            if (numLockPanelUtil == null) {
                numLockPanelUtil = NumLockDialog(this, ACTION_INPUT_PASSWORD_UNLOCK)
                numLockPanelUtil!!.errorMaxBlock = {
                    showSingleDialog(this, "数据销毁中，软件即将关闭") { _, _ ->
                        deleteFileByPath(Config.APPDB_PATH + "AppDb.db")
                        deleteFileByPath(Config.GDB_PATH + "河道.geodatabase")
                        deleteFileByPath(Config.GDB_PATH + "河道范围管理线.geodatabase")
                        deleteFileByPath(Config.GDB_PATH + "河流.geodatabase")
                        PageManager.exit()
                    }
                }
            }
            if (!numLockPanelUtil!!.isShowing) {
                numLockPanelUtil!!.show()
                numLockPanelUtil!!.vb!!.numLockView.resetResult()
            }

        }
    }


    //初始化地图
    private fun initMap() {
        //基本地图
        handleMapUtil = HandleMapUtil(this, context, vb.mapView, vb.mapView2)
        handleMapUtil.easySearchBar = vb.easySearchBar
        // 初始化几何编辑器
        geometryEditorHelper = GeometryEditorHelper(vb.mapView)
        //比对地图
//        mapCompareUtil = HandleMapUtil(this, this, vb.mapView2, vb.mapView)
    }

    //初始化定位
    @SuppressLint("MissingPermission")
    private fun initLocation() {
        locationManager = UnifiedLocationManager(context!!)
        //设置里获取 是否开了持续定位
        val isContinuousLocation = getKV(Keys.IS_CONTINUOUS_LOCATION, false)
        isContinuousLocation.yes {
            vb.tvContinuous.show()
            getLocation(1)
        }
    }

    /**
     * 初始化设备朝向（罗盘）。
     * 箭头图标 icon_location_arrow_up 是朝上的，把设备朝向换算成屏幕角度写进符号，
     * 就能得到「箭头指向设备前方」的效果（百度/高德那种）。
     */
    private fun initCompass() {
        compassHelper.onHeadingChanged = { _ ->
            // 传感器回调已在主线程（CompassHelper 内部指定了主线程 Handler）
            if (handleMapUtil.hasMyLocation()) {
                handleMapUtil.updateMyLocationRotation(currentArrowAngle())
            }
        }
    }

    /**
     * 计算箭头在屏幕上应旋转到的角度。
     * PictureMarkerSymbol 的角度以屏幕上方为 0，所以地图被转动过时要减掉地图旋转角，
     * 否则用户转一下地图箭头就指歪了。
     */
    private fun currentArrowAngle(): Float {
        // 开关关闭时箭头固定朝上（屏幕正北），不随设备旋转
        if (!Config.showHeadingOnLocation) return 0f
        val mapRotation = vb.mapView?.mapRotation?.toFloat() ?: 0f
        return compassHelper.screenAngle(mapRotation)
    }

    /**
     * 用最近一次定位的 bearing 校正罗盘朝向（用于鸿蒙等磁力失效机型的游戏旋转向量漂移）。
     * 只在缓存里存在有效方位（设备在动时才非 0）时调用；静止时 bearing 为 0，跳过。
     * 额外加新鲜度防护：bearing 超过 [SYNC_BEARING_MAX_AGE_MS] 未更新视为陈旧值，
     * 不采用——否则持续定位里某些 provider 合并的旧方位会反复把箭头钉在同一个错误角度。
     * 罗盘未启动（开关关闭）时 syncHeading 内部会直接忽略。
     */
    private fun syncHeadingFromCache() {
        if (!Config.showHeadingOnLocation) return
        val loc = LocationCacheManager.getLastKnownLocation() ?: return
        val bearing = loc.bearing
        val fresh = System.currentTimeMillis() - loc.timestamp <= SYNC_BEARING_MAX_AGE_MS
        if (bearing != 0f && fresh) {
            compassHelper.syncHeading(bearing)
        }
    }

    private fun getFilePath(fileName: String): String {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileDir = File(downloadsDir, packageName)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        return "${fileDir.path}${File.separator}${fileName}"

    }

    fun downloadFile(url: String, fileName: String): DownloadTaskUtil {


        val downloadUrl = url.self()
//        val mSinglePath = getFilePath("${fileName.self()}.xlsx")
        val mSinglePath = "/sdcard/${fileName.self()}"
        "downloadUrl:${downloadUrl}".printMsg()
        "mSinglePath:${mSinglePath}".printMsg()

        if (downloadUtil == null) {

            downloadUtil =
                DownloadTaskUtil(downloadUrl, mSinglePath, false, object : DownloadTaskListener {
                    override fun downloadStatusListener(task: BaseDownloadTask?, status: Int) {

                        val total = task?.totalBytes ?: 0
                        val currentByte = task?.smallFileSoFarBytes ?: 0
//                    vb!!.progressBar.max = total
//                    vb!!.progressBar.progress = currentByte
                        val currentProgress = (currentByte * 1.0 / total) * 100

                        "文件总大小= $total 已下载大小byte= $currentByte 下载任务状态 = $status".printMsg()

                        when (status) {
                            DownloadTaskUtil.DOWNLOAD_START -> {

                            }

                            DownloadTaskUtil.DOWNLOAD_PROCESSING -> {

                            }

                            DownloadTaskUtil.DOWNLOAD_PAUSE -> {
                            }

                            DownloadTaskUtil.DOWNLOAD_COMPLETE -> {
                                ToastUtils.show("下载完成，文件路径：${mSinglePath}")
                            }

                            DownloadTaskUtil.DOWNLOAD_ERROR -> "=======下载出错了===========".printMsg()
                            else -> {
                            }
                        }
                    }
                })
        }

        return downloadUtil!!
    }


    @SuppressLint("MissingPermission")
    override fun initViewListener() {
        vb.btnVerifyGpkg.setOnClickListener {
            vb.btnVerifyGpkg.isEnabled = false
            vb.btnVerifyGpkg.text = "正在验证…"
            val probeDirectory = File(filesDir, "gpkg_probe")
            Thread({
                val result = com.jwch.gwyt_project.util.GpkgProbe.run(probeDirectory)
                android.util.Log.i("GpkgProbe", result)
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        vb.btnVerifyGpkg.isEnabled = true
                        vb.btnVerifyGpkg.text = "验证 GPKG"
                        val text = android.widget.TextView(this).apply {
                            setPadding(32, 24, 32, 24)
                            setTextIsSelectable(true)
                            this.text = result
                        }
                        val scroll = android.widget.ScrollView(this).apply { addView(text) }
                        android.app.AlertDialog.Builder(this)
                            .setTitle("GDAL / GPKG 验证结果")
                            .setView(scroll)
                            .setPositiveButton("确定", null)
                            .show()
                    }
                }
            }, "gpkg-probe").start()
        }
        //防止穿透
        vb.includeViewAreaList.llAreaList.onClick { }
        vb.includeViewCollection.llCollection.onClick { }
        vb.includeViewGeoCollection.llCollection.onClick { }
        vb.includeViewSearch.llSearch.onClick { }
        vb.includeViewSwitchMap.llSwitchMap.onClick { }
        vb.includeViewQueryResultList.llQueryResultList.onClick { }
        vb.includeViewQueryResultDetail.llQueryResultDetail.onClick { }
        vb.includeViewQueryResultDetailList.llQueryResultDetailList.onClick { }
        vb.includeViewQueryResultReport.llQueryResultReport.onClick { }
        vb.includeViewOverlyAnalysisResultList.llOverlyAnalysisList.onClick { }
        vb.includeViewOverlyAnalysisResultList.llOverlyAnalysisList.onClick { }
        vb.includeViewQueryBufferResultList.llQueryBufferResultList.onClick { }

        vb.includeSwitchMapPare.llSwitchMap2.onClick { }
        vb.includeViewMarkerActions.llMarkerActions.onClick { }
        vb.includeViewMeasureActions.llMeasureActions.onClick { }

        vb.includeViewCollectionStatis.llCollectionStatis.onClick { }

        vb.tvBaseMapInfo.onClick {


//            clearAllKv()
//            handleMapUtil.writeShp()
//            handleMapUtil.displayShpOnMap()

            //TODO 测试文件下载
//            val downUrl = "https://mirrors.tuna.tsinghua.edu.cn/github-release/tuna/thuthesis/thuthesis-v5.4.2.zip"
//            downloadFile(downUrl, "100mb.zip").startTask()


            //TODO 测试导出excel
//            val fileName = "111"
//            val titleList = mutableListOf("天时","地利","人和","强子")
//            val valueListList = mutableListOf<MutableList<String>>()
//
//            for (i in 0..2){
//                val valueList = mutableListOf<String>()
//                valueList.add("${i * 1}")
//                valueList.add("${i * 2}")
//                valueList.add("${i * 3}")
//·
//                valueListList.add(valueList)
//            }
//
//            ExcelOutPutUtil.export(Config.OUTPUT_OVERLAY_PATH, fileName, titleList, valueListList)

        }

        //切换测量面积的单位
        vb.includeViewMarkerActions.llMarkerUnit.onClick {
            if (!::unitChooiceDialog.isInitialized) {
                unitChooiceDialog = UnitChooiceDialog(context!!, object : ActionListener {
                    override fun onAction(obj: Any?, flag: Int) {
                        handleMapUtil.markerUtil.unitType = flag
                    }
                })
            }

            if (!unitChooiceDialog.isShowing) {
                unitChooiceDialog.show()
            }
        }

        vb.includeViewMarkerActions.llMarkerPreStep.onClick {
            //如果不是叠加分析完成状态 则标绘可以回退
            (handleMapUtil.actionType == HandleMapUtil.ACTION_OVERLYING_ANALYSIS_FINISH).yes {
                ToastUtils.show("当前状态不可回退")
            }.no {
                handleMapUtil.preStep()
//                mapCompareUtil.preStep()
            }

        }
        vb.includeViewMarkerActions.llMarkerReset.onClick {
            //标绘重置
            handleMapUtil.resetDraw()
//            mapCompareUtil.resetDraw()

            if (vb.includeViewMarkerActions.rbPolygon.isChecked) {
                handleMapUtil.startMarker(DrawUtil.DRAW_POLYGON)
//                mapCompareUtil.startMarker(DrawUtil.DRAW_POLYGON)
            }

            handleMapUtil.clearAnalysisResultPatch()

        }
        vb.includeViewHandSearchActions.tvHandSearchPreStep.onClick {
            //手势查询搜索
            handleMapUtil.preStep()
            //mapCompareUtil.preStep()
        }
        vb.includeViewHandSearchActions.tvHandSearchReset.onClick {
            //手势查询重置
            handleMapUtil.resetDraw()
            //mapCompareUtil.resetDraw()
        }
        vb.includeViewHandSearchActions.tvHandSearchCancel.onClick {
            //手势查询取消
            vb.includeViewHandSearchActions.llHandSearchActions.gone()
            handleMapUtil.endHandSearch()
//            mapCompareUtil.endHandSearch()

        }
        //定位点的 空间检索
        vb.includeViewBufferActions.llBufferLoaction.onClick {
            bufferAnalyisQueryType = 0
            executeBufferQuery(distance)
            EventBus.getDefault().post(DataEvent(DataEvent.INIT_BUFFER_ANALYSIS))
        }
        //自定义点位的 空间检索
        vb.includeViewBufferActions.llBufferCustom.onClick {
            bufferAnalyisQueryType = 1
            handleMapUtil.startDrawBufferPoint()
//            showSingleDialog(this, "请在地图上点击一个位置") { _, _ -> }
            ToastUtils.show("请在地图上点击一个位置")

        }
        //输入点 空间检索
        vb.includeViewBufferActions.llBufferInput.onClick {
            handleMapUtil.endMarker()

            if (coordinateInputPointDialog == null) {
                coordinateInputPointDialog = CoordinateInputPointDialog(
                    context, handleMapUtil.mapView,
                    object : ActionListener {
                        override fun onAction(obj: Any?, flag: Int) {}
                    })
                coordinateInputPointDialog!!.mActivity = this
            }
            //防止重复展示dialog
            if (!coordinateInputPointDialog!!.isShowing) {
                coordinateInputPointDialog?.show()
            }
        }

        vb.includeViewBufferActions.llBufferReset.onClick {
            handleMapUtil.endMarker()
            handleMapUtil.cleaBufferLayer()
            EventBus.getDefault().post(DataEvent(DataEvent.RESERT_BUFFER_ANALYSIS))
        }

        vb.includeViewBufferActions.llCancelBuffer.onClick {
            //取消空间检索
            vb.includeViewBufferActions.llBufferAnalysisActions.gone()
            vb.includeViewQueryBufferResultList.llQueryBufferResultList.gone()
            handleMapUtil.endMarker()

            vb.includeMenuList.ivAnalysis.setImageResource(R.mipmap.icon_search_c)
            handleMapUtil.cleaBufferLayer()
        }


        vb.includeViewMarkerActions.llCancleMarker.onClick {
            //取消标绘
            vb.includeViewMarkerActions.llMarkerActions.gone()
            handleMapUtil.endMarker()
//            mapCompareUtil.endMarker()
            vb.includeViewMarkerActions.rgMarker.clearCheck()
            handleMapUtil.clearAnalysisResultPatch()

            vb.includeMenuList.ivMenuTools.setImageResource(R.mipmap.icon_tools_c)
        }
        vb.includeViewMeasureActions.tvCancleMeasure.onClick {
            //取消量测
            handleMapUtil.endMeasure()
//            mapCompareUtil.endMeasure()
            vb.includeViewMeasureActions.measureGroup.clearCheck()
            vb.includeViewMeasureActions.llMeasureActions.gone()
        }
        vb.includeViewHandSearchActions.tvHandSearch.onClick {
            //开始 手势查询

            (handleMapUtil.selectLayers.size > 0).yes {

                if (handleMapUtil.markerUtil.getPointCount() > 2) {
                    vb.includeViewLoadingData.rlLoading.show()
                    handleMapUtil.handSearchInfo(handleMapUtil.markerUtil.getGeometry())
                } else {
                    tip("请绘制完整面状图形")
                }
            }.no {
                tip("请先加载专题图层")
            }

        }

        vb.includeViewOverlyAnalysisActions.llAnalysisSave.onClick {
            //叠加分析 标绘保存
            handleMapUtil.saveGeometyMarker(editGraphicInfo)
        }
        vb.includeViewMarkerActions.llMarkerSave.onClick {
            //标绘保存
            isMarkerAnalysisAfterSave = false
//            handleMapUtil.saveGeometyMarker(editGraphicInfo)
            handleMapUtil.saveWorkRecord(editGraphicInfo)
        }

        vb.includeViewMeasureActions.measureGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.rbMeasureDistance -> {
                    find<RadioButton>(i).isChecked.yes {
                        handleMapUtil.startMeasure(DrawUtil.DRAW_POLYLINE)
//                        mapCompareUtil.startMeasure(DrawUtil.DRAW_POLYLINE)
                    }
                }

                R.id.rbMeasureArea -> {
                    find<RadioButton>(i).isChecked.yes {
                        handleMapUtil.startMeasure(DrawUtil.DRAW_POLYGON)
//                        mapCompareUtil.startMeasure(DrawUtil.DRAW_POLYGON)
                    }
                }

                else -> {
                }
            }
        }

        vb.includeViewMarkerActions.rgMarker.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.rbPoint -> {
                    find<RadioButton>(i).isChecked.yes {
                        handleMapUtil.startMarker(DrawUtil.DRAW_POINT)
//                        mapCompareUtil.startMarker(DrawUtil.DRAW_POINT)
                    }
                    vb.includeViewMarkerActions.llMarkerUnit.gone()
                }

                R.id.rbLine -> {
                    find<RadioButton>(i).isChecked.yes {
                        handleMapUtil.startMarker(DrawUtil.DRAW_POLYLINE)
//                        mapCompareUtil.startMarker(DrawUtil.DRAW_POLYLINE)
                    }
                    vb.includeViewMarkerActions.llMarkerUnit.gone()
                }

                R.id.rbPolygon -> {
                    find<RadioButton>(i).isChecked.yes {
                        handleMapUtil.startMarker(DrawUtil.DRAW_POLYGON)
//                        mapCompareUtil.startMarker(DrawUtil.DRAW_POLYGON)
                    }
                    vb.includeViewMarkerActions.llMarkerUnit.show()
                }

                R.id.tvPoiMarker -> {
                    find<RadioButton>(i).isChecked.yes {
                        handleMapUtil.endMarker()
//                        mapCompareUtil.endMarker()

                        if (coordinateMarkerDialog == null) {
                            coordinateMarkerDialog = CoordinateMarkerDialog(
                                context,
                                handleMapUtil.mapView,
                                object : ActionListener {
                                    override fun onAction(obj: Any?, flag: Int) {}
                                })
                        }
                        //防止重复展示dialog
                        if (!coordinateMarkerDialog!!.isShowing) {
                            coordinateMarkerDialog?.show()
                        }
                    }
                }

                R.id.rbText -> {
                    find<RadioButton>(i).isChecked.yes {
                        handleMapUtil.startMarker(DrawUtil.DRAW_TEXT)
//                        mapCompareUtil.startMarker(DrawUtil.DRAW_TEXT)
                    }
                }

                else -> {
                }
            }
        }

//        tvPoiMarker.onClick {
//            handleMapUtil.endMarker()
//            mapCompareUtil.endMarker()
//
//            if (coordinateMarkerDialog == null) {
//                coordinateMarkerDialog = CoordinateMarkerDialog(context, object : ActionListener {
//                    override fun onAction(obj: Any?, flag: Int) {}
//                })
//            }
//            //防止重复展示dialog
//            if (!coordinateMarkerDialog!!.isShowing) {
//                coordinateMarkerDialog?.show()
//            }
//        }
        vb.includeViewMeasureActions.tvMeasureReset.onClick {
            handleMapUtil.resetDraw()
//            mapCompareUtil.resetDraw()
        }
        vb.includeViewMeasureActions.tvMeasurePreStep.onClick {
            handleMapUtil.preStep()
//            mapCompareUtil.preStep()
        }

        vb.includeViewRightTools.ivScreenshots.onClick {
            handleMapUtil.captureScreenshotAsync(0)

        }
        //定位
        vb.includeViewRightTools.ivMyLocation.onClick {
            locateWithLocationCheck(0)
        }

        //重置视点
        vb.includeViewRightTools.ivRelocation.onClick {
            handleMapUtil.relocation()

        }
        //清除地图一切绘制物
        vb.includeViewRightTools.ivClearMap.onClick {
            handleMapUtil.clearDrawLayerAndDismissCallout(true) // 主动清除地图，连定位点一起清
            //mapCompareUtil.clearDrawLayerAndDismissCallout()
            handleMapUtil.clearCollectionGrapicslayer()


            EventBus.getDefault().post(DataEvent(DataEvent.CLEAR_ALL_ZZT_LAYER))
            EventBus.getDefault().post(DataEvent(DataEvent.CLEAR_ALL_GEO))

            handleMapUtil.selectLayers.clear()
            handleMapUtil.zttItemList.clear()
            handleMapUtil.checkBaseMap()

//            baseMapStack.clear()
//            baseMapStack.push("影像底图2018")
//            vb.tvBaseMapInfo.text = "当前影像：${baseMapStack.peek()}"

            vb.tvArea.text = "行政区划"
        }
        //清除地图一切绘制物
        vb.ivClearCompareMap.onClick {
            //handleMapUtil.clearDrawLayerAndDismissCallout()
//            mapCompareUtil.clearDrawLayerAndDismissCallout()

            EventBus.getDefault().post(DataEvent(DataEvent.CLEAR_ALL_COMPARE_ZZT_LAYER))
        }
        //底图切换
        vb.includeViewRightTools.ivChangeBaseMap.onClick {
            handleMapUtil.isVector = !handleMapUtil.isVector
            (handleMapUtil.isVector).yes {
                vb.includeViewRightTools.ivChangeBaseMap.setImageResource(R.mipmap.icon_image_map)
                handleMapUtil.switchBaseLyer(MapEvent.SET_VECTOR_MAP)
            }.no {
                vb.includeViewRightTools.ivChangeBaseMap.setImageResource(R.mipmap.icon_vector_map)
                handleMapUtil.switchBaseLyer(MapEvent.SET_IMAGE_MAP)
            }
        }
        //对比地图 底图切换
        vb.ivChangeBaseMap2.onClick {
            mapCompareUtil.isVector = !mapCompareUtil.isVector
            (mapCompareUtil.isVector).yes {
                vb.ivChangeBaseMap2.setImageResource(R.mipmap.icon_image_map)
                mapCompareUtil.switchBaseLyer(MapEvent.SET_VECTOR_MAP)
            }.no {
                vb.ivChangeBaseMap2.setImageResource(R.mipmap.icon_vector_map)
                mapCompareUtil.switchBaseLyer(MapEvent.SET_IMAGE_MAP)
            }
        }
        //行政区划 (现在变成统计了)
        vb.includeMenuList.llMenuArea.onClick {
            
            vb.includeViewAreaList.llAreaList.isShow().yes {
                vb.includeViewAreaList.llAreaList.gone()
                vb.includeMenuList.ivMenuArea.setImageResource(R.mipmap.icon_area_c)
//                vb.includeMenuList.tvMenuArea.setTextColor(resources.getColor(R.color.color_menu_unselect))
                vb.ivFragmentSwitch.gone()
            }.no {
                closeAllMenu()
                vb.includeViewAreaList.llAreaList.show()
                vb.includeMenuList.ivMenuArea.setImageResource(R.mipmap.icon_area_o)
//                vb.includeMenuList.tvMenuArea.setTextColor(resources.getColor(R.color.color_menu_select))
                showRightContent(true)
                EventBus.getDefault().post(MapEvent(MapEvent.INIT_ARAE_FEAG))

                OperationLogger.logOperation(this, "打开模块：数据统计")

            }
        }
        //搜索
        vb.includeMenuList.llMenuSearch.onClick {
            
            vb.includeViewSearch.llSearch.isShow().yes {
                vb.includeViewSearch.llSearch.gone()
                vb.includeMenuList.ivMenuSearch.setImageResource(R.mipmap.icon_search_c)
//                vb.includeMenuList.tvMenuSearch.setTextColor(resources.getColor(R.color.color_menu_unselect))
                handleMapUtil.clearDrawLayerAndDismissCallout()
            }.no {
                closeAllMenu()
                vb.includeViewSearch.llSearch.show()
                vb.includeMenuList.ivMenuSearch.setImageResource(R.mipmap.icon_search_o)
//                vb.includeMenuList.tvMenuSearch.setTextColor(resources.getColor(R.color.color_menu_select))
            }
        }
        //地图
        vb.includeMenuList.llMenuMaps.onClick {
            
            vb.includeViewSwitchMap.llSwitchMap.isShow().yes {
                vb.includeViewSwitchMap.llSwitchMap.gone()
                vb.includeMenuList.ivMenuMaps.setImageResource(R.mipmap.icon_map_c)
//                vb.includeMenuList.tvMenuMaps.setTextColor(resources.getColor(R.color.color_menu_unselect))
                vb.ivFragmentSwitch.gone()
            }.no {
                closeAllMenu()
                vb.includeViewSwitchMap.llSwitchMap.show()
                vb.includeMenuList.ivMenuMaps.setImageResource(R.mipmap.icon_map_o)
//                vb.includeMenuList.tvMenuMaps.setTextColor(resources.getColor(R.color.color_menu_select))
                showRightContent(true)
                OperationLogger.logOperation(this, "打开模块：数据资源")
            }
        }
        //对比地图
        vb.tvShowCompareLayer.onClick {
            vb.includeSwitchMapPare.llSwitchMap2.show()
        }

        //取消地图比对
        vb.imgCancleCompareMap.onClick {
            vb.llMap2.gone()
            vb.llMap2.requestLayout() //重新绘制布局，不然会有问题
        }

        //兴趣点收藏夹
        vb.includeMenuList.llMenuCollection.onClick {
            
            vb.includeViewCollection.llCollection.isShow().yes {
                vb.includeViewCollection.llCollection.gone()
                vb.includeMenuList.ivMenuCollection.setImageResource(R.mipmap.icon_mark_c)
//                vb.includeMenuList.tvMenuCollection.setTextColor(resources.getColor(R.color.color_menu_unselect))
                handleMapUtil.clearDrawLayerAndDismissCallout()
                vb.ivFragmentSwitch.gone()
            }.no {
                closeAllMenu()
                vb.includeViewCollection.llCollection.show()
                vb.includeMenuList.ivMenuCollection.setImageResource(R.mipmap.icon_mark_o)
//                vb.includeMenuList.tvMenuCollection.setTextColor(resources.getColor(R.color.color_menu_select))
                showRightContent(true)
                OperationLogger.logOperation(this, "打开模块：数据统计")
            }
        }
        //工具栏
        vb.includeMenuList.llMenuTools.onClick {
            
//            vb.includeViewTool.llTools.isShow().yes {
//                showToolPopWindow(false)
//            }.no {
//                closeAllMenu()
//                showToolPopWindow(true)
//            }

            //

            (vb.includeViewMarkerActions.llMarkerActions.isShow() || vb.includeViewGeoCollection.llCollection.isShow()).yes {
                vb.includeViewMarkerActions.llCancleMarker.performClick()

                vb.includeViewGeoCollection.llCollection.gone()
                handleMapUtil.clearDrawLayerAndDismissCallout()


                vb.ivFragmentSwitch.gone()
            }.no {
                closeAllMenu()
                vb.includeViewGeoCollection.llCollection.show()

                vb.includeViewMarkerActions.llMarkerActions.show()
                vb.includeMenuList.ivMenuTools.setImageResource(R.mipmap.icon_tools_o)

                showRightContent(true)
                OperationLogger.logOperation(this, "打开模块：新增记录")
            }

        }
        //缓冲区分析
        vb.includeMenuList.llAnalysis.onClick {
            
            vb.includeViewBufferActions.llBufferAnalysisActions.isShow().yes {
                vb.includeViewBufferActions.llCancelBuffer.performClick()
            }.no {
                closeAllMenu()
                vb.includeViewBufferActions.llBufferAnalysisActions.show()
                vb.includeMenuList.ivAnalysis.setImageResource(R.mipmap.icon_search_o)
                OperationLogger.logOperation(this, "打开模块：空间检索")

            }

        }
        //设置 - 允许联网时使用
        vb.includeMenuList.llMenuSetting.onClick {
            showSettingDialog()
            OperationLogger.logOperation(this, "打开模块：设置")
        }
        //图例
        vb.cbLegend.onClick {
            (vb.cbLegend.isChecked).yes {
                vb.imgLegend.show()
            }.no {
                vb.imgLegend.gone()
            }
        }


        vb.includeViewOverlyAnalysisActions.llOverlyAnalysis.onClick {
            //开始叠加分析--数据
            handleMapUtil.startAnalysisOverly()

        }


        vb.includeViewOverlyAnalysisActions.llAnalysisPreStep.onClick {
            //如果不是叠加分析完成状态 则标绘可以回退
            (handleMapUtil.actionType == HandleMapUtil.ACTION_OVERLYING_ANALYSIS_FINISH).yes {
                tip("当前状态不可回退")
            }.no {
                vb.includeViewMarkerActions.llMarkerPreStep.performClick()
            }

        }

        vb.includeViewOverlyAnalysisActions.llAnalysisReset.onClick {
            //叠加分析重置
            vb.includeViewMarkerActions.llMarkerReset.performClick()
            //继续叠加分析标绘
            handleMapUtil.restartOverlyingAnalysis()

            handleMapUtil.clearAnalysisResultPatch()

        }

        vb.includeViewOverlyAnalysisActions.llCancleAnalysis.onClick {
            //叠加分析关闭
            vb.includeViewOverlyAnalysisActions.llAnalysisActions.gone()
            handleMapUtil.endOverlyingAnalysis()
            handleMapUtil.clearAnalysisResultPatch()
        }


        vb.includeViewOverlyAnalysisActions.llImportExcel.onClick {
            //导入excel按钮--打开对话框展示excel文件列表

            excelDialog.show()

        }

        vb.includeViewOverlyAnalysisActions.llImportCollection.onClick {
            //导入收藏标绘按钮--打开对话框展示收藏标绘列表
            if (markerDialog == null) {
                markerDialog = MarkerCollectionListDialog(context)
            }
            markerDialog!!.show()
        }

        vb.includeViewOverlyAnalysisActions.llPoiInputMarker.onClick {
            //叠加分析==手动输入面坐标
            if (coordinateMarkerAnalysisDialog == null) {
                coordinateMarkerAnalysisDialog = CoordinateMarkerAnalysisDialog(
                    context,
                    handleMapUtil.mapView,
                    object : ActionListener {
                        override fun onAction(obj: Any?, flag: Int) {}
                    })
            }
            //防止重复展示dialog
            if (!coordinateMarkerAnalysisDialog!!.isShowing) {
                coordinateMarkerAnalysisDialog?.show()
            }
        }

        vb.ivFragmentSwitch.onClick {
            vb.llRightFragment.isShown.yes {
                vb.llRightFragment.gone()
                vb.ivFragmentSwitch.setImageResource(R.mipmap.icon_arrow_open)
            }.no {
                vb.llRightFragment.show()
                vb.ivFragmentSwitch.setImageResource(R.mipmap.icon_arrow_close)
            }
        }


        vb.easySearchBar.actionBlock = { keyword, isProblem, isFinished, year, cityName, countyName, townName, problemType ->
            closeAllMenu()
            handleMapUtil.executeKeywordAnalysis(queryThemeList, keyword, isProblem, isFinished, year, cityName, countyName, townName, problemType)
            EventBus.getDefault().post(DataEvent(DataEvent.INIT_BUFFER_ANALYSIS_BY_KEYWORDS))
            OperationLogger.logOperation(context, "全局搜索 编号：${keyword}, 是否问题：${isProblem}, 是否办结：${isFinished}, 年份：${year}, 市：${cityName}, 县：${countyName}, 镇：${townName}, 问题类型：${problemType}")

        }

        // ====== 几何编辑器 ======
        vb.btnGeometryEditor.onClick {
            val isVisible = vb.includeViewGeometryEditor.llGeometryEditor.isShow()
            if (isVisible) {
                vb.includeViewGeometryEditor.llGeometryEditor.gone()
                geometryEditorHelper.stop()
            } else {
                vb.includeViewGeometryEditor.llGeometryEditor.show()
            }
        }
        // 模式切换
        vb.includeViewGeometryEditor.rgGeometryMode.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbGeoPoint -> geometryEditorHelper.startPoint()
                R.id.rbGeoLine -> geometryEditorHelper.startPolyline()
                R.id.rbGeoPolygon -> geometryEditorHelper.startPolygon()
                R.id.rbGeoSelect -> geometryEditorHelper.startSelect()
            }
        }
        // 选择模式下点击地图选中图形进行编辑
        handleMapUtil.tapInterceptor = { event ->
            if (geometryEditorHelper.isInSelectMode()) {
                geometryEditorHelper.onMapTapped(event.x.toDouble(), event.y.toDouble())
                true // 拦截，不执行后续地图逻辑
            } else false
        }
        // 测量信息回调
        geometryEditorHelper.onMeasureInfo = { info ->
            if (info.isEmpty()) {
                vb.includeViewGeometryEditor.tvGeoMeasureInfo.gone()
            } else {
                vb.includeViewGeometryEditor.tvGeoMeasureInfo.text = "最新: $info"
                vb.includeViewGeometryEditor.tvGeoMeasureInfo.show()
            }
        }
        // 浮动完成按钮（跟随最后一个顶点）
        geometryEditorHelper.onFloatingBtnPos = { screenPoint ->
            if (screenPoint == null) {
                vb.tvGeoFinishFloating.gone()
            } else {
                vb.tvGeoFinishFloating.show()
                vb.tvGeoFinishFloating.x = screenPoint.x.toFloat()
                vb.tvGeoFinishFloating.y = screenPoint.y.toFloat()
            }
        }
        vb.tvGeoFinishFloating.onClick {
            geometryEditorHelper.finish()
        }
        vb.includeViewGeometryEditor.llGeoUndo.onClick {
            geometryEditorHelper.undo()
        }
        vb.includeViewGeometryEditor.llGeoClear.onClick {
            geometryEditorHelper.clearAll()
        }
        vb.includeViewGeometryEditor.llGeoClose.onClick {
            vb.includeViewGeometryEditor.llGeometryEditor.gone()
            geometryEditorHelper.stop()
        }

    }


    fun executeBufferQuery(distance: Double) {
        locationManager.requestSingleLocation { province, city, district, address, lat, lng ->
            // 处理定位结果
            println("Latitude: $lat, Longitude: $lng")

            queryLocationPoint =
                MapUtil.mapUtil.get_change_geometry_point(lat, lng, Config.sp4490Int)

            handleMapUtil.executeBufferAnalysis(queryThemeList, queryLocationPoint!!, distance)
        }
    }

    fun showToolPopWindow(isShow: Boolean) {
        isShow.yes {
            val offsetY = -(vb.includeMenuList.llMenuTools.width) / 2
            toolPopWindow.showAsDropDown(vb.includeMenuList.llMenuTools, 0, offsetY, Gravity.END)
        }.no {
            toolPopWindow.dismiss()
        }
    }

    fun showAreaPopWindow(isShow: Boolean) {
        isShow.yes {
            areaPopWindow.showAsDropDown(vb.tvArea, 0, 0, Gravity.END)
        }.no {
            areaPopWindow.dismiss()
        }
    }

    //关闭所有页面
    fun closeAllMenu() {
        vb.includeViewAreaList.llAreaList.gone()
        vb.includeMenuList.ivMenuArea.setImageResource(R.mipmap.icon_area_c)
//        vb.includeMenuList.tvMenuArea.setTextColor(resources.getColor(R.color.color_menu_unselect))
        vb.includeViewSearch.llSearch.gone()
        vb.includeMenuList.ivMenuSearch.setImageResource(R.mipmap.icon_search_c)
//        vb.includeMenuList.tvMenuSearch.setTextColor(resources.getColor(R.color.color_menu_unselect))
        vb.includeViewSwitchMap.llSwitchMap.gone()
        vb.includeMenuList.ivMenuMaps.setImageResource(R.mipmap.icon_map_c)
//        vb.includeMenuList.tvMenuMaps.setTextColor(resources.getColor(R.color.color_menu_unselect))
        vb.includeViewCollection.llCollection.gone()
        vb.includeMenuList.ivMenuCollection.setImageResource(R.mipmap.icon_mark_c)
//        vb.includeMenuList.tvMenuCollection.setTextColor(resources.getColor(R.color.color_menu_unselect))
        vb.includeViewTool.llTools.gone()

        vb.includeViewGeoCollection.llCollection.gone()
        EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_COLLECTION_FRAG_NAVIGATION))
        vb.includeMenuList.ivMenuTools.setImageResource(R.mipmap.icon_tools_c)
        vb.includeViewMarkerActions.llCancleMarker.performClick()
//        vb.includeMenuList.tvMenuTools.setTextColor(resources.getColor(R.color.color_menu_unselect))
        //关闭图例
        vb.cbLegend.isShow().yes {
            vb.cbLegend.gone()
        }


        vb.includeViewQueryBufferResultList.llQueryBufferResultList.gone()
        vb.includeViewBufferActions.llBufferAnalysisActions.gone()
        vb.includeMenuList.ivAnalysis.setImageResource(R.mipmap.icon_search_c)
        handleMapUtil.cleaBufferLayer()

        showToolPopWindow(false)

        vb.ivFragmentSwitch.gone()

        handleMapUtil.clearDrawLayerAndDismissCallout()

        vb.includeViewCollectionStatis.llCollectionStatis.gone()
    }

    //操作地图
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: MapEvent) {
        when (event.actionType) {
            //同步绘制
            MapEvent.ADD_POINT -> {
                val point = event.data as Point
                handleMapUtil.markerUtil.addPoint(point)
//                mapCompareUtil.markerUtil.addPoint(point)
            }
            //同步绘制 坐标标绘
            MapEvent.ADD_POINT_COORDINATE -> {
                val point = event.data as Point
                handleMapUtil.mapView.setViewpointCenterAsync(point, 5000.0)
                handleMapUtil.markerUtil.addPoint(point)
//                mapCompareUtil.markerUtil.addPoint(point)
            }

            MapEvent.DO_BUFFER_ANALYSIS_CUSTOM_POINT -> {
                queryInputPoint = event.data as Point
                handleMapUtil.endMarker() //点击完 马上结束查询的操作状态
                handleMapUtil.markerUtil.addPoint(queryInputPoint!!)
                handleMapUtil.executeBufferAnalysis(queryThemeList, queryInputPoint!!, distance)
                EventBus.getDefault().post(DataEvent(DataEvent.INIT_BUFFER_ANALYSIS))

            }
            //同步绘制 坐标标绘 点列表
            MapEvent.ADD_POINT_LIST_COORDINATE -> {
                val pointCollection = event.data as PointCollection
                val polyline = Polyline(pointCollection)
                handleMapUtil.mapView.setViewpointCenterAsync(polyline.extent.center, 5000.0)
                handleMapUtil.markerUtil.addPointCollection(pointCollection)
//                mapCompareUtil.markerUtil.addPointCollection(pointCollection)
            }
            //行政区划标点
            MapEvent.DRAW_AREA_POINT -> {
                val point = event.data as Point
                val drawPoint = handleMapUtil.mapUtil.get_change_geometry_point(
                    point.y,
                    point.x,
                    Config.sp4490Int
                )
                handleMapUtil.drawArea(drawPoint);
            }
            //绘制兴趣点搜索结果
            MapEvent.DRAW_SEARCH_RESULT -> {
                //绘制兴趣点搜搜索结果
                handleMapUtil.drawSearchResult(event.data as MutableList<PoisInfo>)
            }
            //点击了搜索结果列表item
            MapEvent.CLICK_SEARCH_RESULT_ITEM -> {
                handleMapUtil.clickSearchResultItem(event.data as PoisInfo)
            }
            //点击了收藏结果列表item
            MapEvent.DRAW_COllECTION_CLICK_RESULT -> {
                //点击了收藏结果列表item
                handleMapUtil.drawClickCollectResult(event.data as PoisInfo)
            }

            MapEvent.REMOVE_ONE_COLLECT_GRAPHICS -> {
                //删除一个收藏的poi点
                handleMapUtil.removeOneCollectGraphics(event.data as String)
            }
            //关闭弹窗
            MapEvent.HIDE_CALLOUT -> {
                if (handleMapUtil.callout.isShowing) {
                    handleMapUtil.callout.dismiss()
                }
            }
            //清空绘制图层
            MapEvent.CLEAR_GRAPHIC_LAYER -> {
                //清空绘制图层
                handleMapUtil.poiSearchLayer.graphics.clear()
                handleMapUtil.graphicsLayer.graphics.clear()
                handleMapUtil.shpGraphicsLayer.graphics.clear()
            }
            //标绘结果 列表item 点击
            MapEvent.CLICK_MARKER_RESULT_LIST_ITEM -> {

                val markerInfo = event.data as MarkerInfo
                if (vb.llMap2.isShow()) {
                    mapCompareUtil.clickMarkerResultItem(markerInfo)
                } else {
                    handleMapUtil.clickMarkerResultItem(markerInfo)
                }
            }
            //控制专题图展示
            MapEvent.CONTROL_LAYER -> {
                handleMapUtil.controlZttLayer(event.data as ZttItem)
            }

            //控制专题图展示 对比地图
            MapEvent.CONTROL_COMPARE_LAYER -> {
//                mapCompareUtil.controlZttLayer(event.data as ZttItem)
            }

            //清空所有专题图
            MapEvent.CLEAR_OPERATION_LAYERS -> {
                //这三行是为了让图例一直在最上方 但目前我们没有图例了 所以不需要
//                val layer = vb.mapView.map.operationalLayers[vb.mapView.map.operationalLayers.size - 1]
//                handleMapUtil.clearOperationLayers()
//                handleMapUtil.mapView.map.operationalLayers.add(layer)

                handleMapUtil.clearOperationLayers()
                legendList?.clear()
                handleView(DataEvent(DataEvent.HIDE_LAYER_LEGEND))
            }
            //清空所有专题图 对比地图
            MapEvent.CLEAR_COMPARE_OPERATION_LAYERS -> {
                val layer =
                    vb.mapView2.map.operationalLayers[vb.mapView2.map.operationalLayers.size - 1]
//                mapCompareUtil.clearOperationLayers()
//                mapCompareUtil.mapView.map.operationalLayers.add(layer)
            }
            //点击空间查询的 feature 列表
            MapEvent.CLICK_QUERY_RESULT_FEATURE_LIST -> {
                //点击空间查询的 feature 列表
                handleMapUtil.showQueryResultFeature(event.data as HashMap<String?, Any?>)
            }
            // 替换地图
            MapEvent.REPLACE_BASE_MAP -> {
                val replace = event.data as ImageLayerItem
                handleMapUtil.handleBaseMap(MapEvent.REPLACE_BASE_MAP, replace.imgLayer)
            }
            // 叠加底图
            MapEvent.ADD_BASEMAP -> {
                val add = event.data as ImageLayerItem
                handleMapUtil.handleBaseMap(MapEvent.ADD_BASEMAP, add.imgLayer)
            }
            // 移除底图
            MapEvent.REMOVED_ADDED_BASEMAP -> {
                val remove_add = event.data as ImageLayerItem
                handleMapUtil.handleBaseMap(MapEvent.REMOVED_ADDED_BASEMAP, remove_add.imgLayer)
            }


            MapEvent.NET_CONNECT -> {
                //网络连接上 - 显示全屏锁定弹窗，拦截一切操作
                "收到 NET_CONNECT 事件，显示锁定".printMsg()
                showNetworkLock()
            }

            MapEvent.NET_DISCONNECT -> {
                //网络断开 - 移除锁定弹窗
                "收到 NET_DISCONNECT 事件，解除锁定".printMsg()
                hideNetworkLock()
            }

            MapEvent.COORDINATE_POSITION -> {
                //坐标定位
//                SoftUtil().hideKeyboard(mActivity)

                val p = event.data as Point
//                handleMapUtil.mapView.setViewpointCenterAsync(p, 4223.742003018215)
//                handleMapUtil.drawMyLocaton(p)
                handleMapUtil.drawArea(p, 4223.742003018215)
            }

            MapEvent.COORDINATE_MARKER_POINT -> {
                handleMapUtil.startMarker(DrawUtil.DRAW_POINT)
//                mapCompareUtil.startMarker(DrawUtil.DRAW_POINT)
            }

            MapEvent.COORDINATE_MARKER_PLOYLINE -> {
                handleMapUtil.startMarker(DrawUtil.DRAW_POLYLINE)
//                mapCompareUtil.startMarker(DrawUtil.DRAW_POLYLINE)
            }

            MapEvent.COORDINATE_MARKER_POLYGON -> {
                handleMapUtil.startMarker(DrawUtil.DRAW_POLYGON)
//                mapCompareUtil.startMarker(DrawUtil.DRAW_POLYGON)
            }

            MapEvent.COORDINATE_MARKER_CLOSE -> {
                //坐标标绘画完后 把模式改回正常，不然点击屏幕会画点
                handleMapUtil.actionType = HandleMapUtil.ACTION_NOMAL
//                mapCompareUtil.actionType = HandleMapUtil.ACTION_NOMAL
                //重置整个radioGroup
                vb.includeViewMarkerActions.rgMarker.clearCheck()
            }

            //画出叠加分析结果
            MapEvent.DRAW_ANALYSIS_RESULT -> {

                val item = event.data as InterSectionModel
                handleMapUtil.drawAnalysisResult(item)
            }

            //清除叠加分析的标绘
            MapEvent.CLEAR_ANALYSIS_MARKER -> {
                vb.includeViewOverlyAnalysisActions.llAnalysisReset.performClick()
            }
            //查询行政区featureLayer 省级
            MapEvent.QUERY_AREA_PROVINCE -> {
                val map = event.data as MutableMap<String, String>
                val name = map["name"]!!
                val code = map["code"]!!
                val item = event.data2 as DistrictsInfo

                var isStatis = true //默认要统计
                event.data3?.apply {
                    isStatis = event.data3 as Boolean
                }

                handleMapUtil.searchProvinceLayer(name, code, item, isStatis)
            }
            //查询行政区featureLayer 市级
            MapEvent.QUERY_AREA_CITY -> {
                val map = event.data as MutableMap<String, String>
                val name = map["name"]!!
                val code = map["code"]!!
                val item = event.data2 as DistrictsInfo

                var isStatis = true //默认要统计
                event.data3?.apply {
                    isStatis = event.data3 as Boolean
                }

                handleMapUtil.searchCityLayer(name, code, item, isStatis)
            }
            //查询行政区featureLayer 区县级
            MapEvent.QUERY_AREA_AREA -> {
                val map = event.data as MutableMap<String, String>
                val name = map["name"]!!
                val code = map["code"]!!
                val item = event.data2 as DistrictsInfo
                var isStatis = true //默认要统计
                event.data3?.apply {
                    isStatis = event.data3 as Boolean
                }

                handleMapUtil.searchAreaLayer(name, code, item, isStatis)

            }
            //查询行政区featureLayer 乡镇级
            MapEvent.QUERY_AREA_COUNTY -> {
                val map = event.data as MutableMap<String, String>
                val name = map["name"]!!
                val code = map["code"]!!
                val item = event.data2 as DistrictsInfo
                var isStatis = true //默认要统计
                event.data3?.apply {
                    isStatis = event.data3 as Boolean
                }

                handleMapUtil.searchCountyLayer(name, code, item, isStatis)
            }
            //查询行政区featureLayer 村级
            MapEvent.QUERY_AREA_VILLAGE -> {

                val map = event.data as MutableMap<String, String>
                val name = map["name"]!!
                val code = map["code"]!!
                val item = event.data2 as DistrictsInfo
                var isStatis = true //默认要统计
                event.data3?.apply {
                    isStatis = event.data3 as Boolean
                }

                handleMapUtil.searchVillageLayer(name, code, item, isStatis)
            }

            //关闭村定位点
            MapEvent.CLOSE_AREA_POINT -> {
                handleMapUtil.clearAreaPoint()
            }

            //定位 截图
            MapEvent.SCREEN_SHOT_WHTI_LOACTION -> {
                val data = event.data as ImageInfo
                handleMapUtil.captureScreenshotAsync(1, data)

            }

            //定位 空间查询位置信息
            MapEvent.QUERY_LOCATION_AREAINFO -> {
                imageScreenShot = event.data as ImageInfo
                handleMapUtil.queryLocationArea()
            }

            MapEvent.QUERY_LOCATION_AREAINFO_BACK -> {
                event.data?.apply {
                    val value = event.data as String
                    val areaType = event.data2 as String

                    when (areaType) {
                        "city" -> {
                            imageScreenShot?.city = value
                            "======1======1".printMsg()
                        }
                        "county" -> {
                            imageScreenShot?.county = value
                            "======1======2".printMsg()
                        }
                        "town" -> {
                            imageScreenShot?.town = value
                            "======1======3".printMsg()
                            handleMapUtil.captureScreenshotAsync(1, imageScreenShot)
                        }
                        else -> null
                    }
                }
            }

            //画出叠加分析结果
            MapEvent.DRAW_BUFFER_ANALYSIS_RESULT -> {

                val item = event.data as BufferAnalysisModel
                handleMapUtil.showBufferResult(item)
            }

            //绘制当前页的缓冲区查询结果（分页绘制）
            MapEvent.DRAW_BUFFER_PAGE_RESULT -> {
                @Suppress("UNCHECKED_CAST")
                val pageData = event.data as? MutableList<BufferAnalysisModel> ?: mutableListOf()
                handleMapUtil.drawBufferResultForPage(pageData)
            }

            MapEvent.QUERY_AREA_BY_POINT -> {
                val centerPoint = event.data as Point
                handleMapUtil.searchAreaLayerByPoint(centerPoint, "city")
            }

            MapEvent.SEND_BUFFER_QUERY_TASK -> {
                val queryType = event.data as Int
                distance = event.data2 as Double
                closeAllMenu()
                when (queryType) {
                    0 -> {
                        bufferAnalyisQueryType = 0

                        if (event.data3 != null) {
                            handleMapUtil.executeBufferAnalysis(
                                queryThemeList,
                                queryLocationPoint!!,
                                distance
                            )
                        } else {
                            executeBufferQuery(distance)
                        }

                        EventBus.getDefault().post(DataEvent(DataEvent.INIT_BUFFER_ANALYSIS))
                    }

                    1 -> {
                        bufferAnalyisQueryType = 1
                        if (event.data3 != null) {
                            EventBus.getDefault().post(
                                MapEvent(
                                    MapEvent.DO_BUFFER_ANALYSIS_CUSTOM_POINT,
                                    handleMapUtil.clickPoint
                                )
                            )
                        } else {
                            handleMapUtil.startDrawBufferPoint()
                            ToastUtils.show("请在地图上点击一个位置")
                        }
                    }

                    2 -> {


                        if (event.data3 != null && queryInputPoint != null) {
                            EventBus.getDefault().post(
                                MapEvent(MapEvent.DO_BUFFER_ANALYSIS_CUSTOM_POINT, queryInputPoint)
                            )
                        } else {
                            handleMapUtil.endMarker()
                            if (coordinateInputPointDialog == null) {
                                coordinateInputPointDialog = CoordinateInputPointDialog(
                                    context, handleMapUtil.mapView,
                                    object : ActionListener {
                                        override fun onAction(obj: Any?, flag: Int) {}
                                    })
                                coordinateInputPointDialog!!.mActivity = this
                            }
                            //防止重复展示dialog
                            if (!coordinateInputPointDialog!!.isShowing) {
                                coordinateInputPointDialog?.show()
                            }
                        }

                    }
                }


            }

        }
    }

    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleView(event: DataEvent) {
        when (event.actionType) {
            DataEvent.SHOW_GEOMETRY_DETAIL2 -> {
                vb.includeViewFragDatail2.llFragDetail2.show()
            }
            //poi shp详情展示
            DataEvent.SHOW_POI_DETAIL,
            DataEvent.SHOW_GEOMETRY_DETAIL -> {
                vb.includeViewFragDatail.llFragDetail.show()
            }
            //poi详情隐藏
            DataEvent.HIDE_POI_DETAIL -> {
                vb.includeViewFragDatail.llFragDetail.gone()
            }

            DataEvent.HIDE_GEOMETRY_DETAIL2 -> {
                vb.includeViewFragDatail2.llFragDetail2.gone()
            }

            //进入拍照Activity
            DataEvent.GO_PHOTO -> {
                OperationLogger.logOperation(this, "[PhotoDialogActivity] MainActivity 接收GO_PHOTO事件，准备启动PhotoDialogActivity")
                try {
                    startActivity<PhotoDialogActivity>()
                } catch (e: Exception) {
                    OperationLogger.logOperation(this, "[PhotoDialogActivity] 启动失败: ${e.javaClass.simpleName} - ${e.message}")
                    e.printStackTrace()
                }
            }
            //进入拍照Activity
            DataEvent.GO_SELECT_PHOTO -> {

                startActivity<SelectPhotoDialogActivity>()
            }

            //TODO 进入多媒体附件Activity
            DataEvent.GO_MEDIA -> {
                startActivity<MediaDialogActivity>()
            }
            //绘制选中收藏的标绘
            DataEvent.DRAW_SELECT_COLLECTION_MARKER -> {
                val selectList = event.data as MutableList<MarkerInfo>
                handleMapUtil.drawSelectCollectionMarker(selectList)
//                if (llMap2.isShow()) {
//                    mapCompareUtil.drawSelectCollectionMarker(selectList)
//                } else {
//                    handleMapUtil.drawSelectCollectionMarker(selectList)
//                }
            }

            DataEvent.UNDRAW_SELECT_COLLECTION_MARKER_SINGLE -> {
                handleMapUtil.clearCollectionGrapicslayer()
                handleMapUtil.dismissCallout()
            }

            DataEvent.DRAW_SELECT_COLLECTION_MARKER_SINGLE -> {
                val selectList = event.data as MarkerInfo
                handleMapUtil.drawSelectCollectionMarkerSingle(selectList)
            }

            //绘制收藏的标绘 （叠加分析用）
            DataEvent.DRAW_COLLECTION_MARKER_ANALYSIS -> {
                val data = event.data as MarkerInfo
                handleMapUtil.darwCollectionMarkerAnalysis(data)
            }

            //关闭行政区划
            DataEvent.CLOSE_AREA_FRAG -> {
                vb.includeMenuList.llMenuArea.performClick()
            }
            //关闭搜索
            DataEvent.CLOSE_SEARCH_FRAG -> {
                vb.includeMenuList.llMenuSearch.performClick()
            }
            //关闭地图
            DataEvent.CLOSE_SWITCH_MAPS -> {
                vb.includeMenuList.llMenuMaps.performClick()
            }

            DataEvent.CLOSE_COLLECTION_FRAG -> {
                vb.includeMenuList.llMenuCollection.performClick()
            }

            DataEvent.CLOSE_GEO_COLLECTION_FRAG -> {
                vb.includeMenuList.llMenuTools.performClick()
            }
            //展示图例
            DataEvent.SHOW_LAYER_LEGEND -> {
                setLegendVisiable(true, event.data as ZttItem)
            }
            //打开缓冲区分析结果列表
            DataEvent.SHOW_QUERY_BUFFER_RESULT_LIST -> {
                vb.includeViewQueryBufferResultList.llQueryBufferResultList.show()
                vb.includeViewLoadingData.rlLoading.gone()
            }
            //关闭缓冲区分析结果列表
            DataEvent.HIDE_QUERY_BUFFER_RESULT_LIST -> {
                vb.includeViewQueryBufferResultList.llQueryBufferResultList.gone()
                vb.includeMenuList.ivAnalysis.setImageResource(R.mipmap.icon_search_c)
                vb.ivFragmentSwitch.gone()
                handleMapUtil.cleaBufferLayer()
                handleMapUtil.clearAreaPoint()
            }
            //展示空间查询结果
            DataEvent.SHOW_QUERY_RESULT_LIST -> {
                vb.includeViewQueryResultList.llQueryResultList.show()
                vb.includeViewQueryResultDetail.llQueryResultDetail.gone()
                vb.includeViewQueryResultDetailList.llQueryResultDetailList.gone()
                vb.includeViewQueryResultReport.llQueryResultReport.gone()
                vb.includeViewLoadingData.rlLoading.gone()
            }
            //关闭所有空间查询结果
            DataEvent.HIDE_QUERY_RESULT -> {
                vb.includeViewQueryResultList.llQueryResultList.gone()
                vb.includeViewQueryResultDetail.llQueryResultDetail.gone()
                vb.includeViewQueryResultDetailList.llQueryResultDetailList.gone()
                vb.includeViewQueryResultReport.llQueryResultReport.gone()
            }
            //展示空间查询结果详情
            DataEvent.SHOW_QUERY_RESULT_DETAIL -> {
                vb.includeViewQueryResultList.llQueryResultList.gone()
                vb.includeViewQueryResultDetail.llQueryResultDetail.show()
                vb.includeViewQueryResultDetailList.llQueryResultDetailList.gone()
            }
            //隐藏空间查询结果详情
            DataEvent.HIDE_QUERY_RESULT_DETAIL -> {
                vb.includeViewQueryResultList.llQueryResultList.show()
                vb.includeViewQueryResultDetail.llQueryResultDetail.gone()
                vb.includeViewQueryResultDetailList.llQueryResultDetailList.gone()
            }
            //展示空间查询结果详情列表
            DataEvent.SHOW_QUERY_RESULT_DETAIL_LIST -> {
                vb.includeViewQueryResultList.llQueryResultList.gone()
                vb.includeViewQueryResultDetail.llQueryResultDetail.gone()
                vb.includeViewQueryResultDetailList.llQueryResultDetailList.show()
            }
            //隐藏空间查询结果详情列表
            DataEvent.HIDE_QUERY_RESULT_DETAIL_LIST -> {
                vb.includeViewQueryResultList.llQueryResultList.gone()
                vb.includeViewQueryResultDetail.llQueryResultDetail.show()
                vb.includeViewQueryResultDetailList.llQueryResultDetailList.gone()
            }
            //展示空间查询结果 一键分析报告
            DataEvent.SHOW_QUERY_RESULT_REPORT -> {
                vb.includeViewQueryResultList.llQueryResultList.gone()
                vb.includeViewQueryResultReport.llQueryResultReport.show()
            }

            //关闭空间查询结果 一键分析报告
            DataEvent.HIDE_QUERY_RESULT_REPORT -> {
                vb.includeViewQueryResultList.llQueryResultList.show()
                vb.includeViewQueryResultReport.llQueryResultReport.gone()
            }

            //隐藏比对地图图层
            DataEvent.HIDE_LAYER_COMPARE -> {
                vb.includeSwitchMapPare.llSwitchMap2.gone()
            }

            DataEvent.HIDE_LOADING -> {
                vb.includeViewLoadingData.rlLoading.gone()
            }

            DataEvent.SHOW_LOADING -> {
                vb.includeViewLoadingData.rlLoading.show()
            }

            DataEvent.SHOW_OVERLY_ANALYSIS -> {
                vb.includeViewOverlyAnalysisResultList.llOverlyAnalysisList.show()

            }

            DataEvent.HIDE_OVERLY_ANALYSIS -> {
                vb.includeViewOverlyAnalysisResultList.llOverlyAnalysisList.gone()
            }
            //保存了
            DataEvent.UPDATE_MARKER_DATA -> {
                isMarkerAnalysisAfterSave.yes {
                    handleMapUtil.startAnalysisOverly()
                }
            }

            DataEvent.CLOSE_AREA -> {
                handleMapUtil.clearDrawLayerAndDismissCallout()

            }
            //绘制选中收藏的标绘
            DataEvent.DRAW_SELECT_GEOJSON -> {
                val model = event.data as GeoJsonModel
                handleMapUtil.drawSelectGeoJson(model)

            }
            DataEvent.DRAW_SELECT_GEOJSON_LIST -> {
                @Suppress("UNCHECKED_CAST")
                val modelList = event.data as List<GeoJsonModel>
                handleMapUtil.drawSelectGeoJsonList(modelList)
            }
            //绘制 SHP
            DataEvent.DRAW_SELECT_SHP -> {
                val model = event.data as ShpModel
                handleMapUtil.drawShpList(listOf(model))
            }
            DataEvent.DRAW_SELECT_SHP_LIST -> {
                @Suppress("UNCHECKED_CAST")
                val modelList = event.data as List<ShpModel>
                handleMapUtil.drawShpList(modelList)
            }
            //关闭外部导入（现集成在 SwitchMap 中）
            DataEvent.CLOSE_GEOJSON_FRAG -> {
                vb.includeMenuList.llMenuMaps.performClick()
                handleMapUtil.clearDrawLayerAndDismissCallout()
            }
            //关闭 SHP 导入
            DataEvent.CLOSE_SHP_FRAG -> {
                vb.includeMenuList.llMenuMaps.performClick()
                handleMapUtil.clearDrawLayerAndDismissCallout()
            }

            DataEvent.GPS_CONTINUOUS_LOCATION -> {
                val isOpenCoutinuousLocation = event.data as Boolean

                isOpenCoutinuousLocation.yes {
                    locateWithLocationCheck(1)
                    vb.tvContinuous.show()
                }.no {
                    locationManager.stopLocation()
                    vb.tvContinuous.gone()
                }
            }

            DataEvent.UPDATE_COLLECTION_DATA -> {

                val flag = event.data as Int
                if (flag == 0) {
                    //兴趣点收藏
                } else {
                    //图斑收藏
                    handleMapUtil.themeCallOut?.updateCollectUI()
                }
            }

            DataEvent.REFRESH_MARKER -> {
                handleMapUtil.markerRefresh()
            }

            DataEvent.EDIT_MARKER_INFO -> {
                val markerInfo = event.data as MarkerInfo
//                handleMapUtil.editGeometyMarker(markerInfo)
                handleMapUtil.editWorkRecord(markerInfo)

            }

            DataEvent.SELECT_BASE_IMAGE_LAYER_NMAE -> {
                vb.tvBaseMapInfo.text = "当前影像：${handleMapUtil.baseMapStack.peek()}"

            }

            DataEvent.UNSELECT_BASE_IMAGE_LAYER_NMAE -> {
                vb.tvBaseMapInfo.text = "当前影像：${handleMapUtil.baseMapStack.peek()}"
            }

            DataEvent.ZTT_DATA_TO_BUFFER_ANALYSIS -> {
                event.data?.apply {
                    val list = event.data as MutableList<ZttItem>
                    queryThemeList = list.filter {
                        it.name.self().contains("乱占")
                                || it.name.self().contains("乱建")
                                || it.name.self().contains("乱堆")
                                || it.name.self().contains("乱采")
                                || it.name.self().contains("其他")
                                || it.name.self().contains("水葫芦")
                                || it.name.self().contains("水利部图斑")
                                || it.name.self().contains("暗访事件")
                    }.map { it.themesInfo!! } as MutableList<ThemesInfo>
                }
            }

            DataEvent.START_BUFFER_ANALYSIS -> {
                event.data?.apply {
                    distance = event.data as Double
                    when (bufferAnalyisQueryType) {
                        0 -> executeBufferQuery(distance)
                        1 -> {
                            val point = handleMapUtil.clickPoint

                            handleMapUtil.markerUtil.addPoint(point)
                            handleMapUtil.executeBufferAnalysis(queryThemeList, point, distance)
                        }
                    }
                }
            }

            DataEvent.CLEAR_DRAW_COLLECTION_THEME -> {
                handleMapUtil.clearPoiSearchLayer()
            }

            DataEvent.BACK_TO_SWITCH_MAP -> {
                vb.includeViewQueryBufferResultList.llQueryBufferResultList.gone()
                vb.includeViewSwitchMap.llSwitchMap.show()
//                handleMapUtil.cleaBufferLayer()
            }

            DataEvent.CLOSE_SWITCH_MAPS_2 -> {
                closeAllMenu()
            }
            //收藏夹统计
            DataEvent.GO_COLLECTION_STATIS_FRAG -> {
                vb.includeViewGeoCollection.llCollection.gone()
                vb.includeViewCollectionStatis.llCollectionStatis.show()
            }  //返回 收藏夹（我的记录）
            DataEvent.BACK_COLLECTION_FRAG -> {
                vb.includeViewGeoCollection.llCollection.show()
                vb.includeViewCollectionStatis.llCollectionStatis.gone()
            }


            DataEvent.LAYER_FILTER_REFRESH -> {
                event.data?.apply {
                    val type = event.data as Int
                    val eventStatusFilterParams = event.data2 as String
                    handleMapUtil.layerFilterRefresh(type, eventStatusFilterParams)

                }
            }

            DataEvent.CANCEL_COLLECT_PATCH -> {
                handleMapUtil.clearCollcetion()
            }

        }
    }

    fun showRightContent(show: Boolean) {
        show.yes {
            vb.llRightFragment.show()
            vb.ivFragmentSwitch.show()
            vb.ivFragmentSwitch.setImageResource(R.mipmap.icon_arrow_close)
        }.no {
            vb.llRightFragment.gone()
            vb.ivFragmentSwitch.gone()
            vb.ivFragmentSwitch.setImageResource(R.mipmap.icon_arrow_open)
        }
    }

    /**
     * 用户主动定位的入口：先生成检查系统"位置信息"开关，未开启则提示并引导去系统设置。
     * 与 [getLocation] 分开，避免 app 启动时自动恢复持续定位（initLocation 直接调 getLocation）
     * 也弹出"请开启定位"对话框。
     */
    private fun locateWithLocationCheck(type: Int) {
        if (!locationManager.isSystemLocationEnabled()) {
            promptEnableLocation()
            return
        }
        getLocation(type)
    }

    /** 系统位置信息未开启时的提示：引导用户去系统设置开启 */
    private fun promptEnableLocation() {
        showNormalDialog(
            this,
            "系统「位置信息」未开启，无法定位。\n请开启后再试。",
            rightText = "去开启",
            leftText = "取消"
        ) { goSettings ->
            if (goSettings) {
                try {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (e: Exception) {
                    tip("无法打开系统定位设置")
                }
            }
        }
    }

    //type 0-单次定位 1-持续定位
    fun getLocation(type: Int) {
        if (type == 0) {
            // 单次定位：走统一定位入口，按优先级复用
            //   1. 后台服务的10秒新鲜位置（首次定位/无缓存时也能秒回）
            //   2. 3秒内的缓存位置
            //   3. 实时定位（华为设备先用华为定位，其余用原生GPS，再按需 fallback）
            // 命中 1/2 时统一定位在当前调用栈内同步回调，页面无闪烁。
            //
            // requestSingleLocation 内部会先 stopLocation() 停掉持续定位（单次/持续共用底层定位头）。
            // 所以点击单次定位前先记录持续定位是否开着，单次定位一结束（无论成败）就把持续定位拉起来，
            // 否则位置就停在单次结果那里不再更新（bug 修复）。
            val restartContinuous = getKV(Keys.IS_CONTINUOUS_LOCATION, false)
            showLocatingProgress()

            // 单次定位成功结果统一处理：画点、居中、提示（按点位新鲜度区分提示文案）
            val handleSingleResult = { lat: Double, lng: Double, msg: String ->
                hideLocatingProgress()
                println("Latitude: $lat, Longitude: $lng")

                if (lat != 0.0 && lng != 0.0) {
                    val point = MapUtil.mapUtil.get_change_geometry_point(lat, lng, Config.sp4490Int)
                    vb.mapView.setViewpointCenterAsync(point, 10000.0) // 单次定位缩放调到 10000
                    handleMapUtil.drawMyLocaton(point, currentArrowAngle())
                    syncHeadingFromCache() // 设备在动且有方位时，校正游戏旋转向量的漂移
                    ToastUtils.show(msg)
                } else {
                    ToastUtils.show("定位失败")
                }
                // 单次定位成功：若之前开着持续定位，恢复它，避免位置停住不动
                restartContinuousLocating(restartContinuous)
            }

            locationManager.requestSingleLocation(
                showTip = false,
                useCache = true,
                maxCacheAgeMs = 3_000L,
                onSuccess = { _, _, _, _, lat, lng ->
                    handleSingleResult(lat, lng, "定位成功")
                },
                onSuccessWeak = { _, _, _, _, lat, lng ->
                    // 兜底/弱信号点：位置是「当前」但精度差，高铁等场景按需如实提示
                    handleSingleResult(lat, lng, "定位成功（信号较弱，位置可能不准确）")
                },
                onFailure = { reason ->
                    hideLocatingProgress()
                    ToastUtils.show("定位失败：${reason.msg}")
                    // 单次定位失败也应恢复持续定位，否则持续定位会被关停
                    restartContinuousLocating(restartContinuous)
                }
            )
        } else {
            startContinuousLocating(notice = true)
        }
    }

    /**
     * 启动持续定位（仅首次提示成功 + 首次居中视角，后续只更新定位点图标位置）。
     * @param notice 是否弹出「正在启动/定位成功」等提示；单次定位后静默恢复持续定位时传 false，避免重复弹吐司。
     */
    private fun startContinuousLocating(notice: Boolean) {
        if (notice) {
            ToastUtils.show("正在启动持续定位...")
        }
        var firstFixHandled = false
        locationManager.startContinuousLocation(
            onSuccess = { province, city, district, address, lat, lng, _ ->
                // 处理定位结果
                println("Latitude: $lat, Longitude: $lng")

                if (lat != 0.0 && lng != 0.0) {
                    val point = MapUtil.mapUtil.get_change_geometry_point(lat, lng, Config.sp4490Int)
                    // 只首次提示成功、首次居中视角；后续定位不再弹吐司、不再改变视野（用本地标志，不依赖底层 isFirstSuccess）
                    if (!firstFixHandled) {
                        firstFixHandled = true
                        vb.mapView.setViewpointCenterAsync(point, 10000.0)
                        if (notice) {
                            ToastUtils.show("定位成功")
                        }
                    }
                    handleMapUtil.drawMyLocaton(point, currentArrowAngle())
                    syncHeadingFromCache() // 设备在动且有方位时，校正游戏旋转向量的漂移
                }
            },
            onFirstFailure = { reason ->
                if (notice) {
                    ToastUtils.show("定位失败：${reason.msg}")
                }
            }
        )
    }

    /**
     * 单次定位结束后，若当时持续定位是开着的，就静默地把持续定位恢复起来。
     */
    private fun restartContinuousLocating(wasOn: Boolean) {
        if (wasOn) {
            startContinuousLocating(notice = false)
        }
    }

    /**
     * 华为定位测试按钮：只用华为定位，不走缓存/最后已知位置，强制实时定位。
     * 与上方 ivMyLocation（统一定位）对比两种方式的结果差异。
     */
    private fun getHuaweiLocation() {
        // 非华为设备直接提示，避免误导（当前 provider 若非华为，说明华为定位未启用）
        if (locationManager.getCurrentProvider() != UnifiedLocationManager.LocationProvider.HUAWEI) {
            ToastUtils.show("当前非华为定位环境，华为定位可能失败")
        }

        showLocatingProgress()
        locationManager.requestSingleProvider(
            provider = UnifiedLocationManager.LocationProvider.HUAWEI,
            useLastKnownLocation = false, // 强制真实华为定位，不用缓存
            onSuccess = { _, _, _, _, lat, lng ->
                hideLocatingProgress()
                if (lat != 0.0 && lng != 0.0) {
                    val point = MapUtil.mapUtil.get_change_geometry_point(lat, lng, Config.sp4490Int)
                    vb.mapView.setViewpointCenterAsync(point, 8000.0)
                    handleMapUtil.drawMyLocaton(point, currentArrowAngle())
                    ToastUtils.show("华为定位成功")
                } else {
                    ToastUtils.show("华为定位失败")
                }
            },
            onFailure = { reason ->
                hideLocatingProgress()
                ToastUtils.show("华为定位失败：${reason.msg}")
            }
        )
    }

    /**
     * 单次定位等待期：底部标签全程跑"点号递增"动画（每 850ms 递增），
     * 10s / 20s 时切换底文字，动画不间断。
     */
    private fun showLocatingProgress() {
        locatingHandler.removeCallbacksAndMessages(null)
        vb.tvLocating.show()
        startLocatingDotsAnimation("正在定位，请稍候")
        locatingHandler.postDelayed(
            { updateLocatingBaseText("GPS信号较弱，正在搜索卫星") }, 10_000L
        )
        locatingHandler.postDelayed(
            { updateLocatingBaseText("定位较慢，可移动到开阔地带") }, 20_000L
        )
    }

    /** 单次定位结束：取消进度更新（含动画）并隐藏底部标签 */
    private fun hideLocatingProgress() {
        locatingHandler.removeCallbacksAndMessages(null)
        stopLocatingDotsAnimation()
        vb.tvLocating.gone()
    }

    /** 切换动画底文字（保持点号动画不断）：用于 10s / 20s 的进度切换 */
    private fun updateLocatingBaseText(base: String) {
        locateDotsBaseText = base
        val dots = if (locateDotsCount == 0) 1 else locateDotsCount
        vb.tvLocating.text = base + ".".repeat(dots)
    }

    /** 点号递增动画：xxx. → xxx.. → xxx... → 循环（每 850ms） */
    private fun startLocatingDotsAnimation(baseText: String) {
        locateDotsBaseText = baseText
        locateDotsCount = 1
        vb.tvLocating.text = baseText + "."
        locateDotsRunnable = object : Runnable {
            override fun run() {
                locateDotsCount = locateDotsCount % 3 + 1
                vb.tvLocating.text = locateDotsBaseText + ".".repeat(locateDotsCount)
                locateDotsRunnable?.let { locatingHandler.postDelayed(it, 850L) }
            }
        }
        locateDotsRunnable?.let { locatingHandler.postDelayed(it, 850L) }
    }

    /** 停止点号递增动画 */
    private fun stopLocatingDotsAnimation() {
        locateDotsRunnable?.let { locatingHandler.removeCallbacks(it) }
        locateDotsRunnable = null
    }

    fun setLegendVisiable(visiable: Boolean, data: ZttItem?) {
        if (data == null) return

        //单个图层
        val info = data.themesInfo!!
        //选中
        if (isNotEmpty(info.legendFile)) {
            if (data.isCheck) {
                //选中
                if (!legendList?.contains(info)!!) {
                    legendList!!.add(info)
                }
            } else {
                //反选
                if (matchList(legendList) && legendList!!.contains(info)) {
                    legendList?.remove(info)
                }
            }
        }

        if (matchList(legendList)) {
            val info = legendList!![legendList!!.size - 1]
            val imgUrl: String = Config.THEMEDATA_PIC_PATH + info.legendFile
            print("图例url===$imgUrl")
            //加载bitmap
            Glide.with(context).asBitmap().load(imgUrl).into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    vb.imgLegend.setImageBitmap(resource)
                }
            })
            vb.cbLegend.show()
        } else {
            vb.cbLegend.gone()
            vb.cbLegend.isChecked = false
            vb.imgLegend.gone()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        (resultCode == RESULT_OK).yes {
//            when(requestCode){
//
//            }
//        }
    }

    private fun showSettingDialog() {
        if (settingDialog == null) {
            settingDialog = SettingDialog(context)
        }
        //防止重复展示dialog
        if (!settingDialog!!.isShowing) {
            settingDialog!!.show()
        }
    }

    override fun onResume() {
        vb.mapView?.resume()
        vb.mapView2?.resume()
        super.onResume()
        // 罗盘只在主界面可见、且开关开启时监听，页面切走就停，避免常驻耗电
        if (compassHelper.isAvailable && Config.showHeadingOnLocation) {
            compassHelper.start()
        }
//        initLockPane()
//        startActivity<AlbumActivity>()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // 保存当前底图名称
        if (::handleMapUtil.isInitialized) {
            handleMapUtil.baseMapStack.peek()?.let { baseMapName ->
                outState.putString(KEY_BASE_MAP_NAME, baseMapName)
                // 同时保存到 SharedPreferences，处理进程被彻底杀死的情况
                MapStatePrefs.saveBaseMapName(this, baseMapName)
            }

            // 保存地图视角
            vb.mapView.getCurrentViewpoint(com.esri.arcgisruntime.mapping.Viewpoint.Type.CENTER_AND_SCALE)?.let { vp ->
                vp.targetGeometry?.let { geo ->
                    if (geo is Point) {
                        outState.putDouble(KEY_MAP_CENTER_X, geo.x)
                        outState.putDouble(KEY_MAP_CENTER_Y, geo.y)
                        // 同时保存到 SharedPreferences
                        MapStatePrefs.saveMapViewpoint(this, geo.x, geo.y, vp.targetScale)
                    }
                }
                outState.putDouble(KEY_MAP_SCALE, vp.targetScale)
            }
        }
    }

    /**
     * 恢复地图状态（底图和视角）
     * 优先从 Bundle 恢复（Activity 重建场景）
     * 如果 Bundle 为空，则从 SharedPreferences 恢复（进程被杀后重启场景）
     */
    private fun restoreMapState() {
        val state = savedInstanceState
        savedInstanceState = null // 清空，避免重复恢复

        // 1. 尝试从 Bundle 恢复
        var baseMapName: String? = null
        var centerX = 0.0
        var centerY = 0.0
        var scale = 0.0

        state?.let { bundle ->
            baseMapName = bundle.getString(KEY_BASE_MAP_NAME)
            centerX = bundle.getDouble(KEY_MAP_CENTER_X, 0.0)
            centerY = bundle.getDouble(KEY_MAP_CENTER_Y, 0.0)
            scale = bundle.getDouble(KEY_MAP_SCALE, 0.0)
        }

        // 2. 如果 Bundle 为空，尝试从 SharedPreferences 恢复
        if (baseMapName == null) {
            baseMapName = MapStatePrefs.getBaseMapName(this)
            MapStatePrefs.getMapViewpoint(this)?.let { vp ->
                centerX = vp.first
                centerY = vp.second
                scale = vp.third
            }
        }

        // 3. 恢复底图
        baseMapName?.let { name ->
            "恢复底图: $name".printMsg()
            when (name) {
                "影像底图2022" -> handleMapUtil.switchBaseLyer(MapEvent.SET_IMAGE_MAP_2022)
                "影像底图2024" -> handleMapUtil.switchBaseLyer(MapEvent.SET_IMAGE_MAP_2024)
                "影像底图2025" -> handleMapUtil.switchBaseLyer(MapEvent.SET_IMAGE_MAP_2025)
                // 2018 是默认，无需切换
            }
            vb.tvBaseMapInfo.text = "当前影像：$name"
        }

        // 4. 恢复地图视角
        if (centerX != 0.0 && centerY != 0.0 && scale != 0.0) {
            val point = Point(centerX, centerY, handleMapUtil.mapView.spatialReference)
            handleMapUtil.mapView.setViewpointAsync(com.esri.arcgisruntime.mapping.Viewpoint(point, scale))
            "恢复地图视角: x=$centerX, y=$centerY, scale=$scale".printMsg()
        }
    }

    override fun onPause() {
        vb.mapView?.pause()
        vb.mapView2?.pause()
        compassHelper.stop()
        super.onPause()
    }

    override fun onDestroy() {
        locatingHandler.removeCallbacksAndMessages(null)
        vb.tvLocating.gone()
        compassHelper.stop()
        compassHelper.onHeadingChanged = null
        vb.mapView?.dispose()
        vb.mapView2?.dispose()
        EventBus.getDefault().unregister(this)
        locationManager.release()
        OperationLogger.logOperation(context, "软件关闭")
        super.onDestroy()
    }

    override fun onBackPressed() {


        showNormalDialog(context, "是否退出应用？") {
            it.yes {
                PageManager.exit()
            }
        }
    }

    //工具
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            //手势查询
            R.id.rbHandSearch -> (isChecked).yes {

                vb.includeViewHandSearchActions.llHandSearchActions.show() //手势查询
                vb.includeViewMarkerActions.llMarkerActions.gone()//隐藏标绘
                vb.includeViewMeasureActions.llMeasureActions.gone()//隐藏测量
                vb.includeViewOverlyAnalysisActions.llAnalysisActions.gone()//隐藏叠加分析

                handleMapUtil.startHandSearch()
//                mapCompareUtil.startHandSearch()
                vb.includeMenuList.llMenuTools.performClick()
            }.no {
                vb.includeViewHandSearchActions.llHandSearchActions.gone()
                handleMapUtil.endHandSearch()
//                mapCompareUtil.endHandSearch()
            }
            //坐标定位
            R.id.rbCoordinatePosition -> (isChecked).yes {

                if (coordinatePositionDialog == null) {
                    coordinatePositionDialog = CoordinatePositionDialog(
                        context,
                        handleMapUtil.mapView,
                        object : ActionListener {
                            override fun onAction(obj: Any?, flag: Int) {}
                        })
                    coordinatePositionDialog!!.mActivity = this
                }
                //防止重复展示dialog
                if (!coordinatePositionDialog!!.isShowing) {
                    coordinatePositionDialog?.show()
                }
                vb.includeMenuList.llMenuTools.performClick()
            }.no {

            }


        }
    }


    private fun showNetDialog() {

        if (!::netDialog.isInitialized) {
            netDialog = NetDialog(this)
        }
        if (!netDialog.isShowing && FunctionControlUtil.instances.OFFLINE_USE) {
            netDialog.show()
        }
    }

    private fun closeNetDialog() {
        if (::netDialog.isInitialized) {
            if (netDialog.isShowing) {
                netDialog.dismiss()
            }
        }
    }


}
