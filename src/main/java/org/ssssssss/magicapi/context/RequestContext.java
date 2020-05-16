package org.ssssssss.magicapi.context;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.ssssssss.magicapi.expression.ExpressionEngine;
import org.ssssssss.magicapi.session.Statement;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class RequestContext extends HashMap<String, Object> {

    private HttpServletRequest request;

    private List<Object> parameters = new ArrayList<>();

    private ExpressionEngine engine;

    private Map<String, String> pathVariables;

    private String requestMapping;

    private String requestMethod;

    private Object requestBody;

    private Statement statement;

    public RequestContext(HttpServletRequest request, ExpressionEngine engine) {
        this.request = request;
        this.engine = engine;
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            put(key, request.getParameter(key));
        }
        NativeWebRequest webRequest = new ServletWebRequest(request);
        // 解析requestMapping
        this.requestMapping = (String) webRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        // 解析pathVariable
        this.pathVariables = (Map<String, String>) webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        this.putAll(this.pathVariables);
        // 请求方法
        this.requestMethod = request.getMethod();
        put("header", new HeaderContext(request));
        put("cookie", new CookieContext(request));
        put("session", new SessionContext(request.getSession()));
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
    public void addParameter(Object value) {
        this.parameters.add(value);
    }

    /**
     * 获取SQL参数
     *
     * @return
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     */
    public Object evaluate(String expression) {
        return engine.executeWrap(expression, this);
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
        this.put("body", this.requestBody);
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public String getRequestMapping() {
        return requestMapping;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
