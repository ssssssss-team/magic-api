package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.exception.MagicLoginException;

import javax.servlet.http.HttpServletRequest;

public interface AuthorizationInterceptor {


	/**
	 * 是否需要登录
	 */
	default boolean requireLogin() {
		return true;
	}

	/**
	 * 根据Token获取User对象
	 */
	default MagicUser getUserByToken(String token) throws MagicLoginException {
		return null;
	}

	/**
	 * 根据用户名，密码登录
	 *
	 * @param username 用户名
	 * @param password 密码
	 */
	default MagicUser login(String username, String password) throws MagicLoginException {
		return null;
	}

	/**
	 * 退出登录
	 */
	default void logout(String token){

	}

	/**
	 * 是否拥有页面按钮的权限
	 */
	default boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization) {
		return true;
	}
}
