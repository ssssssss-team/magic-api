package org.ssssssss.magicapi.core.config;

import java.util.Arrays;
import java.util.List;

public class Constants {

	/**
	 * true 常量
	 */
	public static final String CONST_STRING_TRUE = "true";


	/**
	 * 接口文件夹名
	 */
	public static final String PATH_API = "api";

	/**
	 * 函数文件夹名
	 */
	public static final String PATH_FUNCTION = "function";

	/**
	 * websocket文件夹名
	 */
	public static final String PATH_WEBSOCKET = "websocket";

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

	/**
	 * 脚本中RequestBody的变量名
	 */
	public static final String VAR_NAME_REQUEST_BODY = "body";

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
	 * 通知新增
	 */
	public static final int NOTIFY_ACTION_ADD = 1;

	/**
	 * 通知修改
	 */
	public static final int NOTIFY_ACTION_UPDATE = 2;

	/**
	 * 通知删除
	 */
	public static final int NOTIFY_ACTION_DELETE = 3;

	/**
	 * 通知更新全部
	 */
	public static final int NOTIFY_ACTION_ALL = 0;

	/**
	 * 通知接口刷新
	 */
	public static final int NOTIFY_ACTION_API = 1;

	/**
	 * 通知分组刷新
	 */
	public static final int NOTIFY_ACTION_GROUP = 2;

	/**
	 * 通知函数刷新
	 */
	public static final int NOTIFY_ACTION_FUNCTION = 3;

	/**
	 * 通知数据源刷新
	 */
	public static final int NOTIFY_ACTION_DATASOURCE = 4;
	/**
	 * 通知WebSocket刷新
	 */
	public static final int NOTIFY_ACTION_WEBSOCKET = 5;


	/**
	 * 通知 C -> S 的WebSocket消息
	 */
	public static final int NOTIFY_WS_C_S = 100;

	/**
	 * 通知 S -> C 的WebSocket消息
	 */
	public static final int NOTIFY_WS_S_C = 200;

	/**
	 * 空数组
	 */
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


	/**
	 * 数据库日期类型
	 */
	public static final List<String> DBTYPE_DATE = Arrays.asList("datetime", "time", "date", "timestamp");

	/**
	 * 数据库数值类型
	 */
	public static final List<String> DBTYPE_NUMBER = Arrays.asList("tinyint", "smallint", "mediumint", "int", "number", "integer", "bit", "bigint");

	/**
	 * 数据库数值类型
	 */
	public static final List<String> DBTYPE_DECIMAL = Arrays.asList("float", "double", "decimal");

	/**
	 * String类型
	 */
	public static final String JAVA_TYPE_STRING = "String";

	/**
	 * Date类型
	 */
	public static final String JAVA_TYPE_DATE = "Date";

	/**
	 * int类型
	 */
	public static final String JAVA_TYPE_INTEGER = "Integer";

	/**
	 * long类型
	 */
	public static final String JAVA_TYPE_LONG = "Long";

	/**
	 * BigDecimal类型
	 */
	public static final String JAVA_TYPE_BIGDECIMAL = "BigDecimal";


}
