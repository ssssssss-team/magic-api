package org.ssssssss.magicapi.core.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.ssssssss.magicapi.core.annotation.Message;
import org.ssssssss.magicapi.core.config.MessageType;
import org.ssssssss.magicapi.core.config.WebSocketSessionManager;
import org.ssssssss.magicapi.core.event.EventAction;
import org.ssssssss.magicapi.core.context.MagicConsoleSession;
import org.ssssssss.magicapi.core.model.MagicNotify;
import org.ssssssss.magicapi.core.service.MagicNotifyService;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.reflection.MethodInvoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.ssssssss.magicapi.core.config.Constants.EMPTY_OBJECT_ARRAY;

/**
 * WebSocket 分发器
 *
 * @author mxd
 */
public class MagicWebSocketDispatcher extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(MagicWebSocketDispatcher.class);

	private static final Map<String, MethodInvoker> HANDLERS = new HashMap<>();

	private final String instanceId;

	private final MagicNotifyService magicNotifyService;

	public MagicWebSocketDispatcher(String instanceId, MagicNotifyService magicNotifyService, List<Object> websocketMessageHandlers) {
		this.instanceId = instanceId;
		this.magicNotifyService = magicNotifyService;
		WebSocketSessionManager.setMagicNotifyService(magicNotifyService);
		WebSocketSessionManager.setInstanceId(instanceId);
		websocketMessageHandlers.forEach(websocketMessageHandler ->
				Stream.of(websocketMessageHandler.getClass().getDeclaredMethods())
						.filter(it -> it.getAnnotation(Message.class) != null)
						.forEach(method -> HANDLERS.put(method.getAnnotation(Message.class).value().name().toLowerCase(), new MethodInvoker(method, websocketMessageHandler)))
		);
	}

	private static Object findHandleAndInvoke(MagicConsoleSession session, String payload) {
		// messageType[, data][,data]
		int index = payload.indexOf(",");
		String msgType = index == -1 ? payload : payload.substring(0, index);
		MethodInvoker invoker = HANDLERS.get(msgType);
		if (invoker != null) {
			Object returnValue;
			try {
				Class<?>[] pTypes = invoker.getParameterTypes();
				int pCount = pTypes.length;
				if (pCount == 0) {
					returnValue = invoker.invoke0(invoker.getDefaultTarget(), null, EMPTY_OBJECT_ARRAY);
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
					returnValue = invoker.invoke0(invoker.getDefaultTarget(), null, pValues);
				}
				return returnValue;
			} catch (Throwable e) {
				logger.error("WebSocket消息处理出错", e);
			}
		}
		return null;
	}

	public static void processMessageReceived(String clientId, String payload) {
		MagicConsoleSession session = WebSocketSessionManager.findSession(clientId);
		if (session != null) {
			findHandleAndInvoke(session, payload);
		}
	}
	public static void processWebSocketEventMessage(String payload) {
		findHandleAndInvoke(null, payload);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		MagicConsoleSession mcsession = MagicConsoleSession.from(session);
		WebSocketSessionManager.remove(mcsession);
		MagicConsoleSession.remove(session);
		if(mcsession.getClientId() != null && mcsession.getAttributes() != null && !mcsession.getAttributes().isEmpty()){
			WebSocketSessionManager.sendToAll(MessageType.USER_LOGOUT, mcsession.getAttributes());
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		MagicConsoleSession consoleSession = MagicConsoleSession.from(session);
		Object returnValue = findHandleAndInvoke(consoleSession, message.getPayload());
		// 如果未成功处理消息，则通知其他机器去处理消息
		if (Boolean.FALSE.equals(returnValue)) {
			magicNotifyService.sendNotify(new MagicNotify(instanceId, EventAction.WS_C_S, consoleSession.getClientId(), message.getPayload()));
		}
	}
}
