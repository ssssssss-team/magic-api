package org.ssssssss.magicapi.session;

import org.ssssssss.magicapi.enums.SqlMode;
import org.ssssssss.magicapi.scripts.SqlNode;
import org.w3c.dom.Node;

import java.util.List;

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

    /**
     * 使用的缓存名称
     */
    private String useCache;

    /**
     * 删除的缓存名称
     */
    private String deleteCache;

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

    public String getUseCache() {
        return useCache;
    }

    public void setUseCache(String useCache) {
        this.useCache = useCache;
    }

    public String getDeleteCache() {
        return deleteCache;
    }

    public void setDeleteCache(String deleteCache) {
        this.deleteCache = deleteCache;
    }

    public ExecuteSqlStatement buildExecuteSqlStatement(String sql, List<Object> parameters){
        ExecuteSqlStatement executeSqlStatement = new ExecuteSqlStatement();
        executeSqlStatement.setSql(sql);
        executeSqlStatement.setParameters(parameters.toArray());
        executeSqlStatement.setId(this.getId());
        executeSqlStatement.setDataSourceName(this.dataSourceName);
        executeSqlStatement.setSqlMode(this.sqlMode);
        executeSqlStatement.setReturnType(this.returnType);
        executeSqlStatement.setSelectKey(this.selectKey);
        executeSqlStatement.setSelectKeySqlNode(this.selectKeySqlNode);
        executeSqlStatement.setUseCache(this.useCache);
        executeSqlStatement.setDeleteCache(this.deleteCache);
        return executeSqlStatement;
    }
}
