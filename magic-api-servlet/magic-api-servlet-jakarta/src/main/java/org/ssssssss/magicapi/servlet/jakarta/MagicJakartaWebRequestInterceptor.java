package org.ssssssss.magicapi.servlet.jakarta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.ssssssss.magicapi.core.config.MagicCorsFilter;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.interceptor.MagicWebRequestInterceptor;


public class MagicJakartaWebRequestInterceptor extends MagicWebRequestInterceptor implements HandlerInterceptor {


	public MagicJakartaWebRequestInterceptor(MagicCorsFilter magicCorsFilter, AuthorizationInterceptor authorizationInterceptor) {
		super(magicCorsFilter, authorizationInterceptor);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		super.handle(handler, new MagicJakartaHttpServletRequest(request, null), new MagicJakartaHttpServletResponse(response));
		return true;
	}
}
