package org.ssssssss.magicapi.servlet.javaee;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.ssssssss.magicapi.core.servlet.MagicCookie;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class MagicJavaEEHttpServletResponse implements MagicHttpServletResponse {

	private final HttpServletResponse response;


	public MagicJavaEEHttpServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	@Override
	public void setHeader(String name, String value) {
		response.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		response.addHeader(name, value);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		response.sendRedirect(location);
	}

	@Override
	public void addCookie(MagicCookie cookie) {
		response.addCookie(cookie.getCookie());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return response.getOutputStream();
	}

	@Override
	public Collection<String> getHeaderNames() {
		return response.getHeaderNames();
	}

	@Override
	public <T> T getResponse() {
		return (T) response;
	}


	public static class ArgumentsResolver implements HandlerMethodArgumentResolver {

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return parameter.getParameterType() == MagicHttpServletResponse.class;
		}

		@Override
		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
			return new MagicJavaEEHttpServletResponse(webRequest.getNativeResponse(HttpServletResponse.class));
		}
	}
}
