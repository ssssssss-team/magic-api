package org.ssssssss.session;

import org.ssssssss.enums.SqlMode;
import org.ssssssss.scripts.SqlNode;
import org.w3c.dom.Node;

public class SqlStatement extends Statement {

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

    /**
     * 数据源名称
     */
    private String dataSourceName;

    /**
     * selectKey节点
     */
    private Node selectKey;

    /**
     * selectKey转SqlNode
     */
    private SqlNode selectKeySqlNode;

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

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Node getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(Node selectKey) {
        this.selectKey = selectKey;
    }

    public SqlNode getSelectKeySqlNode() {
        return selectKeySqlNode;
    }

    public void setSelectKeySqlNode(SqlNode selectKeySqlNode) {
        this.selectKeySqlNode = selectKeySqlNode;
    }
}
