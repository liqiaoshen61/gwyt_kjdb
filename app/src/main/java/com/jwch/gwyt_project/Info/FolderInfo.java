package com.jwch.gwyt_project.Info;

import com.jameni.allutillib.common.TimeUtil;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 新数据库表
 * 文件夹，表设计支持树形关系
 */
@Table(name = "FolderInfo")
public class FolderInfo {
    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "parentId")
    private int parentId;// parentId 为 0 的时候说明他是第一层级
    @Column(name = "name")
    private String name;//名称
    @Column(name = "createTimeStamp")
    private Long createTimeStamp;//创建数据时间戳

    //空的构造函数是必要的，没有的话，会出现异常
    public FolderInfo() {
    }

    public FolderInfo(int parentId, String name) {
        this.parentId = parentId;
        this.name = name;
        this.createTimeStamp = TimeUtil.getCurrentStamp();
    }

    public FolderInfo(String name) {
        this.name = name;
        this.createTimeStamp = TimeUtil.getCurrentStamp();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
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
}
