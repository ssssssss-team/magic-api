package org.ssssssss.magicapi.script;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class UnCompileScript extends CompiledScript {

	private ScriptEngine engine;

	private String script;

	public UnCompileScript(String script, ScriptEngine engine) {
		this.script = script;
		this.engine = engine;
	}

	@Override
	public Object eval(ScriptContext context) throws ScriptException {
		return engine.eval(script, context);
	}

	@Override
	public ScriptEngine getEngine() {
		return engine;
	}
}
