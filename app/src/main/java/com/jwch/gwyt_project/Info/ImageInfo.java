package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 附件表
 */
@Table(name = "ImageInfo")
public class ImageInfo {
    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "linkId")
    private String linkId;//关联的查询id
    @Column(name = "name")
    private String name;//图片名称（纯名称，不含后缀名）
    @Column(name = "filePath")
    private String filePath;//图片完整路径（含路径，含后缀名）
    @Column(name = "remark")
    private String remark;//备注
    @Column(name = "createTimeStamp")
    private Long createTimeStamp;//创建数据时间戳
    @Column(name = "dataType")
    private int dataType;//标绘图片：0 ， 图斑图片：1， 兴趣点图片：2

    public static final int IMAGE_MARKER = 0;//标绘图片
    public static final int IMAGE_PATCH = 1;//图斑图片
    public static final int IMAGE_POI = 2;//兴趣点图片

    public static final int REVIEW_RECORD = 3;//复核记录图片

    String  city;
    String  county;
    String  town;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    //空的构造函数是必要的，没有的话，会出现异常
    public ImageInfo() {
    }


    /**
     * 新增数据 的构造函数
     *
     * @param linkId
     * @param name
     * @param filePath
     * @param remark
     * @param createTimeStamp
     * @param dataType
     */
    public ImageInfo(String linkId, String name, String filePath, String remark, Long createTimeStamp, int dataType) {
        this.linkId = linkId;
        this.name = name;
        this.filePath = filePath;
        this.remark = remark;
        this.createTimeStamp = createTimeStamp;
        this.dataType = dataType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(Long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
