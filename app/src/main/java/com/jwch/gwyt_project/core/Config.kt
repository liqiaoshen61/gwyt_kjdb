package com.jwch.gwyt_project.core

import android.content.Context
import com.esri.arcgisruntime.geometry.SpatialReference
import java.io.File

object Config {

    val requestUrl = ""//汤

    const val pageNum = 10
    const val debugModel = true

    // 是否启用授权码验证
    const val ENABLE_LICENSE_CHECK = true

    /**
     * 定位时是否展示方位角度。
     * true  ：地图上的定位箭头随设备朝向旋转（需要罗盘传感器，常驻监听）。
     * false ：箭头固定朝上（屏幕正北），且不启动罗盘传感器，省电。
     */
    var showHeadingOnLocation = true

    val sp4326Int = 4326
    val sp4490Int = 4490
    val def_sp_Int = 4548
    val sp4548Int_longhai = 4548

    val sp4326 = SpatialReference.create(sp4326Int)
    val sp4490 = SpatialReference.create(sp4490Int)
    val def_sp = SpatialReference.create(def_sp_Int)


    const val licence = "runtimelite,1000,rud4537708469,none,NKMFA0PL4PP4NERL1007"

    const val HEAD_FILE_PATH = "/sdcard/OfficeMap/"
    const val APPDB_PATH = HEAD_FILE_PATH + "AppDb/"

    // 外部存储应用专用目录（用于可读写数据库）
    lateinit var PTDB_PATH: String  // ptAppDb 数据库路径

    fun initPrivatePaths(context: Context) {
        // 使用外部存储应用专用目录：/sdcard/Android/data/包名/databases/
        val basePath = context.getExternalFilesDir("databases")?.absolutePath
            ?: (context.filesDir.absolutePath + "/databases")
        // 确保路径以斜杠结尾，方便后续拼接
        PTDB_PATH = if (basePath.endsWith("/")) basePath else "$basePath/"
        // 确保目录存在
        File(PTDB_PATH).mkdirs()
    }
    const val ATLASDATA_PATH = HEAD_FILE_PATH + "AtlasData/"
    const val DOC_PATH = HEAD_FILE_PATH + "Doc/"
    const val EMAPDATA_PATH = HEAD_FILE_PATH + "EMapData/"
    const val THEMEDATA_TPK_PATH = HEAD_FILE_PATH + "ThemeData/tpk/"
    const val THEMEDATA_SHP_PATH = HEAD_FILE_PATH + "ThemeData/shp/"
    const val THEMEDATA_PIC_PATH = HEAD_FILE_PATH + "ThemeData/pic/"
    const val THEME_CONFIG_PATH = APPDB_PATH + "theme_config.json"
    const val FILE_CAMERA_PATH = HEAD_FILE_PATH + "file/camera/"
    const val FILE_JIETU_PATH = HEAD_FILE_PATH + "file/jietu/"
    const val FILE_HTML_PATH = HEAD_FILE_PATH + "file/"
    const val OUTPUT_PATH = HEAD_FILE_PATH + "output/"
    const val MEDIA_PATH = HEAD_FILE_PATH + "MediaData/"//这个目录在officemap文件夹中都不见了，估计废弃了
    const val GDB_PATH = HEAD_FILE_PATH + "gdb/"
    const val EXCEL_PATH = DOC_PATH + "Excel/"
    const val GEO_JSON = HEAD_FILE_PATH + "geojson/"
    const val SHP_IMPORT_PATH = HEAD_FILE_PATH + "shp/"
    const val SCREEN_SHOTS_PAHT = HEAD_FILE_PATH + "ScreenShots/"

    const val PHOTO_PATH = HEAD_FILE_PATH + "photo/"

    const val TAKE_PHOTO_PATH = HEAD_FILE_PATH + "takePhoto/"


    const val OUTPUT_MARKER_PATH = OUTPUT_PATH + "工作记录/"
    const val OUTPUT_OVERLAY_PATH = OUTPUT_PATH + "叠加分析/"

    const val LOG_PATH = HEAD_FILE_PATH + "log/"


    // ==================== 业务图片配置 ====================
    /**
     * 业务图片配置项
     * @param themeKey themeName 中包含的关键词，用于识别业务类型
     * @param folderName 图片文件夹名称（相对于 OfficeMap/）
     * @param attrField 图斑属性字段名，该字段值用于匹配子文件夹名称
     *
     * 示例：
     * - themeName 包含 "水库" → 到 OfficeMap/水库/ 下查找
     *   匹配 attrs["水库名称"] 的值对应的文件夹
     *
     * - themeName 包含 "厂房" → 到 OfficeMap/厂房/ 下查找
     *   匹配 attrs["厂房名称"] 的值对应的文件夹
     */
    data class BusinessImageConfig(
        val themeKey: String,      // themeName 关键词
        val folderName: String,    // 文件夹名称
        val attrField: String      // 属性字段名
    )

    /**
     * 业务图片配置列表
     * 新增业务类型只需在此列表中添加配置项即可
     */
    val BUSINESS_IMAGE_CONFIGS = listOf(
        BusinessImageConfig("水库", "水库", "水库名称"),
        BusinessImageConfig("厂房", "厂房", "厂房名称"),
        BusinessImageConfig("拦河建筑", "拦河建筑", "拦河建筑名称")
    )
    // ==================== 业务图片配置 END ====================


    val primaryKey = "问题编"
    val primaryKey2 = "编码"
    const val SAVE_GEOMETRY_CAMERA = 100
    const val SAVE_EXTRA_FILE = 101
    const val CALLOUT_GO_CAMERA = 102
    const val SAVE_GEOMETRY_CAMERA_EXIST = 103 //已经收藏的标绘 拍照
    const val POI_SAVE_PHOTO = 104 //兴趣点拍照 结束

    const val CITY_YXT_ID = 1001 //城区高清影像图

    var AreaCode = "" //
    var userLevel = 1  //



    //  val DEF_MAP_SCALE = 228784.240214328 //初始化默认展示缩放大小
//    val DEF_MAP_SCALE = 657195.26864 //初始化默认展示缩放大小
    val DEF_MAP_SCALE = 157195.26864 //初始化默认展示缩放大小

    const val START_PAGE_DELAY_TIME = 1000 * 1L//启动页展示时间
    const val RESULT_CODE = 100 //页面返回成功码


    const val timeFormat1 = "yyyy-MM-dd HH:mm:ss"
    const val timeFormat2 = "yyyy-MM-dd HH:mm"
    const val timeFormat3 = "yyyy-MM-dd"
    const val timeFormat4 = "yyyy年MM月"

    val USER_LEVEL_PROVINCE = 1 //省级
    val USER_LEVEL_CITY = 2 //市级
    val USER_LEVEL_COUNTY = 3 //区县级 区县级用户 不给看 暗访事件和自查县区自查
    val USER_LEVEL_TOWN = 4 //乡镇级
    val USER_LEVEL_VILLAGE = 5 //村级


}