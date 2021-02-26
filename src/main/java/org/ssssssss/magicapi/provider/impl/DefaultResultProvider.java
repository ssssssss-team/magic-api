package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.ResultProvider;

public class DefaultResultProvider implements ResultProvider {

	@Override
	public Object buildResult(int code, String message, Object data, long requestTime) {
		return new JsonBean<>(code, message, data, (int)(System.currentTimeMillis() - requestTime));
	}
}
