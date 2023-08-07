package org.ssssssss.magicapi.nebula.model;

import java.util.HashMap;

public class Node {

    private String id;

    private int EdgeSize;

    private HashMap<String, Object> prop = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEdgeSize() {
        return EdgeSize;
    }

    public void setEdgeSize(int edgeSize) {
        EdgeSize = edgeSize;
    }

    public HashMap<String, Object> getProp() {
        return prop;
    }

    public void setProp(HashMap<String, Object> prop) {
        this.prop = prop;
    }
}
