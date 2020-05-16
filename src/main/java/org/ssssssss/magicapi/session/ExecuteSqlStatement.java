package org.ssssssss.magicapi.session;

import org.ssssssss.magicapi.enums.SqlMode;
import org.ssssssss.magicapi.scripts.SqlNode;
import org.w3c.dom.Node;

public class ExecuteSqlStatement {

    /**
     * ID
     */
    private String id;

    /**
     * SQL
     */
    private String sql;

    /**
     * SQL参数
     */
    private Object[] parameters;

    /**
     * SQL模式
     */
    private SqlMode sqlMode;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public SqlMode getSqlMode() {
        return sqlMode;
    }

    public void setSqlMode(SqlMode sqlMode) {
        this.sqlMode = sqlMode;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
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
}
