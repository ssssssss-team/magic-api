package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.config.Message;
import org.ssssssss.magicapi.config.MessageType;
import org.ssssssss.magicapi.model.MagicConsoleSession;
import org.ssssssss.script.MagicScriptDebugContext;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * WebSocket Debug 处理器
 *
 * @author mxd
 */
public class MagicDebugHandler {

	/**
	 * 设置会话ID
	 * 只在本机处理。
	 */
	@Message(MessageType.SET_SESSION_ID)
	public void setSessionId(MagicConsoleSession session, String sessionId) {
		session.setSessionId(sessionId);
	}

	/**
	 * 设置断点
	 * 当本机没有该Session时，通知其他机器处理
	 */
	@Message(MessageType.SET_BREAKPOINT)
	public boolean setBreakPoint(MagicConsoleSession session, String breakpoints) {
		MagicScriptDebugContext context = session.getMagicScriptDebugContext();
		if (context != null) {
			context.setBreakpoints(Stream.of(breakpoints.split(",")).map(Integer::valueOf).collect(Collectors.toList()));
			return true;
		}
		return false;
	}

	/**
	 * 恢复断点
	 * 当本机没有该Session时，通知其他机器处理
	 */
	@Message(MessageType.RESUME_BREAKPOINT)
	public boolean resumeBreakpoint(MagicConsoleSession session, String stepInto, String breakpoints) {
		MagicScriptDebugContext context = session.getMagicScriptDebugContext();
		if (context != null) {
			context.setStepInto("1".equals(stepInto));
			if (StringUtils.isNotBlank(breakpoints)) {
				context.setBreakpoints(Stream.of(breakpoints.split("\\|")).map(Integer::valueOf).collect(Collectors.toList()));
			}else {
				context.setBreakpoints(Collections.emptyList());
			}
			try {
				context.singal();
			} catch (InterruptedException ignored) {
			}
			return true;
		}
		return false;
	}
}
