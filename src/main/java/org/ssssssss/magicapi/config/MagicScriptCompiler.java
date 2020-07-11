package org.ssssssss.magicapi.config;

import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.script.MagicScript;

/**
 * 脚本编译
 */
public class MagicScriptCompiler {

	/**
	 * 编译缓存
	 */
	private static DefaultSqlCache compileCache = new DefaultSqlCache(500, -1);

	/**
	 * 编译脚本
	 *
	 * @param script 脚本内容
	 */
	public static MagicScript compile(String script) {
		String key = MD5Utils.encrypt(script);    //先对脚本MD5作为key
		MagicScript magicScript = (MagicScript) compileCache.get("default", key);
		if (magicScript == null) {
			magicScript = MagicScript.create(script);    //编译
			compileCache.put("default", key, magicScript, -1);
		}
		return magicScript;
	}
}
