package org.ssssssss.magicapi.servlet.jakarta;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ssssssss.magicapi.core.config.MagicCorsFilter;

import java.io.IOException;

public class MagicJakartaCorsFilter extends MagicCorsFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.process(new MagicJakartaHttpServletRequest((HttpServletRequest) request, null), new MagicJakartaHttpServletResponse((HttpServletResponse) response));
		chain.doFilter(request, response);
	}
}
