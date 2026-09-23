package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.List;

/**
 * 数据类型表--已匹配
 */
@Table(name = "CatType")
public class CatTypeInfo {
    @Column(name = "Id")
    private String id;
    @Column(name = "TypeName")
    private String typeName;
    @Column(name = "ParentId")
    private String parentId;
    @Column(name = "AreaCode")
    private int areaCode;
    @Column(name = "SortCode")
    private int sortCode;
    @Column(name = "USN")
    private int usn;

    //下一级分类
    private List<CatTypeInfo> nextList;
    private int levelIndex;// 在第几个层级
    private boolean hasNext;

    public int getLevelIndex() {
        return levelIndex;
    }

    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(int areaCode) {
        this.areaCode = areaCode;
    }

    public int getSortCode() {
        return sortCode;
    }

    public void setSortCode(int sortCode) {
        this.sortCode = sortCode;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public List<CatTypeInfo> getNextList() {
        return nextList;
    }

    public void setNextList(List<CatTypeInfo> nextList) {
        this.nextList = nextList;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
