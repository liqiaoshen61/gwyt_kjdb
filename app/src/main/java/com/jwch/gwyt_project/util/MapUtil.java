package com.jwch.gwyt_project.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.ImageTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.google.gson.Gson;
import com.jwch.gwyt_project.Info.MarkerInfo;
import com.jwch.gwyt_project.R;
import com.jwch.gwyt_project.core.Config;
import com.jwch.gwyt_project.i.DoneAction;
import com.jwch.gwyt_project.i.DoneListener;
import com.jwch.gwyt_project.model.BufferAnalysisModel;
import com.jwch.gwyt_project.model.StyleConfig;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtil {

    public static MapUtil mapUtil;

    public static MapUtil getMapUtil() {
        if (mapUtil == null) {
            mapUtil = new MapUtil();
        }
        return mapUtil;
    }
    CaculationUtil caculationUtil = new CaculationUtil();
    StyleConfig style = new StyleConfig();

    /**
     * 初始化地图容器
     *
     * @param mapView
     */
    public void initMap(MapView mapView) {
        if (mapView != null && mapView.getMap() == null) {
            Basemap basemap = new Basemap();
            ArcGISMap arcGISMap = new ArcGISMap(basemap);
            mapView.setMap(arcGISMap);
        }
    }

    /**
     * 加载本地 地图图层
     *
     * @param mapView 地图容器
     * @param mapPath sdcard中地图存放路径
     */
    public void addOfflineBaseLayer(MapView mapView, String mapPath) {
        if (mapView != null) {

            if (mapView.getMap() == null) {
                initMap(mapView);
            }
//            图像瓦片层
            ImageTiledLayer imageTiledLayer = new ArcGISTiledLayer(mapPath);
            //添加底图图层
            mapView.getMap().getBasemap().getBaseLayers().add(imageTiledLayer);
        }

    }

    /**
     * 加载本地
     *
     * @param mapView 地图容器
     * @param mapPath sdcard中地图存放路径
     */
    public void addOfflineOperationalLayer(MapView mapView, String mapPath) {
        if (mapView != null) {

            if (mapView.getMap() == null) {
                initMap(mapView);
            }

//          图像瓦片层
//            方式一
//            TileCache tileCache = new TileCache(mapPath);
//            ImageTiledLayer imageTiledLayer = new ArcGISTiledLayer(tileCache);

//            方式二
            ImageTiledLayer imageTiledLayer = new ArcGISTiledLayer(mapPath);
            //添加可操作的专题图 图层
            mapView.getMap().getOperationalLayers().add(imageTiledLayer);

        }

    }

    public void addBaseMapByServiceUrl(MapView mapView, String serviceUrl) {

        if (mapView == null || serviceUrl == null || serviceUrl.equals("")) return;

        serviceUrl = "http://{subDomain}.tianditu.com/DataServer?T=img_c&x={col}&y={row}&l={level}&tk=89f28ef743a9010b6e3b5c8c4705bc67";
        ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(serviceUrl);
        Basemap basemap = new Basemap(tiledLayer);
        ArcGISMap map = new ArcGISMap(basemap);

        Envelope mInitExtent = new Envelope(12152397.115334747, 2780298.008156988, 12204603.605653452, 2804643.2016657833, SpatialReference.create(102100));
        Viewpoint vp = new Viewpoint(mInitExtent);
        map.setInitialViewpoint(vp);

        mapView.setMap(map);

//        graphicsOverlay = new GraphicsOverlay();
//        pointGraphicsOverlay = new GraphicsOverlay();
//        mapView.getGraphicsOverlays().add(graphicsOverlay);
//        mapView.getGraphicsOverlays().add(pointGraphicsOverlay);


    }


    //清除底图图层
    public void clearBaseLayers(MapView mapView) {
        if (mapView != null && mapView.getMap() != null && mapView.getMap().getBasemap() != null && mapView.getMap().getBasemap().getBaseLayers() != null) {
            mapView.getMap().getBasemap().getBaseLayers().clear();
        }
    }

    //清除可操作图层
    public void clearOperationalLayers(MapView mapView) {
        if (mapView != null && mapView.getMap() != null && mapView.getMap().getOperationalLayers() != null) {
            mapView.getMap().getOperationalLayers().clear();
        }
    }

    /**
     * @param graphicsOverlay 覆盖物绘图对象
     * @param lat             地理信息经纬度
     * @param lng
     * @param isRedraw        true 重画   false 追加
     */
    public void drawSymbol(GraphicsOverlay graphicsOverlay, double lat, double lng, boolean isRedraw) {
        drawSymbol(graphicsOverlay, lat, lng, 15f, null, Color.RED, isRedraw);
    }

    /**
     * 已知地理信息坐标系后的绘制标识符
     *
     * @param graphicsOverlay     覆盖物绘图对象
     * @param lat                 地理信息经纬度
     * @param lng
     * @param symbolSizie         标识符大小
     * @param style               标识符样式 默认原型
     * @param symbolFillColor_rgb 整个符号的填充颜色
     * @param isRedraw            true 重画   false 追加
     */
    public void drawSymbol(GraphicsOverlay graphicsOverlay, double lat, double lng, float symbolSizie, SimpleMarkerSymbol.Style style, int symbolFillColor_rgb, boolean isRedraw) {
        //确定画的点的位置
        Point point = new Point(lng, lat, SpatialReferences.getWgs84());
        drawSymbol(graphicsOverlay, point, symbolSizie, style, symbolFillColor_rgb, isRedraw);
    }


    /**
     * 已知地理信息坐标系后的绘制标识符
     *
     * @param graphicsOverlay     覆盖物绘图对象
     * @param point               画的点的位置
     * @param symbolSizie         标识符大小
     * @param style               标识符样式 默认原型
     * @param symbolFillColor_rgb 整个符号的填充颜色
     * @param isRedraw            true 重画   false 追加
     */
    public void drawSymbol(GraphicsOverlay graphicsOverlay, Point point, float symbolSizie, SimpleMarkerSymbol.Style style, int symbolFillColor_rgb, boolean isRedraw) {

        if (graphicsOverlay == null || point == null)
            return;

        if (style == null) {
            //默认圆形样式
            style = SimpleMarkerSymbol.Style.CIRCLE;
        }
        //确定 图形的外观
//        简单标记符号对象
        SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(style, symbolFillColor_rgb, symbolSizie);

//        创建图解对象
        Graphic graphic = new Graphic(point, symbol);

//        添加图解对象
        //是否重画
        if (isRedraw) {
//            清空原来画的标识符
            graphicsOverlay.getGraphics().clear();
        }
        //添加图解对象
        graphicsOverlay.getGraphics().add(graphic);

    }

    /**
     * 已知地理信息坐标系后的绘制图片标识符
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param point           画的点的位置
     * @param isRedraw        true 重画   false 追加
     */
    /**
     * 只更新覆盖物中第一个图标的旋转角度，不重建符号。
     * <p>
     * 与 drawImageWithRotation 的区别：本方法复用已有的 PictureMarkerSymbol，
     * 不解码图片、不重新 loadAsync、不清空整个 overlay。
     * 用于「箭头随设备转动」这类需要频繁调用的场景（按传感器频率重建符号会卡死地图）。
     * <p>
     * 注意：PictureMarkerSymbol 的角度默认以屏幕上方为 0（angleAlignment = screen），
     * 地图被旋转过时调用方需要自行减去 mapView.getMapRotation()。
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param angle           目标角度（度，顺时针为正）
     */
    public void updateMarkerAngle(GraphicsOverlay graphicsOverlay, float angle) {
        if (graphicsOverlay == null) return;

        List<Graphic> graphics = graphicsOverlay.getGraphics();
        if (graphics == null || graphics.isEmpty()) return;

        Graphic graphic = graphics.get(0);
        if (graphic == null) return;

        Symbol symbol = graphic.getSymbol();
        if (symbol instanceof PictureMarkerSymbol) {
            ((PictureMarkerSymbol) symbol).setAngle(angle);
        }
    }

    public void drawImageWithRotation(Context context, GraphicsOverlay graphicsOverlay, Point point, int resId, Object data , float rotation, boolean isRedraw) {

        if (graphicsOverlay == null || point == null)
            return;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);


        //确定 图形的外观
//        简单标记符号对象
        PictureMarkerSymbol symbol = new PictureMarkerSymbol(bitmapDrawable);

        symbol.setAngle(rotation);
        symbol.setWidth(40.0f);
        symbol.setHeight(40.0f);
//        创建图解对象
        Graphic graphic = null;

        if (data != null) {
//            HashMap<String, Object> attrs = new HashMap<>();
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("data", new Gson().toJson(data));
            graphic = new Graphic(point, attrs, symbol);
        } else {

            graphic = new Graphic(point, symbol);
        }

        //涉及到加载图片到符号里，所以需要一个异步监听操作
        symbol.loadAsync();//异步加载

        symbol.addDoneLoadingListener(new DoneAction(new DoneListener() {
            @Override
            public void onDone(Object o, int tag) {
                //                添加图解对象
//                  是否重画
                if (isRedraw) {
//                  清空原来画的标识符
                    graphicsOverlay.getGraphics().clear();
                }
                //添加图解对象
                graphicsOverlay.getGraphics().add((Graphic) o);
            }
        }, graphic));


    }

    /**
     * 已知地理信息坐标系后的绘制图片标识符
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param point           画的点的位置
     * @param isRedraw        true 重画   false 追加
     */
    public void drawImage(Context context, GraphicsOverlay graphicsOverlay, Point point, int resId, boolean isRedraw) {

//        if (graphicsOverlay == null || point == null)
//            return;
//
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
//        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
//
//        //确定 图形的外观
////        简单标记符号对象
//        PictureMarkerSymbol symbol = new PictureMarkerSymbol(bitmapDrawable);
////        创建图解对象
//        final Graphic graphic = new Graphic(point, symbol);
//
//        //涉及到加载图片到符号里，所以需要一个异步监听操作
//        symbol.loadAsync();//异步加载
//        symbol.addDoneLoadingListener(new Runnable() {
//            @Override
//            public void run() {
////                添加图解对象
////                  是否重画
//                if (isRedraw) {
////                  清空原来画的标识符
//                    graphicsOverlay.getGraphics().clear();
//                }
//                //添加图解对象
//                graphicsOverlay.getGraphics().add(graphic);
//            }
//        });

        drawImage(context, graphicsOverlay, point, resId, null, isRedraw);
    }

    /**
     * 已知地理信息坐标系后的绘制图片标识符
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param point           画的点的位置
     * @param isRedraw        true 重画   false 追加
     */
    public void drawImage(Context context, GraphicsOverlay graphicsOverlay, Point point, int resId, Object data ,boolean isRedraw) {

        if (graphicsOverlay == null || point == null)
            return;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);


        //确定 图形的外观
