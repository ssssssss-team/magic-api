package com.ssssssss.handler;

import com.ssssssss.expression.DefaultExpressionEngine;
import com.ssssssss.mapping.SqlMappingManager;
import com.ssssssss.model.SqlMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RequestHandler {

    @Autowired
    private SqlMappingManager manager;

    @Autowired
    private JdbcTemplate template;

    private Pattern optionRegx = Pattern.compile("\\[(.*?)\\]");

    private Pattern expressionRegx = Pattern.compile("#\\{(.*?)\\}");

    @ResponseBody
    public Object request(HttpServletRequest request) {
        String mapping = request.getServletPath();
        if(mapping.endsWith("/")){
            mapping = mapping.substring(0,mapping.length() - 1);
        }
        SqlMapping sqlMapping = manager.getSqlMapping(mapping);
        return invoke(sqlMapping,new RequestContext(request));
    }

    private Object invoke(SqlMapping sqlMapping, Map<String,Object> context){
        String sql = sqlMapping.getSql();
        List<String> options = extract(sql, optionRegx);
        DefaultExpressionEngine engine = new DefaultExpressionEngine();
        List<Object> params = new ArrayList<>();
        Map<String,Object> cachedValues = new HashMap<>();
        for (String option : options) {
            List<String> expressions = extract(option, expressionRegx);
            boolean hasNull = false;
            for (String expression : expressions) {
                Object val = engine.execute("${" + expression + "}", context);
                cachedValues.put(expression,val);
                if(val == null){
                    hasNull = true;
                    break;
                }
            }
            sql = sql.replaceFirst(optionRegx.pattern() ,hasNull ? "":"$1");
        }
        List<String> expressions = extract(sql, expressionRegx);
        for (String expression : expressions) {
            Object val = cachedValues.get(expression);
            if(!cachedValues.containsKey(expression)){
                val = engine.execute("${" + expression + "}", context);
            }
            sql = sql.replaceFirst(expressionRegx.pattern(),"?");
            params.add(val);
        }
        System.out.println(sql);
        System.out.println(params);
        if(params.size() == 0){
            return template.queryForList(sql);
        }
        return template.queryForList(sql,params.toArray());
    }

    public List<String> extract(String sql, Pattern pattern){
        Matcher matcher = pattern.matcher(sql);
        List<String> results = new ArrayList<>();
        while(matcher.find()){
            results.add(matcher.group(1));
        }
        return results;
    }

    public static void main(String[] args) {
        String sql = "select * from sys_user where user_id = #{userId} [and user_name = #{userName.substring(2)}] and abc = #{abc}";
        SqlMapping mapping = new SqlMapping(sql,null,null);
        new RequestHandler().invoke(mapping,new HashMap<String,Object>(){
            {
                put("userId","123");
               // put("userName","abcd");
            }
        });
    }
}
