package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


/**
 * 对应数据库表
 * 搜索的POI类型 ==已匹配
 */
@Table(name="PoiTypes")
public class PoiTypesInfo {

    @Column(name="Id")
    private String id;
    @Column(name="TypeName")
    private String typeName;//类型名称
    @Column(name="OrderNo")
    private int orderNo;//排序
    @Column(name = "USN")
    private int usn;
    private boolean isSelect =false;


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

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
