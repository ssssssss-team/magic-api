package org.ssssssss.script.exception;

import org.ssssssss.script.parsing.Span;

public class MagicScriptException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String simpleMessage;
	private Span.Line line;

	public MagicScriptException() {
	}

	public MagicScriptException(String errorMessage, String simpleMessage, Span.Line line) {
		super(errorMessage);
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
		this.line = line;
	}

	public String getSimpleMessage() {
		return simpleMessage;
	}

	public Span.Line getLine() {
		return line;
	}
}