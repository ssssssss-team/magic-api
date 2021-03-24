package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.JsonCode;
import org.ssssssss.magicapi.model.JsonCodeConstants;

import javax.servlet.http.HttpServletRequest;

public class MagicController implements JsonCodeConstants {

	MagicConfiguration configuration;

	MagicController(MagicConfiguration configuration) {
		this.configuration = configuration;
	}

	public void doValid(HttpServletRequest request, Valid valid) {
		if (valid != null) {
			if (!valid.readonly() && configuration.getWorkspace().readonly()) {
				throw new InvalidArgumentException(IS_READ_ONLY);
			}
			if (valid.authorization() != Authorization.NONE && !allowVisit(request, valid.authorization())) {
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
	boolean allowVisit(HttpServletRequest request, Authorization authorization) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization);
	}

	@ExceptionHandler(MagicLoginException.class)
	@ResponseBody
	public JsonBean<Void> invalidLogin(MagicLoginException exception) {
		return new JsonBean<>(-1, exception.getMessage());
	}
}
