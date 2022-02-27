package org.ssssssss.magicapi.core.config;

import java.util.Arrays;
import java.util.List;

public class Constants {

	/**
	 * true 常量
	 */
	public static final String CONST_STRING_TRUE = "true";


	/**
	 * 空值
	 */
	public static final String EMPTY = "";

	/**
	 * 表达式验证
	 */
	public static final String VALIDATE_TYPE_EXPRESSION = "expression";

	/**
	 * 正则验证
	 */
	public static final String VALIDATE_TYPE_PATTERN = "pattern";

	/**
	 * 表达式验证中变量的默认名称
	 */
	public static final String EXPRESSION_DEFAULT_VAR_NAME = "value";

	/**
	 * 脚本中session的变量名
	 */
	public static final String VAR_NAME_SESSION = "session";

	/**
	 * 脚本中cookie的变量名
	 */
	public static final String VAR_NAME_COOKIE = "cookie";

	/**
	 * 脚本中路径变量的变量名
	 */
	public static final String VAR_NAME_PATH_VARIABLE = "path";

	/**
	 * 脚本中header的变量名
	 */
	public static final String VAR_NAME_HEADER = "header";

	/**
	 * 脚本中query的变量名
	 */
	public static final String VAR_NAME_QUERY = "query";
	/**

	/**
	 * 脚本中RequestBody的变量名
	 */
	public static final String VAR_NAME_REQUEST_BODY = "body";

	/**
	 * 脚本中RequestBody的变量值字段类型
	 */
	public static final String VAR_NAME_REQUEST_BODY_VALUE_TYPE_OBJECT = "object";

	/**
	 * 脚本中RequestBody的变量名字段类型
	 */
	public static final String VAR_NAME_REQUEST_BODY_VALUE_TYPE_ARRAY = "array";

	public static final String HEADER_REQUEST_SCRIPT_ID = "Magic-Request-Script-Id";

	public static final String HEADER_REQUEST_CLIENT_ID = "Magic-Request-Client-Id";

	public static final String HEADER_REQUEST_BREAKPOINTS = "Magic-Request-Breakpoints";

	public static final String ATTRIBUTE_MAGIC_USER = "MAGIC_API_ATTRIBUTE_USER";

	public static final String MAGIC_TOKEN_HEADER = "Magic-Token";

	public static final String GROUP_METABASE = "group.json";

	public static final String UPLOAD_MODE_FULL = "full";

	public static final String LOCK = "1";

	public static final String UNLOCK = "0";

	public static final String ROOT_ID = "0";

	public static final String EVENT_TYPE_FILE = "file";

	public static final String EVENT_SOURCE_NOTIFY = "notify";

	public static final String WEBSOCKET_ATTRIBUTE_FILE_ID = "fileId";

	public static final String WEBSOCKET_ATTRIBUTE_USER_ID = "id";

	public static final String WEBSOCKET_ATTRIBUTE_USER_NAME = "username";

	public static final String WEBSOCKET_ATTRIBUTE_USER_IP = "ip";

	public static final String WEBSOCKET_ATTRIBUTE_CLIENT_ID = "cid";

	/**
	 * 执行成功的code值
	 */
	public static int RESPONSE_CODE_SUCCESS = 1;

	/**
	 * 执行成功的message值
	 */
	public static final String RESPONSE_MESSAGE_SUCCESS = "success";

	/**
	 * 执行出现异常的code值
	 */
	public static int RESPONSE_CODE_EXCEPTION = -1;

	/**
	 * 参数验证未通过的code值
	 */
	public static int RESPONSE_CODE_INVALID = 0;

	/**
	 * 空数组
	 */
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


}
