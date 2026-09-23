package com.jwch.gwyt_project.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.esri.arcgisruntime.arcgisservices.LabelDefinition;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.layers.WebTiledLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.symbology.Symbol;
import com.jameni.allutillib.common.CommonUtil;
import com.jameni.allutillib.common.PrintUtil;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * ********************************************************** 内容摘要 ：工具类
 * <p>
 * <p>
 * 作者 ： 创建时间 ：2013-4-25 上午10:25:40 当前版本号：v1.0 历史记录 : 日期 : 2013-4-25 上午10:25:40
 * 修改人： 描述 :
 * **********************************************************
 */
public class ArcgisUtils {

    private static ArcgisUtils arcgisUtils = null;
    private final static String PING_TAI = "poi采集";//配置目录-多源采集

    public static String Arcgis_root_map_setting_path = "";//配置文件路径
    public static String Arcgis_root_map_ditu_file_dir_path = "";//底图存放路径目录
    public static String Arcgis_root_map_layer_path = "";//图层文件路径
    public static String Arcgis_root_map_html_path = "";//html文件路径
    public static String Arcgis_root_map_quanjing_path = "";//全景文件路径
    public static String Arcgis_root_map_photo_path = "";//图片存放路径
    public static String Arcgis_root_map_cache_path = "";//缓存路径
    public static String Arcgis_root_map_doc_path = "";//文档路径
    public static String Arcgis_root_map_off_line_path = "";//离线路径
    public static double best_scale = 96361.81837417118;//最佳比例


    /**
     * isSDCard 判断底图是否在外置存储卡
     */


    public ArcgisUtils() {
        super();


    }


    public static ArcgisUtils getInstance() {
        if (arcgisUtils == null) {
            arcgisUtils = new ArcgisUtils();
        }
        return arcgisUtils;
    }


    public static void set_bottom_tiandilayer(MapView mapview, TianDiTuMethodsClass.LayerType layerType) {
//        TianDiTuMethodsClass.CreateTianDiTuTiledLayer(TianDiTuMethodsClass.LayerType.TIANDITU_IMAGE_ANNOTATION_CHINESE_2000);
        if (mapview.getMap() == null) {
            Basemap basemap = new Basemap();
            ArcGISMap mArcGISMap = new ArcGISMap(basemap);
            mapview.setMap(mArcGISMap);
        } else {
            mapview.getMap().getBasemap().getBaseLayers().clear();
        }
        WebTiledLayer webTiledLayer = TianDiTuMethodsClass.CreateTianDiTuTiledLayer(layerType);
        webTiledLayer.loadAsync();
        mapview.getMap().getBasemap().getBaseLayers().add(webTiledLayer);
    }


