package org.ssssssss.context;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class HeaderContext extends HashMap<String,Object> {

    private HttpServletRequest request;

    public HeaderContext(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Object get(Object key){
        return request.getHeader(key.toString());
    }
}
