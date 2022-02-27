package org.ssssssss.magicapi.core.context;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * Cookie Context 用于脚本中获取cookie信息
 *
 * @author mxd
 */
public class CookieContext extends HashMap<String, String> {

	private final Cookie[] cookies;

	public CookieContext(HttpServletRequest request) {
		this.cookies = request.getCookies();
	}

	@Override
	public String get(Object key) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase("" + key)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
