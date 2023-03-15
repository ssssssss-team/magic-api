package org.ssssssss.magicapi.servlet.javaee;

import org.springframework.web.servlet.HandlerInterceptor;
import org.ssssssss.magicapi.core.config.MagicCorsFilter;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.interceptor.MagicWebRequestInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MagicJavaEEWebRequestInterceptor extends MagicWebRequestInterceptor implements HandlerInterceptor {


	public MagicJavaEEWebRequestInterceptor(MagicCorsFilter magicCorsFilter, AuthorizationInterceptor authorizationInterceptor) {
		super(magicCorsFilter, authorizationInterceptor);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		super.handle(handler, new MagicJavaEEHttpServletRequest(request, null), new MagicJavaEEHttpServletResponse(response));
		return true;
	}
}
