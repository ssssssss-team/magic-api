package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.script.MagicScriptContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultResultProvider implements ResultProvider {

	private final String responseScript;

	public DefaultResultProvider() {
		this.responseScript = null;
	}

	public DefaultResultProvider(String responseScript) {
		this.responseScript = responseScript;
	}

	@Override
	public Object buildResult(ApiInfo apiInfo, HttpServletRequest request, HttpServletResponse response, int code, String message, Object data, long requestTime) {
		long timestamp = System.currentTimeMillis();
		if (this.responseScript != null) {
			MagicScriptContext context = new MagicScriptContext();
			context.set("code", code);
			context.set("message", message);
			context.set("data", data);
			context.set("apiInfo", apiInfo);
			context.set("request", request);
			context.set("response", response);
			context.set("timestamp", timestamp);
			context.set("requestTime", requestTime);
			context.set("executeTime", timestamp - requestTime);
			return ScriptManager.executeExpression(responseScript, context);
		} else {
			return new JsonBean<>(code, message, data, (int) (timestamp - requestTime));
		}
	}
}
