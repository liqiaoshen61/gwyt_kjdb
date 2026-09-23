package com.jwch.gwyt_project.Info;

import com.jameni.allutillib.common.TimeUtil;
import com.jwch.gwyt_project.core.Config;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 图斑收藏表
 */
@Table(name = "CollectPatch", onCreated = "")
public class CollecPatchInfo {
    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "linkId")
    private String linkId;//关联id,原来shp里面的FID(图斑id)
    @Column(name = "themeId")
    private String themeId; //专题图层id
    @Column(name = "themeName")
    private String themeName; //专题图层名称
    @Column(name = "name")
    private String name;//图斑名称
    @Column(name = "createTimeStamp")
    private Long createTimeStamp;//创建时间搓
    @Column(name = "centerPointJson")
    private String centerPointJson;//图形的中心点json

    public Boolean isSelect = false ;//是否选中
    public CollecPatchInfo() {

    }


    public CollecPatchInfo(String linkId, String themeId, String themeName, String name) {
        this.themeId = themeId;
        this.themeName = themeName;
        this.linkId = linkId;
        this.name = name;
        this.createTimeStamp = TimeUtil.getCurrentStamp();
    }

    public CollecPatchInfo(String linkId, String themeId, String themeName, String name,String centerPoint) {
        this.themeId = themeId;
        this.themeName = themeName;
        this.linkId = linkId;
        this.name = name;
        this.centerPointJson = centerPoint;
        this.createTimeStamp = TimeUtil.getCurrentStamp();
    }

    public String getCenterPointJson() {
        return centerPointJson;
    }

    public void setCenterPointJson(String centerPointJson) {
        this.centerPointJson = centerPointJson;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