    public static void load_server_ArcGISMapImageLayer(MapView mapview, ArcGISMapImageLayer layer, String layer_id) {
        layer.setRefreshInterval(3000);
//        layer.setId(layer_id);
        layer.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
                    PrintUtil.printMsg("================要素图层加载成功========================");
                }
                if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    PrintUtil.printMsg("================要素图层加载失败========================");
                }
            }
        });
        mapview.getMap().getOperationalLayers().add(layer);
    }


    public static void load_server_ArcGISMapImageLayer(MapView mapview, String layer_id, String path) {
        ArcGISMapImageLayer arcGISMapImageLayer = new ArcGISMapImageLayer(path);
        arcGISMapImageLayer.setRefreshInterval(3000);
        arcGISMapImageLayer.setId(layer_id);
        arcGISMapImageLayer.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
                    PrintUtil.printMsg("================要素图层加载成功========================");
//                    ServiceFeatureTable.FeatureRequestMode mainFeatureRequestMode =
//                            mainServiceFeatureTable.getFeatureRequestMode();
//                    String mainFeatureRequestModeName = mainFeatureRequestMode.name();
                }
                if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    PrintUtil.printMsg("================要素图层加载失败========================");
                }
            }
        });
        mapview.getMap().getOperationalLayers().add(arcGISMapImageLayer);
    }

    /**
     * @param mapview
     * @param id
     */
    public static Layer getLayer(MapView mapview, String id) {
        if (mapview.getMap() == null) {
            Basemap basemap = new Basemap();
            ArcGISMap mArcGISMap = new ArcGISMap(basemap);
            mapview.setMap(mArcGISMap);
        }
        Layer l = null;
        List<Layer> list = mapview.getMap().getOperationalLayers();
        for (Layer layer : list) {
            PrintUtil.printMsg("id=" + id);
            PrintUtil.printMsg("layer.getId()=" + layer.getId());
            if (id.equals(layer.getId())) {
                l = layer;
                break;
            }
        }
        return l;
    }

    /**
     * 移除图层
     *
     * @param mapview
     * @param layer
     */
    public static void removeLayer(MapView mapview, Layer layer) {
        mapview.getMap().getOperationalLayers().remove(layer);
    }


    public static void load_server_layer(MapView mapview, String path, String geometry_type) {
//        path = "http://192.168.104.236:6080/arcgis/rest/services/bhxc/yy/MapServer/0";
        final ServiceFeatureTable mainServiceFeatureTable = new ServiceFeatureTable(path);
        mainServiceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        mainServiceFeatureTable.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
                    PrintUtil.printMsg("================要素图层加载成功========================");
//                    ServiceFeatureTable.FeatureRequestMode mainFeatureRequestMode =
//                            mainServiceFeatureTable.getFeatureRequestMode();
//                    String mainFeatureRequestModeName = mainFeatureRequestMode.name();
                }
                if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    PrintUtil.printMsg("================要素图层加载失败========================");
                }
            }
        });
        FeatureLayer featureLayer = new FeatureLayer(mainServiceFeatureTable);
        featureLayer.setLabelsEnabled(true);//开启标记
        if (geometry_type == null) {//默认

        } else {
            Symbol symbol = null;
            if (geometry_type.equals("点")) {
                symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#B03060"), 10);//点
            } else if (geometry_type.equals("线")) {
                if (path.contains("高铁线路.shp")) {
                    symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#DC143C"), 6);//线
                } else {
                    symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#FFFF00"), 2);//线
                }


            } else if (geometry_type.equals("面")) {
                symbol = new SimpleFillSymbol(SimpleFillSymbol.Style.HORIZONTAL, Color.parseColor("#B03060"), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#B03060"), 3));//面
            }
//       Symbol symbol=new SimpleFillSymbol(Color.BLUE);
            Renderer renderer = new SimpleRenderer(symbol);
            featureLayer.setRenderer(renderer);


            // 创建label字符串，labelExpression是绑定字段，
            //正常标记设置
            //   String strLabelDefinition = "{\"labelExpression\": \"[名称]\",\"labelPlacement\": \"esriServerPointLabelPlacementAboveCenter\",\"symbol\": {\"color\": [255,0,255,123],\"font\": {\"size\": 16,\"family\":\"DroidSansFallback.ttf\"},\"type\": \"esriTS\"}}";
            //设置掩膜晕圈效果
            String strLabelDefinition = "{\"labelPlacement\":\"esriServerPointLabelPlacementAboveRight\",\"where\":null,\"labelExpression\":\"[名称]\",\"useCodedValues\":true,\"symbol\":{\"type\":\"esriTS\",\"color\":[255,0,0,255],\"backgroundColor\":null,\"borderLineColor\":null,\"borderLineSize\":null,\"verticalAlignment\":\"baseline\",\"horizontalAlignment\":\"left\",\"rightToLeft\":false,\"angle\":0,\"xoffset\":0,\"yoffset\":0,\"kerning\":true,\"haloColor\":[255,255,255,253],\"haloSize\":1,\"font\":{\"family\":\"DroidSansFallback.ttf\",\"size\":14,\"style\":\"normal\",\"weight\":\"normal\",\"decoration\":\"none\"}},\"minScale\":0,\"maxScale\":0}";

// 构建LabelDefinition
            LabelDefinition labelDefinition = LabelDefinition.fromJson(strLabelDefinition);
            featureLayer.getLabelDefinitions().add(labelDefinition);

        }
        mapview.getMap().getOperationalLayers().add(featureLayer);
        mainServiceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        mainServiceFeatureTable.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
                    ServiceFeatureTable.FeatureRequestMode mainFeatureRequestMode =
                            mainServiceFeatureTable.getFeatureRequestMode();
                    String mainFeatureRequestModeName = mainFeatureRequestMode.name();
                }
            }
        });
    }


    /**
     * 加载shp图层
     *
     * @param mapview
     * @param path
     */
    public static void load_shp(final MapView mapview, final String path, final String geometry_type) {
        if (mapview.getMap() == null) {
            Basemap basemap = new Basemap();
            ArcGISMap mArcGISMap = new ArcGISMap(basemap);
            mapview.setMap(mArcGISMap);
        }
        try {
            PrintUtil.printMsg("shp路径3=" + path);
            final ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(path);
            shapefileFeatureTable.loadAsync();
            shapefileFeatureTable.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    if (shapefileFeatureTable.getLoadStatus() == LoadStatus.LOADED) {//加载完成
                        PrintUtil.printMsg("shp加载完成");
                        for (Field field : shapefileFeatureTable.getFields()) {
                            PrintUtil.printMsg("字段名：" + field.getName());
                        }
                        // create a feature layer to display the shapefile
                        FeatureLayer featureLayer = new FeatureLayer(shapefileFeatureTable);
                        featureLayer.setId(path);
                        //     featureLayer.setLabelsEnabled(true);//开启标记
                        Symbol symbol = null;
                        if (geometry_type.equals("点")) {
                            PrintUtil.printMsg("=======点============");
                            symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#B03060"), 10);//点
                        } else if (geometry_type.equals("线")) {
                            if (path.contains("高铁线路.shp")) {
                                symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#DC143C"), 6);//线
                            } else {
                                symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#FFFF00"), 2);//线
                            }
                        } else if (geometry_type.equals("面")) {
                            PrintUtil.printMsg("=======面============");
                            symbol = new SimpleFillSymbol(SimpleFillSymbol.Style.HORIZONTAL, Color.parseColor("#B03060"), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#B03060"), 3));//面
                        }
                        Renderer renderer = new SimpleRenderer(symbol);
                        featureLayer.setRenderer(renderer);
                        // 创建label字符串，labelExpression是绑定字段，
                        //正常标记设置
//           String strLabelDefinition = "{\"labelExpression\": \"[名称]\",\"labelPlacement\": \"esriServerPointLabelPlacementAboveCenter\",\"symbol\": {\"color\": [255,0,255,123],\"font\": {\"size\": 12,\"family\":\"DroidSansFallback.ttf\"},\"type\": \"esriTS\"}}";
                        //设置掩膜晕圈效果
                        String strLabelDefinition = "{\"labelPlacement\":\"esriServerPointLabelPlacementAboveRight\",\"where\":null,\"labelExpression\":\"[名称]\",\"useCodedValues\":true,\"symbol\":{\"type\":\"esriTS\",\"color\":[255,0,0,255],\"backgroundColor\":null,\"borderLineColor\":null,\"borderLineSize\":null,\"verticalAlignment\":\"baseline\",\"horizontalAlignment\":\"left\",\"rightToLeft\":false,\"angle\":0,\"xoffset\":0,\"yoffset\":0,\"kerning\":true,\"haloColor\":[255,255,255,253],\"haloSize\":1,\"font\":{\"family\":\"DroidSansFallback.ttf\",\"size\":14,\"style\":\"normal\",\"weight\":\"normal\",\"decoration\":\"none\"}},\"minScale\":0,\"maxScale\":0}";

// 构建LabelDefinition
                        LabelDefinition labelDefinition = LabelDefinition.fromJson(strLabelDefinition);
//        featureLayer.getLabelDefinitions().add(labelDefinition);
                        mapview.getMap().getOperationalLayers().add(featureLayer);
                    } else {
                        PrintUtil.printMsg("Shapefile feature table failed to load: " + shapefileFeatureTable.getLoadError().toString());
                    }
                }
            });

