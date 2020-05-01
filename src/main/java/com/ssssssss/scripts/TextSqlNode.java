package com.ssssssss.scripts;

import com.ssssssss.context.RequestContext;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public class TextSqlNode extends SqlNode{

    private String text;

    public TextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public String getSql(RequestContext context) {
        String sql = text;
        if(StringUtils.isNotBlank(text)){
            List<String> expressions = extractParameter(expressionRegx,text);
            for (String expression : expressions) {
                Object val = context.evaluate(expression);
                context.addParameter(val);
                sql = sql.replaceFirst(expressionRegx.pattern(), "?");
            }
            expressions = extractParameter(replaceRegx,text);
            for (String expression : expressions) {
                Object val = context.evaluate(expression);
                sql = sql.replaceFirst(replaceRegx.pattern(), Objects.toString(val,""));
            }
        }
        return sql + executeChildren(context).trim();
    }

}
