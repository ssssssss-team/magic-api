package org.ssssssss.magicapi.modules.mybatis;

import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 对应XML中 <if>
 * @author jmxd
 * @version : 2020-05-18
 */
public class IfSqlNode extends SqlNode {
    /** 判断表达式 */
    private String test;

    public IfSqlNode(String test) {
        this.test = test;
    }

    @Override
    public String getSql(Map<String, Object> paramMap, List<Object> parameters) {
        // 执行表达式
        Object value = ScriptManager.executeExpression(test, paramMap);
        // 判断表达式返回结果是否是true，如果不是则过滤子节点
        if (BooleanLiteral.isTrue(value)) {
            return executeChildren(paramMap, parameters);
        }
        return "";
    }
}