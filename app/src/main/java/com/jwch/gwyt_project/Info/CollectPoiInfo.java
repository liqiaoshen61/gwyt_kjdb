package com.jwch.gwyt_project.Info;

import com.jameni.allutillib.common.TimeUtil;
import com.jwch.gwyt_project.core.Config;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 兴趣点表
 */
@Table(name = "CollectPoi", onCreated = "")
public class CollectPoiInfo {

    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "linkId")
    private String linkId;//关联id, 也就是兴趣点的id
    @Column(name = "poiTypeId")
    private String poiTypeId; //兴趣点分类id
    @Column(name = "name")
    private String name;//名称
    @Column(name = "createTimeStamp")
    private Long createTimeStamp;//创建时间搓


    public CollectPoiInfo() {
    }

    /**
     * 新增收藏时候用的构造函数
     *
     * @param linkId
     * @param name
     */
    public CollectPoiInfo(String linkId, String name, String poiTypeId) {
        this.linkId = linkId;
        this.name = name;
        this.poiTypeId = poiTypeId;
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

    public String getPoiTypeId() {
        return poiTypeId;
    }

    public void setPoiTypeId(String poiTypeId) {
        this.poiTypeId = poiTypeId;
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
