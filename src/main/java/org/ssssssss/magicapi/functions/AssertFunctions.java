package org.ssssssss.magicapi.functions;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.script.exception.MagicScriptAssertException;

import java.util.regex.Pattern;

/**
 * 断言模块
 */
public class AssertFunctions implements MagicModule {

	/**
	 * 判断值不能为null
	 * @param value	值
	 * @param code	状态码
	 * @param message	状态说明
	 */
	public static void notNull(Object value, int code, String message) {
		if (value == null) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	/**
	 * 判断值不能为empty
	 * @param value	值
	 * @param code	状态码
	 * @param message	状态说明
	 */
	public static void notEmpty(String value, int code, String message) {
		if (StringUtils.isEmpty(value)) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	/**
	 * 判断值不能为blank
	 * @param value	值
	 * @param code	状态码
	 * @param message	状态说明
	 */
	public static void notBlank(String value, int code, String message) {
		if (StringUtils.isBlank(value)) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	/**
	 * 正则验证值
	 * @param value	值
	 * @param code	状态码
	 * @param message	状态说明
	 */
	public static void regx(String value, String pattern, int code, String message) {
		if (value == null || !Pattern.compile(pattern).matcher(value).matches()) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	/**
	 * 判断值值是否为true
	 * @param value	值
	 * @param code	状态码
	 * @param message	状态说明
	 */
	public static void isTrue(boolean value, int code, String message) {
		if (!value) {
			throw new MagicScriptAssertException(code, message);
		}
	}


	@Override
	public String getModuleName() {
		return "assert";
	}
}
