package org.ssssssss.magicapi.core.context;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.ssssssss.magicapi.core.config.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MagicConsoleSession {

	private static final Map<String, MagicConsoleSession> cached = new ConcurrentHashMap<>();

	private String clientId;

	private WebSocketSession webSocketSession;

	private final Map<String, Object> attributes = new HashMap<>();

	private long activateTime = System.currentTimeMillis();

	public MagicConsoleSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}

	public String getClientId() {
		return clientId;
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

	public Map<String, Object> getAttributes(){
		return attributes;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
		setAttribute(Constants.WEBSOCKET_ATTRIBUTE_CLIENT_ID, clientId);
	}

	public long getActivateTime() {
		return activateTime;
	}

	public void setActivateTime(long activateTime) {
		this.activateTime = activateTime;
	}

	public void close(){
		if(this.webSocketSession != null){
			remove(this.webSocketSession);
			try {
				this.webSocketSession.close(CloseStatus.SESSION_NOT_RELIABLE);
			} catch (Exception ignored) {

			}
			this.webSocketSession = null;
		}
	}
}
