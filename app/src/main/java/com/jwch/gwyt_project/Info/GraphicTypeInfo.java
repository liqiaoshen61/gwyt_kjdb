package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.List;


@Table(name = "GraphicTypeInfo", onCreated = "")
public class GraphicTypeInfo {
    @Column(name = "Id", isId = true, property = "NOT NULL")
    private int id;
    @Column(name = "Name")
    private String name;
    private Boolean isOutPut=false;//是否导出
    private List<GraphicInfo> list;

    public Boolean getOutPut() {
        return isOutPut;
    }

    public void setOutPut(Boolean outPut) {
        isOutPut = outPut;
    }

    public List<GraphicInfo> getList() {
        return list;
    }

    public void setList(List<GraphicInfo> list) {
        this.list = list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GraphicTypeInfo(String name) {
        this.name = name;
    }

    //默认的构造方法必须写出，如果没有，这张表是创建不成功的
    public GraphicTypeInfo() {
    }
}
