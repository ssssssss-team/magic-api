package com.ssssssss.session;

import java.util.ArrayList;
import java.util.List;

/**
 * xml文件对应的实体
 */
public class XMLStatement {

    /**
     * 映射URL路径
     */
    private String requestMapping;

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

    public void setSqlStatements(List<SqlStatement> sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    public void addSqlStatement(List<SqlStatement> sqlStatements){
        this.sqlStatements.addAll(sqlStatements);
    }
}
