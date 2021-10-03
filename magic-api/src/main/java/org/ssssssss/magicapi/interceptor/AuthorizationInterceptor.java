package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.DataSourceInfo;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.Group;

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
	 * @param apiInfo       接口信息
	 * @return true 有权限访问， false 无权限访问
	 */
	default boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, ApiInfo apiInfo) {
		return allowVisit(magicUser, request, authorization);
	}

	/**
	 * 是否拥有对该函数的增删改权限
	 *
	 * @param magicUser     登录的用户对象
	 * @param request       HttpServletRequest
	 * @param authorization 鉴权方法
	 * @param functionInfo  函数信息
	 * @return true 有权限访问， false 无权限访问
	 */
	default boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, FunctionInfo functionInfo) {
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

	/**
	 * 是否拥有对该数据源的增删改权限
	 *
	 * @param magicUser      登录的用户对象
	 * @param request        HttpServletRequest
	 * @param authorization  鉴权方法
	 * @param dataSourceInfo 数据源信息
	 * @return true 有权限访问， false 无权限访问
	 */
	default boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, DataSourceInfo dataSourceInfo) {
		return allowVisit(magicUser, request, authorization);
	}
}
