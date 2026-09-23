package com.jwch.gwyt_project.bean;


import com.jwch.gwyt_project.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class TreeHelper {
    /**
     *
     *
     * @param datas
     * @param defaultExpandLevel
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static <T> List<Node<T>> getSortedNodes(List<T> datas, int defaultExpandLevel) throws IllegalArgumentException,
            IllegalAccessException

    {
        List<Node<T>> result = new ArrayList<Node<T>>();

        List<Node<T>> nodes = convetData2Node(datas);

        List<Node<T>> rootNodes = getRootNodes(nodes);

        for (Node<T> node : rootNodes) {
            addNode(result, node, defaultExpandLevel, 1);
        }
        return result;
    }

    /**
     *
     *
     * @param nodes
     * @return
     */
    public static <T> List<Node<T>> filterVisibleNode(List<Node<T>> nodes) {
        List<Node<T>> result = new ArrayList<Node<T>>();

        for (Node node : nodes) {

            if (node.isRoot() || node.isParentExpand()) {
                setNodeIcon(node);
                result.add(node);
            }
        }
        return result;
    }

    /**
     *
     *
     * @param datas
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private static <T> List<Node<T>> convetData2Node(List<T> datas)
            throws IllegalArgumentException, IllegalAccessException

    {
        List<Node<T>> nodes = new ArrayList<Node<T>>();
        Node<T> node = null;

        for (T t : datas) {
            String id = null;
            String pId = null;
            String label = null;
            Class<? extends Object> clazz = t.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field f : declaredFields) {
                if (f.getAnnotation(TreeNodeId.class) != null) {
                    f.setAccessible(true);
                    id = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodePid.class) != null) {
                    f.setAccessible(true);
                    pId = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeLabel.class) != null) {
                    f.setAccessible(true);
                    label = (String) f.get(t);
                }
                if (id != null && pId != null && label != null) {
                    break;
                }
            }
            node = new Node<T>(t, id, pId, label);
            nodes.add(node);
        }


        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Node m = nodes.get(j);
                if (m.getpId().equals(n.getId())) {
                    n.getChildren().add(m);
                    m.setParent(n);
                } else if (m.getId().equals(n.getpId())) {
                    m.getChildren().add(n);
                    n.setParent(m);
                }
            }
        }


        for (Node n : nodes) {
            setNodeIcon(n);
        }
        return nodes;
    }

    private static <T> List<Node<T>> getRootNodes(List<Node<T>> nodes) {
        List<Node<T>> root = new ArrayList<Node<T>>();
        for (Node<T> node : nodes) {
            if (node.isRoot())
                root.add(node);
        }
        return root;
    }


    private static <T> void addNode(List<Node<T>> nodes, Node node,
                                    int defaultExpandLeval, int currentLevel) {

        nodes.add(node);
        if (defaultExpandLeval >= currentLevel) {
            node.setExpand(true);
        }

        if (node.isLeaf())
            return;
        for (int i = 0; i < node.getChildren().size(); i++) {
            addNode(nodes, (Node) node.getChildren().get(i), defaultExpandLeval,
                    currentLevel + 1);
        }
    }

    /**
     *
     *
     * @param node
     */
    private static void setNodeIcon(Node node) {
        if (node.getChildren().size() > 0 && node.isExpand()) {
            node.setIcon(R.mipmap.tree_ex);
        } else if (node.getChildren().size() > 0 && !node.isExpand()) {
            node.setIcon(R.mipmap.tree_ec);
        } else
            node.setIcon(-1);

    }

}
