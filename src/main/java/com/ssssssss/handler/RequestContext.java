package com.ssssssss.handler;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;

public class RequestContext extends HashMap<String,Object> {

    private HttpServletRequest request;

    public RequestContext(HttpServletRequest request) {
        this.request = request;
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()){
            String key = parameterNames.nextElement();
            put(key,request.getParameter(key));
        }
        put("header",new HeaderContext(request));
    }
}
