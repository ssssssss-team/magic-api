package org.ssssssss.magicapi.controller;

import org.ssssssss.magicapi.config.Message;
import org.ssssssss.magicapi.config.MessageType;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.model.MagicConsoleSession;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
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
	public void onLogin(MagicConsoleSession session, String token) {
		try {
			MagicUser user = guest;
			if (!authorizationInterceptor.requireLogin() || (user = authorizationInterceptor.getUserByToken(token)) != null) {
				String ip = Optional.ofNullable(session.getWebSocketSession().getRemoteAddress()).map(it -> it.getAddress().getHostAddress()).orElse("unknown");
				session.setAttribute("user", user);
				session.setAttribute("ip", ip);
				WebSocketSessionManager.add(session);
				List<Object> messages = Arrays.asList(session.getId(), ip, user);
				WebSocketSessionManager.sendBySession(session, WebSocketSessionManager.buildMessage(MessageType.SESSION_ID, messages));
				WebSocketSessionManager.sendToAll(MessageType.USER_LOGIN, messages);
			}
		} catch (MagicLoginException ignored) {

		}
	}

	@Message(MessageType.GET_ONLINE)
	public boolean getOnline(MagicConsoleSession session) {
		List<MagicConsoleSession> sessions = WebSocketSessionManager.getSessions();
		if(sessions.size() > 0){
			List<List<Object>> messages = sessions.stream()
					.map(it -> Arrays.asList(it.getId(), it.getAttribute("ip"), it.getAttribute("user")))
					.collect(Collectors.toList());
			WebSocketSessionManager.sendBySessionId(session.getId(), WebSocketSessionManager.buildMessage(MessageType.ONLINE_USERS, messages));
		}
		return false;
	}
}
