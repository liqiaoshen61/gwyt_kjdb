package com.jwch.gwyt_project.util.mpchart.model;

/**
 * 图表数据模型
 */
public class ChartData {


    private float yValue;//y刻度值
    public String xLable;//x轴刻度名称
    private int barColor;//柱子颜色资源id
    private String title;//头部

    public ChartData(float yValue, String xLable) {
        this.yValue = yValue;
        this.xLable = xLable;
    }

    public ChartData(int yValue, String xLable) {
        this.yValue = yValue;
        this.xLable = xLable;
    }

    public float getyValue() {
        return yValue;
    }

    public void setyValue(float yValue) {
        this.yValue = yValue;
    }

    public String getxLable() {
        return xLable;
    }

    public void setxLable(String xLable) {
        this.xLable = xLable;
    }

    public int getBarColor() {
        return barColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
