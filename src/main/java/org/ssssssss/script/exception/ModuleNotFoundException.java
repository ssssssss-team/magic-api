package org.ssssssss.script.exception;

import org.ssssssss.script.parsing.Span;

public class ModuleNotFoundException extends MagicScriptException {

	public ModuleNotFoundException(String errorMessage, Span.Line line) {
		super(errorMessage, line);
	}
}
