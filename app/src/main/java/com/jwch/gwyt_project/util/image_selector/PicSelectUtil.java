package com.jwch.gwyt_project.util.image_selector;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.jameni.allutillib.common.CommonUtil;
import com.jameni.allutillib.common.PrintUtil;
import com.jwch.gwyt_project.core.Config;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PicSelectUtil {

    /**图片来源：拍照*/
    public static final int SOURCE_CAMERA = 0;
    /**图片来源：相册*/
    public static final int SOURCE_ALBUM = 1;

    private Activity mActivity;
    private Fragment mFragment;
    private SelectPicListener listener;
    private int actionType = SOURCE_CAMERA;//默认拍照
    private boolean defCompress = true;
    private boolean hasGifImage = false;//选择图片的时候是否可以选gif图
    private int selectType = -1;//可选择的文件类型

    private Gson gson = new Gson();

    public PicSelectUtil(Activity mActivity, SelectPicListener listener, int fileType) {
        this.mActivity = mActivity;
        switch (fileType) {
            case 0:
                selectType = SelectMimeType.ofAll();
                break;
            case 1:
                selectType = SelectMimeType.ofImage();
                break;
            case 2:
                selectType = SelectMimeType.ofVideo();
                break;
        }

        setListener(listener);
    }

    public PicSelectUtil(Fragment mFragment, SelectPicListener listener) {
        this.mFragment = mFragment;
        setListener(listener);
    }

    public void setListener(SelectPicListener listener) {
        this.listener = listener;
    }

    public void selectImage() {
        selectImage(20);
    }

    public void selectImage(int maxSelectCount) {

        //默认 selectImage 走拍照
        actionType = SOURCE_CAMERA;
        selectImageFromCamera(maxSelectCount);
    }

    /**
     * 从相册选择图片
     */
    public void selectImageFromAlbum(int maxSelectCount) {

        actionType = SOURCE_ALBUM;
        PictureSelector selector = getPictureSelector();
        //PictureSelector 3.0 功能api说明 :
        // https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-3.0-%E5%8A%9F%E8%83%BDapi%E8%AF%B4%E6%98%8E
        if (selector != null) {
            selector.openGallery(selectType) //仅拍照:openCamera  打开相册:openGallery
                    .setImageEngine(GlideEngine.createGlideEngine()) //相册必须设置图片加载引擎
                    .setMaxSelectNum(maxSelectCount)
                    .isWithSelectVideoImage(true) // 图片和视频是否可以同选,只在ofAll模式下有效
                    .isOriginalControl(true) // 是否显示原图选项
                    .setSelectorUIStyle(new PictureSelectorStyle()) // 使用默认样式
                    .forResult(callback);
        }

    }

    private PictureSelector getPictureSelector() {
        PictureSelector selector = null;

        if (mActivity != null) {
            selector = PictureSelector.create(mActivity);
        } else if (mFragment != null) {
            selector = PictureSelector.create(mFragment);
        }

        if (mActivity == null) {
            PrintUtil.printMsg("activity是null 要将对象传入，不然不能选图片");
        }

        if (mFragment == null) {
            PrintUtil.printMsg("fragment 是null 要将对象传入，不然不能选图片");
        }
        return selector;
    }

    public void selectImageFromCamera(String fileName) {

        PictureSelector selector = getPictureSelector();
//        使用系统相册
        if (selector != null) {
            selector.openCamera(selectType)
                    .setOutputCameraDir(getOutPutPath())
                    .setOutputCameraImageFileName(fileName + ".jpeg")
                    .forResult(callback);
        }
    }


    public void selectImageFromCamera() {
        PictureSelector selector = getPictureSelector();
//        使用系统相册
        if (selector != null) {
            selector
                    .openCamera(selectType)
                    .setOutputCameraDir(getOutPutPath())
                    .forResult(callback);
        }
    }

    /**
     * 拍照并指定最大选择数（保持与相册接口签名一致，单选拍照固定为1）
     */
    public void selectImageFromCamera(int maxSelectCount) {
        actionType = SOURCE_CAMERA;
        selectImageFromCamera();
    }

    public String getOutPutPath(){
        File dir = new File(Config.TAKE_PHOTO_PATH);
        String outputPath = "";
        if (!dir.exists() && !dir.mkdirs()) {
        }else {
            outputPath = dir.getAbsolutePath();
        }
        return  outputPath;

    }


    private OnResultCallbackListener<LocalMedia> callback = new OnResultCallbackListener<LocalMedia>() {

        @Override
        public void onResult(ArrayList<LocalMedia> result) {
            PrintUtil.printMsg("选择图片json==" + gson.toJson(result));

            if (listener != null) {
                listener.onSelectPicSuccess(result, actionType);
            }
        }

        @Override
        public void onCancel() {
            PrintUtil.printMsg("取消了选择图片");
            listener.onSelectPicSuccess(null, actionType);
        }
    };


    //单选的时候用的方法
    public String getUploadUrl(List<LocalMedia> resultList) {

        String uploadUrl = "";

        if (CommonUtil.matchList(resultList) && resultList.size() == 1) {
            LocalMedia item = resultList.get(0);

            if (CommonUtil.isNotNull(item)) {

                if (item.isCompressed()) {
                    uploadUrl = CommonUtil.getSelfValue(item.getCompressPath());
                    PrintUtil.printMsg("压缩图片地址：" + uploadUrl);
                } else {
                    uploadUrl = CommonUtil.getSelfValue(item.getRealPath());
                    PrintUtil.printMsg("真实图片地址：" + uploadUrl);
                }
                if (!CommonUtil.isNotEmpty(uploadUrl)) {
                    uploadUrl = CommonUtil.getSelfValue(item.getPath());
                    PrintUtil.printMsg("图片地址：" + uploadUrl);
                }
            }
        }

        return uploadUrl;
    }

    //多选的时候用的方法
    public List<String> getUploadUrls(List<LocalMedia> resultList) {

        List<String> list = new ArrayList<>();

        if (CommonUtil.matchList(resultList)) {

            resultList.forEach(it -> {
                String uploadUrl = "";
                LocalMedia item;

                item = it;

                if (CommonUtil.isNotNull(item)) {

                    if (item.isCompressed()) {
                        uploadUrl = CommonUtil.getSelfValue(item.getCompressPath());
                        PrintUtil.printMsg("压缩图片地址：" + uploadUrl);
                    } else {
                        uploadUrl = CommonUtil.getSelfValue(item.getRealPath());
                        PrintUtil.printMsg("真实图片地址：" + uploadUrl);
                    }
                    if (!CommonUtil.isNotEmpty(uploadUrl)) {
                        uploadUrl = CommonUtil.getSelfValue(item.getPath());
                        PrintUtil.printMsg("图片地址：" + uploadUrl);
                    }
                }
                list.add(uploadUrl);
            });


        }

        return list;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
}
