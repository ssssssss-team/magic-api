package org.ssssssss.magicapi.logging;

import org.ssssssss.magicapi.config.MessageType;
import org.ssssssss.magicapi.config.WebSocketSessionManager;

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
		}else{
			WebSocketSessionManager.sendToAll(MessageType.LOG, logInfo);
		}
	}

	/**
	 * 生成appender
	 */
	void generateAppender();
}
