package org.ssssssss.magicapi.core.model;

/**
 * Json状态码信息
 *
 * @author mxd
 */
public class JsonCode {

	private int code;

	private String message;

	public JsonCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JsonCode format(Object... args) {
		return new JsonCode(this.code, String.format(this.message, args));
	}
}
