package com.ssssssss.session;

import com.ssssssss.enums.SqlMode;
import com.ssssssss.scripts.SqlNode;

public class SqlStatement {

    private String requestMapping;

    private String requestMethod;

    private SqlMode sqlMode;

    private SqlNode sqlNode;

    private boolean pagination;

    private Class<?> returnType;

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
}
