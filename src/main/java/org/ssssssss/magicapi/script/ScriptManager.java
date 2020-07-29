package org.ssssssss.magicapi.script;

import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.script.MagicScript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
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

	public static ScriptEngine getEngine(String engine){
		return scriptEngineManager.getEngineByName(engine);
	}

	public static List<ScriptInfo> engines(){
		return scriptEngineManager.getEngineFactories().stream().map(ScriptInfo::new).collect(Collectors.toList());
	}

	public static void main(String[] args) {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
		for (ScriptEngineFactory factory : factories) {
			System.out.println(factory.getEngineName() + "------" + factory.getLanguageVersion());
		}
	}
}
