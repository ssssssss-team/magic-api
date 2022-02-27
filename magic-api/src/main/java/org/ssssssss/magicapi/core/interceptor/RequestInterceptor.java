package org.ssssssss.magicapi.core.interceptor;

import org.ssssssss.magicapi.core.model.ApiInfo;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.script.MagicScriptContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求拦截器
 *
 * @author mxd
 */
public interface RequestInterceptor {

	/**
	 * 请求之前执行
	 *
	 * @param requestEntity 请求对象
	 * @return 当返回对象时，直接将此对象返回到页面，返回null时，继续执行后续操作
	 */
	default Object preHandle(RequestEntity requestEntity) throws Exception {
		return preHandle(requestEntity.getApiInfo(), requestEntity.getMagicScriptContext(), requestEntity.getRequest(), requestEntity.getResponse());
	}

	/**
	 * 请求之前执行
	 *
	 * @param info     接口信息
	 * @param context  脚本上下文
	 * @param request  HttpServletRequest
	 * @param response HttpServletResponse
	 * @return 当返回对象时，直接将此对象返回到页面，返回null时，继续执行后续操作
	 * @throws Exception 处理失败时抛出的异常
	 */
	default Object preHandle(ApiInfo info, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return null;
	}


	/**
	 * 执行完毕之后执行
	 *
	 * @param info        接口信息
	 * @param context     脚本上下文
	 * @param returnValue 即将要返回到页面的值
	 * @param request     HttpServletRequest
	 * @param response    HttpServletResponse
	 * @return 返回到页面的对象, 当返回null时执行后续拦截器，否则直接返回该值，不执行后续拦截器
	 * @throws Exception 处理失败时抛出的异常
	 */
	default Object postHandle(ApiInfo info, MagicScriptContext context, Object returnValue, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return null;
	}

	/**
	 * 执行完毕之后执行
	 *
	 * @param requestEntity 请求对象
	 * @param returnValue   即将要返回到页面的值
	 * @return 返回到页面的对象, 当返回null时执行后续拦截器，否则直接返回该值，不执行后续拦截器
	 */
	default Object postHandle(RequestEntity requestEntity, Object returnValue) throws Exception {
		return postHandle(requestEntity.getApiInfo(), requestEntity.getMagicScriptContext(), returnValue, requestEntity.getRequest(), requestEntity.getResponse());
	}

	/**
	 * 接口执行完毕之后执行
	 *
	 * @param requestEntity 请求对象
	 * @param returnValue   即将要返回到页面的值
	 * @param throwable     异常对象
	 */
	default void afterCompletion(RequestEntity requestEntity, Object returnValue, Throwable throwable) {
		afterCompletion(requestEntity.getApiInfo(), requestEntity.getMagicScriptContext(), returnValue, requestEntity.getRequest(), requestEntity.getResponse(), throwable);
	}

	/**
	 * 接口执行完毕之后执行
	 *
	 * @param info        接口信息
	 * @param context     脚本上下文
	 * @param returnValue 即将要返回到页面的值
	 * @param request     HttpServletRequest
	 * @param response    HttpServletResponse
	 * @param throwable   异常对象
	 */
	default void afterCompletion(ApiInfo info, MagicScriptContext context, Object returnValue, HttpServletRequest request, HttpServletResponse response, Throwable throwable) {

	}

}
