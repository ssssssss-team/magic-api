package org.ssssssss.magicapi.context;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class CookieContext extends HashMap<String,String> {

    private Cookie[] cookies;

    public CookieContext(HttpServletRequest request){
        this.cookies = request.getCookies();
    }

    @Override
    public String get(Object key) {
        for (int i = 0; i < cookies.length; i++) {
            if(cookies[i].getName().equalsIgnoreCase("" + key)){
                return cookies[i].getValue();
            }
        }
        return null;
    }
}
