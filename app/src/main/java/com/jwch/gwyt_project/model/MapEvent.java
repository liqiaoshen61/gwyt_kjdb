package com.jwch.gwyt_project.model;

/**
 * 操作地图的行为
 */
public class MapEvent {

    public static final int SET_VECTOR_MAP = 0;//设置矢量图
    public static final int SET_IMAGE_MAP = 1;//设置影像图


    public static final int ADD_BASEMAP =30;//添加地图，类似专题图
    public static final int REMOVED_ADDED_BASEMAP = 31;//删除已经叠加的底图
    public static final int REPLACE_BASE_MAP = 32;//替换地图


    public static final int MOVE_TO_CENTER = 2;//定位到某个点
    public static final int DRAW_AREA_POINT = 3;//行政区划定位点
    public static final int DRAW_SEARCH_RESULT = 4;//绘制搜索结果
    public static final int CLICK_SEARCH_RESULT_ITEM = 5;//点击了搜索结果列表item
    public static final int DRAW_COllECTION_CLICK_RESULT = 6;//绘制点击了收藏项
    public static final int REMOVE_ONE_COLLECT_GRAPHICS = 7;//删除一个收藏的poi点
    public static final int CONTROL_LAYER = 8;//控制专题图展示
    public static final int CLEAR_OPERATION_LAYERS = 9;//清空所有专题图

    public static final int CONTROL_COMPARE_LAYER = 13;//控制比对  专题图展示


    public static final int CLEAR_COMPARE_OPERATION_LAYERS = 14;//清空比对地图 专题图
    public static final int CLICK_QUERY_RESULT_FEATURE_LIST = 15;//点击空间查询的 feature 列表
    public static final int CLEAR_GRAPHIC_LAYER = 16;//清空绘制图层
    public static final int HIDE_CALLOUT = 17;//关闭弹窗

    public static final int CLICK_MARKER_RESULT_LIST_ITEM = 18;//标绘结果 列表item 点击


    public static final int DRAW_MARKER_LNGLAT_INFO = 19;//绘制坐标信息

    public static final int START_EDIT_GEOMETRY = 20;//准备开始编辑图形

    public static final int ADD_POINT  = 23;//添加点位置 准备绘图
    public static final int POI_MARKER = 24;//根据填写的坐标 准备绘图
    public static final int DRWA_STYLE = 25;//设置标绘样式

    public static final int  ADD_POINT_MEASURE_DISTANCE = 26;//添加点位置 准备测距离
    public static final int ADD_POINT_MEASURE_AREA = 27;//添加点位置 准备测面积

    public static final int  NET_CONNECT = 33;//网络连接上
    public static final int NET_DISCONNECT = 34;//网络断开

    public static final int COORDINATE_POSITION = 35;//坐标定位

    public static final int COORDINATE_MARKER_POINT = 36;//坐标标绘 画点
    public static final int COORDINATE_MARKER_PLOYLINE = 37;//坐标标绘 画线
    public static final int COORDINATE_MARKER_POLYGON = 38;//坐标标绘 画面
    public static final int COORDINATE_MARKER_CLOSE = 39;//坐标标绘 关闭

    public static final int ADD_POINT_COORDINATE  = 40;//添加点位置 准备坐标绘图
    public static final int ADD_POINT_LIST_COORDINATE  = 41;//添加点位置 准备坐标绘图

    public static final int DRAW_ANALYSIS_RESULT = 42;//画出叠加分析结果
    public static final int CLEAR_ANALYSIS_MARKER = 43;//清除叠加分析的标绘

    public static final int QUERY_AREA_COUNTY = 44;//查询行政区featureLayer
    public static final int QUERY_AREA_VILLAGE = 45;//查询行政区featureLayer
    public static final int CLOSE_AREA_POINT = 46;//关闭村定位点

    public static final int CONTROL_ANALYSIS_LAYER = 47;//控制 叠加分析的图层

    public static final int QUERY_AREA_AREA = 48;//查询行政区featureLayer

    public static final int SCREEN_SHOT_WHTI_LOACTION = 49;//定位截图（现场拍照之后）

    public static final int GO_STRAT = 50; //（获取到行政区划后）去分析

    public static final int INIT_ARAE_FEAG = 51; //初始化 行政区划

    public static final int STATUS_DATA_BY_AREA = 52; //分析数据

    public static final int QUERY_AREA_CITY = 53;//查询行政区featureLayer


    public static final int UPDATE_STATS_YEAR = 54;//统计年份

    public static final int DRAW_BUFFER_ANALYSIS_RESULT = 55;

    public static final int DO_BUFFER_ANALYSIS_CUSTOM_POINT = 56; //绘制自由点后 执行缓冲区分析

    public static final int QUERY_AREA_BY_POINT = 57; //通过点位查询 所在市 所在县
    public static final int QUERY_AREA_CALLBACK = 58; //通过点位查询 所在市 返回结果

    public static final int QUERY_AREA_PROVINCE = 59;//查询行政区featureLayer 省级

    public static final int SEND_BUFFER_QUERY_TASK = 60;//查询行政区featureLayer 省级

    public static final int QUERY_LOCATION_AREAINFO = 61;//查询屏幕中心点位置信息（截图用）

    public static final int QUERY_LOCATION_AREAINFO_BACK = 62;//

    public static final int SET_IMAGE_MAP_2018 = 63;//设置影像底图2018
    public static final int SET_IMAGE_MAP_2022 = 64;//设置影像底图2022
    public static final int SET_IMAGE_MAP_2024 = 65;//设置影像底图2024
    public static final int SET_IMAGE_MAP_2025 = 66;//设置影像底图2025

    public static final int DRAW_BUFFER_PAGE_RESULT = 67;//绘制当前页的缓冲区查询结果（分页绘制）



    private int actionType;
    private Object data;
    private Object data2;
    private Object data3;


    public MapEvent(int actionType) {
        this.actionType = actionType;
    }

    public MapEvent(int actionType, Object data) {
        this.actionType = actionType;
        this.data = data;
    }
    public MapEvent(int actionType, Object data, Object data2) {
        this.actionType = actionType;
        this.data = data;
        this.data2 = data2;
    }

    public MapEvent(int actionType, Object data, Object data2,  Object data3) {
        this.actionType = actionType;
        this.data = data;
        this.data2 = data2;
        this.data3 = data3;
    }

    public Object getData() {
        return data;
    }

    public Object getData2() {
        return data2;
    }

    public Object getData3() {
        return data3;
    }


    public void setData(Object data) {
        this.data = data;
    }

    public void setData2(Object data2) {
        this.data2 = data2;
    }

    public void setData3(Object data3) {
        this.data3 = data3;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
}
