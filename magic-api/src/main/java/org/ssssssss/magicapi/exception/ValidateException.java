package org.ssssssss.magicapi.exception;

import org.ssssssss.magicapi.model.JsonCode;

public class ValidateException extends RuntimeException{

	private final JsonCode jsonCode;

	public ValidateException(JsonCode jsonCode, String message) {
		super(message);
		this.jsonCode = jsonCode;
	}

	public JsonCode getJsonCode() {
		return jsonCode;
	}
}
