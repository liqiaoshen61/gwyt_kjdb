package com.jwch.gwyt_project.core

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.BuildConfig
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.getListFromJson
import com.jwch.gwyt_project.ext.getObjFromJson2
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.model.UserModel
import com.jwch.gwyt_project.config.ThemeConfigLoader
import com.jwch.gwyt_project.util.DistrictQueryHelper
import com.jwch.gwyt_project.util.GdbSizeManager
import com.jwch.gwyt_project.util.GetJsonUtil
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.util.NetUtil
import com.jwch.gwyt_project.util.NetworkConnectChangedReceiver
import com.jwch.gwyt_project.util.RegionConfigManager
import com.jwch.gwyt_project.util.ShpLoader
import com.jwch.gwyt_project.service.BackgroundLocationService
import android.content.Intent
import com.jwch.gwyt_project.util.download.NoEtagFileDownloadUrlConnection
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.connection.FileDownloadConnection
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection
import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AppContext : MultiDexApplication() {


    companion object {
        lateinit var app: AppContext
        lateinit var map: MutableMap<String, Any?>
        open var hasNet = false//无网络

        @Volatile
        private var currentActivity: android.app.Activity? = null

        // License过期信息（用于延迟显示弹窗）
        @Volatile
        private var pendingLicenseExpiredDate: String? = null

        // 授权是否有效
        @Volatile
        private var isLicenseValid: Boolean = true

        // 默认异常处理器
        private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

        fun getCurrentActivity(): android.app.Activity? = currentActivity

        /**
         * 检查授权是否有效
         */
        fun isAuthorized(): Boolean = isLicenseValid
    }

    lateinit var jsonMap: Map<String, JSONObject>

    private var user: UserModel? = null

    lateinit var districtHelper : DistrictQueryHelper

    var regionInfo = ""

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)

        // 在最早的时机设置异常捕获
        setupCrashHandler()
    }

    //建立索引
    fun JsonDataManager(json: String) : Map<String, JSONObject>{

        val startTime = System.currentTimeMillis()
        val jsonArray = CommonUtil.makeJSONArray(json)
        val tempMap = mutableMapOf<String, JSONObject>()

        (0 until jsonArray.length()).forEach { i ->
            val item = jsonArray.getJSONObject(i)
            val spotId = item.optString("问题编号")
            if (!spotId.isNullOrEmpty()) {
                tempMap[spotId] = item
            }
        }

        Log.d("JsonDataManager", "JSON索引初始化完成，耗时 ${System.currentTimeMillis() - startTime}ms")
        return  tempMap.toMap() // 转为不可变Map

    }



    override fun onCreate() {


        super.onCreate()
        app = this
        map = mutableMapOf()

        // 初始化路径配置（放在最前面）
        Config.initPrivatePaths(this)

        // 注册Activity生命周期回调，追踪当前Activity
        registerActivityLifecycleCallbacks(object : android.app.Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityStarted(activity: android.app.Activity) {}
            override fun onActivityResumed(activity: android.app.Activity) {
                currentActivity = activity
                // 检查是否有待显示的License过期弹窗
                pendingLicenseExpiredDate?.let { expireDate ->
                    pendingLicenseExpiredDate = null // 清除标记
                    showLicenseExpiredDialog(expireDate)
                }
            }
            override fun onActivityPaused(activity: android.app.Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }
            override fun onActivityStopped(activity: android.app.Activity) {}
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: android.app.Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }
        })

        // 迁移旧的 ptAppDb 数据库到新位置
        migratePtDatabaseIfNeeded()

        // 删除 AppDb 的临时文件（ptAppDb 已迁移，不需要删除）
        deleteFilesIfExist(Config.APPDB_PATH+"AppDb.db-shm")



//        initGreenDao()
        initXUtil()
        initBaiduMap()
        initGDAL()
        initMmkv()
        netListener()
        ToastUtils.init(this)
        initDownload()
        initDistrictsData()


        val regionInfoJson = GetJsonUtil.getJsonFromFile(Config.APPDB_PATH +"region_info.txt").self().trim()
        val regionInfo = try {
            org.json.JSONObject(regionInfoJson).optString("region", "")
        } catch (e: Exception) {
            regionInfoJson
        }
        RegionConfigManager.setRegion(regionInfo)


