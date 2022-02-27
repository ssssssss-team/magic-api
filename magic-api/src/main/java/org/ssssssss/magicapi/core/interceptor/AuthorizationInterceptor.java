package org.ssssssss.magicapi.core.interceptor;

import org.ssssssss.magicapi.core.context.MagicUser;
import org.ssssssss.magicapi.core.exception.MagicLoginException;
import org.ssssssss.magicapi.core.model.Group;
import org.ssssssss.magicapi.core.model.MagicEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * UI权限拦截器
 *
 * @author mxd
 */
public interface AuthorizationInterceptor {


	/**
	 * 是否需要登录
	 *
	 * @return true 需要登录， false 不需要登录
	 */
	default boolean requireLogin() {
		return false;
	}

	/**
	 * 根据Token获取User对象
	 *
	 * @param token token值
	 * @return 登录成功后返回MagicUser对象
	 * @throws MagicLoginException 登录失败抛出
	 */
	default MagicUser getUserByToken(String token) throws MagicLoginException {
		return null;
	}

	/**
	 * 根据用户名，密码登录
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @return 登录成功后返回MagicUser对象
	 * @throws MagicLoginException 登录失败抛出
	 */
	default MagicUser login(String username, String password) throws MagicLoginException {
		return null;
	}

	/**
	 * 退出登录
	 *
	 * @param token token值
	 */
	default void logout(String token) {

	}

	/**
	 * 是否拥有页面按钮的权限
	 *
	 * @param magicUser     登录的用户对象
	 * @param request       HttpServletRequest
	 * @param authorization 鉴权方法
	 * @return true 有权限访问， false 无权限访问
	 */
	default boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization) {
		return true;
	}

	/**
	 * 是否拥有对该接口的增删改权限
	 *
	 * @param magicUser     登录的用户对象
	 * @param request       HttpServletRequest
	 * @param authorization 鉴权方法
	 * @param entity        接口、函数、数据源信息
	 * @return true 有权限访问， false 无权限访问
	 */
	default boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, MagicEntity entity) {
		return allowVisit(magicUser, request, authorization);
	}


	/**
	 * 是否拥有对该分组的增删改权限
	 *
	 * @param magicUser     登录的用户对象
	 * @param request       HttpServletRequest
	 * @param authorization 鉴权方法
	 * @param group         分组信息
	 * @return true 有权限访问， false 无权限访问
	 */
	default boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, Group group) {
		return allowVisit(magicUser, request, authorization);
	}

}
