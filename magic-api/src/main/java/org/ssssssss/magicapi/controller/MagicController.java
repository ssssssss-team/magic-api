package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.model.*;

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

	/**
	 * 判断是否有权限访问接口
	 */
	boolean allowVisit(HttpServletRequest request, Authorization authorization, ApiInfo apiInfo) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization, apiInfo);
	}

	/**
	 * 判断是否有权限访问函数
	 */
	boolean allowVisit(HttpServletRequest request, Authorization authorization, FunctionInfo functionInfo) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization, functionInfo);
	}

	/**
	 * 判断是否有权限访问分组
	 */
	boolean allowVisit(HttpServletRequest request, Authorization authorization, Group group) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization, group);
	}

	/**
	 * 判断是否有权限访问分组
	 */
	boolean allowVisit(HttpServletRequest request, Authorization authorization, DataSourceInfo dataSourceInfo) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization, dataSourceInfo);
	}

	@ExceptionHandler(MagicLoginException.class)
	@ResponseBody
	public JsonBean<Void> invalidLogin(MagicLoginException exception) {
		return new JsonBean<>(-1, exception.getMessage());
	}
}
