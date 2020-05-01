package com.ssssssss.context;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Objects;

public class SessionContext extends HashMap<String,Object> {

    private HttpSession session;

    public SessionContext(HttpSession session){
        this.session = session;
    }

    @Override
    public Object get(Object key) {
        return session.getAttribute(key.toString());
    }
}
