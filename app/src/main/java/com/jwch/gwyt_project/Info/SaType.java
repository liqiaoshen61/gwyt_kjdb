package com.jwch.gwyt_project.Info;


public class SaType {

    public enum Mode {
        NORMAL, TOTAL, OTHER, IGNOR , UNCOVER   //UNCOVER 表示在绘制图形区域内 没有重叠的面积
    }

    private String name; //类别
    private int count = 1;
    private float totalArea = 0;

    private String parentName; //父类别
    private String parentParentName; //父父类别

    private float totalAreaPercentage = 0.0F;
    private Mode dataType = Mode.NORMAL;

    private String level = "0";  //默认0 缩进1

    public SaType() {
    }

    public SaType(String name, float area) {
        this.name = name;
        totalArea = area;
    }

    public SaType(String name, float area,float percentage,String level ) {
        this.name = name;
        totalArea = area;
        totalAreaPercentage = percentage;
        this.level = level;
    }

    public SaType(String name, Mode dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public SaType(String name, int count, Mode dataType) {
        this.name = name;
        this.count = count;

        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void addCount() {
        count++;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Mode getDataType() {
        return dataType;
    }

    public void setDataType(Mode dataType) {
        this.dataType = dataType;
    }

    public float getTotalArea() {
        return totalArea;
    }

    public void addTotalArea(float area) {
        totalArea += area;
    }

    public void addTotalPercentage(float percentage) {
        totalAreaPercentage += percentage;
    }

    public void setTotalArea(float area) {
        totalArea = area;
    }


    public float getTotalAreaPercentage() {
        return totalAreaPercentage;
    }

    public void setTotalAreaPercentage(float totalAreaPercentage) {
        this.totalAreaPercentage = totalAreaPercentage;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getParentParentName() {
        return parentParentName;
    }

    public void setParentParentName(String parentParentName) {
        this.parentParentName = parentParentName;
    }
}
