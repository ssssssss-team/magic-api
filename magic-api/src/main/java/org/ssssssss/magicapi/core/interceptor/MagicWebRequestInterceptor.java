package org.ssssssss.magicapi.core.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.MagicCorsFilter;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.exception.MagicLoginException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * /magic/web相关接口的拦截器
 *
 * @author mxd
 */
public class MagicWebRequestInterceptor implements HandlerInterceptor {

	private final MagicCorsFilter magicCorsFilter;

	private final AuthorizationInterceptor authorizationInterceptor;

	public MagicWebRequestInterceptor(MagicCorsFilter magicCorsFilter, AuthorizationInterceptor authorizationInterceptor) {
		this.magicCorsFilter = magicCorsFilter;
		this.authorizationInterceptor = authorizationInterceptor;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws MagicLoginException {
		HandlerMethod handlerMethod;
		if (handler instanceof HandlerMethod) {
			handlerMethod = (HandlerMethod) handler;
			handler = handlerMethod.getBean();
			if (handler instanceof MagicController) {
				if (magicCorsFilter != null) {
					magicCorsFilter.process(request, response);
				}
				Valid valid = handlerMethod.getMethodAnnotation(Valid.class);
				boolean requiredLogin = authorizationInterceptor.requireLogin();
				boolean validRequiredLogin = (valid == null || valid.requireLogin());
				if (requiredLogin && validRequiredLogin) {
					request.setAttribute(Constants.ATTRIBUTE_MAGIC_USER, authorizationInterceptor.getUserByToken(request.getHeader(Constants.MAGIC_TOKEN_HEADER)));
				}
				((MagicController) handler).doValid(request, valid);
			}
		}
		return true;
	}
}
