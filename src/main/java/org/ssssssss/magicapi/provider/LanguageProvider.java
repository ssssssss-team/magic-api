package org.ssssssss.magicapi.provider;

import javax.script.ScriptException;
import java.util.Map;

public interface LanguageProvider {

	/**
	 * 是否支持该语言
	 */
	boolean support(String languageName);

	/**
	 * 执行具体脚本
	 * @param languageName 语言类型
	 * @param script	脚本内容
	 * @param context	当前环境中的变量信息
	 */
	Object execute(String languageName,String script, Map<String, Object> context) throws Exception;
}