//        简单标记符号对象
        PictureMarkerSymbol symbol = new PictureMarkerSymbol(bitmapDrawable);

//        创建图解对象
        Graphic graphic = null;

        if (data != null) {
//            HashMap<String, Object> attrs = new HashMap<>();
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("data", new Gson().toJson(data));
            graphic = new Graphic(point, attrs, symbol);
        } else {

            graphic = new Graphic(point, symbol);
        }

        //涉及到加载图片到符号里，所以需要一个异步监听操作
        symbol.loadAsync();//异步加载

        symbol.addDoneLoadingListener(new DoneAction(new DoneListener() {
            @Override
            public void onDone(Object o, int tag) {
                //                添加图解对象
//                  是否重画
                if (isRedraw) {
//                  清空原来画的标识符
                    graphicsOverlay.getGraphics().clear();
                }
                //添加图解对象
                graphicsOverlay.getGraphics().add((Graphic) o);
            }
        }, graphic));


    }

    /**
     * 已知地理信息坐标系后的绘制文本标识符
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param point           画的点的位置
     * @param isRedraw        true 重画   false 追加
     */
    public void drawText(GraphicsOverlay graphicsOverlay, String text, Point point, boolean isRedraw) {

        if (graphicsOverlay == null || point == null)
            return;

//        text = "绘制文字abc123\n啊啊啊123abc";
        TextSymbol.HorizontalAlignment textHorizonAlign = TextSymbol.HorizontalAlignment.LEFT;
        TextSymbol.VerticalAlignment textVerticalAlign = TextSymbol.VerticalAlignment.BOTTOM;

        TextSymbol textSymbol = new TextSymbol(12f, text, Color.BLACK, textHorizonAlign, textVerticalAlign);

//        textSymbol.setBackgroundColor(Color.WHITE);
        //设置光环，和光环的宽度
        textSymbol.setHaloColor(Color.WHITE);
        textSymbol.setHaloWidth(2f);

        //设置字体
//        textSymbol.setFontFamily("微软雅黑");
//        textSymbol.setFontStyle(TextSymbol.FontStyle.ITALIC);//设置斜体  这个好像数字和英文可以变成斜体，中文不行

        Graphic graphic = new Graphic(point, textSymbol);
//        是否重画
        if (isRedraw) {
//       清空原来画的标识符
            graphicsOverlay.getGraphics().clear();
        }
        graphicsOverlay.getGraphics().add(graphic);

    }

    /**
     * 已知地理信息坐标系后的绘制文本标识符
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param isRedraw        true 重画   false 追加
     */
    public void drawText(GraphicsOverlay graphicsOverlay, String text, Geometry geometry, boolean isRedraw) {

        if (graphicsOverlay == null || geometry == null)
            return;

//        text = "绘制文字abc123\n啊啊啊123abc";
        TextSymbol.HorizontalAlignment textHorizonAlign = TextSymbol.HorizontalAlignment.LEFT;
        TextSymbol.VerticalAlignment textVerticalAlign = TextSymbol.VerticalAlignment.BOTTOM;

        TextSymbol textSymbol = new TextSymbol(12f, text, Color.BLACK, textHorizonAlign, textVerticalAlign);

//        textSymbol.setBackgroundColor(Color.WHITE);
        //设置光环，和光环的宽度
        textSymbol.setHaloColor(Color.WHITE);
        textSymbol.setHaloWidth(2f);

        //设置字体
//        textSymbol.setFontFamily("微软雅黑");
//        textSymbol.setFontStyle(TextSymbol.FontStyle.ITALIC);//设置斜体  这个好像数字和英文可以变成斜体，中文不行

        Graphic graphic = new Graphic(geometry, textSymbol);
//        是否重画
        if (isRedraw) {
//       清空原来画的标识符
            graphicsOverlay.getGraphics().clear();
        }
        graphicsOverlay.getGraphics().add(graphic);

    }

    /**
     * 已知屏幕上点的 x y 坐标  绘制标识符
     *
     * @param mapView             地图容器
     * @param graphicsOverlay     覆盖物绘图对象
     * @param screenX             屏幕的x坐标
     * @param screenY             屏幕y坐标
     * @param symbolSizie         标识符符号大小
     * @param style               标识符样式
     * @param symbolFillColor_rgb 标识符填充的颜色
     * @param isRedraw            true 重画   false 追加
     */
    public void drawSymbol_clickScreen(MapView mapView, GraphicsOverlay graphicsOverlay, float screenX, float screenY, float symbolSizie, SimpleMarkerSymbol.Style style, int symbolFillColor_rgb, boolean isRedraw) {

        if (mapView == null)
            return;

        Point point = transScreenPoint2MapPoint(mapView, screenX, screenY);

        if (point != null) {
            drawSymbol(graphicsOverlay, point, symbolSizie, style, symbolFillColor_rgb, isRedraw);
        }

    }

    /**
     * 已知屏幕上点的 x y 坐标  绘制标识符
     *
     * @param mapView         地图容器
     * @param graphicsOverlay 覆盖物绘图对象
     * @param screenX         屏幕的x坐标
     * @param screenY         屏幕y坐标
     * @param isRedraw        true 重画   false 追加
     */

    public void drawSymbol_clickScreen(MapView mapView, GraphicsOverlay graphicsOverlay, float screenX, float screenY, boolean isRedraw) {
        drawSymbol_clickScreen(mapView, graphicsOverlay, screenX, screenY, 15f, null, Color.RED, isRedraw);
    }

    /**
     * 将地图旋转至目标角度
     *
     * @param mapView     地图容器
     * @param targetAngle 目标角度
     */
    public void rollTo(MapView mapView, double targetAngle) {
        if (mapView == null) return;
        mapView.setViewpointRotationAsync(targetAngle);
    }

    /**
     * 获取当前地图的旋转角度
     *
     * @param mapView 地图容器
     */
    public double getCurrentAngle(MapView mapView) {
        if (mapView == null) return 0;
        return mapView.getMapRotation();
    }


    /**
     * @param mapView   地图容器
     * @param scaleSize 缩放倍数  值大于1 的时候 外国人的缩小是 视野缩小，对于中国人是放大， 小于1 的时候是放大，对于中国人是缩小
     */
    public void setScale(MapView mapView, double scaleSize) {
        if (mapView == null) return;

        mapView.setViewpointScaleAsync(getCurrentScale(mapView) * scaleSize);
    }

    /**
     * 获取当前缩放比例
     *
     * @param mapView
     * @return
     */
    public double getCurrentScale(MapView mapView) {
        if (mapView == null) return 0;
        return mapView.getMapScale();
    }


    /**
     * 绘制折线
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param pointCollection 点的集合
     * @param lineWidth       折线的粗细
     * @param lineColor       折线颜色
     * @param lineStyle       折线样式
     * @param isRedraw        true 重画   false 追加
     */
    public void drawPolyline(GraphicsOverlay graphicsOverlay, PointCollection pointCollection, float lineWidth, int lineColor, SimpleLineSymbol.Style lineStyle, boolean isRedraw) {

        if (graphicsOverlay != null && pointCollection != null) {

            if (pointCollection.size() > 1) {
                if (lineStyle == null) {
                    lineStyle = SimpleLineSymbol.Style.SOLID;
                }

                //创建 线符号对象
                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(lineStyle, lineColor, lineWidth);

                //创建折线对象  把点的集合传进去
                Polyline polyline = new Polyline(pointCollection);
                //创建折线绘制对象
                Graphic graphic = new Graphic(polyline, lineSymbol);

                if (isRedraw) {
                    //先清空绘制对象
                    graphicsOverlay.getGraphics().clear();
                }
                //添加绘制对象
                graphicsOverlay.getGraphics().add(graphic);
            }


            for (Point point : pointCollection) {
                drawPoint(graphicsOverlay, point, lineWidth, Color.BLUE, false);
            }
        }
    }


    /**
     * 绘制折线
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param pointCollection 点的集合
     */
    public void drawPolyline(GraphicsOverlay graphicsOverlay, PointCollection pointCollection) {
        drawPolyline(graphicsOverlay, pointCollection, 5.0f, Color.RED, null, true);
    }

    public void drawPolyline(GraphicsOverlay graphicsOverlay, PointCollection pointCollection, int lineSize, int lineColor) {
        drawPolyline(graphicsOverlay, pointCollection, lineSize, lineColor, null, true);
    }

    public double lineLength;

    /**
     * 绘制线段
     *
     * @param mapview         地图容器
     * @param graphicsOverlay 覆盖物绘图对象
     * @param prePoint        开始点
     * @param nextPoint       结束点
     * @param lineWidth       折线的粗细
     * @param lineColor       折线颜色
     * @param lineStyle       折线样式
     * @param isRedraw        true 重画   false 追加
     */
    public void drawLine(MapView mapview, GraphicsOverlay graphicsOverlay, Point prePoint, Point nextPoint, float lineWidth, int lineColor, SimpleLineSymbol.Style lineStyle, boolean isRedraw) {

        if (graphicsOverlay != null && prePoint != null && nextPoint != null) {

            if (lineStyle == null) {
                lineStyle = SimpleLineSymbol.Style.SOLID;
            }

            //创建 线符号对象
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(lineStyle, lineColor, lineWidth);

            //创建折线对象构建  把点添加进去
            PolylineBuilder lineBuilder = new PolylineBuilder(mapview.getSpatialReference());
            lineBuilder.addPoint(prePoint);
            lineBuilder.addPoint(nextPoint);

            double lastLength = GeometryEngine.length(lineBuilder.toGeometry());
            lineLength = lineLength + lastLength;

//            距离转化
//            String strLength = formatDistance(Math.abs(formatDistanceUnit(lastLength, Variable.Measure.KM)));
//            String strLength = formatDistance(Math.abs(formatDistanceUnit(lineLength, Variable.Measure.KM)));
//            String strLength2 = formatDistance(Math.abs(formatDistanceUnit(lastLength, Variable.Measure.KM)));

//            String strUnit = "千米";
//            画距离文本
//            drawText(graphicsOverlay, strLength + strUnit, nextPoint, false);
//            PrintUtil.printMsg("last length : " + lastLength + "  line length2结果 " + strLength2 + "  line length " + lineLength + "  计算结果 " + strLength);


            //创建折线绘制对象
            Graphic graphic = new Graphic(lineBuilder.toGeometry(), lineSymbol);

            if (isRedraw) {
                //先清空绘制对象
                graphicsOverlay.getGraphics().clear();
            }
            //添加绘制对象
            graphicsOverlay.getGraphics().add(graphic);
        }
    }


    /**
     * @param mapView 地图容器
     * @param screenX 屏幕上的X坐标
     * @param screexY 屏幕上的Y坐标
     * @return 地图上的Point 对象
     */
    public Point transScreenPoint2MapPoint(MapView mapView, float screenX, float screexY) {

        if (mapView == null)
            return null;

        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(screenX), Math.round(screexY));
        Point mapPoint = mapView.screenToLocation(screenPoint);

        return mapPoint;
    }


    /**
     * 将地图移动到 屏幕点击的点位置
     *
     * @param mapView 地图容器
     * @param screenX 屏幕上的X坐标
     * @param screexY 屏幕上的Y坐标
     */
    public void moveToCenter(MapView mapView, float screenX, float screexY) {
        if (mapView == null)
            return;

        Point clickMapPoint = transScreenPoint2MapPoint(mapView, screenX, screexY);
        moveToCenter(mapView, clickMapPoint);

    }

    /**
     * 将地图移动到目标点位置
     *
     * @param mapView     地图容器
     * @param targetPoint 目标点位置
     */

    public void moveToCenter(MapView mapView, Point targetPoint) {
        if (mapView == null)
            return;

        mapView.setViewpointCenterAsync(targetPoint);

    }


    /**
     * 将地图移动到目标点位置
     */

    public void moveToCenter(MapView mapView, double lat, double lng, Double scale) {
        if (mapView == null)
            return;

        Point point = get_change_geometry_point(lat, lng);
        if (scale == null) {
            mapView.setViewpointCenterAsync(point);
        } else {
            mapView.setViewpointCenterAsync(point, scale);
        }


    }


    /**
     * 画多边形的面
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param pointCollection 边缘点的集合
     * @param fillColor       面的内部填充颜色
     * @param fillStyle       面的样式
     * @param outlineWidth    外边线的粗细
     * @param outlineColor    外边线的颜色
     * @param outlineStyle    外边线的样式  （外边线类似是跟折线类似的代码）
     * @param isRedraw        true 重画   false 追加
     */
    public void drawPolygon(GraphicsOverlay graphicsOverlay, PointCollection pointCollection, int fillColor, SimpleFillSymbol.Style fillStyle, float outlineWidth, int outlineColor, SimpleLineSymbol.Style outlineStyle, boolean isRedraw) {

        //创建多边形对象
        Polygon polygon = new Polygon(pointCollection);

        if (fillStyle == null) {
            fillStyle = SimpleFillSymbol.Style.SOLID;
        }

        //创建边缘线对象
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(outlineStyle, outlineColor, outlineWidth);
        // 创建多边形对象
        SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(fillStyle, fillColor, lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
        Graphic polygonGraphic = new Graphic(polygon, polygonSymbol);

        if (isRedraw) {
            //绘制之前先清空绘制对象
            graphicsOverlay.getGraphics().clear();
        }

        //添加绘制对象
        graphicsOverlay.getGraphics().add(polygonGraphic);

        for (Point point : pointCollection) {
            drawPoint(graphicsOverlay, point, outlineWidth, Color.BLUE, false);
        }

    }


    /**
     * 画多边形的面
     *
     * @param graphicsOverlay 覆盖物绘图对象
     * @param pointCollection 边缘点的集合
     * @param isRedraw        true 重画   false 追加
     */
    public void drawPolygon(GraphicsOverlay graphicsOverlay, PointCollection pointCollection, boolean isRedraw) {
        drawPolygon(graphicsOverlay, pointCollection, Color.parseColor("#90444444"), null, 5.0f, Color.RED, SimpleLineSymbol.Style.SOLID, true);
    }

    public void drawPolygon(GraphicsOverlay graphicsOverlay, PointCollection pointCollection, int lineSize, int lineColor, boolean isRedraw) {
        drawPolygon(graphicsOverlay, pointCollection, Color.parseColor("#90444444"), null, lineSize, lineColor, SimpleLineSymbol.Style.SOLID, true);
    }


    public void drawGeomety(GraphicsOverlay layer, Geometry geometry, int type, boolean isRedraw, Object data,boolean isMeasure) {
        Graphic graphic = null;


        if (isRedraw) {
            //绘制之前先清空绘制对象
            layer.getGraphics().clear();
        }


        if (type == 0) {

            //确定 图形的外观
//        简单标记符号对象
            SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 8f);
//        创建图解对象
            graphic = new Graphic(geometry, symbol);

            if (data != null) {
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("data", new Gson().toJson(data));
                graphic = new Graphic(geometry, attrs, symbol);
            } else {
                graphic = new Graphic(geometry, symbol);
            }

        } else if (type == 1) {
            //折线对象

            //创建 线符号对象
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5f);
            //创建折线绘制对象
            graphic = new Graphic(geometry, lineSymbol);
            if (data != null) {
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("data", new Gson().toJson(data));
                graphic = new Graphic(geometry, attrs, lineSymbol);
            } else {
                graphic = new Graphic(geometry, lineSymbol);
            }

            if(isMeasure){
                //计算距离
                var length = caculationUtil.caculateLengthUnit((Polyline) geometry, true);
                Graphic graphicText = new Graphic(geometry.getExtent().getCenter(), style.getTextSymbol1(length));
                layer.getGraphics().add(graphicText);
            }




        } else if (type == 2) {
            //面对象
            //面的内部样式
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,  Color.parseColor("#52ade6"), 1f);
            //面的内部样式
            SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,  Color.parseColor("#9052ade6"), lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
            if (data != null) {
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("data", new Gson().toJson(data));
                graphic = new Graphic(geometry, attrs, polygonSymbol);
            } else {
                graphic = new Graphic(geometry, polygonSymbol);
            }

        }

        //添加绘制对象
        layer.getGraphics().add(graphic);
    }

    public void drawGeometyOutline(GraphicsOverlay layer, Geometry geometry, int type, boolean isRedraw) {
        Graphic graphic = null;

        if (type == 0) {

            //确定 图形的外观
//        简单标记符号对象
            SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.YELLOW, 5f);
//        创建图解对象
            graphic = new Graphic(geometry, symbol);

        } else if (type == 1) {
            //折线对象

            //创建 线符号对象
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.YELLOW, 2f);
            //创建折线绘制对象
            graphic = new Graphic(geometry, lineSymbol);

        } else if (type == 2) {
            //面对象
            //面的内部样式
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f);
            //面的内部样式
            SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.NULL, R.color.transColor, lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
            graphic = new Graphic(geometry, polygonSymbol);
        }


        if (isRedraw) {
            //绘制之前先清空绘制对象
            layer.getGraphics().clear();
        }

        //添加绘制对象
        layer.getGraphics().add(graphic);
    }

    public void drawPolygonArea(GraphicsOverlay layer, Geometry geometry, boolean isRedraw) {
        Graphic graphic = null;

        //面对象
        //面的内部样式
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.YELLOW, 4f);
        //面的内部样式
        SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.NULL, R.color.transColor, lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
        graphic = new Graphic(geometry, polygonSymbol);


        if (isRedraw) {
            //绘制之前先清空绘制对象
            layer.getGraphics().clear();
        }

        //添加绘制对象
        layer.getGraphics().add(graphic);
    }


    public void drawGeomety(GraphicsOverlay layer, Geometry geometry, StyleConfig styleConfig, boolean isRedraw) {

        if (geometry == null) return;
        Graphic graphic = null;
        GeometryType type = geometry.getGeometryType();

        if (type == GeometryType.POINT || type == GeometryType.MULTIPOINT) {
            graphic = new Graphic(geometry, styleConfig.getPointSymbol1());

        } else if (type == GeometryType.POLYLINE) {
            //折线对象
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5f);
            graphic = new Graphic(geometry, styleConfig.getPolylineSymbol1());

        } else if (type == GeometryType.POLYGON) {
            //面对象
            graphic = new Graphic(geometry, styleConfig.getPolygonSymbol1());
        } else if (type == GeometryType.MULTIPOINT) {
            //面对象
//            graphic = new Graphic(geometry, styleConfig.getPointSymbol());
        }

        if (isRedraw) {
            layer.getGraphics().clear();
        }

        if (graphic == null) return;
        //添加绘制对象
        layer.getGraphics().add(graphic);
    }

    public static final int RED_HALF = 0x40FF0000;

    //重叠的部分标红
    public void drawOverlap(GraphicsOverlay layer, Geometry geometry, boolean isRedraw) {
        Graphic graphic = null;


        //面对象
        //面的内部样式
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1f);
        //面的内部样式
        SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, RED_HALF, lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
        graphic = new Graphic(geometry, polygonSymbol);


        if (isRedraw) {
            //绘制之前先清空绘制对象
            layer.getGraphics().clear();
        }

        //添加绘制对象
        layer.getGraphics().add(graphic);
    }

    public void drawCollectionMarker(GraphicsOverlay layer, Geometry geometry, int type, boolean isRedraw) {
        Graphic graphic = null;

        if (type == 0) {
            drawGeomety(layer, geometry, type, isRedraw,null,true);

        } else if (type == 1) {
            drawGeomety(layer, geometry, type, isRedraw, null, true);

        } else if (type == 2) {

            //面对象
            //面的内部样式
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1f);
            //面的内部样式
            SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#90444444"), lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
            graphic = new Graphic(geometry ,polygonSymbol);

//            drawPolygon(graphicsOverlay, pointCollection, Color.parseColor("#90444444"), null, lineSize, lineColor, SimpleLineSymbol.Style.SOLID, true);
            if (isRedraw) {
                //绘制之前先清空绘制对象
                layer.getGraphics().clear();
            }

            //添加绘制对象
            layer.getGraphics().add(graphic);
        }


    }

    public void drawCollectionMarkerWithAttr(GraphicsOverlay layer, Geometry geometry, int type, MarkerInfo data , boolean isRedraw) {
        Graphic graphic = null;

        if (type == 0) {
            drawGeomety(layer, geometry, type, isRedraw, data, true);

        } else if (type == 1) {
            drawGeomety(layer, geometry, type, isRedraw, data, true);

        } else if (type == 2) {

            //面对象
            //面的内部样式
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1f);
            //面的内部样式
            SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#90444444"), lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
            if (data != null) {
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("data", new Gson().toJson(data));
                graphic = new Graphic(geometry, attrs, polygonSymbol);
            } else {
                graphic = new Graphic(geometry, polygonSymbol);
                }

//            drawPolygon(graphicsOverlay, pointCollection, Color.parseColor("#90444444"), null, lineSize, lineColor, SimpleLineSymbol.Style.SOLID, true);
            if (isRedraw) {
                //绘制之前先清空绘制对象
                layer.getGraphics().clear();
            }

            //添加绘制对象
            layer.getGraphics().add(graphic);


            //计算面积
            var area = caculationUtil.caculateAreaSizeUnit((Polygon) geometry, CaculationUtil.UNIT_DEFAULT, true);
            Graphic graphicText = new Graphic(geometry.getExtent().getCenter(), style.getTextSymbol1(area));
            layer.getGraphics().add(graphicText);

        }

    }


    public void drawPoint(GraphicsOverlay layer, Geometry geometry, float size, int color, boolean isRedraw) {
        Graphic graphic = null;
        SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, color, size);
        graphic = new Graphic(geometry, symbol);
        if (isRedraw) {
            layer.getGraphics().clear();
        }
        layer.getGraphics().add(graphic);
    }


    /**
     * 坐标系转化
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public Point get_change_geometry_point(double latitude, double longitude) {
        Point center = new Point(longitude, latitude, Config.INSTANCE.getSp4326());
        return center;
    }

    public Point get_change_geometry_point(double latitude, double longitude, int spatialReference) {
        SpatialReference mSR4326 = SpatialReference.create(spatialReference);
        Point center = new Point(longitude, latitude, mSR4326);
        return center;
    }


    /**
     * 在定位的点设置固定的缩放大小
     *
     * @param mapView
     */
    public void setDefineScaleSzie(MapView mapView) {
        setDefineScaleSzie(mapView, 9000);
    }


    /**
     * 在定位的点设置固定的缩放大小
     *
     * @param scaleSize 缩放大小
     * @param mapView
     */
    public void setDefineScaleSzie(MapView mapView, long scaleSize) {
        if (mapView == null) return;

        LocationDisplay locationDisplay = mapView.getLocationDisplay();
        Point locationPoint = locationDisplay.getMapLocation();
        Viewpoint viewpoint = new Viewpoint(locationPoint, scaleSize);

        ArcGISMap arcGISMap = new ArcGISMap(Basemap.createImageryWithLabelsVector());
        arcGISMap.setInitialViewpoint(viewpoint);
        mapView.setMap(arcGISMap);


    }

    /**
     * 去掉证书水印
     *
     * @param licence
     */
    public void deleteWaterMark(String licence) {
        if (licence == null || licence.equals("")) return;
        ArcGISRuntimeEnvironment.setLicense(licence);
    }

    /**
     * 去除 Powered by Esri 字样
     *
     * @param mapView
     * @param visiable
     */
    public void setAuthorVisiable(MapView mapView, boolean visiable) {
        if (mapView == null) return;
        mapView.setAttributionTextVisible(visiable);
    }

    public void drawBuffer(GraphicsOverlay layer, Geometry geometry, boolean isRedraw) {
        Graphic graphic = null;

        //面对象
        //面的内部样式
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1f);
        //面的内部样式
        SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#33444444"), lineSymbol);

