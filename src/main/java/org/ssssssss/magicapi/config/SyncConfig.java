package org.ssssssss.magicapi.config;

/**
 * 同步配置
 *
 * @since 0.7.0
 */
public class SyncConfig {

	/**
	 * 秘钥
	 */
	private String secret;

	/**
	 * 是否允许pull
	 */
	private boolean allowPull = true;

	/**
	 * 是否允许push
	 */
	private boolean allowPush = true;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public boolean isAllowPull() {
		return allowPull;
	}

	public void setAllowPull(boolean allowPull) {
		this.allowPull = allowPull;
	}

	public boolean isAllowPush() {
		return allowPush;
	}

	public void setAllowPush(boolean allowPush) {
		this.allowPush = allowPush;
	}
}
