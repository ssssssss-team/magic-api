package org.ssssssss.script.exception;

public class MagicScriptAssertException extends MagicScriptException {

	private int code;

	private String message;

	public MagicScriptAssertException(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
