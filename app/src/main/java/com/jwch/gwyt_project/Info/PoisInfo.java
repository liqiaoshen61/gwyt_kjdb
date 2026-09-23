package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 兴趣点表--已匹配
 */
@Table(name = "Pois")
public class PoisInfo {

    @Column(name = "Id")
    private String id;
    @Column(name = "PoiName")
    private String poiName;//兴趣点名称
    @Column(name = "X")
    private double x;//经度
    @Column(name = "Y")
    private double y;//纬度
    @Column(name = "TypeName")
    private String typeName;//兴趣点类型名称
    @Column(name = "Address")
    private String address;//兴趣点地址
    @Column(name = "CityName")
    private String cityName;//城市名
    @Column(name = "CountyName")
    private String countyName;//县名
    @Column(name = "TownName")
    private String townName;//镇名
    @Column(name = "DistCode")
    private String distCode;//地区名
//    @Column(name = "SourceName")
//    private String sourceName;

    @Column(name = "USN")
    private int usn;


    private int mapId;
    private int index;
    private int markerNum;


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getDistCode() {
        return distCode;
    }

    public void setDistCode(String distCode) {
        this.distCode = distCode;
    }

    public void setMarkerNum(int markerNum) {
        this.markerNum = markerNum;
    }

    public int getMarkerNum() {
        return markerNum;
    }

    public List<Map<String, String>> changeToDetail() {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(getDataMap("名称", poiName));
        list.add(getDataMap("经度", x + ""));
        list.add(getDataMap("纬度", y + ""));
        list.add(getDataMap("类别名称", typeName));
        list.add(getDataMap("地址", address));
        list.add(getDataMap("地级市名", cityName));
        list.add(getDataMap("区县名", countyName));
        list.add(getDataMap("乡镇名", townName));
        list.add(getDataMap("行政区划代码", distCode));
        return list;
    }

    private Map<String, String> getDataMap(String k, String v) {
        Map<String, String> m = new HashMap<>();
        m.put("key", k);
        m.put("values", v);
        return m;
    }


}
