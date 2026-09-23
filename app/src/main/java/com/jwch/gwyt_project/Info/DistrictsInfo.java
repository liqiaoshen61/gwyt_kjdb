package com.jwch.gwyt_project.Info;


import com.esri.arcgisruntime.geometry.Geometry;
import com.jwch.gwyt_project.bean.TreeNodeId;
import com.jwch.gwyt_project.bean.TreeNodeLabel;
import com.jwch.gwyt_project.bean.TreeNodePid;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.List;

/**
 * 行政区域表--已匹配
 */
@Table(name = "Districts")
public class DistrictsInfo {
    @Column(name = "Id")
    private String id;
    @Column(name = "DistName")
    @TreeNodeLabel
    private String distName;//地区名称
    @Column(name = "DistLevel")
    private int distLevel;//地区级别
    @Column(name = "DistCode")
    @TreeNodeId
    private String distCode;//地区行政编码
    @TreeNodePid
    private String pDistCode;
    @Column(name = "CenterX")
    private double centerX;//经度
    @Column(name = "CenterY")
    private double centerY;//纬度
    @Column(name = "USN")
    private int usn;

    private boolean isSelect;

    List<Geometry> geometryList;

    int count;
    int countFinish;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCountFinish() {
        return countFinish;
    }

    public void setCountFinish(int countFinish) {
        this.countFinish = countFinish;
    }

    public static final int LEVEL_PROVINCE = 1;//省级
    public static final int LEVEL_CITY = 2;//市级
    public static final int LEVEL_COUNTY = 3;//区县级
    public static final int LEVEL_TOWN = 4;//乡镇
    public static final int LEVEL_VILLAGE = 5;//村庄


    public DistrictsInfo() {
    }

    public  List<Geometry> getGeometryList() {
        return geometryList;
    }

    public void setGeometryList( List<Geometry> list) {
        geometryList = list;
    }


    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistName() {
        return distName;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public int getDistLevel() {
        return distLevel;
    }

    public void setDistLevel(int distLevel) {
        this.distLevel = distLevel;
    }

    public String getDistCode() {
        return distCode;
    }

    public void setDistCode(String distCode) {
        this.distCode = distCode;
    }

    public String getpDistCode() {
        return pDistCode;
    }

    public void setpDistCode(String pDistCode) {
        this.pDistCode = pDistCode;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }
}
