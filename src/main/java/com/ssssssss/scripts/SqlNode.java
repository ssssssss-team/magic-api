package com.ssssssss.scripts;

import com.ssssssss.context.RequestContext;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SqlNode {

    List<SqlNode> nodes = new ArrayList<>();

    final Pattern expressionRegx = Pattern.compile("#\\{(.*?)\\}");

    final Pattern replaceRegx = Pattern.compile("\\$\\{(.*?)\\}");

    public void addChildNode(SqlNode node){
        this.nodes.add(node);
    }

    public abstract String getSql(RequestContext context);

    public String executeChildren(RequestContext context){
        String sql = "";
        for (SqlNode node : nodes) {
            sql += StringUtils.defaultString(node.getSql(context)) + " ";
        }
        return sql;
    }

    public List<String> extractParameter(Pattern pattern,String sql) {
        Matcher matcher = pattern.matcher(sql);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group(1));
        }
        return results;
    }
}
