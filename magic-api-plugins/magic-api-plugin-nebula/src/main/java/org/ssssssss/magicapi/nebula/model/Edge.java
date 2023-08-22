package org.ssssssss.magicapi.nebula.model;

import java.util.HashMap;

/**
 * 描述node的方向的边
 */
public class Edge {

    /**
     * 起始节点的id
     */
    private String source;

    /**
     * 终止节点的id
     */
    private String target;

    /**
     * 边描述
     */
    private String label;


    private String value;

    private HashMap<String, Object> prop = new HashMap<>();

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public HashMap<String, Object> getProp() {
        return prop;
    }

    public void setProp(HashMap<String, Object> prop) {
        this.prop = prop;
    }
}
