package com.ssssssss.context;

import com.ssssssss.expression.ExpressionEngine;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

public class RequestContext extends HashMap<String,Object> {

    private HttpServletRequest request;

    private List<Object> parameters = new ArrayList<>();

    private ExpressionEngine engine;

    public RequestContext(HttpServletRequest request,ExpressionEngine engine) {
        this.request = request;
        this.engine = engine;
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()){
            String key = parameterNames.nextElement();
            put(key,request.getParameter(key));
        }
        put("header",new HeaderContext(request));
        put("cookie",new CookieContext(request));
        put("session",new SessionContext(request.getSession()));
    }

    /**
     * 获取HttpServletRequest对象
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * 追加SQL参数
     *
     * @param value
     */
    public void addParameter(Object value){
        this.parameters.add(value);
    }

    /**
     * 获取SQL参数
     * @return
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * 执行表达式
     * @param expression    表达式
     */
    public Object evaluate(String expression){
        return engine.executeWrap(expression,this);
    }
}
