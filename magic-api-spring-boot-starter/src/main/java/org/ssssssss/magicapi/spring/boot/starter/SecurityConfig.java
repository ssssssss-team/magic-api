package org.ssssssss.magicapi.spring.boot.starter;

/**
 * 安全配置
 * @since 0.4.0
 */
public class SecurityConfig {

	/**
	 * 登录用的用户名
	 */
	private String username;

	/**
	 * 登录用的密码
	 */
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
