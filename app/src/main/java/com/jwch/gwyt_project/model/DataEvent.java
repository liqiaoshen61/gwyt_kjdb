package com.jwch.gwyt_project.model;

public class DataEvent {

    public static final int HIDE_ALL = 0;//
    public static final int SHOW_ALL = 1;//
    public static final int SHOW_POI_DETAIL = 2;//poi详情
    public static final int HIDE_POI_DETAIL = 3;//poi详情

    public static final int UPDATE_COLLECTION_DATA = 4;//更新收藏信息
    public static final int UPDATE_MARKER_DATA = 5;//更新标绘信息
    public static final int SHOW_LAYER_LEGEND = 6;//展示图例
    public static final int HIDE_LAYER_LEGEND = 7;//隐藏图例

    public static final int HIDE_LAYER_COMPARE = 8;//关闭 比对 地图图层页面
    public static final int SHOW_LAYER_COMPARE = 9;//展示 比对 地图图层页面

    public static final int CLEAR_COMPARE_DATA = 10;//清空对比图层数据

    public static final int SHOW_QUERY_RESULT_LIST = 11;// 展示空间查询-手势查询结果(各个专题图结果列表，第一个页面)
    public static final int HIDE_QUERY_RESULT_LIST = 12;//关闭空间查询结果页面
    public static final int SHOW_QUERY_RESULT_DETAIL = 13;// 展示空间查询结果图表页面
    public static final int HIDE_QUERY_RESULT_DETAIL = 14;// 关闭空间查询结果图表页面
    public static final int SHOW_QUERY_RESULT_DETAIL_LIST = 15;//展示空间查询结果详情列表点页面
    public static final int HIDE_QUERY_RESULT_DETAIL_LIST = 16;//关闭空间查询结果情列表点页面
    public static final int HIDE_QUERY_RESULT = 17;//关闭所有空间查询结果

    public static final int DRAW_SELECT_COLLECTION_MARKER = 18;//绘制选中收藏的标绘
    public static final int SHOW_MEASURE_RESULT = 19;//展示测量结果

    public static final int SHOW_GEOMETRY_DETAIL = 20;//手势查询 的图形详情
    public static final int SHOW_LOADING = 21;//显示进度条
    public static final int HIDE_LOADING = 22;//隐藏进度条

    public static final int MARKER_STATISTICS = 23;//标绘面状图形 统计
    public static final int SAVE_MARKER = 24;//保存图形

    public static final int CLEAR_MEASURE_RESULT = 25;//清空测量结果
    public static final int SHOW_TIP = 26;//展示toast

    public static final int YXT_ALL_UNSELECT = 27;//历史影像 全部反选

    public static final int GO_PHOTO = 28; //进入拍照Activity
    public static final int GO_MEDIA = 29; //进入多媒体附件Activity

    public static final int UPDATE_MARKER_DATA_AND_DRAW = 30;//更新标绘信息 并绘制标绘
    public static final int CLOSE_AREA_FRAG = 31; //关闭行政区划
    public static final int CLOSE_SEARCH_FRAG = 32; //关闭搜索
    public static final int CLOSE_SWITCH_MAPS = 33; //关闭地图切换
    public static final int CLOSE_COLLECTION_FRAG = 34; //关闭我的收藏
    public static final int CLEAR_ALL_ZZT_LAYER = 35; //清空所有专题图图层
    public static final int CLEAR_ALL_COMPARE_ZZT_LAYER = 36; //清空所有对比专题图图层
    public static final int CLEAR_ALL_GEO = 37; //清空所有的收藏标绘

    public static final int SHOW_OVERLY_ANALYSIS = 38; //展示叠加分析结果
    public static final int HIDE_OVERLY_ANALYSIS = 39; //隐藏叠加分析结果

    public static final int DRAW_COLLECTION_MARKER_ANALYSIS = 40;//叠加分析 绘制收藏的标绘

    public static final int FINISH_ANALYSIS = 41;//完成叠加分析
    public static final int SEND_ATTR = 42;//发送叠加分析交集所在的图斑的属性
    public static final int SELECT_ITEM = 43;//发送叠加结果item选中
    public static final int CLOSE_AREA = 44; //关闭行政区划绘制的图

    public static final int CLOSE_SWITCH_MAPS_ANALYSIS = 45; //关闭地图切换

    public static final int SHOW_QUERY_RESULT_REPORT = 46;// 展示空间查询结果 一键分析报告页面
    public static final int HIDE_QUERY_RESULT_REPORT = 47;// 关闭空间查询结果 一键分析报告页面
    public static final int COUNTY_NAME_LIST = 48;// 叠加分析 所在行政区的镇名列表

    public static final int DRAW_SELECT_GEOJSON = 49;//绘制geojson

