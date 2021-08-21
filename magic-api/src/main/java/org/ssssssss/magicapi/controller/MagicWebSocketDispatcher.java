package org.ssssssss.magicapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.ssssssss.magicapi.config.Message;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.MagicConsoleSession;
import org.ssssssss.magicapi.model.MagicNotify;
import org.ssssssss.magicapi.provider.MagicNotifyService;
import org.ssssssss.magicapi.utils.Invoker;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.reflection.MethodInvoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.ssssssss.magicapi.model.Constants.EMPTY_OBJECT_ARRAY;

public class MagicWebSocketDispatcher extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(MagicWebSocketDispatcher.class);

	private static final Map<String, Invoker> handlers = new HashMap<>();

	private final String instanceId;

	private final MagicNotifyService magicNotifyService;

	public MagicWebSocketDispatcher(String instanceId, MagicNotifyService magicNotifyService, List<Object> websocketMessageHandlers) {
		this.instanceId = instanceId;
		this.magicNotifyService = magicNotifyService;
		WebSocketSessionManager.setMagicNotifyService(magicNotifyService);
		WebSocketSessionManager.setInstanceId(instanceId);
		websocketMessageHandlers.forEach(websocketMessageHandler ->
				Stream.of(websocketMessageHandler.getClass().getDeclaredMethods())
						.forEach(method -> handlers.put(method.getAnnotation(Message.class).value().name().toLowerCase(), Invoker.from(new MethodInvoker(method, websocketMessageHandler))))
		);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		WebSocketSessionManager.remove(MagicConsoleSession.from(session));
		MagicConsoleSession.remove(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		MagicConsoleSession consoleSession = MagicConsoleSession.from(session);
		Object returnValue = findHandleAndInvoke(consoleSession, message.getPayload());
		// 如果未成功处理消息，则通知其他机器去处理消息
		if (Boolean.FALSE.equals(returnValue)) {
			magicNotifyService.sendNotify(new MagicNotify(instanceId, Constants.NOTIFY_WS_C_S, consoleSession.getId(), message.getPayload()));
		}
	}


	private static Object findHandleAndInvoke(MagicConsoleSession session, String payload) {
		// messageType[, data][,data]
		int index = payload.indexOf(",");
		String msgType = index == -1 ? payload : payload.substring(0, index);
		Invoker invoker = handlers.get(msgType);
		if (invoker != null) {
			Object returnValue;
			try {
				Class<?>[] pTypes = invoker.getParameterTypes();
				int pCount = pTypes.length;
				if (pCount == 0) {
					returnValue = invoker.invoke(null, null, EMPTY_OBJECT_ARRAY);
				} else {
					Object[] pValues = new Object[pCount];
					for (int i = 0; i < pCount; i++) {
						Class<?> pType = pTypes[i];
						if (pType == MagicConsoleSession.class) {
							pValues[i] = session;
						} else if (pType == String.class) {
							int subIndex = payload.indexOf(",", index + 1);
							if (subIndex > -1) {
								pValues[i] = payload.substring(index + 1, index = subIndex);
							} else if (index > -1) {
								pValues[i] = payload.substring(index + 1);
							}
						} else {
							pValues[i] = JsonUtils.readValue(payload, pType);
						}
					}
					returnValue =  invoker.invoke(null, null, pValues);
				}
				return returnValue;
			} catch (Throwable e) {
				logger.error("WebSocket消息处理出错", e);
			}
		}
		return null;
	}

	public static void processMessageReceived(String sessionId, String payload) {
		MagicConsoleSession session = WebSocketSessionManager.findSession(sessionId);
		if (session != null) {
			findHandleAndInvoke(session, payload);
		}
	}
}
