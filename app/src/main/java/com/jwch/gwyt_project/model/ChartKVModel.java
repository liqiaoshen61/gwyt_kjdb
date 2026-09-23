package com.jwch.gwyt_project.model;

public class ChartKVModel {

    private String key;
    private String value;
    private int valueInteger;

    public ChartKVModel(String key, int valueInteger) {
        this.key = key;
        this.valueInteger = valueInteger;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getValueInteger() {
        return valueInteger;
    }

    public void setValueInteger(int valueInteger) {
        this.valueInteger = valueInteger;
    }
}
