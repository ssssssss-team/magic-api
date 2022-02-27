package org.ssssssss.magicapi.core.model;

import org.ssssssss.magicapi.core.config.WebSocketSessionManager;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.functions.ObjectConvertExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ssssssss.magicapi.core.config.MessageType.BREAKPOINT;
import static org.ssssssss.magicapi.core.config.Constants.*;

public class DebugRequest {

	private HttpServletRequest request;

	private DebugRequest(HttpServletRequest request) {
		this.request = request;
	}

	public static DebugRequest create(HttpServletRequest request){
		return new DebugRequest(request);
	}

	/**
	 * 获得断点
	 */
	public List<Integer> getRequestedBreakpoints() {
		String breakpoints = request.getHeader(HEADER_REQUEST_BREAKPOINTS);
		if (breakpoints != null) {
			return Arrays.stream(breakpoints.split(","))
					.map(val -> ObjectConvertExtension.asInt(val, -1))
					.filter(it -> it > 0)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * 获取测试scriptId
	 */
	public String getRequestedScriptId() {
		return request.getHeader(HEADER_REQUEST_SCRIPT_ID);
	}

	/**
	 * 获取测试clientId
	 */
	public String getRequestedClientId() {
		return request.getHeader(HEADER_REQUEST_CLIENT_ID);
	}

	public MagicScriptDebugContext createMagicScriptContext(int debugTimeout){
		MagicScriptDebugContext debugContext = new MagicScriptDebugContext(getRequestedBreakpoints());
		String scriptId = getRequestedScriptId();
		String clientId = getRequestedClientId();
		debugContext.setTimeout(debugTimeout);
		debugContext.setId(scriptId);
		debugContext.setCallback(variables -> {
			List<Map<String, Object>> varList = (List<Map<String, Object>>) variables.get("variables");
			varList.stream().filter(it -> it.containsKey("value")).forEach(variable -> {
				variable.put("value", JsonUtils.toJsonStringWithoutLog(variable.get("value")));
			});
			WebSocketSessionManager.sendByClientId(clientId, BREAKPOINT, scriptId, variables);
		});
		return debugContext;
	}
}
