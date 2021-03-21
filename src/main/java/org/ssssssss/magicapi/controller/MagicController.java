package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.JsonCode;
import org.ssssssss.magicapi.model.JsonCodeConstants;
import org.ssssssss.magicapi.utils.MD5Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class MagicController implements JsonCodeConstants {

	private static final Logger logger = LoggerFactory.getLogger(MagicController.class);

	public static final String HEADER_REQUEST_SESSION = "Magic-Request-Session";

	final String HEADER_REQUEST_BREAKPOINTS = "Magic-Request-Breakpoints";

	final String HEADER_REQUEST_CONTINUE = "Magic-Request-Continue";

	final String HEADER_REQUEST_STEP_INTO = "Magic-Request-Step-Into";

	final String HEADER_RESPONSE_WITH_MAGIC_API = "Response-With-Magic-API";

	MagicConfiguration configuration;

	MagicController(MagicConfiguration configuration) {
		this.configuration = configuration;
	}

	public void doValid(HttpServletRequest request, Valid valid) {
		if (valid != null) {
			if (!valid.readonly() && configuration.getWorkspace().readonly()) {
				throw new InvalidArgumentException(IS_READ_ONLY);
			}
			if (valid.authorization() != RequestInterceptor.Authorization.NONE && !allowVisit(request, valid.authorization())) {
				throw new InvalidArgumentException(PERMISSION_INVALID);
			}
		}
	}

	public void notNull(Object value, JsonCode jsonCode) {
		if (value == null) {
			throw new InvalidArgumentException(jsonCode);
		}
	}

	public void isTrue(boolean value, JsonCode jsonCode) {
		if (!value) {
			throw new InvalidArgumentException(jsonCode);
		}
	}

	public void notBlank(String value, JsonCode jsonCode) {
		isTrue(StringUtils.isNotBlank(value), jsonCode);
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
