package org.ssssssss.magicapi.model;

import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MagicConsoleSession {

	private static final Map<String, MagicConsoleSession> cached = new ConcurrentHashMap<>();

	private final String id = UUID.randomUUID().toString();

	private final WebSocketSession webSocketSession;

	private final Map<String, Object> attributes = new HashMap<>();

	public MagicConsoleSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}

	public String getId() {
		return id;
	}

	public WebSocketSession getWebSocketSession() {
		return webSocketSession;
	}

	public boolean writeable() {
		return webSocketSession != null && webSocketSession.isOpen();
	}

	public static MagicConsoleSession from(WebSocketSession session) {
		MagicConsoleSession magicConsoleSession = cached.get(session.getId());
		if (magicConsoleSession == null) {
			magicConsoleSession = new MagicConsoleSession(session);
			cached.put(session.getId(), magicConsoleSession);
		}
		return magicConsoleSession;
	}

	public static void remove(WebSocketSession session) {
		cached.remove(session.getId());
	}

	public Object getAttribute(String key){
		return attributes.get(key);
	}
	public void setAttribute(String key, Object value){
		attributes.put(key, value);
	}
}