//        创建绘制对象 ,将边缘和多边形对象传入
        graphic = new Graphic(geometry, polygonSymbol);

//            drawPolygon(graphicsOverlay, pointCollection, Color.parseColor("#90444444"), null, lineSize, lineColor, SimpleLineSymbol.Style.SOLID, true);
        if (isRedraw) {
            //绘制之前先清空绘制对象
            layer.getGraphics().clear();
        }

        //添加绘制对象
        layer.getGraphics().add(graphic);


    }


    public void drawBufferResult(GraphicsOverlay layer, Geometry geometry, int type, String data,
                                 boolean isRedraw,BitmapDrawable bitmapDrawable,SimpleFillSymbol polygonSymbol) {
        Graphic graphic = null;

        if (isRedraw) {
            //绘制之前先清空绘制对象
            layer.getGraphics().clear();
        }



        if (type == 0) {

//        简单标记符号对象
            SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 8f);


            PictureMarkerSymbol symbol1 = new PictureMarkerSymbol(bitmapDrawable);

            if (data != null) {
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("data", new Gson().toJson(data));
                graphic = new Graphic(geometry, attrs, symbol1);
            } else {
                graphic = new Graphic(geometry, symbol);
            }

            layer.getGraphics().add(graphic);
        } else if (type == 1) {

            //创建 线符号对象
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5f);

            if (data != null) {
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("data", new Gson().toJson(data));
                graphic = new Graphic(geometry, attrs, lineSymbol);
            } else {
                graphic = new Graphic(geometry, lineSymbol);
            }
            layer.getGraphics().add(graphic);


        } else if (type == 2) {
            //面对象
            //面的内部样式

//            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 3f);
//            //面的内部样式
//            SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#90444444"), lineSymbol);

            Graphic graphicOriginal = new Graphic(geometry, polygonSymbol);

            //确定 图形的外观

            PictureMarkerSymbol symbol = new PictureMarkerSymbol(bitmapDrawable);


            if (data != null) {
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("data", new Gson().toJson(data));
                graphicOriginal = new Graphic(geometry, attrs, polygonSymbol);

                graphic = new Graphic(geometry.getExtent().getCenter(), attrs, symbol);

            } else {
                graphicOriginal = new Graphic(geometry, polygonSymbol);
                graphic = new Graphic(geometry.getExtent().getCenter(), symbol);
            }
            //添加面
            layer.getGraphics().add(graphicOriginal);
            //添加中心点
            layer.getGraphics().add(graphic);

        }


    }



}