package org.ssssssss.magicapi.controller;

import org.ssssssss.magicapi.config.Message;
import org.ssssssss.magicapi.config.MessageType;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.model.MagicConsoleSession;

/**
 * UI上其它操作处理
 *
 * @author mxd
 */
public class MagicWorkbenchHandler {

	private final AuthorizationInterceptor authorizationInterceptor;

	public MagicWorkbenchHandler(AuthorizationInterceptor authorizationInterceptor) {
		this.authorizationInterceptor = authorizationInterceptor;
	}

	@Message(MessageType.LOGIN)
	public void onLogin(MagicConsoleSession session, String token) {
		try {
			if (!authorizationInterceptor.requireLogin() || authorizationInterceptor.getUserByToken(token) != null) {
				WebSocketSessionManager.add(session);
			}
		} catch (MagicLoginException ignored) {

		}
	}
}
