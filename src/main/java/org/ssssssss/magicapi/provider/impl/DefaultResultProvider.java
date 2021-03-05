package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.ResultProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultResultProvider implements ResultProvider {

	@Override
	public Object buildResult(ApiInfo apiInfo, HttpServletRequest request, HttpServletResponse response, int code, String message, Object data, long requestTime) {
		return new JsonBean<>(code, message, data, (int) (System.currentTimeMillis() - requestTime));
	}
}
