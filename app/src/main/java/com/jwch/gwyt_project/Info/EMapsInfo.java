package com.jwch.gwyt_project.Info;


import com.esri.arcgisruntime.layers.ArcGISTiledLayer;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 地图--底图表 匹配完成
 */
@Table(name = "EMaps")
public class EMapsInfo {

    @Column(name = "Id")
    private String id;
    @Column(name = "EMapName")
    private String eMapName;//图层名称
    @Column(name = "EMapType")
    private int emapType;//图层类型
    @Column(name = "CatTypeID")
    private int catTypeId;//图层类型id
    @Column(name = "BaseMapType")
    private int baseMapType;
    @Column(name = "DispDistrict")
    private String dispDistrict;//图层地区（福建省）
    @Column(name = "TileFileId")
    private String tileFileId;//图层文件夹名字
    @Column(name = "TileFile")
    private String tileFile;//图层文件夹名字
    @Column(name = "ThumbnailFileId")
    private String thumbnailFileId;
    @Column(name = "ThumbnailFile")
    private String thumbnailFile;
    @Column(name = "Introduction")
    private String introduction;
    @Column(name = "IsOnline")
    private int isOnline;
    @Column(name = "OnlineURL")
    private String onlineURL;
    @Column(name = "USN")
    private int usn;

    private ArcGISTiledLayer layer;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String geteMapName() {
        return eMapName;
    }

    public void seteMapName(String eMapName) {
        this.eMapName = eMapName;
    }

    public int getEmapType() {
        return emapType;
    }

    public void setEmapType(int emapType) {
        this.emapType = emapType;
    }

    public int getCatTypeId() {
        return catTypeId;
    }

    public void setCatTypeId(int catTypeId) {
        this.catTypeId = catTypeId;
    }

    public String getDispDistrict() {
        return dispDistrict;
    }

    public void setDispDistrict(String dispDistrict) {
        this.dispDistrict = dispDistrict;
    }

    public String getTileFileId() {
        return tileFileId;
    }

    public void setTileFileId(String tileFileId) {
        this.tileFileId = tileFileId;
    }

    public String getTileFile() {
        return tileFile;
    }

    public void setTileFile(String tileFile) {
        this.tileFile = tileFile;
    }

    public String getThumbnailFileId() {
        return thumbnailFileId;
    }

    public void setThumbnailFileId(String thumbnailFileId) {
        this.thumbnailFileId = thumbnailFileId;
    }

    public String getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(String thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public String getOnlineURL() {
        return onlineURL;
    }

    public void setOnlineURL(String onlineURL) {
        this.onlineURL = onlineURL;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public ArcGISTiledLayer getLayer() {
        return layer;
    }

    public void setLayer(ArcGISTiledLayer layer) {
        this.layer = layer;
    }

    public int getBaseMapType() {
        return baseMapType;
    }

    public void setBaseMapType(int baseMapType) {
        this.baseMapType = baseMapType;
    }
}
