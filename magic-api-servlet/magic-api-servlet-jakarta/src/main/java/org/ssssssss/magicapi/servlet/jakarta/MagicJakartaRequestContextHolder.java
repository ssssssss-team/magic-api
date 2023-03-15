package org.ssssssss.magicapi.servlet.jakarta;

import org.springframework.web.multipart.MultipartResolver;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletResponse;
import org.ssssssss.magicapi.core.servlet.MagicRequestContextHolder;

public class MagicJakartaRequestContextHolder implements MagicRequestContextHolder {

	private final MultipartResolver multipartResolver;

	public MagicJakartaRequestContextHolder(MultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}

	@Override
	public MagicHttpServletRequest getRequest() {
		return convert(servletRequestAttributes -> new MagicJakartaHttpServletRequest(servletRequestAttributes.getRequest(), multipartResolver));
	}

	@Override
	public MagicHttpServletResponse getResponse() {
		return convert(servletRequestAttributes -> new MagicJakartaHttpServletResponse(servletRequestAttributes.getResponse()));
	}
}
