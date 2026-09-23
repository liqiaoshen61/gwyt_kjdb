 package com.jwch.gwyt_project.Info;


 import com.esri.arcgisruntime.data.Feature;
 import com.esri.arcgisruntime.data.Geodatabase;
 import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
 import com.esri.arcgisruntime.data.ServiceFeatureTable;
 import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
 import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
 import com.esri.arcgisruntime.layers.FeatureLayer;
 import com.esri.arcgisruntime.loadable.LoadStatus;
 import com.jameni.allutillib.common.CommonUtil;
 import com.jameni.allutillib.common.FileUtil;
 import com.jameni.allutillib.common.PrintUtil;
 import com.jwch.gwyt_project.common.Tools;
 import com.jwch.gwyt_project.core.Config;
 import com.jwch.gwyt_project.i.ActionListener;
 import com.jwch.gwyt_project.model.InterSectionModel;
 import com.jwch.gwyt_project.util.CryptoUtils;

 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xutils.db.annotation.Column;
 import org.xutils.db.annotation.Table;

 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;


@Table(name = "Themes", onCreated = "")
public class ThemesInfo {

    @Column(name = "Id")
    private String id;
    @Column(name = "ThemeName")
    private String themeName;//专题图名称
    @Column(name = "DispName")
    private String DispName;//展示名称--这个字段基本的空字符串
    @Column(name = "CatTypeID")
    private String catTypeId;//分类id
    @Column(name = "CatType2ID")
    private int catType2Id;//分类id2
    @Column(name = "DispDistrict")
    private String dispDistrict;//地区展示名称
    @Column(name = "ThemeFile")
    private String themeFile;//专题文件，目前这个字段是shp的文件名（含shp后缀）
    @Column(name = "TileFile")
    private String tileFile;//瓦片文件，tpk图层文件名（含tpk后缀）
    @Column(name = "Introduction")
    private String introduction;//介绍==基本是空的字符串
    @Column(name = "NameField")
    private String nameField;//名称字段
    @Column(name = "MinLevel")
    private int minLevel;//
    @Column(name = "MaxLevel")
    private int maxLevel;//
    @Column(name = "XMin")
    private double xMin;//
    @Column(name = "XMax")
    private double xMax;//
    @Column(name = "YMin")
    private double yMin;//
    @Column(name = "YMax")
    private double yMax;//
    @Column(name = "ThumbnailFile")
    private String ThumbnailFile;//缩略图文件--基本是空字符串
    @Column(name = "LegendFile")
    private String LegendFile;//图例文件名--都是图片，含后缀
    @Column(name = "IsOnline")
    private String isOnline;//
    @Column(name = "OnlineURL")
    private String onlineURL;//基本是空字符字段
    @Column(name = "Version")
    private int version;//
    @Column(name = "GroupField")
    private String groupField;//分组字段
    @Column(name = "StatisticField")
    private String statisticField;//统计字段
//    @Column(name = "ShowReplaceAction")
//    private int showReplaceAction;//
//    @Column(name = "ShowAddAction")
//    private int showAddAction;//
    @Column(name = "TablePictureFile")
    private String TablePictureFile;//基本是空字符字段
    @Column(name = "ListInfoField1")
    private String listInfoField1;//列表信息字段1
    @Column(name = "ListInfoField2")
    private String listInfoField2;//列表信息字段2
    @Column(name = "ListInfoField3")
    private String listInfoField3;//列表信息字段3
    @Column(name = "PopupInfoField1")
    private String popupInfoField1;//弹窗信息字段1
    @Column(name = "PopupInfoField2")
    private String popupInfoField2;//弹窗信息字段2
    @Column(name = "PopupInfoField3")
    private String popupInfoField3;//弹窗信息字段3
    @Column(name = "ThemeFilePath")
    private String themeFilePath;

    private List<String> listInfoList = new ArrayList<String>();
    private List<String> popupInfoList = new ArrayList<String>();
    private FeatureLayer layer;
    private ArcGISTiledLayer tiledLayer;

    private ArcGISVectorTiledLayer vectorTiledLayer;

    private ArcGISVectorTiledLayer vectorTiledLayer_1;
    private ArcGISVectorTiledLayer vectorTiledLayer_2;
    private List<Feature> features = new ArrayList<>();
    private boolean Selected = false;
    private List<InterSectionModel> interSectionList = new ArrayList<>();//图形分析结果
    private String gdbPath;//geodatabase 的目录



