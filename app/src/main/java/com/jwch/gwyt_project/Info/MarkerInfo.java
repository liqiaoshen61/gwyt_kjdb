package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 新数据库表
 * 文件夹，表设计支持树形关系
 */
@Table(name = "MarkerInfo")
public class MarkerInfo {

    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "folderId")
    private int folderId;// 所属文件夹id 只为0的时候，就是标绘没有归属文件夹
    @Column(name = "name")
    private String name;//标绘名称
    @Column(name = "geoType")
    private int geoType;//保存的图形类型 point:0  polyline:1  polygon:2  drawtext:3
    @Column(name = "geometryJson")
    private String geometryJson;//图形的json
    @Column(name = "centerPointJson")
    private String centerPointJson;//图形的中心点json
    @Column(name = "createTimeStamp")
    private Long createTimeStamp;//创建时间搓
    @Column(name = "scale")
    private double scale;//保存时的缩放大小
    @Column(name = "size")
    private int size;//线条粗细
    @Column(name = "remark")
    private String remark;//保存备注
    @Column(name = "themeName")
    private String themeName;//分析时保存的专题图名称  形式以,隔开
    @Column(name = "themeId")
    private String themeId;//分析时保存的专题图id  形式以,隔开

    @Column(name = "riverName")
    private String riverName;
    @Column(name = "city")
    private String city;
    @Column(name = "county")
    private String county;
    @Column(name = "town")
    private String town;
    @Column(name = "village")
    private String village;
    @Column(name = "questionType")
    private String questionType;
    @Column(name = "questionAttr")
    private String questionAttr;
    @Column(name = "severity")
    private String severity;
    @Column(name = "description")
    private String description;
    @Column(name = "location")
    private String location;
    @Column(name = "latLng")
    private String latLng;
    @Column(name = "checkTime")
    private Long checkTime;

    @Column(name = "isFinish")
    private String isFinish;

    @Column(name = "spotRectificationSituation")
    private String spotRectificationSituation;

    public String getSpotRectificationSituation() {
        return spotRectificationSituation;
    }

    public void setSpotRectificationSituation(String spotRectificationSituation) {
        this.spotRectificationSituation = spotRectificationSituation;
    }

    public String getIsFinish() {
        return isFinish;
    }

    public void setIsFinish(String isFinish) {
        this.isFinish = isFinish;
    }

    //空的构造函数是必要的，没有的话，会出现异常
    public MarkerInfo() {
    }

    /**
     * 新增时候用
     * @param folderId
     * @param name
     * @param geoType
     * @param geometryJson
     * @param centerPointJson
     * @param createTimeStamp
     * @param remark
     */
    public MarkerInfo(int folderId, String name, int geoType, String geometryJson, String centerPointJson, Long createTimeStamp, String remark) {
        this.folderId = folderId;
        this.name = name;
        this.geoType = geoType;
        this.geometryJson = geometryJson;
        this.centerPointJson = centerPointJson;
        this.createTimeStamp = createTimeStamp;
        this.remark = remark;
    }

    /**
     * 编辑时候用
     * @param id
     * @param folderId
     * @param name
     * @param geoType
     * @param geometryJson
     * @param centerPointJson
     * @param createTimeStamp
     * @param remark
     */
    public MarkerInfo(int id, int folderId, String name, int geoType, String geometryJson, String centerPointJson, Long createTimeStamp, String remark) {
        this.id = id;
        this.folderId = folderId;
        this.name = name;
        this.geoType = geoType;
        this.geometryJson = geometryJson;
        this.centerPointJson = centerPointJson;
        this.createTimeStamp = createTimeStamp;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGeoType() {
        return geoType;
    }

    public void setGeoType(int geoType) {
        this.geoType = geoType;
    }

    public String getGeometryJson() {
        return geometryJson;
    }

    public void setGeometryJson(String geometryJson) {
        this.geometryJson = geometryJson;
    }

    public String getCenterPointJson() {
        return centerPointJson;
    }

    public void setCenterPointJson(String centerPointJson) {
        this.centerPointJson = centerPointJson;
    }

    public Long getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(Long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }


    public String getRiverName() {
        return riverName;
    }

    public void setRiverName(String riverName) {
        this.riverName = riverName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {return county;}

    public void setCounty(String county) {
        this.county = county;
    }

    public String getTown() {return town;}

    public void setTown(String town) {
        this.town = town;
    }

    public String getVillage() {return village;}

    public void setVillage(String village) {
        this.village = village;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionAttr() {
        return questionAttr;
    }

    public void setQuestionAttr(String questionAttr) {
        this.questionAttr = questionAttr;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public Long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Long checkTime) {
        this.checkTime = checkTime;
    }


}
