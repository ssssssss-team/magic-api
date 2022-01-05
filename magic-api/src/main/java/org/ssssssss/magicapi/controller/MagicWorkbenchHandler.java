package org.ssssssss.magicapi.controller;

import org.ssssssss.magicapi.config.Message;
import org.ssssssss.magicapi.config.MessageType;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.MagicConsoleSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UI上其它操作处理
 *
 * @author mxd
 */
public class MagicWorkbenchHandler {

	private final AuthorizationInterceptor authorizationInterceptor;

	private final static MagicUser guest = new MagicUser("guest","游客", "unauthorization");

	public MagicWorkbenchHandler(AuthorizationInterceptor authorizationInterceptor) {
		this.authorizationInterceptor = authorizationInterceptor;
	}

	@Message(MessageType.LOGIN)
	public void onLogin(MagicConsoleSession session, String token, String clientId) {
		try {
			MagicUser user = guest;
			if (!authorizationInterceptor.requireLogin() || (user = authorizationInterceptor.getUserByToken(token)) != null) {
				String ip = Optional.ofNullable(session.getWebSocketSession().getRemoteAddress()).map(it -> it.getAddress().getHostAddress()).orElse("unknown");
				session.setAttribute(Constants.WEBSOCKET_ATTRIBUTE_USER_ID, user.getId());
				session.setAttribute(Constants.WEBSOCKET_ATTRIBUTE_USER_IP, ip);
				session.setAttribute(Constants.WEBSOCKET_ATTRIBUTE_USER_NAME, user.getUsername());
				session.setClientId(clientId);
				WebSocketSessionManager.add(session);
				WebSocketSessionManager.sendBySession(session, WebSocketSessionManager.buildMessage(MessageType.LOGIN_RESPONSE, "1", session.getAttributes()));
				WebSocketSessionManager.sendToOther(session.getClientId(), MessageType.USER_LOGIN, session.getAttributes());
			}
		} catch (MagicLoginException ignored) {
			WebSocketSessionManager.sendBySession(session, WebSocketSessionManager.buildMessage(MessageType.LOGIN_RESPONSE, "0"));
		}
	}

	@Message(MessageType.GET_ONLINE)
	public boolean getOnline(MagicConsoleSession session) {
		List<MagicConsoleSession> sessions = WebSocketSessionManager.getSessions();
		if(sessions.size() > 0){
			List<Map<String, Object>> messages = sessions.stream()
					.map(MagicConsoleSession::getAttributes)
					.collect(Collectors.toList());
			WebSocketSessionManager.sendByClientId(session.getClientId(), WebSocketSessionManager.buildMessage(MessageType.ONLINE_USERS, messages));
		}
		return false;
	}
}
