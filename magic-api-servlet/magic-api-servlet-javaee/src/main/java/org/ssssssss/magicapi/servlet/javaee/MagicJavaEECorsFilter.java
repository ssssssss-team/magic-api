package org.ssssssss.magicapi.servlet.javaee;

import org.ssssssss.magicapi.core.config.MagicCorsFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MagicJavaEECorsFilter extends MagicCorsFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.process(new MagicJavaEEHttpServletRequest((HttpServletRequest) request, null), new MagicJavaEEHttpServletResponse((HttpServletResponse) response));
		chain.doFilter(request, response);
	}
}
