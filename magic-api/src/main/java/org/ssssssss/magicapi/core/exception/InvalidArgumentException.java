package org.ssssssss.magicapi.core.exception;

import org.ssssssss.magicapi.core.model.JsonCode;

/**
 * 参数错误异常
 *
 * @author mxd
 */
public class InvalidArgumentException extends RuntimeException {

	private final transient JsonCode jsonCode;

	public InvalidArgumentException(JsonCode jsonCode) {
		super(jsonCode.getMessage());
		this.jsonCode = jsonCode;
	}

	public int getCode() {
		return jsonCode.getCode();
	}
}
