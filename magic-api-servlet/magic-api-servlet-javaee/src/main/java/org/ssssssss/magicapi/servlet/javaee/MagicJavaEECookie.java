package org.ssssssss.magicapi.servlet.javaee;

import org.ssssssss.magicapi.core.servlet.MagicCookie;

import javax.servlet.http.Cookie;

public class MagicJavaEECookie implements MagicCookie {

	private final Cookie cookie;

	public MagicJavaEECookie(Cookie cookie) {
		this.cookie = cookie;
	}

	@Override
	public String getName() {
		return cookie.getName();
	}

	@Override
	public String getValue() {
		return cookie.getValue();
	}

	public Cookie getOriginCookie() {
		return cookie;
	}
}
