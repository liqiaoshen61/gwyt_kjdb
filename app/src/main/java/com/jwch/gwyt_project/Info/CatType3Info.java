package com.jwch.gwyt_project.Info;

import android.widget.ImageView;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.List;


@Table(name = "CAtType3")
public class CatType3Info {
    @Column(name = "Id")
    private int id;
    @Column(name = "TypeName")
    private String typeName;
    @Column(name = "PTypeID")
    private int pTypeId;
    @Column(name = "PTypeName")
    private String pTypeName;
    @Column(name = "OrderNo")
    private int orderNo;
    private boolean Selected = false;
    private ImageView sel = null;

    private List<ThemesInfo> themesInfoList;

    public List<ThemesInfo> getThemesInfoList() {
        return themesInfoList;
    }

    public void setThemesInfoList(List<ThemesInfo> themesInfoList) {
        this.themesInfoList = themesInfoList;
    }

    public ImageView getSel() {
        return sel;
    }

    public void setSel(ImageView sel) {
        this.sel = sel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getpTypeId() {
        return pTypeId;
    }

    public void setpTypeId(int pTypeId) {
        this.pTypeId = pTypeId;
    }

    public String getpTypeName() {
        return pTypeName;
    }

    public void setpTypeName(String pTypeName) {
        this.pTypeName = pTypeName;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public boolean isSelected() {
        return Selected;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
    }

    @Override
    public String toString() {
        return "CatType3Info{" +
                "id=" + id +
                ", typeName='" + typeName + '\'' +
                ", pTypeId=" + pTypeId +
                ", pTypeName='" + pTypeName + '\'' +
                ", orderNo=" + orderNo +
                ", Selected=" + Selected +
                ", sel=" + sel +
                ", themesInfoList=" + themesInfoList +
                '}';
    }
}
