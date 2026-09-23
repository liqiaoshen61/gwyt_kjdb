package com.jwch.gwyt_project.model;

import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.jameni.allutillib.common.CommonUtil;
import com.jwch.gwyt_project.common.Tools;
import com.jwch.gwyt_project.core.Config;


public class ImageLayerItem {

    private int superId;//最外层id
    private int pid; //父id ，第二层id
    private int id; //当前id

    private String name;//展示的名字
    private String localPath;//图层在sd卡的地址
    private ArcGISTiledLayer imgLayer;


    private boolean showReplaceAction;//是否展示替换底图的勾选按钮
    private boolean showAddAction;//是否展示添加底图的勾选按钮

    public boolean selectReplaceAction;//是否选中替换底图的勾选按钮
    public boolean selectAddAction;//是否选中添加底图的勾选按钮

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalPath() {
        if (!CommonUtil.isNotEmpty(localPath)) {
            localPath = Config.EMAPDATA_PATH + Tools.getTpkPath(getName());
        }
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public ArcGISTiledLayer getImgLayer() {

        if (imgLayer == null) {
            imgLayer = new ArcGISTiledLayer(getLocalPath());
            imgLayer.setId(getId() + "");
        }

        return imgLayer;
    }

    public void setImgLayer(ArcGISTiledLayer imgLayer) {
        this.imgLayer = imgLayer;
    }

    public boolean isShowReplaceAction() {
        return showReplaceAction;
    }

    public void setShowReplaceAction(int showReplaceAction) {
        this.showReplaceAction = showReplaceAction == 1;
    }

    public boolean isShowAddAction() {
        return showAddAction;
    }

    public void setShowAddAction(int showAddAction) {
        this.showAddAction = showAddAction == 1;
    }

    public boolean isSelectReplaceAction() {
        return selectReplaceAction;
    }

    public void setSelectReplaceAction(boolean selectReplaceAction) {
        this.selectReplaceAction = selectReplaceAction;
    }

    public boolean isSelectAddAction() {
        return selectAddAction;
    }

    public void setSelectAddAction(boolean selectAddAction) {
        this.selectAddAction = selectAddAction;
    }

    public int getSuperId() {
        return superId;
    }

    public void setSuperId(int superId) {
        this.superId = superId;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
