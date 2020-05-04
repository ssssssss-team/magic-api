package com.ssssssss.session;

import java.util.ArrayList;
import java.util.List;

public class Statement {

    /**
     * ID
     */
    private String id;

    /**
     * 请求路径
     */
    private String requestMapping;

    /**
     * 请求方法
     */
    private String requestMethod;

    private List<String> validates = new ArrayList<>();

    /**
     * XMLStatement对象
     */
    private XMLStatement xmlStatement;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestMapping() {
        return requestMapping;
    }

    public void setRequestMapping(String requestMapping) {
        this.requestMapping = requestMapping;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public XMLStatement getXmlStatement() {
        return xmlStatement;
    }

    public void setXmlStatement(XMLStatement xmlStatement) {
        this.xmlStatement = xmlStatement;
    }

    public List<String> getValidates() {
        return validates;
    }

    public void addValidate(String id) {
        this.validates.add(id);
    }
}
