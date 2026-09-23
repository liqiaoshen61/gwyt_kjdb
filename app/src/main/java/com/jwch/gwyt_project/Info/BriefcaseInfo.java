package com.jwch.gwyt_project.Info;


/**
 * 公文包
 */
public class BriefcaseInfo {
    private String name;
    private String size;

    public BriefcaseInfo() {

    }

    public BriefcaseInfo(String name, String size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
