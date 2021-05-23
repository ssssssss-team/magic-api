package org.ssssssss.magicapi.exception;

import org.ssssssss.magicapi.model.JsonCode;

public class InvalidArgumentException extends RuntimeException {

	private final JsonCode jsonCode;

	public InvalidArgumentException(JsonCode jsonCode) {
		super(jsonCode.getMessage());
		this.jsonCode = jsonCode;
	}

	public int getCode() {
		return jsonCode.getCode();
	}
}