//         return featureLayer;
        } catch (Exception e) {
            e.printStackTrace();
        }


//        return null;
    }

    /**
     * 加载本地layer底层图层
     *
     * @param mapview
     * @param layer_dir
     */
    public static void load_layer(MapView mapview, String layer_dir) {
        ArcGISTiledLayer layer = new ArcGISTiledLayer(Arcgis_root_map_ditu_file_dir_path + layer_dir + File.separator); // ok
        mapview.getMap().getBasemap().getBaseLayers().add(layer);
//        Basemap basemap=new Basemap(layer);
//        ArcGISMap arcGISMap=new ArcGISMap(basemap);
//        mapview.setMap(arcGISMap);
    }


    /**
     * 读取Geodatabase中离线地图信息
     *
     * @param geodatabsePath 离线Geodatabase文件路径
     */
    public static void addFeatureLayer(MapView mapview, String geodatabsePath) {
        Geodatabase localGdb = new Geodatabase(geodatabsePath);
        // 添加FeatureLayer到MapView中
        if (localGdb != null) {
            for (GeodatabaseFeatureTable gdbFeatureTable : localGdb.getGeodatabaseFeatureTables()) {
                if (gdbFeatureTable.hasGeometry()) {
                    PrintUtil.printMsg("=======添加=======");
                    FeatureLayer layer = new FeatureLayer(gdbFeatureTable);
//                    layer.setEnableLabels(true);//标注开启显示
                    mapview.getMap().getOperationalLayers().add(layer);
//                    return gdbFeatureTable;
                }
            }
        }
    }

    /**
     * 获取shp文件的表名
     *
     * @param path
     * @return
     */
    public static String getShpName(String path) {
        ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(path);
        shapefileFeatureTable.loadAsync();
        return shapefileFeatureTable.getTableName();
    }

    /**
     * shp的存储类型,点线面
     *
     * @param path
     * @return
     */
    public static String getShpGeometryType(String path) {
        ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(path);
        shapefileFeatureTable.loadAsync();//异步加载
        GeometryType geometryType = shapefileFeatureTable.getGeometryType();
        if (geometryType == GeometryType.POINT) {//点集合
            return "点";
        } else if (geometryType == GeometryType.POLYLINE) {
            return "线";
        } else if (geometryType == GeometryType.POLYGON) {
            return "面";
        }

        return null;
    }

    /**
     * 获取shp字段列表
     *
     * @param path
     * @return
     */
    public static List<Field> getShpFieldList(String path) {
        ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(path);
        shapefileFeatureTable.loadAsync();//异步加载
        return shapefileFeatureTable.getFields();
    }

    public static String getLayerName(String geodatabsePath) {

        Geodatabase localGdb = new Geodatabase(geodatabsePath);


//         layerList = new ArrayList<>();
        // 添加FeatureLayer到MapView中
        if (localGdb != null) {
            for (GeodatabaseFeatureTable gdbFeatureTable : localGdb.getGeodatabaseFeatureTables()) {
                if (gdbFeatureTable.hasGeometry()) {
                    PrintUtil.printMsg("=======添加=======");
                    FeatureLayer layer = new FeatureLayer(gdbFeatureTable);
                    return layer.getName();
                }
            }
        }
        return "";
    }

    /**
     * 移除属性层
     *
     * @param mapview
     */
    public static void removeFeatureLayer(MapView mapview, String layer_name) {

//        LayerList layers = mapview.getMap().getOperationalLayers();
//        for (int i = layers.size() - 1; i > -1; i--) {
//            Layer layer = layers.get(i);
//            if (layer instanceof FeatureLayer) {
//                if (layer_name.equals(layer.getName())) {
//                    layers.remove()
//                    mapview.removeLayer(layer);
//                    break;
//                }
//            }
//        }
    }

    /**
     * 获取图层
     *
     * @param mapview
     * @param layer_name
     * @return
     */
    public static FeatureLayer getFeatureLayer(MapView mapview, String layer_name) {
        LayerList layers = mapview.getMap().getOperationalLayers();
        for (int i = layers.size() - 1; i > -1; i--) {
            Layer layer = layers.get(i);
            if (layer instanceof FeatureLayer) {
                if (layer_name.equals(layer.getName())) {
                    return (FeatureLayer) layer;
                }
            }
        }
        return null;
    }

    public static void removeAllOperationalLayers(MapView mapview) {
        mapview.getMap().getOperationalLayers().clear();
    }


    public static void cancel_selected_feature_all(MapView mapview) {
        LayerList layers = mapview.getMap().getOperationalLayers();
        for (int i = layers.size() - 1; i > -1; i--) {
            Layer layer = layers.get(i);
            if (layer instanceof FeatureLayer) {
                final FeatureLayer featureLayer = (FeatureLayer) layer;
                featureLayer.clearSelection();
            }
        }
    }

    public static void load_geodatbase(final MapView mapview, String geodatabsePath) {
        final Geodatabase mainGeodatabase = new Geodatabase(geodatabsePath);
        mainGeodatabase.loadAsync();
        mainGeodatabase.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                List<GeodatabaseFeatureTable> results = mainGeodatabase.getGeodatabaseFeatureTables();
                PrintUtil.printMsg("results.size()=" + results.size());
                for (GeodatabaseFeatureTable value : results
                ) {
                    PrintUtil.printMsg("=========添加=======");
                    FeatureLayer featureLayer = new FeatureLayer(value);
                    mapview.getMap().getOperationalLayers().add(featureLayer);
                }
            }
        });

    }


