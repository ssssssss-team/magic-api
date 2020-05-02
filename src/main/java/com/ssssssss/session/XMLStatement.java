package com.ssssssss.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * xml文件对应的实体
 */
public class XMLStatement {

    /**
     * 映射URL路径
     */
    private String requestMapping;

    /**
     * 缓存验证节点
     */
    private Map<String, ValidateStatement> validateStatements = new HashMap<>();

    /**
     * xml文件中sql语句，包括select-list/select-one/insert/update/delete
     */
    private List<SqlStatement> sqlStatements = new ArrayList<>();

    public String getRequestMapping() {
        return requestMapping;
    }

    public void setRequestMapping(String requestMapping) {
        this.requestMapping = requestMapping;
    }

    public List<SqlStatement> getSqlStatements() {
        return sqlStatements;
    }

    /**
     * 添加一个SQL节点
     *
     * @param sqlStatements
     */
    public void addSqlStatement(List<SqlStatement> sqlStatements) {
        this.sqlStatements.addAll(sqlStatements);
    }

    /**
     * 添加一个验证节点
     */
    public void addValidateStatement(ValidateStatement validateStatement) {
        this.validateStatements.put(validateStatement.getId(), validateStatement);
    }

    /**
     * 获取验证节点
     */
    public ValidateStatement getValidateStatement(String id) {
        return this.validateStatements.get(id);
    }

    /**
     * 判断是否有验证节点
     */
    public boolean containsValidateStatement(String id) {
        return this.validateStatements.containsKey(id);
    }
}
