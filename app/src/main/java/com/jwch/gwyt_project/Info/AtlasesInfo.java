package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 地图集地图册---已匹配
 */
@Table(name = "Atlases")
public class AtlasesInfo {
    @Column(name = "Id")
    private String id;
    @Column(name = "AtlasName")
    private String atlasName;
    @Column(name = "CatTypeID")
    private int catTypeId;
    @Column(name = "DispDistrict")
    private String dispDistrict;
    @Column(name = "TileFile")
    private String tileFile;
    @Column(name = "MinLevel")
    private int minLevel;
    @Column(name = "MaxLevel")
    private int maxLevel;
    @Column(name = "XMin")
    private double xMin;
    @Column(name = "XMax")
    private double xMax;
    @Column(name = "YMin")
    private double yMin;
    @Column(name = "YMax")
    private double yMax;
    @Column(name = "ThambnailFile")
    private String thambnailFile;
    @Column(name = "Introduction")
    private String introduction;
    @Column(name = "ThumbnailFileId")
    private String thumbnailFileId;
    @Column(name = "TileFileId")
    private String tileFileId;
    @Column(name = "USN")
    private int usn;
    @Column(name = "TileFilePath")
    private String tileFilePath;
    @Column(name = "ThumbnailFilePath")
    private String thumbnailFilePath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAtlasName() {
        return atlasName;
    }

    public void setAtlasName(String atlasName) {
        this.atlasName = atlasName;
    }

    public int getCatTypeId() {
        return catTypeId;
    }

    public void setCatTypeId(int catTypeId) {
        this.catTypeId = catTypeId;
    }

    public String getDispDistrict() {
        return dispDistrict;
    }

    public void setDispDistrict(String dispDistrict) {
        this.dispDistrict = dispDistrict;
    }

    public String getTileFile() {
        return tileFile;
    }

    public void setTileFile(String tileFile) {
        this.tileFile = tileFile;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public String getThambnailFile() {
        return thambnailFile;
    }

    public void setThambnailFile(String thambnailFile) {
        this.thambnailFile = thambnailFile;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getThumbnailFileId() {
        return thumbnailFileId;
    }

    public void setThumbnailFileId(String thumbnailFileId) {
        this.thumbnailFileId = thumbnailFileId;
    }

    public String getTileFileId() {
        return tileFileId;
    }

    public void setTileFileId(String tileFileId) {
        this.tileFileId = tileFileId;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public String getTileFilePath() {
        return tileFilePath;
    }

    public void setTileFilePath(String tileFilePath) {
        this.tileFilePath = tileFilePath;
    }

    public String getThumbnailFilePath() {
        return thumbnailFilePath;
    }

    public void setThumbnailFilePath(String thumbnailFilePath) {
        this.thumbnailFilePath = thumbnailFilePath;
    }
}
