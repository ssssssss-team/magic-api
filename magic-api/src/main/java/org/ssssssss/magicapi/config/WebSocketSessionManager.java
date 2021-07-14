package org.ssssssss.magicapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketSessionManager {

	private static Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);

	private static final Map<String, WebSocketSession> SESSION = new ConcurrentHashMap<>();

	public static void add(WebSocketSession session) {
		SESSION.put(session.getId(), session);
	}

	public static void remove(WebSocketSession session) {
		SESSION.remove(session.getId());
	}

	public static void sendBySessionId(String sessionId, MessageType messageType, Object... values) {
		WebSocketSession session = findSession(sessionId);
		if (session != null) {
			StringBuilder builder = new StringBuilder(messageType.name().toLowerCase());
			if (values != null) {
				for (int i = 0, len = values.length; i < len; i++) {
					builder.append(",");
					Object value = values[i];
					if (i + 1 < len || value instanceof CharSequence || value instanceof Number) {
						builder.append(value);
					} else {
						builder.append(JsonUtils.toJsonString(value));
					}
				}
			}
			try {
				session.sendMessage(new TextMessage(builder.toString()));
			} catch (IOException e) {
				logger.error("发送WebSocket消息失败", e);
			}
		}
	}

	/**
	 * 获取Session中的属性
	 */
	public static <T> T getSessionAttribute(String sessionId, String key) {
		WebSocketSession session = findSession(sessionId);
		if (session != null) {
			return (T) session.getAttributes().get(key);
		}
		return null;
	}

	public static void setSessionAttribute(String sessionId, String key, Object value) {
		WebSocketSession session = findSession(sessionId);
		if (session != null) {
			session.getAttributes().put(key, value);
		}
	}

	private static WebSocketSession findSession(String sessionId) {
		return SESSION.values().stream().filter(it -> sessionId.equals(it.getAttributes().get(Constants.WS_DEBUG_SESSION_KEY))).findFirst().orElse(null);
	}
}
