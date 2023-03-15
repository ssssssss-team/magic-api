package org.ssssssss.magicapi.servlet.javaee;

import org.springframework.web.multipart.MultipartResolver;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletResponse;
import org.ssssssss.magicapi.core.servlet.MagicRequestContextHolder;

public class MagicJavaEERequestContextHolder implements MagicRequestContextHolder {

	private final MultipartResolver multipartResolver;

	public MagicJavaEERequestContextHolder(MultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}

	@Override
	public MagicHttpServletRequest getRequest() {
		return convert(servletRequestAttributes -> new MagicJavaEEHttpServletRequest(servletRequestAttributes.getRequest(), multipartResolver));
	}

	@Override
	public MagicHttpServletResponse getResponse() {
		return convert(servletRequestAttributes -> new MagicJavaEEHttpServletResponse(servletRequestAttributes.getResponse()));
	}
}
