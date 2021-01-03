package org.ssssssss.magicapi.script;

import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptManager {

	private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();


	/**
	 * 编译缓存
	 */
	private static DefaultSqlCache compileCache = new DefaultSqlCache(500, -1);

	/**
	 * 编译脚本
	 *
	 * @param script 脚本内容
	 */
	public static MagicScript compile(String engine, String script) {
		String key = MD5Utils.encrypt(script);    //先对脚本MD5作为key
		MagicScript magicScript = (MagicScript) compileCache.get("default", key);
		if (magicScript == null) {
			magicScript = MagicScript.create(script, null);    //编译
			compileCache.put("default", key, magicScript, -1);
		}
		return magicScript;
	}

	/**
	 * 执行脚本
	 */
	public static Object executeScript(String script, MagicScriptContext context) {
		SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
		simpleScriptContext.setAttribute(MagicScript.CONTEXT_ROOT, context, ScriptContext.ENGINE_SCOPE);
		// 执行脚本
		return compile("MagicScript", script).eval(simpleScriptContext);
	}

	public static ScriptEngine getEngine(String engine){
		return scriptEngineManager.getEngineByName(engine);
	}

	public static List<ScriptInfo> engines(){
		return scriptEngineManager.getEngineFactories().stream().map(ScriptInfo::new).collect(Collectors.toList());
	}
}
