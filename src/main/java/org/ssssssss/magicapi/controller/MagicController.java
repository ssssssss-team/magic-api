package org.ssssssss.magicapi.controller;

import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.utils.MD5Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class MagicController {


	public static final String HEADER_REQUEST_SESSION = "Magic-Request-Session";

	final String HEADER_REQUEST_BREAKPOINTS = "Magic-Request-Breakpoints";

	final String HEADER_REQUEST_CONTINUE = "Magic-Request-Continue";

	final String HEADER_REQUEST_STEP_INTO = "Magic-Request-Step-Into";

	final String HEADER_RESPONSE_WITH_MAGIC_API = "Response-With-Magic-API";

	MagicConfiguration configuration;

	MagicController(MagicConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * 判断是否有权限访问按钮
	 */
	boolean allowVisit(HttpServletRequest request, RequestInterceptor.Authorization authorization) {
		if (authorization == null) {
			if (configuration.getUsername() != null && configuration.getPassword() != null) {
				String headerValue = request.getHeader(configuration.getTokenKey());
				String realValue = MD5Utils.encrypt(String.format("%s||%s", configuration.getUsername(), configuration.getPassword()));
				return Objects.equals(realValue, headerValue);
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
