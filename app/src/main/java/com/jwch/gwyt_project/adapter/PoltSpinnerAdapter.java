package com.jwch.gwyt_project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jameni.allutillib.common.CommonUtil;
import com.jwch.gwyt_project.Info.FolderInfo;
import com.jwch.gwyt_project.R;
import com.jwch.gwyt_project.db.DbUtil;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;


public class PoltSpinnerAdapter extends BaseAdapter {
    //    public List<GraphicInfo> list = new ArrayList<>();
    public List<FolderInfo> list = new ArrayList<>();
    private Context ac;

    public PoltSpinnerAdapter(Context ac) {
        this.ac = ac;

        //获取所有文件夹
//            List<GraphicInfo> graphicInfoLists = DbUtil.Companion.getDb().queryGraphicFolder();
        List<FolderInfo> folderLists = DbUtil.Companion.getDb().queryAllFolderList();
        if (CommonUtil.matchList(folderLists)) {
            list.addAll(folderLists);
        }

//        GraphicInfo noFolder = new GraphicInfo();
        FolderInfo noFolder = new FolderInfo("无");
        list.add(0, noFolder);

    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(ac).inflate(R.layout.item_polt, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.setData(position);

        return convertView;
    }

    class ViewHolder {

        @ViewInject(R.id.tv_name)
        public TextView name;

        public ViewHolder(View convertView) {
            x.view().inject(this, convertView);
        }

        public void setData(final int position) {

            name.setText(list.get(position).getName());

        }
    }
}
