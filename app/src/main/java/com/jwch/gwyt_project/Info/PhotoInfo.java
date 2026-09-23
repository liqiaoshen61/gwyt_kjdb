package com.jwch.gwyt_project.Info;

import com.esri.arcgisruntime.geometry.Point;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


@Table(name = "Photos", onCreated = "")
public class PhotoInfo {
    @Column(name = "Id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "Pid")
    private String pid;//关联的 保存的图形id
    @Column(name = "Remark")
    private String remark;//备注
    @Column(name = "Date")
    private String date;//创建时间
    @Column(name = "Name")
    private String name;//图片名字
    @Column(name = "X")
    private double x;//经度
    @Column(name = "Y")
    private double y;//纬度
    @Column(name = "Type")
    private int type;
    public static int POI = 0;//兴趣点
    public static int SHP = 1;//shp
    public static int PLOT = 2;//标绘
    public static int MEDIA=3;//标绘多媒体，附件 图片 什么的
    public static int LIN;//临时

    //默认的构造方法必须写出，如果没有，这张表是创建不成功的
    public PhotoInfo() {
    }

    public PhotoInfo(String pid, String name, int type, String remark, String date, Point point) {
        this.pid = pid;
        this.name = name;
        this.type = type;
        this.remark = remark;
        this.date = date;
        if (point!=null) {
            this.x = point.getX();
            this.y = point.getY();
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
