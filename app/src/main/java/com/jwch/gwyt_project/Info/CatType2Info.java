package com.jwch.gwyt_project.Info;

import android.widget.ImageView;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.List;


@Table(name = "CatType2")
public class CatType2Info {
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
    private List<CatType3Info> catType3InfoList;

    public List<CatType3Info> getCatType3InfoList() {
        return catType3InfoList;
    }

    public void setCatType3InfoList(List<CatType3Info> catType3InfoList) {
        this.catType3InfoList = catType3InfoList;
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
}
