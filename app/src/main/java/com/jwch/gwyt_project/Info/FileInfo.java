package com.jwch.gwyt_project.Info;


import com.jwch.gwyt_project.bean.TreeNodeId;
import com.jwch.gwyt_project.bean.TreeNodeLabel;
import com.jwch.gwyt_project.bean.TreeNodePid;

public class FileInfo {
    @TreeNodeLabel
    private String name;
    @TreeNodeId
    private String path;
    @TreeNodePid
    private String parentPath;

    public FileInfo(String name, String path, String parentPath) {
        this.name = name;
        this.path = path;
        this.parentPath = parentPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
}
