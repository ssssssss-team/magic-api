package org.ssssssss.magicapi.jsr223;

import org.ssssssss.magicapi.core.exception.MagicAPIException;
import org.ssssssss.magicapi.jsr223.LanguageProvider;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * JSR223规范支持
 *
 * @author mxd
 */
public class JSR223LanguageProvider implements LanguageProvider {

	ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

	@Override
	public boolean support(String languageName) {
		return scriptEngineManager.getEngineByName(languageName) != null;
	}

	@Override
	public Object execute(String languageName, String script, Map<String, Object> context) throws Exception {
		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(languageName);
		if (scriptEngine instanceof Compilable) {
			Compilable compilable = (Compilable) scriptEngine;
			try {
				return compilable.compile(script).eval(new SimpleBindings(context));
			} catch (Exception e) {
				throw new MagicAPIException(String.format("编译%s出错", languageName), e);
			}
		} else {
			return scriptEngine.eval(script, new SimpleBindings(context));
		}
	}

}