//

        // 初始化专题配置（JSON优先，DB回退）
        initThemeConfig()

        var gson = GetJsonUtil.getJsonFromFile(Config.HEAD_FILE_PATH +"四乱+暗访_全省.json")
        if(gson.isBlank()){
            gson = GetJsonUtil.getJsonFromAssets("四乱+暗访_全省.json", this)
        }
        jsonMap =  JsonDataManager(gson)

        val manager = GdbSizeManager()
        manager.checkAndUpdateFolderSize(Config.GDB_PATH)

        // 启动后台定位服务
        startBackgroundLocationService()

    }


    fun initThemeConfig() {
        try {
            // 始终从 assets 读取到内存，不再写入 SD 卡
            val jsonStr = GetJsonUtil.getJsonFromAssets("theme_config.json", this)
            ThemeConfigLoader.init(jsonStr)
        } catch (e: Exception) {
            Log.e("AppContext", "初始化 ThemeConfig 失败: ${e.message}")
        }
    }

    fun initDistrictsData(){
        val districtsGson1 = GetJsonUtil.getJsonFromAssets("全省省级_modified.json", this)
        val districtsGson2 = GetJsonUtil.getJsonFromAssets("全省市级_modified.json", this)
        val districtsGson3 = GetJsonUtil.getJsonFromAssets("全省区县_modified.json", this)
        val districtsGson4 = GetJsonUtil.getJsonFromAssets("全省乡镇_modified.json", this)
        val districtsGson5 = GetJsonUtil.getJsonFromAssets("全省村级_modified.json", this)

        val districtsList1  = getListFromJson<DistrictsInfo>(districtsGson1)
        val districtsList2  = getListFromJson<DistrictsInfo>(districtsGson2)
        val districtsList3  = getListFromJson<DistrictsInfo>(districtsGson3)
        val districtsList4  = getListFromJson<DistrictsInfo>(districtsGson4)
        val districtsList5  = getListFromJson<DistrictsInfo>(districtsGson5)

        districtHelper = DistrictQueryHelper(districtsList1 + districtsList2 + districtsList3 + districtsList4+districtsList5)
    }

    /**
     * 启动后台定位服务
     * Android 8.0+ 需要使用 startForegroundService
     */
    private fun startBackgroundLocationService() {
        try {
            val intent = Intent(this, BackgroundLocationService::class.java).apply {
                action = BackgroundLocationService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Log.d("AppContext", "后台定位服务已启动")
        } catch (e: Exception) {
            Log.e("AppContext", "启动后台定位服务失败: ${e.message}")
        }
    }

    /**
     * 停止后台定位服务
     */
    fun stopBackgroundLocationService() {
        try {
            val intent = Intent(this, BackgroundLocationService::class.java).apply {
                action = BackgroundLocationService.ACTION_STOP
            }
            startService(intent)
            Log.d("AppContext", "后台定位服务已停止")
        } catch (e: Exception) {
            Log.e("AppContext", "停止后台定位服务失败: ${e.message}")
        }
    }


    // 快速查询
    fun getJsonBySpotId(spotId: String?): JSONObject? {
        return spotId?.let { jsonMap[it] }
    }

    private fun netListener() {
        // 初始化当前网络状态
        hasNet = NetUtil.isNetworkConnected(this)

        val networkConnectChangedReceiver  = NetworkConnectChangedReceiver()
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkConnectChangedReceiver,filter)
    }



    private fun initXUtil() {
        org.xutils.x.Ext.init(this)
        org.xutils.x.Ext.setDebug(BuildConfig.DEBUG)
        //初始化数据库
        //DbUtil.db
    }

