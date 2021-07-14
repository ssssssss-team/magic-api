package org.ssssssss.magicapi.logging;

import org.slf4j.MDC;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.config.MessageType;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public interface MagicLoggerContext {

	String LOGGER_NAME = "magic";

	ThreadLocal<String> SESSION = new InheritableThreadLocal<>();

	/**
	 * 打印日志
	 *
	 * @param logInfo 日志信息
	 */
	static void println(LogInfo logInfo) {
		// 获取SessionId
		String sessionId = SESSION.get();
		if (sessionId != null) {
			WebSocketSessionManager.sendBySessionId(sessionId, MessageType.LOG, logInfo);
		}
	}

	/**
	 * 生成appender
	 */
	void generateAppender();
}
