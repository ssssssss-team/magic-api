package org.ssssssss.script.exception;

import org.ssssssss.script.parsing.Span;

public class ScriptException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final String errorMessage;
	private final String simpleMessage;
	private final Span.Line line;

	public ScriptException(String errorMessage, String simpleMessage, Span.Line line) {
		super(errorMessage);
		this.errorMessage = errorMessage;
		this.simpleMessage = simpleMessage;
		this.line = line;
	}

	public ScriptException(String errorMessage, Span.Line line) {
		this(errorMessage, errorMessage, line);
	}

	public ScriptException(String errorMessage) {
		this(errorMessage, errorMessage, null);
	}

	public ScriptException(String message, String simpleMessage, Throwable cause, Span.Line line) {
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
			if (cause instanceof ScriptException) {
				ScriptException ex = (ScriptException) cause;
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