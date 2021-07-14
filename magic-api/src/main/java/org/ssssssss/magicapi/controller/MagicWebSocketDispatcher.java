package org.ssssssss.magicapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.ssssssss.magicapi.config.Message;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.reflection.MethodInvoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MagicWebSocketDispatcher extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(MagicWebSocketDispatcher.class);

	private final Map<String, MethodInvoker> handlers = new HashMap<>();

	public MagicWebSocketDispatcher(List<Object> websocketMessageHandlers) {
		websocketMessageHandlers.forEach(websocketMessageHandler ->
				Stream.of(websocketMessageHandler.getClass().getDeclaredMethods())
						.forEach(method -> handlers.put(method.getAnnotation(Message.class).value().name().toLowerCase(), new MethodInvoker(method, websocketMessageHandler)))
		);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		WebSocketSessionManager.add(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		WebSocketSessionManager.remove(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		// messageType[, data][,data]
		String payload = message.getPayload();
		int index = payload.indexOf(",");
		String msgType = index == -1 ? payload : payload.substring(0, index);
		MethodInvoker invoker = handlers.get(msgType);
		if (invoker != null) {
			try {
				Class<?>[] pTypes = invoker.getParameterTypes();
				int pCount = pTypes.length;
				if (pCount == 0) {
					invoker.invoke0(null, null);
				} else {
					Object[] pValues = new Object[pCount];
					for (int i = 0; i < pCount; i++) {
						Class<?> pType = pTypes[i];
						if (pType == WebSocketSession.class) {
							pValues[i] = session;
						} else if (pType == String.class) {
							int subIndex = payload.indexOf(",", index + 1);
							if (subIndex > -1) {
								pValues[i] = payload.substring(index + 1, index = subIndex);
							} else if(index > -1){
								pValues[i] = payload.substring(index + 1);
							}
						} else {
							pValues[i] = JsonUtils.readValue(payload, pType);
						}
					}
					invoker.invoke0(null, null, pValues);
				}
			} catch (Throwable e) {
				logger.error("WebSocket消息处理出错", e);
			}
		}
	}

}
