package com.jwch.gwyt_project.bean;

import java.util.ArrayList;
import java.util.List;


public class Node<T> {

    private String id;

    private String pId = "";

    private String name;


    private int level;


    private boolean isExpand = false;

    private int icon;


    private List<Node> children = new ArrayList<Node>();


    private Node parent;
    private T obj;

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public Node() {
    }

    public Node(T t, String id, String pId, String name) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
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

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     *
     *
     * @return
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     *
     *
     * @return
     */
    public boolean isParentExpand() {
        if (parent == null)
            return false;
        return parent.isExpand();
    }

    /**
     *
     *
     * @return
     */
    public boolean isLeaf() {
        return children.size() == 0;
    }


    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }

    /**
     *
     *
     * @param isExpand
     */
    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
        if (!isExpand) {

            for (Node node : children) {
                node.setExpand(isExpand);
            }
        }
    }

}
