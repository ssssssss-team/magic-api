package org.ssssssss.magicapi.core.context;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * Session Context 用于脚本中获取Session信息
 *
 * @author mxd
 */
public class SessionContext extends HashMap<String, Object> {

	private final HttpSession session;

	public SessionContext(HttpSession session) {
		this.session = session;
	}

	@Override
	public Object get(Object key) {
		return session != null ? session.getAttribute(key.toString()) : null;
	}

	@Override
	public Object put(String key, Object value) {
		Object oldValue = session.getAttribute(key);
		session.setAttribute(key, value);
		return oldValue;
	}
}
