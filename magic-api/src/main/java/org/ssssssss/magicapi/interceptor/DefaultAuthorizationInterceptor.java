package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.utils.MD5Utils;

import java.util.Objects;

public class DefaultAuthorizationInterceptor implements AuthorizationInterceptor {

	private final boolean requireLogin;
	private String validToken;
	private MagicUser configMagicUser;

	public DefaultAuthorizationInterceptor(String username, String password) {
		if (this.requireLogin = username != null && password != null) {
			this.validToken = MD5Utils.encrypt(String.format("%s||%s", username, password));
			this.configMagicUser = new MagicUser(username, username, this.validToken);
		}
	}

	@Override
	public boolean requireLogin() {
		return this.requireLogin;
	}

	@Override
	public MagicUser getUserByToken(String token) throws MagicLoginException {
		if (requireLogin && Objects.equals(validToken, token)) {
			return configMagicUser;
		}
		throw new MagicLoginException("token无效");
	}

	@Override
	public MagicUser login(String username, String password) throws MagicLoginException {
		if (requireLogin && Objects.equals(MD5Utils.encrypt(String.format("%s||%s", username, password)), this.validToken)) {
			return configMagicUser;
		}
		throw new MagicLoginException("用户名或密码不正确");
	}
}
