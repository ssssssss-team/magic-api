package org.ssssssss.magicapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.ssssssss.magicapi.event.EventAction;
import org.ssssssss.magicapi.model.MagicConsoleSession;
import org.ssssssss.magicapi.model.MagicNotify;
import org.ssssssss.magicapi.model.Pair;
import org.ssssssss.magicapi.provider.MagicNotifyService;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.MagicScriptDebugContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WebSocketSessionManager {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);

	private static final Map<String, MagicConsoleSession> SESSIONS = new ConcurrentHashMap<>();

	private static MagicNotifyService magicNotifyService;

	private static final Map<String, MagicScriptDebugContext> CONTEXTS = new ConcurrentHashMap<>();

	private static String instanceId;

	private static final List<Pair<String, String>> MESSAGE_CACHE = new ArrayList<>(200);

	public static void add(MagicConsoleSession session) {
		SESSIONS.put(session.getClientId(), session);
	}

	static{
		// 1秒1次发送日志
		new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "magic-api-send-log-task")).scheduleAtFixedRate(WebSocketSessionManager::flushLog, 1, 1, TimeUnit.SECONDS);
	}

	public static List<MagicConsoleSession> getSessions() {
		return new ArrayList<>(SESSIONS.values());
	}

	public static void remove(MagicConsoleSession session) {
		if (session.getClientId() != null) {
			remove(session.getClientId());
		}
	}

	public static void remove(String sessionId) {
		SESSIONS.remove(sessionId);
	}

	public static void sendToAll(MessageType messageType, Object... values) {
		String content = buildMessage(messageType, values);
		sendToAll(content);
	}

	private static void sendToAll(String content) {
		SESSIONS.values().stream().filter(MagicConsoleSession::writeable).forEach(session -> sendBySession(session, content));
		sendToMachine(null, content);
	}

	public static void sendLogs(String sessionId, String message) {
		synchronized (MESSAGE_CACHE) {
			MESSAGE_CACHE.add(Pair.of(sessionId, message));
			if (MESSAGE_CACHE.size() >= 100) {
				flushLog();
			}
		}
	}

	public static void flushLog() {
		try {
			Map<String, List<String>> messages;
			synchronized (MESSAGE_CACHE) {
				messages = MESSAGE_CACHE.stream().collect(Collectors.groupingBy(Pair::getFirst, Collectors.mapping(Pair::getSecond, Collectors.toList())));
				MESSAGE_CACHE.clear();
			}
			messages.forEach((clientId, logs) -> {
				if (logs.size() > 1) {
					sendByClientId(clientId, MessageType.LOGS, logs);
				} else {
					sendByClientId(clientId, MessageType.LOG, logs.get(0));
				}
			});
		} catch (Exception e) {
			logger.warn("发生日志失败", e);
		}
	}

	public static void sendByClientId(String clientId, MessageType messageType, Object... values) {
		MagicConsoleSession session = findSession(clientId);
		String content = buildMessage(messageType, values);
		if (session != null && session.writeable()) {
			sendBySession(session, content);
		} else {
			sendToMachine(clientId, content);
		}
	}

	public static void sendToOther(String excludeClientId, MessageType messageType, Object... values) {
		String content = buildMessage(messageType, values);
		SESSIONS.values().stream()
				.filter(MagicConsoleSession::writeable)
				.filter(it -> !it.getClientId().equals(excludeClientId))
				.forEach(session -> sendBySession(session, content));
		sendToMachine(null, content);
	}

	private static void sendToMachine(String clientId, String content) {
		if (magicNotifyService != null) {
			// 通知其他机器去发送消息
			magicNotifyService.sendNotify(new MagicNotify(instanceId, EventAction.WS_S_C, clientId, content));
		}
	}

	public static String buildMessage(MessageType messageType, Object... values) {
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

	public static void sendByClientId(String clientId, String content) {
		if (clientId == null) {
			sendToAll(content);
		} else {
			MagicConsoleSession session = findSession(clientId);
			if (session != null) {
				sendBySession(session, content);
			}
		}
	}

	public static void sendBySession(MagicConsoleSession session, String content) {
		try {
			if (session != null) {
				synchronized (session.getClientId()) {
					session.getWebSocketSession().sendMessage(new TextMessage(content));
				}
			}
		} catch (IOException e) {
			logger.error("发送WebSocket消息失败", e);
		}
	}

	public static MagicConsoleSession findSession(String clientId) {
		return SESSIONS.values().stream()
				.filter(it -> Objects.equals(clientId, it.getClientId()))
				.findFirst()
				.orElse(null);
	}

	public static void setMagicNotifyService(MagicNotifyService magicNotifyService) {
		WebSocketSessionManager.magicNotifyService = magicNotifyService;
	}

	public static void setInstanceId(String instanceId) {
		WebSocketSessionManager.instanceId = instanceId;
	}

	public static void addMagicScriptContext(String sessionAndScriptId, MagicScriptDebugContext context) {
		CONTEXTS.put(sessionAndScriptId, context);
	}

	public static MagicScriptDebugContext findMagicScriptContext(String sessionAndScriptId) {
		return CONTEXTS.get(sessionAndScriptId);
	}

	public static void removeMagicScriptContext(String sessionAndScriptId) {
		CONTEXTS.remove(sessionAndScriptId);
	}

}
