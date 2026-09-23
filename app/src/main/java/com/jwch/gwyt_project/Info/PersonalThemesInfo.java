package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


@Table(name="PersonalThemes")
public class PersonalThemesInfo {
    @Column(name="Id")
    private int id;
    @Column(name="PersonalCatId")
    private int personalCatId;
    @Column(name="ThemeId")
    private int themeId;
    @Column(name="OrderNo")
    private int orderNo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPersonalCatId() {
        return personalCatId;
    }

    public void setPersonalCatId(int personalCatId) {
        this.personalCatId = personalCatId;
    }

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }
}
