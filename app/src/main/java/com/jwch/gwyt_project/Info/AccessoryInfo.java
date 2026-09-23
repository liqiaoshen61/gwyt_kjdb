package com.jwch.gwyt_project.Info;

import com.jameni.allutillib.common.TimeUtil;
import com.jwch.gwyt_project.core.Config;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 附件表
 */
@Table(name = "AccessoryInfo")
public class AccessoryInfo {
    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "linkId")
    private String linkId;//关联的查询id
    @Column(name = "name")
    private String name;//名称
    @Column(name = "path")
    private String path;//文件路径
    @Column(name = "createTimeStamp")
    private Long createTimeStamp;//创建数据时间戳
    @Column(name = "dataType")
    private int dataType;//标绘附件：0 ， 图斑附件：1， 兴趣点附件：2

    public static final int ACCESSORY_MARKER = 0;//标绘附件
    public static final int ACCESSORY_PATCH = 1;//图斑附件
    public static final int ACCESSORY_POI = 2;//兴趣点附件

    //空的构造函数是必要的，没有的话，会出现异常
    public AccessoryInfo() {
    }

    /**
     * 新增数据用的构造函数
     *
     * @param linkId
     * @param path
     * @param dataType
     */
    public AccessoryInfo(String linkId, String path, int dataType) {
        this.linkId = linkId;
        this.path = path;
        this.dataType = dataType;
        this.createTimeStamp = TimeUtil.getCurrentStamp();
        this.name = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(Long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    private String time;

    public String getTime() {

        if (time == null) {
            time = TimeUtil.getDateToString(createTimeStamp, Config.timeFormat1);
        }

        return time;
    }
}
