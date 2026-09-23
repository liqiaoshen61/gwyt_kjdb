package com.jwch.gwyt_project.db;


import com.esri.arcgisruntime.layers.ArcGISTiledLayer;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


@Table(name = "EMaps")
public class EMapsInfo {
    @Column(name = "Id")
    private int id;
    @Column(name = "EMapName")
    private String eMapName;
    @Column(name = "EMapType")
    private int emapType;
    @Column(name = "CatTypeID")
    private int catTypeId;
    @Column(name = "DispDistrict")
    private String dispDistrict;//图层地区（福建省）
    @Column(name = "TileFile")
    private String tileFile;//图层文件夹名字
    @Column(name = "ThambnailFile")
    private String thambnailFile;
    @Column(name = "Introduction")
    private String introduction;

    private ArcGISTiledLayer layer;

    public ArcGISTiledLayer getLayer() {
        return layer;
    }

    public void setLayer(ArcGISTiledLayer layer) {
        this.layer = layer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getTileFile() {
        return tileFile;
    }

    public void setTileFile(String tileFile) {
        this.tileFile = tileFile;
    }

    public String getThambnailFile() {
        return thambnailFile;
    }

    public void setThambnailFile(String thambnailFile) {
        this.thambnailFile = thambnailFile;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    @Override
    public String toString() {
        return "EMapsInfo{" +
                "id=" + id +
                ", eMapName='" + eMapName + '\'' +
                ", emapType=" + emapType +
                ", catTypeId=" + catTypeId +
                ", dispDistrict='" + dispDistrict + '\'' +
                ", tileFile='" + tileFile + '\'' +
                ", thambnailFile='" + thambnailFile + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
