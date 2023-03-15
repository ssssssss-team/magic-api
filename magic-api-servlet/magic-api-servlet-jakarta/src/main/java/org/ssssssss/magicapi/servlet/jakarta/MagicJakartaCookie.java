package org.ssssssss.magicapi.servlet.jakarta;

import jakarta.servlet.http.Cookie;
import org.ssssssss.magicapi.core.servlet.MagicCookie;

public class MagicJakartaCookie implements MagicCookie {

	private final Cookie cookie;

	public MagicJakartaCookie(Cookie cookie) {
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

	public Cookie getOriginCookie(){
		return cookie;
	}
}
