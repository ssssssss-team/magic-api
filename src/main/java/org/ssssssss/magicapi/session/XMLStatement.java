package org.ssssssss.magicapi.session;

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
     * xml文件中function，以及sql语句包括select-list/select-one/insert/update/delete
     */
    private List<Statement> statements = new ArrayList<>();

    public String getRequestMapping() {
        return requestMapping;
    }

    public void setRequestMapping(String requestMapping) {
        this.requestMapping = requestMapping;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * 添加statement
     *
     * @param statements
     */
    public void addStatement(List<Statement> statements) {
        this.statements.addAll(statements);
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
