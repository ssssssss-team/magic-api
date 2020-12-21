package org.ssssssss.magicapi.logging;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface MagicLoggerContext {

	String LOGGER_NAME = "MagicAPI";

	Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

	ThreadLocal<String> SESSION = new InheritableThreadLocal<>();

	/**
	 * 创建sseEmitter推送
	 * @param sessionId	会话id
	 */
	static SseEmitter createEmitter(String sessionId){
		SseEmitter sseEmitter = new SseEmitter(0L);
		emitterMap.put(sessionId,sseEmitter);
		return sseEmitter;
	}

	/**
	 * 删除会话
	 * @param sessionId	会话id
	 */
	static void remove(String sessionId){
		SseEmitter sseEmitter = emitterMap.remove(sessionId);
		SESSION.remove();
		if(sseEmitter != null){
			try {
				sseEmitter.send(SseEmitter.event().data(sessionId).name("close"));
			} catch (IOException ignored) {
			}
		}
	}


	/**
	 * 打印日志
	 * @param logInfo 日志信息
	 */
	static void println(LogInfo logInfo){
		// 获取SessionId
		MagicScriptContext context = MagicScriptContext.get();
		String sessionId;
		if(context instanceof MagicScriptDebugContext){
			sessionId = ((MagicScriptDebugContext) context).getId();
		}else{
			sessionId = SESSION.get();
		}
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

	/**
	 * 生成appender
	 */
	void generateAppender();
}
