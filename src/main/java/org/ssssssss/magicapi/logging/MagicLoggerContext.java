package org.ssssssss.magicapi.logging;

import org.slf4j.MDC;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface MagicLoggerContext {

	String LOGGER_NAME = "MagicAPI";

	String MAGIC_CONSOLE_SESSION = "MagicConsoleSession";

	Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

	static SseEmitter createEmitter(String sessionId){
		SseEmitter sseEmitter = new SseEmitter(0L);
		emitterMap.put(sessionId,sseEmitter);
		return sseEmitter;
	}

	static void remove(String sessionId){
		SseEmitter sseEmitter = emitterMap.remove(sessionId);
		MDC.remove(MAGIC_CONSOLE_SESSION);
		if(sseEmitter != null){
			try {
				sseEmitter.send(SseEmitter.event().data(sessionId).name("close"));
			} catch (IOException ignored) {
			}
			//sseEmitter.complete();
		}
	}


	default void println(LogInfo logInfo){
		String sessionId = MDC.get(MAGIC_CONSOLE_SESSION);
		if(sessionId != null){
			SseEmitter sseEmitter = emitterMap.get(sessionId);
			if(sseEmitter != null){
				try {
					// 推送日志事件
					sseEmitter.send(SseEmitter.event().data(logInfo).name("log"));
				} catch (IOException ignored) {
				}
			}
		}
	}
	void generateAppender();
}
