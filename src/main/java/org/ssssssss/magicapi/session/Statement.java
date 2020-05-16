package org.ssssssss.magicapi.session;

import org.ssssssss.magicapi.utils.DomUtils;
import org.w3c.dom.Node;

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

    /**
     * 是否支持requestBody
     */
    private boolean requestBody = false;

    private List<String> validates = new ArrayList<>();

    private Node node;

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

    public boolean isRequestBody() {
        return requestBody;
    }

    public void setRequestBody(boolean requestBody) {
        this.requestBody = requestBody;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String getNodeData(String dataName) {
        return DomUtils.getNodeAttributeValue(node, "data-" + dataName);
    }
}