    public static final int DRAW_SELECT_GEOJSON_LIST = 65;//绘制geojson列表（多选）

    public static final int CLOSE_GEOJSON_FRAG = 50; //关闭导入geojson

    public static final int DRAW_SELECT_SHP = 94;//绘制shp
    public static final int DRAW_SELECT_SHP_LIST = 95;//绘制shp列表（多选）
    public static final int CLOSE_SHP_FRAG = 96; //关闭导入shp
    public static final int OPEN_SHP_EDITOR = 97; //在地图上编辑导入的 SHP

    public static final int ZTT_DATA_TO_AREA_STAT = 51;//把专题数据发送到行政区划 准备做统计

    public static final int GPS_CONTINUOUS_LOCATION = 52;//gps持续定位

    public static final int UPDATE_PHOTO_LIST = 53;//定位截图完成后 更新相册列表

    public static final int REFRESH_MARKER = 54;//（调整单位后）重新绘制marker

    public static final int EDIT_MARKER_INFO = 55;//编辑标绘信息

    public static final int SHOW_GEOMETRY_DETAIL2 = 56;//展示 事件处理状态详情
    public static final int HIDE_GEOMETRY_DETAIL2 = 57;//隐藏 事件处理状态详情

    public static final int UPDATE_REVIEW_RECORD_DATA = 58;//更新复核记录

    public static final int SELECT_BASE_IMAGE_LAYER_NMAE = 59;//更新地图上左下角的底图文字 选中
    public static final int UNSELECT_BASE_IMAGE_LAYER_NMAE = 60;//更新地图上左下角的底图文字 反选

    public static final int GO_SELECT_PHOTO = 61; //进入选择照片Activity
    public static final int GO_BACK_REVIEW_RECORD_DIALOG = 62; //返回到新增复核记录dialog

    public static final int DRAW_SELECT_COLLECTION_MARKER_SINGLE = 63;//绘制单个标绘（工作记录）

    public static final int ZTT_DATA_TO_BUFFER_ANALYSIS= 64;//把专题数据发送到首页 准备进行缓冲区分析

    public static final int SHOW_QUERY_BUFFER_RESULT_LIST = 69; //缓冲区查询结果

    public static final int HIDE_QUERY_BUFFER_RESULT_LIST = 70;//关闭缓冲区查询结果

    public static final int UNDRAW_SELECT_COLLECTION_MARKER_SINGLE = 71; //反选选中的标绘 （取消绘制）

    public static final int START_BUFFER_ANALYSIS = 72; //

    public static final int INIT_BUFFER_ANALYSIS = 73; //

    public static final int CLOSE_GEO_COLLECTION_FRAG = 74; //关闭我的标绘收藏

    public static final int CLEAR_DRAW_COLLECTION_THEME = 75; //清除收藏夹中

    public static final int BACK_TO_SWITCH_MAP = 76; //从全局搜索结果 返回到数据资源列表

    public static final int CLOSE_SWITCH_MAPS_2 = 77; //从全局搜索结果 直接关闭该模块

    public static final int REVERSE_TABLE = 78;//反向给表格数据

    public static final int GO_COLLECTION_STATIS_FRAG = 79;//我的收藏统计
    public static final int BACK_COLLECTION_FRAG = 80; //返回我的收藏

    public static final int RESERT_BUFFER_ANALYSIS = 81; //


     public static final int OPEN_LICENSE_DIALOG = 82; // 打开授权输入弹窗
     public static final int UPDATE_LICENSE = 83; // 更新授权时间

    public static final int INIT_BUFFER_ANALYSIS_BY_KEYWORDS = 86;

    public static final int LAYER_FILTER_REFRESH = 87; // 图层筛选后 刷新

    public static final int CANCEL_COLLECT_PATCH = 88;

    public static final int SELECT_NAVIGATION_LIST_CHANGE = 89; //

    public static final int CLOSE_COLLECTION_FRAG_NAVIGATION = 90; //关闭收藏夹的导航模式

    public static final int WATERMARK_STATE_CHANGED = 91; //水印开关状态改变
    public static final int WATERMARK_TEXT_CHANGED = 92; //水印自定义文字改变（已废弃）
    public static final int WATERMARK_CONFIG_CHANGED = 93; //水印配置改变（文字、大小、透明度）


    private int actionType;
    private Object data;
    private Object data2;

    public DataEvent(int actionType) {
        this.actionType = actionType;
    }

    public DataEvent(int actionType, Object data) {
        this.actionType = actionType;
        this.data = data;
    }

    public DataEvent(int actionType, Object data,Object data2) {
        this.actionType = actionType;
        this.data = data;
        this.data2 = data2;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData2() {
        return data2;
    }

    public void setData2(Object data2) {
        this.data2 = data2;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }


}
