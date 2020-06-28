package org.ssssssss.magicapi.context;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

public class SessionContext extends HashMap<String,Object> {

    private HttpSession session;

    public SessionContext(HttpSession session){
        this.session = session;
    }

    @Override
    public Object get(Object key) {
        return session != null ? session.getAttribute(key.toString()) : null;
    }
}