    public String getListInfoField(int index) {
        return listInfoList.get(index);
    }
    public String getPopupInfoField(int index) {
        return popupInfoList.get(index);
    }


    public void setData() {
        listInfoList.add(listInfoField1);
        listInfoList.add(listInfoField2);
        listInfoList.add(listInfoField3);
        popupInfoList.add(popupInfoField1);
        popupInfoList.add(popupInfoField2);
        popupInfoList.add(popupInfoField3);
    }

    public ThemesInfo() {

    }

    public ThemesInfo(JSONObject object) {
        try {
            id = object.getString("Id");
            themeName = object.getString("ThemeName");
            themeFile = object.getString("ThemeFile");
            tileFile = object.getString("TileFile");
            version = object.getInt("Version");
            isOnline = object.getString("IsOnline");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getDispName() {
        return DispName;
    }

    public void setDispName(String dispName) {
        DispName = dispName;
    }

    public String getCatTypeId() {
        return catTypeId;
    }

    public void setCatTypeId(String catTypeId) {
        this.catTypeId = catTypeId;
    }

    public int getCatType2Id() {
        return catType2Id;
    }

    public void setCatType2Id(int catType2Id) {
        this.catType2Id = catType2Id;
    }

    public String getDispDistrict() {
        return dispDistrict;
    }

    public void setDispDistrict(String dispDistrict) {
        this.dispDistrict = dispDistrict;
    }

    public String getThemeFile() {
        return themeFile;
    }

    public void setThemeFile(String themeFile) {
        this.themeFile = themeFile;
    }

    public String getTileFile() {
        return tileFile;
    }

    public void setTileFile(String tileFile) {
        this.tileFile = tileFile;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getNameField() {
        return nameField;
    }

    public void setNameField(String nameField) {
        this.nameField = nameField;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public String getThumbnailFile() {
        return ThumbnailFile;
    }

    public void setThumbnailFile(String thumbnailFile) {
        ThumbnailFile = thumbnailFile;
    }

    public String getLegendFile() {
        return LegendFile;
    }

    public void setLegendFile(String legendFile) {
        LegendFile = legendFile;
    }

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }

    public String getOnlineURL() {
        return onlineURL;
    }

    public void setOnlineURL(String onlineURL) {
        this.onlineURL = onlineURL;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getGroupField() {
        return groupField;
    }

    public void setGroupField(String groupField) {
        this.groupField = groupField;
    }

    public String getStatisticField() {
        return statisticField;
    }

    public void setStatisticField(String statisticField) {
        this.statisticField = statisticField;
    }

    public String getTablePictureFile() {
        return TablePictureFile;
    }

    public void setTablePictureFile(String tablePictureFile) {
        TablePictureFile = tablePictureFile;
    }

    public String getListInfoField1() {
        return listInfoField1;
    }

    public void setListInfoField1(String listInfoField1) {
        this.listInfoField1 = listInfoField1;
    }

    public String getListInfoField2() {
        return listInfoField2;
    }

    public void setListInfoField2(String listInfoField2) {
        this.listInfoField2 = listInfoField2;
    }

    public String getListInfoField3() {
        return listInfoField3;
    }

    public void setListInfoField3(String listInfoField3) {
        this.listInfoField3 = listInfoField3;
    }

    public String getPopupInfoField1() {
        return popupInfoField1;
    }

    public void setPopupInfoField1(String popupInfoField1) {
        this.popupInfoField1 = popupInfoField1;
    }

    public String getPopupInfoField2() {
        return popupInfoField2;
    }

    public void setPopupInfoField2(String popupInfoField2) {
        this.popupInfoField2 = popupInfoField2;
    }

    public String getPopupInfoField3() {
        return popupInfoField3;
    }

    public void setPopupInfoField3(String popupInfoField3) {
        this.popupInfoField3 = popupInfoField3;
    }

    public List<String> getListInfoList() {
        return listInfoList;
    }

    public void setListInfoList(List<String> listInfoList) {
        this.listInfoList = listInfoList;
    }

    public List<String> getPopupInfoList() {
        return popupInfoList;
    }

    public void setPopupInfoList(List<String> popupInfoList) {
        this.popupInfoList = popupInfoList;
    }

    public FeatureLayer getLayer() {
        return layer;
    }

    public void setLayer(FeatureLayer layer) {
        this.layer = layer;
    }

    public ArcGISTiledLayer getTiledLayer() {
        return tiledLayer;
    }

    public void setTiledLayer(ArcGISTiledLayer tiledLayer) {
        this.tiledLayer = tiledLayer;
    }

    public ArcGISVectorTiledLayer getVectorTiledLayer() {
        return vectorTiledLayer;
    }

    public void setVectorTiledLayer(ArcGISVectorTiledLayer vectorTiledLayer) {
        this.vectorTiledLayer = vectorTiledLayer;
    }

    public ArcGISVectorTiledLayer getVectorTiledLayer_1() {
        return vectorTiledLayer_1;
    }

    public void setVectorTiledLayer_1(ArcGISVectorTiledLayer vectorTiledLayer) {
        this.vectorTiledLayer_1 = vectorTiledLayer;
    }

    public ArcGISVectorTiledLayer getVectorTiledLayer_2() {
        return vectorTiledLayer_2;
    }

    public void setVectorTiledLayer_2(ArcGISVectorTiledLayer vectorTiledLayer) {
        this.vectorTiledLayer_2 = vectorTiledLayer;
    }


    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public boolean isSelected() {
        return Selected;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
    }

    public List<InterSectionModel> getInterSectionList() {
        return interSectionList;
    }

    public void setInterSectionList(List<InterSectionModel> interSectionList) {
        this.interSectionList = interSectionList;
    }

    public void setGeodatabase(Geodatabase geodatabase) {
        this.geodatabase = geodatabase;
    }


    public String getGdbPath() {

        if (CommonUtil.isNotEmpty(getThemeName()) && !CommonUtil.isNotEmpty(gdbPath)) {
            String suffx = ".geodatabase";
            gdbPath = Config.GDB_PATH + getThemeName() + suffx;
        }

//        PrintUtil.printMsg("gdb路径==" + gdbPath);
        return gdbPath;
    }

    //是否存在gdb文件
    public boolean isGdbFileExist() {
        if (!CommonUtil.isNotEmpty(getGdbPath())) return false;
        return FileUtil.isFileExists(new File(getGdbPath()));
    }

    public boolean isGdbFileNotEmpty() {
        if (!CommonUtil.isNotEmpty(getGdbPath())) return false;
        File file = new File(getGdbPath());
        return FileUtil.isFileExists(file) && file.length() > 0;
    }

    public void setGdbPath(String gdbPath) {
        this.gdbPath = gdbPath;
    }

    public void loadTileLayer() {
        loadTileLayer(null);
    }

    public void loadTileLayer(final ActionListener listener) {

//        String layerPath = Config.THEMEDATA_TPK_PATH + Tools.getTpkPath(getTileFile());

//        PrintUtil.printMsg("tile layer==" + layerPath);

        tiledLayer = Tools.getTiledLayerByPath(Config.THEMEDATA_TPK_PATH, themeName);

        tiledLayer.setId(getId() + "");
        tiledLayer.loadAsync();
        tiledLayer.addDoneLoadingListener(() -> {
            if (tiledLayer.getLoadStatus() == LoadStatus.LOADED) {
//                PrintUtil.printMsg("tpk图层加载成id==" + tiledLayer.getId());


                if (listener != null) {
                    listener.onAction(tiledLayer, 0);
                }
            }
        });
    }


    public void loadVectorTileLayer() {
        loadVectorTileLayer(null);
    }

    public void loadVectorTileLayer(final ActionListener listener) {

        vectorTiledLayer = Tools.getVectorTiledLayerByPath(Config.THEMEDATA_TPK_PATH, themeName);
        if(vectorTiledLayer != null){
            vectorTiledLayer.setId(getId() + "");
            vectorTiledLayer.loadAsync();
            vectorTiledLayer.addDoneLoadingListener(() -> {
                if (vectorTiledLayer.getLoadStatus() == LoadStatus.LOADED) {
                PrintUtil.printMsg(themeName+" vtpk图层加载成id==" + tiledLayer.getId());

                    if (listener != null) {
                        listener.onAction(vectorTiledLayer, 0);
                    }
                }else if (vectorTiledLayer.getLoadStatus() == LoadStatus.FAILED_TO_LOAD){
                    PrintUtil.printMsg("加载失败。。。" + vectorTiledLayer.getLoadError().getMessage());
                }
            });
        }


        vectorTiledLayer_1 = Tools.getVectorTiledLayerByPath(Config.THEMEDATA_TPK_PATH, themeName+"_未办结");
        if(vectorTiledLayer_1 != null){
            vectorTiledLayer_1.setId(getId() + "");
            vectorTiledLayer_1.loadAsync();
            vectorTiledLayer_1.addDoneLoadingListener(() -> {
                if (vectorTiledLayer_1.getLoadStatus() == LoadStatus.LOADED) {
                    PrintUtil.printMsg(themeName+" vtpk图层_1加载成id==" + tiledLayer.getId());

                    if (listener != null) {
                        listener.onAction(vectorTiledLayer_1, 0);
                    }
                }else if (vectorTiledLayer_1.getLoadStatus() == LoadStatus.FAILED_TO_LOAD){
                    PrintUtil.printMsg("加载失败。。。" + vectorTiledLayer_1.getLoadError().getMessage());
                }
            });
        }


        vectorTiledLayer_2 = Tools.getVectorTiledLayerByPath(Config.THEMEDATA_TPK_PATH, themeName+"_已办结");
        if(vectorTiledLayer_2 != null){
            vectorTiledLayer_2.setId(getId() + "");
            vectorTiledLayer_2.loadAsync();
            vectorTiledLayer_2.addDoneLoadingListener(() -> {
                if (vectorTiledLayer_2.getLoadStatus() == LoadStatus.LOADED) {
                    PrintUtil.printMsg(themeName+" vtpk图层_2加载成id==" + tiledLayer.getId());

                    if (listener != null) {
                        listener.onAction(vectorTiledLayer_2, 0);
                    }
                }else if (vectorTiledLayer_2.getLoadStatus() == LoadStatus.FAILED_TO_LOAD){
                    PrintUtil.printMsg("加载失败。。。" + vectorTiledLayer_2.getLoadError().getMessage());
                }
            });
        }

    }



    public Geodatabase geodatabase;

    public void loadGdbLayer() {
        loadGdbLayer(null);
    }

    public void loadGdbLayer(final ActionListener listener) {

        if (getLayer() == null && isGdbFileExist()) {


            String realPath = CryptoUtils.INSTANCE.decryptGeodatabase(getGdbPath());
            geodatabase = new Geodatabase(realPath);
            geodatabase.loadAsync();
            geodatabase.addDoneLoadingListener(() -> {

//                PrintUtil.printMsg("gdb 加载状态==" + geodatabase.getLoadStatus());

                if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {

                    List<GeodatabaseFeatureTable> tableList = geodatabase.getGeodatabaseFeatureTables();

                    if (CommonUtil.matchList(tableList)) {


                        GeodatabaseFeatureTable table = tableList.get(0);
                        table.loadAsync();
                        //创建要素图层

                        final FeatureLayer featureLayer = new FeatureLayer(table);
//                        featureLayer.setRenderingMode(FeatureLayer.RenderingMode.STATIC);//静态模式渲染，提高性能呢

                        featureLayer.setId(getId() + "");

                        featureLayer.addDoneLoadingListener(() -> {

                            if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
                                setLayer(featureLayer);

                                //PrintUtil.printMsg("featureLayer 加载成功 id==" + getThemeName() + "     " + featureLayer.getId());

                                if (listener != null) {
                                    listener.onAction(featureLayer.getFeatureTable(), 0);
                                }
                            }
                        });

                    }

                }
            });
        }
    }

    //加载在线服务图层
    public void loadServiceLayer() {

        if (getLayer() == null) {

            ServiceFeatureTable table =new ServiceFeatureTable(getThemeFilePath());
            table.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
            FeatureLayer layer =new FeatureLayer(table);
            layer.setLabelsEnabled(true);
            table.loadAsync();
            table.addDoneLoadingListener(() -> {
                PrintUtil.printMsg("加载状态:" +table.getLoadStatus());
                if(table.getLoadStatus() == LoadStatus.LOADED) {
                    PrintUtil.printMsg("加载成功。。。");
                }else if (table.getLoadStatus() == LoadStatus.FAILED_TO_LOAD){
                    PrintUtil.printMsg("加载失败。。。" + table.getLoadError().getMessage());
                }
            });
            setLayer(layer);

        }
    }

    public String getThemeFilePath() {
        return themeFilePath;
    }

    public void setThemeFilePath(String themeFilePath) {
        this.themeFilePath = themeFilePath;
    }

    public Geodatabase getGeodatabase() {
        return geodatabase;
    }


    @Override
    public String toString() {
        return "ThemesInfo{" +
                "id='" + id + '\'' +
                ", themeName='" + themeName + '\'' +
                ", catTypeId='" + catTypeId + '\'' +
                ", LegendFile='" + LegendFile + '\'' +
                '}';
    }
}
