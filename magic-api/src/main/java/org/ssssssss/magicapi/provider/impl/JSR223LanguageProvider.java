package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.provider.LanguageProvider;
import org.ssssssss.magicapi.script.ScriptManager;

import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.Map;

public class JSR223LanguageProvider implements LanguageProvider {

	ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

	@Override
	public boolean support(String languageName) {
		return scriptEngineManager.getEngineByName(languageName) != null;
	}

	@Override
	public Object execute(String languageName, String script, Map<String, Object> context) throws Exception {
		return ScriptManager.compile(languageName, script).eval(new SimpleBindings(context));
	}

}
