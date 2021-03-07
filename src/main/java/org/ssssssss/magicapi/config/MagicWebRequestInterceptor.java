package org.ssssssss.magicapi.config;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.ssssssss.magicapi.controller.MagicController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * /magic/web相关接口的拦截器
 */
public class MagicWebRequestInterceptor implements HandlerInterceptor {

	private final MagicCorsFilter magicCorsFilter;

	public MagicWebRequestInterceptor(MagicCorsFilter magicCorsFilter) {
		this.magicCorsFilter = magicCorsFilter;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		HandlerMethod handlerMethod;
		if (handler instanceof HandlerMethod) {
			handlerMethod = (HandlerMethod) handler;
			handler = handlerMethod.getBean();
			if (handler instanceof MagicController) {
				if (magicCorsFilter != null) {
					magicCorsFilter.process(request, response);
				}
				((MagicController) handler).doValid(request, handlerMethod.getMethodAnnotation(Valid.class));
			}
		}
		return true;
	}
}
