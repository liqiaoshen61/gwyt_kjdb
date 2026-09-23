package com.jwch.gwyt_project.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;


public abstract class MyTreeListViewAdapter<T> extends BaseAdapter {

    protected Context mContext;

    public List<MyNode<T>> mNodes;
    protected LayoutInflater mInflater;

    public List<MyNode<T>> mAllNodes;


    private OnTreeNodeClickListener onTreeNodeClickListener;

    public interface OnTreeNodeClickListener<T> {
        void onClick(MyNode<T> node, int position);
    }

    public void setOnTreeNodeClickListener(
            OnTreeNodeClickListener onTreeNodeClickListener) {
        this.onTreeNodeClickListener = onTreeNodeClickListener;
    }

    /**
     * @param mTree
     * @param context
     * @param datas
     * @param defaultExpandLevel
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public MyTreeListViewAdapter(ListView mTree, Context context, List<T> datas, int defaultExpandLevel) throws IllegalArgumentException,
            IllegalAccessException {
        mContext = context;

        mAllNodes = MyTreeHelper.getSortedNodes(datas, defaultExpandLevel);

        mNodes = MyTreeHelper.filterVisibleNode(mAllNodes);
        mInflater = LayoutInflater.from(context);


        mTree.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                expandOrCollapse(position);
                if (onTreeNodeClickListener != null) {
                    onTreeNodeClickListener.onClick(mNodes.get(position), position);
                }
            }

        });

    }

    /**
     * @param position /**
     * @param position
     */
    public void expandOrCollapse(int position) {
        MyNode n = mNodes.get(position);

        if (n != null) {
            if (!n.isLeaf()) {
                n.setExpand(!n.isExpand());
                mNodes = MyTreeHelper.filterVisibleNode(mAllNodes);
                notifyDataSetChanged();
            }
        }
    }


    @Override
    public int getCount() {
        return mNodes.size();
    }

    @Override
    public Object getItem(int position) {
        return mNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyNode node = mNodes.get(position);
        convertView = getConvertView(node, position, convertView, parent);
        convertView.setPadding(node.getLevel() * 30, 15, 3, 15);
        return convertView;
    }

    public abstract View getConvertView(MyNode node, int position,
                                        View convertView, ViewGroup parent);

    public void update(List<T> datas) throws IllegalArgumentException, IllegalAccessException {

        mAllNodes = MyTreeHelper.getSortedNodes(datas, 0);
        mNodes = MyTreeHelper.filterVisibleNode(mAllNodes);
        notifyDataSetChanged();


    }


}
