package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.WebSocketSession;
import org.ssssssss.magicapi.config.Message;
import org.ssssssss.magicapi.config.MessageType;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.script.MagicScriptDebugContext;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicDebugHandler {

	/**
	 * 设置会话ID
	 */
	@Message(MessageType.SET_SESSION_ID)
	public void setSessionId(WebSocketSession session, String sessionId) {
		if(StringUtils.isNotBlank(sessionId)){
			session.getAttributes().put(Constants.WS_DEBUG_SESSION_KEY, sessionId);
		}
	}
	/**
	 * 设置断点
	 */
	@Message(MessageType.SET_BREAKPOINT)
	public void setBreakPoint(WebSocketSession session, String breakpoints) {
		if(StringUtils.isNotBlank(breakpoints)){
			MagicScriptDebugContext context = (MagicScriptDebugContext) session.getAttributes().get(Constants.WS_DEBUG_MAGIC_SCRIPT_CONTEXT);
			context.setBreakpoints(Stream.of(breakpoints.split(",")).map(Integer::valueOf).collect(Collectors.toList()));
		}
	}

	/**
	 * 恢复断点
	 */
	@Message(MessageType.RESUME_BREAKPOINT)
	public void resumeBreakpoint(WebSocketSession session, String stepInto) {
		MagicScriptDebugContext context = (MagicScriptDebugContext) session.getAttributes().get(Constants.WS_DEBUG_MAGIC_SCRIPT_CONTEXT);
		context.setStepInto("1".equals(stepInto));
		try {
			context.singal();
		} catch (InterruptedException ignored) {
		}
	}
}
