package org.ssssssss.magicapi.servlet.javaee;

import org.ssssssss.magicapi.core.servlet.MagicHttpSession;

import javax.servlet.http.HttpSession;

public class MagicJavaEEHttpSession implements MagicHttpSession {

	private HttpSession session;

	public MagicJavaEEHttpSession(HttpSession session) {
		this.session = session;
	}

	@Override
	public Object getAttribute(String key) {
		return session.getAttribute(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		session.setAttribute(key, value);
	}
}
