package org.ssssssss.magicapi.core.context;

/**
 * magic 用户对象
 *
 * @author mxd
 */
public class MagicUser {

	private String id;

	private String username;

	private String token;

	public MagicUser(String id, String username, String token) {
		this.id = id;
		this.username = username;
		this.token = token;
	}

	public static MagicUser guest() {
		return new MagicUser(null, "guest", null);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
