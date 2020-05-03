package com.ssssssss.session;

import com.ssssssss.enums.SqlMode;
import com.ssssssss.scripts.SqlNode;

import java.util.ArrayList;
import java.util.List;

public class SqlStatement {

    /**
     * 请求路径
     */
    private String requestMapping;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * SQL模式
     */
    private SqlMode sqlMode;

    /**
     * SQL节点
     */
    private SqlNode sqlNode;

    /**
     * 是否开启分页
     */
    private boolean pagination;

    /**
     * 返回值类型
     */
    private Class<?> returnType;

    private List<String> validates = new ArrayList<>();

    /**
     * XMLStatement对象
     */
    private XMLStatement xmlStatement;

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

    public SqlMode getSqlMode() {
        return sqlMode;
    }

    public void setSqlMode(SqlMode sqlMode) {
        this.sqlMode = sqlMode;
    }

    public SqlNode getSqlNode() {
        return sqlNode;
    }

    public void setSqlNode(SqlNode sqlNode) {
        this.sqlNode = sqlNode;
    }

    public XMLStatement getXmlStatement() {
        return xmlStatement;
    }

    public void setXmlStatement(XMLStatement xmlStatement) {
        this.xmlStatement = xmlStatement;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public boolean isPagination() {
        return pagination;
    }

    public void setPagination(boolean pagination) {
        this.pagination = pagination;
    }

    public List<String> getValidates() {
        return validates;
    }

    public void addValidate(String id) {
        this.validates.add(id);
    }
}
