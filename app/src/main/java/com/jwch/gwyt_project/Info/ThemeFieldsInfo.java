package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 专题图字段表--已匹配
 */
@Table(name = "ThemeFields")
public class ThemeFieldsInfo {
    @Column(name = "Id")
    private String id;
    @Column(name = "ThemeId")
    private String themeId;
    @Column(name = "FieldName")
    private String fieldName;
    @Column(name = "DispName")
    private String dispName;
    @Column(name = "FieldType")
    private String fieldType;
    @Column(name = "ClientVisible")
    private int clientVisible;
    @Column(name = "USN")
    private int usn;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDispName() {
        return dispName;
    }

    public void setDispName(String dispName) {
        this.dispName = dispName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public int getClientVisible() {
        return clientVisible;
    }

    public void setClientVisible(int clientVisible) {
        this.clientVisible = clientVisible;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }
}
