package com.ssssssss.session;

import org.w3c.dom.NodeList;

public class ValidateStatement {

    private String id;

    private Integer code;

    private String message;

    private NodeList nodes;

    public ValidateStatement(String id, Integer code, String message, NodeList nodes) {
        this.id = id;
        this.code = code;
        this.message = message;
        this.nodes = nodes;
    }

    public String getId() {
        return id;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public NodeList getNodes() {
        return nodes;
    }
}
