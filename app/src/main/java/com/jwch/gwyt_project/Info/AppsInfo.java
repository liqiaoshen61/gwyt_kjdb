package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 应用信息表---数据库表 已匹配
 */
@Table(name = "Apps")
public class AppsInfo {
    @Column(name = "Id")
    private String id;
    @Column(name = "AppCode")
    private String appCode;
    @Column(name = "AppName")
    private String appName;//app名字
    @Column(name = "DistName")
    private String distName;//地区名字
    @Column(name = "DistCode")
    private String distCode;
    @Column(name = "Introduction")
    private String introduction;//介绍
    @Column(name = "DispName")
    private String DispName;//展示名字


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDistName() {
        return distName;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public String getDistCode() {
        return distCode;
    }

    public void setDistCode(String distCode) {
        this.distCode = distCode;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getDispName() {
        return DispName;
    }

    public void setDispName(String dispName) {
        DispName = dispName;
    }
}