//    private fun initGreenDao() {
//        DbManager.initDao(app)
//    }


    private fun initBaiduMap() {
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(applicationContext)
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }

    private fun initGDAL() {
        ShpLoader.initGDAL()
    }

    private fun initMmkv() {


        val rootDir = MMKV.initialize(app)
//        PrintUtil.printMsg("rootDir==$rootDir")
    }



    fun getUser(): UserModel? {

        if (user == null) {

            val strdata = getKV<String>(Keys.USER_DATA).self()
            if (strdata.isNotEmpty()) {
                user = getObjFromJson2(strdata, UserModel::class.java)
            }
        }

        return user
    }


    fun saveUser(userModel: UserModel?) {
        user = userModel
        if (user == null) saveKV(Keys.USER_DATA, "")
        else saveKV(Keys.USER_DATA, user.toJson())
    }


    fun islogin(): Boolean {
        getUser()
        return user != null
    }


    fun logout() {
        saveUser(null)
        user = null
    }

    private fun initDownload() {

        FileDownloader.setupOnApplicationOnCreate(this).connectionCreator(object : FileDownloadUrlConnection.Creator(
            FileDownloadUrlConnection.Configuration().connectTimeout(15000) // set connection timeout.
                .readTimeout(15000) // set read timeout.
        ) {
            @Throws(IOException::class)
            //对于一个链接 如果在浏览器中访问的行为不是下载，而是浏览，那么需要执行下面的代码，抛弃Etag 之后文件是可以正常下载
            override fun create(originUrl: String): FileDownloadConnection {
                return NoEtagFileDownloadUrlConnection(originUrl)
            }
        }).connectionCountAdapter { downloadId: Int, url: String?, path: String?, totalLength: Long ->
            1 //下载文件块数是1,解决偶现的下载任务停止问题。
        }.commit()

        //FileDownload 日志
//        FileDownloadLog.NEED_LOG = true

    }

    /**
     * 迁移 ptAppDb 数据库到新位置（从公共目录迁移到应用专用目录）
     */
    private fun migratePtDatabaseIfNeeded() {
        val oldDbPath = Config.APPDB_PATH + "ptAppDb.db"
        val newDbPath = Config.PTDB_PATH + "ptAppDb.db"

        val oldDbFile = File(oldDbPath)
        val newDbFile = File(newDbPath)

        // 如果新位置没有数据库，但旧位置有，则迁移
        if (!newDbFile.exists() && oldDbFile.exists()) {
            try {
                newDbFile.parentFile?.mkdirs()
                oldDbFile.copyTo(newDbFile, overwrite = true)
                Log.d("Migration", "成功迁移 ptAppDb.db: $oldDbPath -> $newDbPath")

                // 同时复制相关的临时文件（-shm, -wal）
                listOf("-shm", "-wal").forEach { suffix ->
                    val oldFile = File(oldDbPath + suffix)
                    if (oldFile.exists()) {
                        oldFile.copyTo(File(newDbPath + suffix), overwrite = true)
                        Log.d("Migration", "成功迁移 ptAppDb.db$suffix")
                    }
                }
            } catch (e: Exception) {
                Log.e("Migration", "迁移失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteFilesIfExist(vararg fileNames: String): List<String> {
        val deletedFiles = mutableListOf<String>()

        fileNames.forEach { fileName ->
            val file = File(fileName)
            if (file.exists()) {
                if (file.delete()) {
                    println("成功删除文件: $fileName")
                    deletedFiles.add(fileName)
                } else {
                    println("无法删除文件: $fileName")
                }
            } else {
                println("文件不存在: $fileName")
            }
        }

        return deletedFiles
    }

    /**
     * 验证License授权
     */
    fun verifyLicense(regionInfoJson: String) {
        Log.d("LicenseManager", "========== 开始验证授权 ==========")

        // 检查是否启用授权验证
        if (!Config.ENABLE_LICENSE_CHECK) {
            Log.d("LicenseManager", "授权验证已禁用，跳过验证")
            Log.d("LicenseManager", "========== 授权验证结果: 通过（已禁用） ==========")
            isLicenseValid = true
            return
        }

        // 1. 文件不存在或内容为空
        if (regionInfoJson.isBlank()) {
            Log.e("LicenseManager", "验证失败: region_info.txt 文件不存在或内容为空")
            Log.d("LicenseManager", "========== 授权验证结果: 失败 ==========")
            isLicenseValid = false
            showLicenseExpiredDialog("授权失败，请检查授权码")
            return
        }

        Log.d("LicenseManager", "读取到内容: $regionInfoJson")

        try {
            val jsonObj = org.json.JSONObject(regionInfoJson)

            // 2. 无 license 字段
            if (!jsonObj.has("license")) {
                Log.e("LicenseManager", "验证失败: 缺少 license 字段")
                Log.d("LicenseManager", "========== 授权验证结果: 失败 ==========")
                isLicenseValid = false
                showLicenseExpiredDialog("授权失败，请检查授权码")
                return
            }

            val license = jsonObj.optString("license", "")
            Log.d("LicenseManager", "license字段值: $license")

            // 3. license 字段值为空
            if (license.isBlank()) {
                Log.e("LicenseManager", "验证失败: license 字段值为空")
                Log.d("LicenseManager", "========== 授权验证结果: 失败 ==========")
                isLicenseValid = false
                showLicenseExpiredDialog("授权失败，请检查授权码")
                return
            }

            // 4. 验证 license
            val result = com.jwch.gwyt_project.util.LicenseManager.verifyLicense(license)
            when (result) {
                is com.jwch.gwyt_project.util.LicenseManager.LicenseResult.Valid -> {
                    Log.d("LicenseManager", "验证通过: 授权有效，到期日期: ${result.expireDate}")
                    Log.d("LicenseManager", "========== 授权验证结果: 通过 ==========")
                    isLicenseValid = true
                }
                is com.jwch.gwyt_project.util.LicenseManager.LicenseResult.Expired -> {
                    Log.w("LicenseManager", "验证失败: 授权已过期，到期日期: ${result.expireDate}")
                    Log.d("LicenseManager", "========== 授权验证结果: 失败（已过期） ==========")
                    isLicenseValid = false
                    showLicenseExpiredDialog("授权失败，\n到期日期：${result.expireDate}")
                }
                is com.jwch.gwyt_project.util.LicenseManager.LicenseResult.Error -> {
                    Log.e("LicenseManager", "验证失败: ${result.message}")
                    Log.d("LicenseManager", "========== 授权验证结果: 失败 ==========")
                    isLicenseValid = false
                    showLicenseExpiredDialog("授权失败，请检查授权码")
                }
            }
        } catch (e: Exception) {
            // JSON 解析失败
            Log.e("LicenseManager", "验证失败: JSON解析异常 - ${e.message}")
            Log.d("LicenseManager", "========== 授权验证结果: 失败 ==========")
            isLicenseValid = false
            showLicenseExpiredDialog("授权失败，请检查授权码")
        }
    }

    /**
     * 显示授权过期弹窗
     */
    private fun showLicenseExpiredDialog(message: String) {
        val activity = currentActivity
        if (activity != null) {
            activity.runOnUiThread {
                try {
                    val dialog = com.jwch.gwyt_project.view.LicenseExpiredDialog(activity, message)
                    dialog.show()
                } catch (e: Exception) {
                    Log.e("LicenseManager", "显示弹窗失败: ${e.message}")
                }
            }
        } else {
            // 暂时没有Activity，记录下来等Activity启动后显示
            pendingLicenseExpiredDate = message
            Log.d("LicenseManager", "暂无Activity，延迟显示过期弹窗")
        }
    }

    /**
     * 设置全局异常捕获
     */
    private fun setupCrashHandler() {
        try {
            defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
            Log.e("CrashHandler", "全局异常捕获器已设置")
        } catch (e: Exception) {
            Log.e("CrashHandler", "设置异常捕获器失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 自定义异常处理器
     */
    private inner class CrashHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, ex: Throwable) {
            // 保存异常信息到本地文件
            saveCrashToFile(ex)
            // 调用默认的异常处理器（让系统处理崩溃）
            defaultExceptionHandler?.uncaughtException(thread, ex) ?: run {
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
            }
        }
    }

    /**
     * 将异常信息保存到本地txt文件
     */
    private fun saveCrashToFile(ex: Throwable) {
        try {
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            // 使用应用外部存储目录：Android/data/com.jwch.gwyt_project/files/crash_logs/
            // 这个目录不需要权限，Android 10+ 也可以正常访问
            val crashDir = File(getExternalFilesDir(null), "crash_logs")
            if (!crashDir.exists()) {
                crashDir.mkdirs()
            }

            // 创建日志文件
            val crashFile = File(crashDir, "crash_$fileName.txt")

            // 写入异常信息
            PrintWriter(FileWriter(crashFile, true)).use { writer ->
                writer.println("========== 崩溃日志 ==========")
                writer.println("时间: $time")
                writer.println("设备型号: ${Build.MODEL}")
                writer.println("系统版本: ${Build.VERSION.RELEASE}")
                writer.println("应用版本: ${BuildConfig.VERSION_NAME}")
                writer.println("版本号: ${BuildConfig.VERSION_CODE}")
                writer.println("当前Activity: ${currentActivity?.javaClass?.simpleName ?: "无"}")
                writer.println()
                writer.println("异常信息:")
                ex.printStackTrace(writer)
                writer.println()
                writer.println("==============================")
                writer.println()
            }

            Log.e("CrashHandler", "崩溃日志已保存: ${crashFile.absolutePath}")
            Log.e("CrashHandler", "请使用文件管理器查看: Android/data/com.jwch.gwyt_project/files/crash_logs/")

        } catch (e: Exception) {
            Log.e("CrashHandler", "保存崩溃日志失败: ${e.message}")
            e.printStackTrace()
        }
    }
}