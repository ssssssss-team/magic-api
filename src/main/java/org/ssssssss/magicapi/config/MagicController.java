package org.ssssssss.magicapi.config;

import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.utils.MD5Utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class MagicController {


	final String HEADER_REQUEST_SESSION = "Magic-Request-Session";

	final String HEADER_REQUEST_BREAKPOINTS = "Magic-Request-Breakpoints";

	final String HEADER_REQUEST_CONTINUE = "Magic-Request-Continue";

	final String HEADER_REQUEST_STEP_INTO = "Magic-Request-Step-Into";

	final String HEADER_RESPONSE_WITH_MAGIC_API = "Response-With-Magic-API";

	MagicConfiguration configuration;

	public MagicController(MagicConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * 判断是否有权限访问按钮
	 */
	boolean allowVisit(HttpServletRequest request, RequestInterceptor.Authorization authorization) {
		if (authorization == null) {
			if (configuration.getUsername()!= null && configuration.getUsername() != null) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (configuration.getTokenKey().equals(cookie.getName())) {
							return cookie.getValue().equals(MD5Utils.encrypt(String.format("%s||%s", configuration.getUsername(), configuration.getPassword())));
						}
					}
				}
				return false;
			}
			return true;
		}
		for (RequestInterceptor requestInterceptor : configuration.getRequestInterceptors()) {
			if (!requestInterceptor.allowVisit(request, authorization)) {
				return false;
			}
		}
		return true;
	}

}
