package com.jwch.gwyt_project.Info;

import android.view.View;
import android.widget.ImageView;

import com.esri.arcgisruntime.mapping.view.Graphic;
import com.jameni.allutillib.common.PrintUtil;
import com.jwch.gwyt_project.bean.TreeNodeId;
import com.jwch.gwyt_project.bean.TreeNodeLabel;
import com.jwch.gwyt_project.bean.TreeNodePid;
import com.jwch.gwyt_project.db.DbUtil;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 收藏标绘表  或 创建文件夹的表
 * 从 2021年6月29日开始弃用
 */
@Table(name = "child_info", onCreated = "")
public class GraphicInfo {

    @Column(name = "id", isId = true, autoGen = true, property = "NOT NULL")
//    @Column(name = "id")
    @TreeNodeId
    private int id;
    @Column(name = "pid")
    @TreeNodePid
    private int pid;// pid值 -1 文件夹  -2是 不在文件目录下的图形
    @Column(name = "title")
    @TreeNodeLabel
    private String title;//展示名称
    @Column(name = "type")
    private String type;//数据类型 null 文件夹  point 点  polyline 线 polygon 面
    @Column(name = "geo")
    private String geo;// geo是 图形的json数据
    @Column(name = "color")
    private int color;//图形的配色
    @Column(name = "centerPoint")
    private String centerPoint;//图形的中心点的 json数据
    @Column(name = "scale")
    private double scale;//保存时的缩放大小
    @Column(name = "size")
    private int size;//线条粗细
    @Column(name = "remark")
    private String remark;//保存备注
    @Column(name = "date")
    private String date; //文本日期
    @Column(name = "picture_source")
    private String pictureSource;//所关联的图片资源

    private Integer showID;
    private ImageView img;
    private boolean isChecked;
    private View view;
    private Graphic graphic;
    private boolean showLngLatinfo;
    private Boolean isOutPut = false;//是否导出
    public static final int FOLDER = -1;
    public static final int GEOMETRY = -2;


    public Graphic getGraphic() {
        return graphic;
    }

    public void setGraphic(Graphic graphic) {
        this.graphic = graphic;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public ImageView getImg() {
        return img;
    }

    public void setImg(ImageView img) {
        this.img = img;
    }

    public Integer getShowID() {
        return showID;
    }

    public void setShowID(Integer showID) {
        this.showID = showID;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
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


    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(String centerPoint) {
        this.centerPoint = centerPoint;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getPictureSource() {
        return pictureSource;
    }

    public void setPictureSource(String pictureSource) {
        this.pictureSource = pictureSource;
    }

    public Boolean getOutPut() {
        return isOutPut;
    }

    public void setOutPut(Boolean outPut) {
        isOutPut = outPut;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public GraphicInfo(int id, int pid, String title, String type, String geo, int color, int size, String centerPoint, double scale, String date, String remark, boolean isChecked, String pictureSource) {
        this.id = id;
        this.pid = pid;
        this.title = title;
        this.type = type;
        this.geo = geo;
        this.color = color;
        this.size = size;
        this.centerPoint = centerPoint;
        this.scale = scale;
        this.date = date;
        this.remark = remark;
        this.isChecked = isChecked;
        this.pictureSource = pictureSource;
    }

    //默认的构造方法必须写出，如果没有，这张表是创建不成功的
    public GraphicInfo() {
    }

    public GraphicInfo(int id) {
        this.id = id;
    }

    public boolean isShowLngLatinfo() {
        return showLngLatinfo;
    }

    public void setShowLngLatinfo(boolean showLngLatinfo) {
        this.showLngLatinfo = showLngLatinfo;
    }


    public int createFolder(String folderName) {
        int result = 0;
        long count = 0;
        try {
            //查询文件夹
            count = DbUtil.Companion.getDb().queryFolderCountByTitle(folderName);

            if (count > 0) {
                result = -1;//文件夹名称已存在
            } else {
                //获取最新的一个GraphicInfo
                GraphicInfo latestInfo = DbUtil.Companion.getDb().queryLastGraphic();
                int id = latestInfo != null ? latestInfo.getId() + 1 : 0;
                setId(id);
                setPid(-1);
                setTitle(folderName);
                DbUtil.Companion.getDb().saveGraphicInfo(this);
                result = 0;
            }

        } catch (Exception e) {
            PrintUtil.printMsg(e.getMessage());
            return -2;
        }

        return result;


    }
}
