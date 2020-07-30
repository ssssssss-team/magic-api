package org.ssssssss.magicapi.context;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * Header Context 用于脚本中获取Header信息
 */
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
