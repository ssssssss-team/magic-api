package com.ssssssss.scripts;

import com.ssssssss.context.RequestContext;

import java.util.Objects;

/**
 * 对应XML中 <if>
 */
public class IfSqlNode extends SqlNode{

    private String test;

    public IfSqlNode(String test) {
        this.test = test;
    }

    @Override
    public String getSql(RequestContext context) {
        Object value = context.evaluate(test);
        if(Objects.equals(value,true)){
            return executeChildren(context);
        }
        return "";
    }
}
