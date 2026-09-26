package com.jwch.gwyt_project.util

import com.esri.arcgisruntime.internal.jni.fa
import kotlin.math.truncate


class FunctionControlUtil {

    companion object {
        var instances = FunctionControlUtil()
    }

    val OFFLINE_USE = true //断网离线使用

    val AUTHORIZE_DEVICE = true //设备id授权

    val FILE_NEED_DECRYPTED = true //附件文件是否需要解密

    //左侧菜单按钮
    val BUTTON_AREA = true //行政区划
    val BUTTON_SERACH = false //搜索
    val BUTTON_THEME_LAYER = true //专题图
    val BUTTON_COLLECTION = true //收藏夹
    val BUTTON_TOOL_BOX = true //工具
    val BUTTON_SETTING = true //设置

    //右下角按钮
    val BUTTON_CHANGE_SCREEN_SHOTS = true //截屏
    val BUTTON_LOCATION = true //定位
    val BUTTON_CLEAR_MAP = true //清除地图
    val BUTTON_RELOCATION = true //回到初始视角
    val BUTTON_CHANGE_BASE_MAP = false //切换底图

    //行政区功能块中的 功能配置
    val MODULE_AREA_STATISTICS = true //行政区划--统计

    //专题图功能块中的 功能配置
    val MODULE_LAYERS_ALBUM= false //专题图--地图册

    //我的收藏功能块中的 功能配置
    val MODULE_COLLECTION_EXPORT= true //我的收藏--导出数据
    val MODULE_COLLECTION_CREATE_FOLDER= false //我的收藏--新建文件夹

    //工具功能块中的 功能配置
    val MODULE_TOOLS_MARKER= false //工具--标会
    val MODULE_TOOLS_MEASURE= false //工具--量测
    val MODULE_TOOLS_COMPARE_MAP= false //工具--地图比对

    //设置功能块中的 功能配置
    val MODULE_SETTING_NUMBER_PW= true //设置--数字密码
    val MODULE_SETTING_LOCATION = true //设置--持续定位

    val GEO_COLLECTION_STATIS = true //工作记录统计分析



    //数据资源顶部tab
    val TAB_DATA_RESOURCE = true //数据资源tab
    val TAB_EXTERNAL_IMPORT = false //外部导入tab(geojson)
    val TAB_IMPORT_SHP = false //SHP 勘界作业使用独立入口

    val CLEAR_ALL_KV = false //清除所有kv





}
