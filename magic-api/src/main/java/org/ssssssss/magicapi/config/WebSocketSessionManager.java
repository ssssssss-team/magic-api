package org.ssssssss.magicapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.MagicConsoleSession;
import org.ssssssss.magicapi.model.MagicNotify;
import org.ssssssss.magicapi.provider.MagicNotifyService;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.MagicScriptDebugContext;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketSessionManager {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);

	private static final Map<String, MagicConsoleSession> SESSION = new ConcurrentHashMap<>();

	private static MagicNotifyService magicNotifyService;

	private static String instanceId;

	public static void add(MagicConsoleSession session) {
		SESSION.put(session.getId(), session);
	}

	public static void remove(MagicConsoleSession session) {
		if (session.getId() != null) {
			remove(session.getId());
		}
	}

	public static void remove(String sessionId) {
		SESSION.remove(sessionId);
	}

	public static void sendToAll(MessageType messageType, Object... values) {
		String content = buildMessage(messageType, values);
		sendToAll(content);
	}

	private static void sendToAll(String content) {
		SESSION.values().stream().filter(MagicConsoleSession::writeable).forEach(session -> sendBySession(session, content));
		sendToOther(null, content);
	}

	public static void sendBySessionId(String sessionId, MessageType messageType, Object... values) {
		MagicConsoleSession session = findSession(sessionId);
		String content = buildMessage(messageType, values);
		if (session != null && session.writeable()) {
			sendBySession(session, content);
		} else {
			sendToOther(sessionId, content);
		}
	}

	private static void sendToOther(String sessionId, String content){
		if (magicNotifyService != null) {
			// 通知其他机器去发送消息
			magicNotifyService.sendNotify(new MagicNotify(instanceId, Constants.NOTIFY_WS_S_C, sessionId, content));
		}
	}

	private static String buildMessage(MessageType messageType, Object... values) {
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
		return builder.toString();
	}

	public static void sendBySessionId(String sessionId, String content) {
		if (sessionId == null) {
			sendToAll(content);
		} else {
			MagicConsoleSession session = findSession(sessionId);
			if (session != null) {
				sendBySession(session, content);
			}
		}
	}

	public static void sendBySession(MagicConsoleSession session, String content) {
		try {
			session.getWebSocketSession().sendMessage(new TextMessage(content));
		} catch (IOException e) {
			logger.error("发送WebSocket消息失败", e);
		}
	}

	public static MagicConsoleSession findSession(String sessionId) {
		return SESSION.values().stream()
				.filter(it -> Objects.equals(sessionId, it.getSessionId()))
				.findFirst()
				.orElse(null);
	}

	public static void setMagicNotifyService(MagicNotifyService magicNotifyService) {
		WebSocketSessionManager.magicNotifyService = magicNotifyService;
	}

	public static void setInstanceId(String instanceId) {
		WebSocketSessionManager.instanceId = instanceId;
	}

	public static void createSession(String sessionId, MagicScriptDebugContext debugContext) {
		MagicConsoleSession consoleSession = findSession(sessionId);
		if (consoleSession == null) {
			consoleSession = new MagicConsoleSession(sessionId, debugContext);
			SESSION.put(sessionId, consoleSession);
		} else {
			consoleSession.setMagicScriptDebugContext(debugContext);
		}
	}
}
