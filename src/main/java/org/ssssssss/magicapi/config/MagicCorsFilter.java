package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MagicCorsFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {

	}

	@Override
	public void destroy() {

	}

	public void process(HttpServletRequest request, HttpServletResponse response) {
		String value = request.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Origin", StringUtils.isBlank(value) ? "*" : value);
		response.setHeader("Access-Control-Allow-Credentials", "true");
		value = request.getHeader("Access-Control-Request-Headers");
		if (StringUtils.isNotBlank(value)) {
			response.setHeader("Access-Control-Allow-Headers", value);
		}
		value = request.getHeader("Access-Control-Request-Method");
		if (StringUtils.isNotBlank(value)) {
			response.setHeader("Access-Control-Allow-Method", value);
		} else {
			response.setHeader("Access-Control-Allow-Method", "GET,POST,OPTIONS,PUT,DELETE");
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		if (StringUtils.isNotBlank(MagicAPIController.HEADER_REQUEST_SESSION)) {
			process(request, (HttpServletResponse) resp);
		}
		chain.doFilter(req, resp);
	}
}
