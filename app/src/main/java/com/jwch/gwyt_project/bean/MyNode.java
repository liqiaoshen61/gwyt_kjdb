package com.jwch.gwyt_project.bean;


import java.util.ArrayList;
import java.util.List;

public class MyNode<T> {

    private int id;

    private int pId = 0;

    private String name;


    private int level;


    private boolean isExpand = false;

    private int icon;


    private List<MyNode> children = new ArrayList<MyNode>();

    private MyNode parent;
    private T obj;

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public MyNode() {
    }

    public MyNode(T t, int id, int pId, String name) {
        super();
        this.id = id;
        this.pId = pId;
        this.name = name;
        this.obj = t;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public List<MyNode> getChildren() {
        return children;
    }

    public void setChildren(List<MyNode> children) {
        this.children = children;
    }

    public MyNode getParent() {
        return parent;
    }

    public void setParent(MyNode parent) {
        this.parent = parent;
    }

    /**
     * @return
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * @return
     */
    public boolean isParentExpand() {
        if (parent == null)
            return false;
        return parent.isExpand();
    }

    /**
     * @return
     */
    public boolean isLeaf() {
        return children.size() == 0;
    }


    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }

    /**
     * @param isExpand
     */
    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
        if (!isExpand) {

            for (MyNode node : children) {
                node.setExpand(isExpand);
            }
        }
    }

}

