package org.ssssssss.magicapi.scripts;

import org.ssssssss.magicapi.context.RequestContext;

import java.util.Objects;

/**
 * 对应XML中 <if>
 */
public class IfSqlNode extends SqlNode{

    /**
     * 判断表达式
     */
    private String test;

    public IfSqlNode(String test) {
        this.test = test;
    }

    @Override
    public String getSql(RequestContext context) {
        // 执行表达式
        Object value = context.evaluate(test);
        // 判断表达式返回结果是否是true，如果不是则过滤子节点
        if(Objects.equals(value,true)){
            return executeChildren(context);
        }
        return "";
    }
}
