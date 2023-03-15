package org.ssssssss.magicapi.core.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletResponse;

public abstract class MagicCorsFilter {

	public void process(MagicHttpServletRequest request, MagicHttpServletResponse response) {
		if (StringUtils.isNotBlank(Constants.HEADER_REQUEST_CLIENT_ID)) {
			String value = request.getHeader(HttpHeaders.ORIGIN);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, StringUtils.isBlank(value) ? "*" : value);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Constants.CONST_STRING_TRUE);
			value = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
			if (StringUtils.isNotBlank(value)) {
				response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, value);
			}
			value = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, StringUtils.isBlank(value) ? "GET,POST,OPTIONS,PUT,DELETE" : value);
		}
	}
}
