package org.ssssssss.magicapi.scripts;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.context.RequestContext;

import java.util.List;
import java.util.Objects;

/**
 * 普通SQL节点
 */
public class TextSqlNode extends SqlNode{

    /**
     * SQL
     */
    private String text;

    public TextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public String getSql(RequestContext context) {
        String sql = text;
        if(StringUtils.isNotBlank(text)){
            // 提取#{}表达式
            List<String> expressions = extractParameter(expressionRegx,text);
            for (String expression : expressions) {
                // 执行表达式
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