//    public void load_shp(MapView mapview, String shp_file, String color) {
//        PrintUtil.printMsg("shp路径=" + Arcgis_root_map_layer_path + shp_file);
//        ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(Arcgis_root_map_layer_path + shp_file);
//        FeatureLayer featureLayer = new FeatureLayer(shapefileFeatureTable);
//        Symbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor(color), 14);
////            Symbol symbol=new SimpleFillSymbol(Color.BLUE);
//        Renderer renderer = new SimpleRenderer(symbol);
//        featureLayer.setRenderer(renderer);
//        mapview.getMap().getOperationalLayers().add(featureLayer);
//
//    }


    /**
     * 以一点为中心放大或缩小
     *
     * @param point 中心点
     * @param scale 比例尺
     */
    public void center_ZoomToScale(MapView mapView, Point point, double scale) {
        mapView.setViewpointCenterAsync(point, scale);//设置中心点
    }

    /**
     * 居中缩放向下移动六分一
     *
     * @param mapView
     * @param point
     * @param scale
     */
    public void center_ZoomToScale_6(MapView mapView, Point point, double scale) {
        android.graphics.Point screenPoint = mapView.locationToScreen(point);
        android.graphics.Point pMirrScreen = new android.graphics.Point(screenPoint.x,
                screenPoint.y - mapView.getHeight() / 5);
        mapView.setViewpointCenterAsync(mapView.screenToLocation(pMirrScreen), scale);
    }

    public void center_ZoomToScale_up_2(MapView mapView, Point point, double scale) {
        if (point != null) {
            if (point.getSpatialReference() == null)
                point = new Point(point.getX(), point.getY(), mapView.getSpatialReference());
            android.graphics.Point screenPoint = mapView.locationToScreen(point);
            android.graphics.Point pMirrScreen = new android.graphics.Point(screenPoint.x,
                    screenPoint.y + mapView.getHeight() / 4);
            mapView.setViewpointCenterAsync(mapView.screenToLocation(pMirrScreen), scale);
        }

    }

    /**
     * 显示小数点最后五位
     *
     * @return
     */
    public String get_MaximumFractionDigits_5(double x) {
        NumberFormat ddf1 = NumberFormat.getNumberInstance();
        ddf1.setMaximumFractionDigits(5);
        String s = ddf1.format(x);
        return s;
    }


    /**
     * 把一个View的对象转换成bitmap
     */
    private Bitmap getViewBitmap(MapView v) {

        v.clearFocus();
        v.setPressed(false);


        //能画缓存就返回false
        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = null;
        while (cacheBitmap == null) {
            cacheBitmap = v.getDrawingCache();
//            cacheBitmap = v.getDrawingMapCache(0, 0, v.getWidth(), v.getHeight());
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }


    /**
     * @param startPoint 地图开始点
     * @param endPoint   地图结束点
     * @return
     */
//    public double get_measure_distance_3(Point startPoint, Point endPoint) {
//        com.esri.arcgisruntime.geometry.SpatialReference mSR4326 = com.esri.arcgisruntime.geometry.SpatialReference.create(4326);
//        com.esri.arcgisruntime.geometry.SpatialReference mSR3857 = com.esri.arcgisruntime.geometry.SpatialReference.create(3857);
//
//        // 坐标转换
//        Point startPoint_tmp = (Point) GeometryEngine.project(startPoint, mSR4326,
//                mSR3857);
//        Point endPoint_tmp = (Point) GeometryEngine.project(endPoint, mSR4326, mSR3857);
//        double mLength = GeometryEngine.distance(startPoint_tmp, endPoint_tmp, mSR3857);
//
//        return mLength;
//    }

    /**
     * 测量面积
     *
     * @param polygon 面
     * @return
     */
//    public String get_measure_area_1(Polygon polygon) {
//        double area = Math.abs(polygon.calculateArea2D());
//        if (area > 1000000) {
//            return String.format("%.2f", area / 1000000) + "km²";
//        } else {
//            return String.format("%.2f", area) + "㎡";
//        }
//    }

    /**
     * @param latitude
     * @param longitude
     * @param map
     * @return 经纬度转换 arcgis地理点
     */
    public Point wgs84_to_map(double latitude, double longitude, MapView map) {
        SpatialReference curMapSR = map.getMap().getSpatialReference();
        Point wgs84point = new Point(longitude, latitude, SpatialReferences.getWgs84());
        Point mapPoint = (Point) GeometryEngine.project(wgs84point, curMapSR);

        return mapPoint;
    }


    /**
     * 测量面积
     *
     * @param polygon 面
     * @return
     */
//    public String get_measure_area_2(Polygon polygon) {
//        SpatialReference mSR4326 = SpatialReference.create(4326);
//        SpatialReference mSR3857 = SpatialReference.create(3857);
//        Polygon tempPolygon = (Polygon) GeometryEngine.project(polygon,
//                mSR4326, mSR3857);
//        double area = Math.abs(tempPolygon.calculateArea2D());
//        if (area > 1000000) {
//            return String.format("%.2f", area / 1000000) + "km²";
//        } else {
//            return String.format("%.2f", area) + "㎡";
//        }
//
//
//    }


    /**
     * 添加要素
     *
     * @return
     */
    public static void AddGeometry(final Context context, FeatureLayer featureLayer, Map<String, Object> attributes, Geometry geometry) {
        // 构建新增几何
//        Point mapPoint = new Point(140.0, 39.0, SpatialReference.create(4326));
        PrintUtil.printMsg("============shp添加要素==============");
        if (featureLayer.getFeatureTable().canAdd()) {
            PrintUtil.printMsg("============shp允许添加==============");
        }


        // 构建待增加的Feature对象，设置几何，设置属性
        Feature feature = featureLayer.getFeatureTable().createFeature();
        feature.setGeometry(geometry);
        feature.getAttributes().putAll(attributes);
        final ListenableFuture<Void> voidListenableFuture = featureLayer.getFeatureTable().addFeatureAsync(feature);
        voidListenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    voidListenableFuture.get();
                    CommonUtil.tip(context, "新增成功");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    CommonUtil.tip(context, "新增失败");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    CommonUtil.tip(context, "新增失败");
                }
            }
        });
    }


    /**
     * 删除要素
     */
    public static void DeleteGeometry(final Context context, FeatureLayer featureLayer, Feature feature) {
        final ListenableFuture<Void> voidListenableFuture = featureLayer.getFeatureTable().deleteFeatureAsync(feature);
        voidListenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    voidListenableFuture.get();
                    if (voidListenableFuture.isDone()) {
                        CommonUtil.tip(context, "删除成功");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    CommonUtil.tip(context, "删除失败");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    CommonUtil.tip(context, "删除失败");
                }

            }
        });
    }

    /**
     * 更新要素
     */
    public static void UpdateGeometry(final Context context, FeatureLayer featureLayer, Feature feature) {
        final ListenableFuture<Void> voidListenableFuture = featureLayer.getFeatureTable().updateFeatureAsync(feature);
        voidListenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    voidListenableFuture.get();
                    CommonUtil.tip(context, 1 + "条记录，更新成功！");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    CommonUtil.tip(context, "更新失败");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    CommonUtil.tip(context, "更新失败");
                }

            }
        });


    }


    /**
     * 添加要素
     *
     * @return
     */
    public static void AddServerGeometry(final Context context, FeatureLayer featureLayer, Map<String, Object> attributes, Geometry geometry) {// 创建要素的属性信息
        final ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) featureLayer.getFeatureTable();
        //创建要素的空间信息并关联属性信息
        Feature feature = serviceFeatureTable.createFeature(attributes, geometry);
        //添加要素
        final ListenableFuture<Void> result = serviceFeatureTable.addFeatureAsync(feature);
        result.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // 追踪FeatureTable，核实要素添加成功
                    result.get();
                    final ListenableFuture<List<FeatureEditResult>> applyEditsFuture = serviceFeatureTable.applyEditsAsync();
                    applyEditsFuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final List<FeatureEditResult> featureEditResults = applyEditsFuture.get();
                                CommonUtil.tip(context, "添加成功=" + featureEditResults.size());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                }
            }
        });

    }


    /**
     * 删除要素
     */
    public static void DeleteServerGeometry(final Context context, final FeatureLayer featureLayer, final Feature feature) {
        final ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) featureLayer.getFeatureTable();
        final ListenableFuture<Void> delete = serviceFeatureTable.deleteFeatureAsync(feature);
        delete.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    delete.get();
                    final ListenableFuture<List<FeatureEditResult>> applyEditsFuture = serviceFeatureTable.applyEditsAsync();
                    applyEditsFuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final List<FeatureEditResult> featureEditResults = applyEditsFuture.get();
                                CommonUtil.tip(context, "删除成功=" + featureEditResults.size());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    public static void UpdateServerGeometry(final Context context, FeatureLayer featureLayer, final Feature feature) {
        final ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) featureLayer.getFeatureTable();
        final ListenableFuture<Void> update = serviceFeatureTable.updateFeatureAsync(feature);
        update.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    update.get();
                    final ListenableFuture<List<FeatureEditResult>> applyEditsFuture = serviceFeatureTable.applyEditsAsync();
                    applyEditsFuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final List<FeatureEditResult> featureEditResults = applyEditsFuture.get();
                                CommonUtil.tip(context, featureEditResults.size() + "条记录，更新成功！");
                            } catch (InterruptedException e) {
                                CommonUtil.tip(context, "更新失败");
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                CommonUtil.tip(context, "更新失败");
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    CommonUtil.tip(context, "更新失败");
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    CommonUtil.tip(context, "更新失败");
                    e.printStackTrace();
                }
            }
        });


    }

    /**
     * 获取地图中心点
     *
     * @param mapview
     * @return
     */
    public static Point getCenterPoint(MapView mapview) {
        Point centPoint = mapview.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();

        return centPoint;
    }

    /**
     * 点击查询-转化矩形面
     *
     * @param mapview
     * @param screenPoint
     * @return
     */
    public static Geometry changeClickFindGeometry(MapView mapview, android.graphics.Point screenPoint) {
        int screen_point_x = screenPoint.x;
        int screen_point_y = screenPoint.y;
        PointCollection pointCollection = new PointCollection(SpatialReferences.getWgs84());
        int distance = 30;
        Point point_top_left = mapview.screenToLocation(new android.graphics.Point(screenPoint.x - distance, screenPoint.y - distance));
        Point point_top_right = mapview.screenToLocation(new android.graphics.Point(screenPoint.x - distance, screenPoint.y + distance));
        Point point_bottom_right = mapview.screenToLocation(new android.graphics.Point(screenPoint.x + distance, screenPoint.y + distance));
        Point point_bottom_left = mapview.screenToLocation(new android.graphics.Point(screenPoint.x + distance, screenPoint.y - distance));
        //注意：mapview.screenToLocation()转成空间坐标点，没有空间参考，添加到pointCollection报错
        pointCollection.add(new Point(point_top_left.getX(), point_top_left.getY()));
        pointCollection.add(new Point(point_top_right.getX(), point_top_right.getY()));
        pointCollection.add(new Point(point_bottom_right.getX(), point_bottom_right.getY()));
        pointCollection.add(new Point(point_bottom_right.getX(), point_bottom_right.getY()));
//        pointCollection.add(new Point(119.32452600115595,26.08838119831343));
//        pointCollection.add(new Point(119.32510684360228,26.08846417580577));
//        pointCollection.add(new Point(119.32512848990464,26.087926625964116));
        Polygon polygon = new Polygon(pointCollection);
        return polygon;
    }

    /**
     * 投影坐标转换成经纬度,经纬度为单位的都是地理坐标系，因为它归根结底是一个椭球体，
     * 只不过各个国家为了反映该国家所在区域地球的真实形状，而采用不同的数学模型对本不是椭球体的地球进行椭球体化
     *
     * @param mapview
     * @param point
     * @return
     */
    public static Point changeTouYingToJingWei(MapView mapview, Point point) {
        SpatialReference mSR4326 = SpatialReference.create(4326);
        SpatialReference mSR3857 = SpatialReference.create(3857);
        Point wgsPoint = (Point) GeometryEngine.project(point, mSR3857);
        return wgsPoint;
    }

    /**
     * 经纬度转换成投影坐标,投影坐标系，是对地理坐标系按照某种方式投影到平面上的，所以可以认为它是一个平面坐标系，单位自然是米或千米。
     *
     * @param mapview
     * @return
     */
    public static Geometry changeJingWeiToTouYing(MapView mapview, Geometry geometry) {
        SpatialReference mSR4326 = SpatialReference.create(4326);
        SpatialReference mSR3857 = SpatialReference.create(3857);
        Geometry mapPoint = GeometryEngine.project(geometry, mSR4326);
        PrintUtil.printMsg("changeJingWeiToTouYing=" + mapPoint.toJson());
        return mapPoint;
    }


    public static void draw_gaoliang(GraphicsOverlay graphicsOverlay, Geometry geometry) {
        graphicsOverlay.getGraphics().clear();
//        PointCollection pointCollection = new PointCollection(SpatialReferences.getWgs84());
        Symbol symbol = null;
        if (geometry.getGeometryType() == GeometryType.POINT) {
            symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#7FFFD4"), 20);//点
        } else if (geometry.getGeometryType() == GeometryType.POLYLINE) {
            symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#7FFFD4"), 3);//线
        } else if (geometry.getGeometryType() == GeometryType.POLYGON) {
            PrintUtil.printMsg("=======面============");
            symbol = new SimpleFillSymbol(SimpleFillSymbol.Style.HORIZONTAL, Color.parseColor("#7FFFD4"), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#7FFFD4"), 3));//面
        }
//        SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#FC8145"),simpleLineSymbol1);
//        Polygon polygon = new Polygon(pointCollection);
        Graphic graphic = new Graphic(geometry, symbol);
        graphicsOverlay.getGraphics().add(graphic);
    }


}
