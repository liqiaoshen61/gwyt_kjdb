package com.jwch.gwyt_project.Info;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.layers.FeatureLayer;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * 专题图实体类
 * Created by czb on 2017/6/11.
 */
@Table(name = "TOPICDATA")
public class ToPicDataInfo {
    @Column(name = "TOPICNAME")
    private String toPicName;
    @Column(name = "ALIASNAME")
    private String allAsName;
    @Column(name = "TOPICTYPE")
    private String toPicType;
    @Column(name = "TOPICFORMAT")
    private String toPicFormat;
    @Column(name = "SHPNAME")
    private String shpName;
    @Column(name = "DATALEVEL")
    private Double dataLevel;
    @Column(name = "CENTER_X")
    private Double X;
    @Column(name = "CENTER_Y")
    private Double Y;
    @Column(name = "TOPICDOC")
    private String toPicDoc;
    private FeatureLayer layer;
    private List<Feature> features = new ArrayList<>();
    private boolean Selected = false;

    public boolean isSelected() {
        return Selected;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public FeatureLayer getLayer() {
        return layer;
    }

    public void setLayer(FeatureLayer layer) {
        this.layer = layer;
    }

    public String getToPicName() {
        return toPicName;
    }

    public void setToPicName(String toPicName) {
        this.toPicName = toPicName;
    }

    public String getAllAsName() {
        return allAsName;
    }

    public void setAllAsName(String allAsName) {
        this.allAsName = allAsName;
    }

    public String getToPicType() {
        return toPicType;
    }

    public void setToPicType(String toPicType) {
        this.toPicType = toPicType;
    }

    public String getToPicFormat() {
        return toPicFormat;
    }

    public void setToPicFormat(String toPicFormat) {
        this.toPicFormat = toPicFormat;
    }

    public String getShpName() {
        return shpName;
    }

    public void setShpName(String shpName) {
        this.shpName = shpName;
    }

    public Double getDataLevel() {
        return dataLevel;
    }

    public void setDataLevel(Double dataLevel) {
        this.dataLevel = dataLevel;
    }

    public Double getX() {
        return X;
    }

    public void setX(Double x) {
        X = x;
    }

    public Double getY() {
        return Y;
    }

    public void setY(Double y) {
        Y = y;
    }

    public String getToPicDoc() {
        return toPicDoc;
    }

    public void setToPicDoc(String toPicDoc) {
        this.toPicDoc = toPicDoc;
    }
}
