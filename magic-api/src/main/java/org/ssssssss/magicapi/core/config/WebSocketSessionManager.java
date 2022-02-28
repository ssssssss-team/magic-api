package org.ssssssss.magicapi.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.ssssssss.magicapi.core.event.EventAction;
import org.ssssssss.magicapi.core.context.MagicConsoleSession;
import org.ssssssss.magicapi.core.model.MagicNotify;
import org.ssssssss.magicapi.core.model.Pair;
import org.ssssssss.magicapi.core.service.MagicNotifyService;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.MagicScriptDebugContext;

import java.util.*;
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

	private static final int CHECK_INTERVAL = 20;

	private static final int KEEPALIVE_TIMEOUT = 60 * 1000;

	private static final List<Pair<String, String>> MESSAGE_CACHE = new ArrayList<>(200);

	public static void add(MagicConsoleSession session) {
		SESSIONS.put(session.getClientId(), session);
	}

	public static MagicConsoleSession getConsoleSession(String clientId) {
		return SESSIONS.get(clientId);
	}

	static {
		// 1秒1次发送日志
		new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "magic-api-send-log-task")).scheduleAtFixedRate(WebSocketSessionManager::flushLog, 1, 1, TimeUnit.SECONDS);
		// 60秒检测一次是否在线
		new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "magic-api-websocket-clean-task")).scheduleAtFixedRate(WebSocketSessionManager::checkSession, CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
	}

	public static Collection<MagicConsoleSession> getSessions() {
		return SESSIONS.values();
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
		getSessions().stream().filter(MagicConsoleSession::writeable).forEach(session -> sendBySession(session, content));
		sendToMachineByClientId(null, content);
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
			messages.forEach((clientId, logs) -> sendByClientId(clientId, logs.size() > 1 ? MessageType.LOGS : MessageType.LOG, logs));
		} catch (Exception e) {
			logger.warn("发送日志失败", e);
		}
	}

	public static void sendByClientId(String clientId, MessageType messageType, Object... values) {
		MagicConsoleSession session = findSession(clientId);
		String content = buildMessage(messageType, values);
		if (session != null && session.writeable()) {
			sendBySession(session, content);
		} else {
			sendToMachineByClientId(clientId, content);
		}
	}

	public static void sendToOther(String excludeClientId, MessageType messageType, Object... values) {
		String content = buildMessage(messageType, values);
		getSessions().stream()
				.filter(MagicConsoleSession::writeable)
				.filter(it -> !it.getClientId().equals(excludeClientId))
				.forEach(session -> sendBySession(session, content));
		sendToMachineByClientId(null, content);
	}

	public static void sendToMachineByClientId(String clientId, String content) {
		if (magicNotifyService != null) {
			// 通知其他机器去发送消息
			magicNotifyService.sendNotify(new MagicNotify(instanceId, EventAction.WS_S_C, clientId, content));
		}
	}

	public static void sendToMachine(MessageType messageType, Object... args) {
		if (magicNotifyService != null) {
			// 通知其他机器去发送消息
			magicNotifyService.sendNotify(new MagicNotify(instanceId, EventAction.WS_S_S, null, buildMessage(messageType, args)));
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
			getSessions().stream().filter(MagicConsoleSession::writeable).forEach(session -> sendBySession(session, content));
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
		} catch (Exception e) {
			logger.warn("发送WebSocket消息失败: {}", e.getMessage());
		}
	}

	public static MagicConsoleSession findSession(String clientId) {
		return getSessions().stream()
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

	private static void checkSession() {
		try {
			long activateTime = System.currentTimeMillis() - KEEPALIVE_TIMEOUT;
			SESSIONS.entrySet().stream()
					.peek(it -> WebSocketSessionManager.sendBySession(it.getValue(), WebSocketSessionManager.buildMessage(MessageType.PING)))
					.filter(it -> it.getValue().getActivateTime() < activateTime)
					.collect(Collectors.toList())
					.forEach(entry -> {
						MagicConsoleSession session = entry.getValue();
						SESSIONS.remove(entry.getKey());
						session.close();
						sendToAll(MessageType.USER_LOGOUT, session.getAttributes());
					});
		} catch (Exception ignored) {
		}
	}

}
