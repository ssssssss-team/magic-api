package org.ssssssss.session;

import org.ssssssss.enums.SqlMode;
import org.ssssssss.scripts.SqlNode;

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

    private String dataSourceName;

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
}
