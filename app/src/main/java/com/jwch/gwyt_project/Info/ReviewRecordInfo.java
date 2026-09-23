package com.jwch.gwyt_project.Info;

import com.jameni.allutillib.common.TimeUtil;
import com.jwch.gwyt_project.core.Config;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 复核记录表
 */
@Table(name = "ReviewRecordInfo", onCreated = "")
public class ReviewRecordInfo {
    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "linkId")
    private String linkId;//关联id,原来shp里面的FID(图斑id)
    @Column(name = "themeId")
    private String themeId; //专题图层id
    @Column(name = "themeName")
    private String themeName; //专题图层名称
    @Column(name = "createTimeStamp")
    private Long createTimeStamp;//时间
    @Column(name = "pics")
    private String pics;//图片


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

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }

    public Boolean isSelect = false; //是否选中

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

    @Column(name = "questionCode")
    private String questionCode;

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
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

    public String getCounty() {
        return county;
    }

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

    public void setTime(String time) {
        this.time = time;
    }

    public String getPics() {
        return pics;
    }

    public void setPics(String pics) {
        this.pics = pics;
    }

    public ReviewRecordInfo() {
    }

    public ReviewRecordInfo(String linkId, String themeId, String themeName) {
        this.themeId = themeId;
        this.themeName = themeName;
        this.linkId = linkId;
        this.createTimeStamp = TimeUtil.getCurrentStamp();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public Long getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(Long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    private String time;

    public String getTime() {

        if (time == null) {
            time = TimeUtil.getDateToString(createTimeStamp, Config.timeFormat1);
        }

        return time;
    }
}
