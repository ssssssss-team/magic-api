package org.ssssssss.magicapi.functions;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.script.exception.MagicScriptAssertException;

import java.util.regex.Pattern;

public class AssertFunctions {

	public static void notNull(Object value, int code, String message) {
		if (value == null) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	public static void notEmpty(String value, int code, String message) {
		if (StringUtils.isEmpty(value)) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	public static void notBlank(String value, int code, String message) {
		if (StringUtils.isBlank(value)) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	public static void regx(String value, String pattern, int code, String message) {
		if (value == null || !Pattern.compile(pattern).matcher(value).matches()) {
			throw new MagicScriptAssertException(code, message);
		}
	}

	public static void isTrue(boolean value, int code, String message) {
		if (!value) {
			throw new MagicScriptAssertException(code, message);
		}
	}


}
