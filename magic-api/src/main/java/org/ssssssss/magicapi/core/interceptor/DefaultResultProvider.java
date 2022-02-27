package org.ssssssss.magicapi.core.interceptor;

import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.MagicScriptContext;

/**
 * 默认结果封装实现
 *
 * @author mxd
 */
public class DefaultResultProvider implements ResultProvider {

	private final String responseScript;

	public DefaultResultProvider(String responseScript) {
		this.responseScript = responseScript;
	}

	@Override
	public Object buildResult(RequestEntity requestEntity, int code, String message, Object data) {
		long timestamp = System.currentTimeMillis();
		if (this.responseScript != null) {
			MagicScriptContext context = new MagicScriptContext();
			context.setScriptName(requestEntity.getMagicScriptContext().getScriptName());
			context.set("code", code);
			context.set("message", message);
			context.set("data", data);
			context.set("apiInfo", requestEntity.getApiInfo());
			context.set("request", requestEntity.getRequest());
			context.set("response", requestEntity.getResponse());
			context.set("timestamp", timestamp);
			context.set("requestTime", requestEntity.getRequestTime());
			context.set("executeTime", timestamp - requestEntity.getRequestTime());
			return ScriptManager.executeExpression(responseScript, context);
		} else {
			return new JsonBean<>(code, message, data, (int) (timestamp - requestEntity.getRequestTime()));
		}
	}
}
