package org.ssssssss.script.exception;

import org.ssssssss.script.parsing.Span;

public class MagicScriptException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String errorMessage;
	private String simpleMessage;
	private Span.Line line;

	public MagicScriptException() {
	}

	public MagicScriptException(String errorMessage, String simpleMessage, Span.Line line) {
		super(errorMessage);
		this.errorMessage = errorMessage;
		this.simpleMessage = simpleMessage;
		this.line = line;
	}

	public MagicScriptException(String errorMessage, Span.Line line) {
		this(errorMessage, errorMessage, line);
	}

	public MagicScriptException(String errorMessage) {
		this(errorMessage, errorMessage, null);
	}

	public MagicScriptException(String message, String simpleMessage, Throwable cause, Span.Line line) {
		super(message, cause);
		this.simpleMessage = simpleMessage;
		this.errorMessage = message;
		this.line = line;
	}

	public String getSimpleMessage() {
		return simpleMessage;
	}

	public Span.Line getLine() {
		return line;
	}

	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();

		if (getCause() == null || getCause() == this) {
			return super.getMessage();
		}

		builder.append(errorMessage, 0, errorMessage.indexOf('\n'));
		builder.append("\n");

		Throwable cause = getCause();
		while (cause != null && cause != this) {
			if (cause instanceof MagicScriptException) {
				MagicScriptException ex = (MagicScriptException) cause;
				if (ex.getCause() == null || ex.getCause() == ex) {
					builder.append(ex.errorMessage);
				} else {
					builder.append(ex.errorMessage, 0, ex.errorMessage.indexOf('\n'));
				}
				builder.append("\n");
			}
			cause = cause.getCause();
		}
		return builder.toString();
	}
}