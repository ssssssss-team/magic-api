package org.ssssssss.magicapi.config;

import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.script.MagicScript;

public class MagicScriptCompiler {

	private static DefaultSqlCache compileCache = new DefaultSqlCache(500, -1);

	public static MagicScript compile(String script) {
		String key = MD5Utils.encrypt(script);
		MagicScript magicScript = (MagicScript) compileCache.get("default", key);
		if(magicScript == null){
			magicScript = MagicScript.create(script);
			compileCache.put("default",key,magicScript,-1);
		}
		return magicScript;
	}
}
