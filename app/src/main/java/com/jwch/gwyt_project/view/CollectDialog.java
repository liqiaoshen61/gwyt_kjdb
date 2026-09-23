package com.jwch.gwyt_project.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Point;
import com.hjq.toast.ToastUtils;
import com.jameni.allutillib.common.CommonUtil;
import com.jameni.allutillib.common.SoftKey;
import com.jameni.allutillib.common.ToastUtil;
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog;
import com.jwch.gwyt_project.Info.CollecPatchInfo;
import com.jwch.gwyt_project.Info.CollectPoiInfo;
import com.jwch.gwyt_project.R;
import com.jwch.gwyt_project.i.ActionListener;
import com.jwch.gwyt_project.model.DataEvent;
import com.jwch.gwyt_project.db.DbUtil;
import com.jwch.gwyt_project.util.OperationLogger;


import org.greenrobot.eventbus.EventBus;

/**
 * 收藏对话框
 */
public class CollectDialog extends JameniBaseDialog implements View.OnClickListener {

    private String strName, linkId, poiTypeId, themeId, themeName;//收藏名称，关联id,兴趣点分类id，专题图id,专题图名字
    private int saveType;//保存兴趣点0  保存图斑 1

    private ActionListener listener;
    private EditText etName;
    private TextView tvCancle, tvOk;

    private ImageView imgClose;

    Point  centerPoint;

    public CollectDialog(Context context, ActionListener listener) {
        super(context, true);
        this.listener = listener;
    }

    public CollectDialog(Context context) {
        super(context, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View layout = LayoutInflater.from(context).inflate(R.layout.view_collect_dialog, null);
        setContentView(layout);
        etName = layout.findViewById(R.id.etName);
        imgClose = layout.findViewById(R.id.imgClose);
        tvOk = layout.findViewById(R.id.tvOk);
        getWindow().setLayout(context.getResources().getDimensionPixelOffset(R.dimen.nSize500), ViewGroup.LayoutParams.WRAP_CONTENT);
        imgClose.setOnClickListener(this);
        tvOk.setOnClickListener(this);
        setCanceledOnTouchOutside(false);
        etName.setText(CommonUtil.getSelfValue(strName));
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.imgClose) {
            //取消
            etName.setText("");
            SoftKey.closeSoftKeyboard(etName, context);
            dismiss();
        } else if (v.getId() == R.id.tvOk) {
            //确定
            if (!CommonUtil.isNotEmpty(getName())) {
                CommonUtil.tip(context, "名称不能为空");
                return;
            }

            if (checkKeyword(getName())) {
                CommonUtil.tip(context, "该名称已经存在");
                return;
            }

            //插入数据库
            if (saveType == 0) {
                CollectPoiInfo data = new CollectPoiInfo(linkId, getName(), poiTypeId);
                DbUtil.Companion.getDb().saveCollectPoiInfo(data);
                EventBus.getDefault().post(new DataEvent(DataEvent.UPDATE_COLLECTION_DATA, 0));
            } else if (saveType == 1) {
                if(centerPoint == null){
                    CollecPatchInfo data = new CollecPatchInfo(linkId, themeId, themeName, getName());
                    DbUtil.Companion.getDb().saveCollectPatchInfo(data);
                }else {
                    CollecPatchInfo data = new CollecPatchInfo(linkId, themeId, themeName, getName(), centerPoint.toJson());
                    DbUtil.Companion.getDb().saveCollectPatchInfo(data);
                }

                EventBus.getDefault().post(new DataEvent(DataEvent.UPDATE_COLLECTION_DATA, 1));
            }
            ToastUtils.show(context.getResources().getString(R.string.collect_success));
            //关闭弹窗
            imgClose.performClick();

            OperationLogger.INSTANCE.logOperation(context, "收藏图斑："+strName);
        }
    }

    private String getName() {
        return isNull(etName) ? "" : etName.getText().toString();
    }

    //判断是否已经收藏过了
    public boolean checkKeyword(String key) {

        try {
            return DbUtil.Companion.getDb().getPtDb().selector(CollectPoiInfo.class).where("name", "=", key).count() > 0;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 设置兴趣点数据
     *
     * @param name
     * @param linkId
     * @param poiTypeId
     */
    public void setPoiData(String name, String linkId, String poiTypeId) {
        this.strName = name;
        this.saveType = 0;
        this.poiTypeId = poiTypeId;
        this.linkId = linkId;
        if (!isNull(etName)) {
            etName.setText(CommonUtil.getSelfValue(name));
        }
    }

    /**
     * 设置图斑数据
     *
     * @param name
     * @param linkId
     * @param themeId
     */
    public void setPatchData(String name, String linkId, String themeId, String themeName) {
        this.strName = name;
        this.saveType = 1;
        this.themeId = themeId;
        this.linkId = linkId;
        this.themeName = themeName;
        if (!isNull(etName)) {
            etName.setText(CommonUtil.getSelfValue(name));
        }
    }

    public void setPatchData2(String name, String linkId, String themeId, String themeName, Point centerPoint) {
        this.strName = name;
        this.saveType = 1;
        this.themeId = themeId;
        this.linkId = linkId;
        this.themeName = themeName;
        this.centerPoint = centerPoint;
        if (!isNull(etName)) {
            etName.setText(CommonUtil.getSelfValue(name));
        }
    }
}