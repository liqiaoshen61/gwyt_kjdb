package com.jwch.gwyt_project.Info;


import com.jwch.gwyt_project.bean.TreeNodeId;
import com.jwch.gwyt_project.bean.TreeNodeLabel;
import com.jwch.gwyt_project.bean.TreeNodePid;

public class Item {
    @TreeNodeId
    String id;
    @TreeNodePid
    String pid;
    @TreeNodeLabel
    String name;
    String url;

    public Item(String id, String pid, String name, String url) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
