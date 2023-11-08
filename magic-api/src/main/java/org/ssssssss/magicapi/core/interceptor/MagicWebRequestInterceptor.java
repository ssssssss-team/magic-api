package org.ssssssss.magicapi.core.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.MagicCorsFilter;
import org.ssssssss.magicapi.core.exception.MagicLoginException;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletResponse;
import org.ssssssss.magicapi.core.web.MagicController;


public abstract class MagicWebRequestInterceptor {

	private final MagicCorsFilter magicCorsFilter;

	private final AuthorizationInterceptor authorizationInterceptor;

	public MagicWebRequestInterceptor(MagicCorsFilter magicCorsFilter, AuthorizationInterceptor authorizationInterceptor) {
		this.magicCorsFilter = magicCorsFilter;
		this.authorizationInterceptor = authorizationInterceptor;
	}

	public void handle(Object handler, MagicHttpServletRequest request, MagicHttpServletResponse response) throws MagicLoginException {
		HandlerMethod handlerMethod;
		if (handler instanceof HandlerMethod) {
			handlerMethod = (HandlerMethod) handler;
			handler = handlerMethod.getBean();
			if (handler instanceof MagicController) {
				if (magicCorsFilter != null) {
					magicCorsFilter.process(request, response);
				}
				Valid valid = handlerMethod.getMethodAnnotation(Valid.class);
				boolean validRequiredLogin = (valid == null || valid.requireLogin());
				if (validRequiredLogin) {
					request.setAttribute(Constants.ATTRIBUTE_MAGIC_USER, authorizationInterceptor.getUserByToken(request.getHeader(Constants.MAGIC_TOKEN_HEADER)));
				}
				((MagicController) handler).doValid(request, valid);
			}
		}
	}
}
