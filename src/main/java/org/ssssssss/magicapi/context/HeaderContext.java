package org.ssssssss.magicapi.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Header Context 用于脚本中获取Header信息
 */
public class HeaderContext extends HashMap<String, Object> {

    private final Map<String, Object> headers;

    public HeaderContext(Map<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public Object get(Object key) {
        return headers.get(key);
    }
}
