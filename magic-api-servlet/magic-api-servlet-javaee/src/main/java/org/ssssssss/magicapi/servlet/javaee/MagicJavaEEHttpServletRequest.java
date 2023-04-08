package org.ssssssss.magicapi.servlet.javaee;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.ssssssss.magicapi.core.servlet.MagicCookie;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.servlet.MagicHttpSession;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;

public class MagicJavaEEHttpServletRequest implements MagicHttpServletRequest {

	private final HttpServletRequest request;

	private final MultipartResolver multipartResolver;

	public MagicJavaEEHttpServletRequest(HttpServletRequest request, MultipartResolver multipartResolver) {
		this.request = request;
		this.multipartResolver = multipartResolver;
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return request.getHeaders(name);
	}

	@Override
	public String getRequestURI() {
		return request.getRequestURI();
	}

	@Override
	public String getMethod() {
		return request.getMethod();
	}

	@Override
	public void setAttribute(String key, Object value) {
		request.setAttribute(key, value);
	}

	@Override
	public String[] getParameterValues(String name) {
		return request.getParameterValues(name);
	}

	@Override
	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}

	@Override
	public HttpInputMessage getHttpInputMessage() {
		return new ServletServerHttpRequest(request);
	}

	@Override
	public String getContentType() {
		return request.getContentType();
	}

	@Override
	public MagicHttpSession getSession() {
		return new MagicJavaEEHttpSession(request.getSession());
	}

	@Override
	public MagicCookie[] getCookies() {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return new MagicJavaEECookie[0];
		}
		return Arrays.stream(request.getCookies()).map(MagicJavaEECookie::new).toArray(MagicJavaEECookie[]::new);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	@Override
	public boolean isMultipart() {
		return multipartResolver.isMultipart(request);
	}

	@Override
	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	@Override
	public MultipartRequest resolveMultipart() {
		return multipartResolver.resolveMultipart(request);
	}

	@Override
	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	public static class ArgumentsResolver implements HandlerMethodArgumentResolver {

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return parameter.getParameterType() == MagicHttpServletRequest.class;
		}

		@Override
		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
			return new MagicJavaEEHttpServletRequest(webRequest.getNativeRequest(HttpServletRequest.class), null);
		}
	}
}
