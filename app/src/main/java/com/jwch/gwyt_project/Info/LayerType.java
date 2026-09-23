package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 专题图类别表
 */
@Table(name = "LayerType")
public class LayerType {
    @Column(name = "TypeId")
    private String TypeId;
    @Column(name = "TypeCode")
    private String TypeCode;
    @Column(name = "TypeName")
    private String TypeName;
    @Column(name = "TypeType")
    private String TypeType;
    @Column(name = "ParentId0")
    private String ParentId0;
    @Column(name = "ParentId0Name")
    private String ParentId0Name;
    @Column(name = "ParentId1")
    private String ParentId1;
    @Column(name = "ParentId1Name")
    private String ParentId1Name;
    @Column(name = "AreaCode")
    private String AreaCode;
    @Column(name = "SortCode")
    private String SortCode;
    @Column(name = "USN")
    private int usn;

    public String getTypeId() {
        return TypeId;
    }

    public void setTypeId(String typeId) {
        TypeId = typeId;
    }

    public String getTypeCode() {
        return TypeCode;
    }

    public void setTypeCode(String typeCode) {
        TypeCode = typeCode;
    }

    public String getTypeName() {
        return TypeName;
    }

    public void setTypeName(String typeName) {
        TypeName = typeName;
    }

    public String getTypeType() {
        return TypeType;
    }

    public void setTypeType(String typeType) {
        TypeType = typeType;
    }

    public String getParentId0() {
        return ParentId0;
    }

    public void setParentId0(String parentId0) {
        ParentId0 = parentId0;
    }

    public String getParentId0Name() {
        return ParentId0Name;
    }

    public void setParentId0Name(String parentId0Name) {
        ParentId0Name = parentId0Name;
    }

    public String getParentId1() {
        return ParentId1;
    }

    public void setParentId1(String parentId1) {
        ParentId1 = parentId1;
    }

    public String getParentId1Name() {
        return ParentId1Name;
    }

    public void setParentId1Name(String parentId1Name) {
        ParentId1Name = parentId1Name;
    }

    public String getAreaCode() {
        return AreaCode;
    }

    public void setAreaCode(String areaCode) {
        AreaCode = areaCode;
    }

    public String getSortCode() {
        return SortCode;
    }

    public void setSortCode(String sortCode) {
        SortCode = sortCode;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }



}
