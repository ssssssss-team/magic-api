package com.ssssssss.scripts;

import com.ssssssss.context.RequestContext;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql节点
 */
public abstract class SqlNode {

    /**
     * 提取#{}的正则
     */
    final Pattern expressionRegx = Pattern.compile("#\\{(.*?)\\}");
    /**
     * 提取${}的正则
     */
    final Pattern replaceRegx = Pattern.compile("\\$\\{(.*?)\\}");
    /**
     * 子节点
     */
    List<SqlNode> nodes = new ArrayList<>();

    /**
     * 追加子节点
     */
    public void addChildNode(SqlNode node){
        this.nodes.add(node);
    }

    /**
     * 获取该节点的SQL
     */
    public abstract String getSql(RequestContext context);

    /**
     * 获取子节点SQL
     */
    public String executeChildren(RequestContext context){
        String sql = "";
        for (SqlNode node : nodes) {
            sql += StringUtils.defaultString(node.getSql(context)) + " ";
        }
        return sql;
    }

    /**
     * 根据正则表达式提取参数
     *
     * @param pattern 正则表达式
     * @param sql     SQL
     */
    public List<String> extractParameter(Pattern pattern, String sql) {
        Matcher matcher = pattern.matcher(sql);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group(1));
        }
        return results;
    }
}
