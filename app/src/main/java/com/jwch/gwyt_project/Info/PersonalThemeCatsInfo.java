package com.jwch.gwyt_project.Info;

import android.widget.ImageView;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


@Table(name="PersonalThemeCats")
public class PersonalThemeCatsInfo {
    @Column(name="Id")
    private int id;
    @Column(name="CatName")
    private String catName;
    @Column(name="OrderNo")
    private int orderNo;
    private boolean Selected = false;
    private ImageView sel=null;
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

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
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
