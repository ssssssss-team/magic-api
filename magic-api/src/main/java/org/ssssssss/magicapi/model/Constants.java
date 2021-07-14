package org.ssssssss.magicapi.model;

public class Constants {

	/**
	 * true 常量
	 */
	public static final String CONST_STRING_TRUE = "true";

	/**
	 * false 常量
	 */
	public static final String CONST_STRING_FALSE = "false";


	/**
	 * 分组类型: 接口
	 */
	public static final String GROUP_TYPE_API = "1";

	/**
	 * 分组类型: 函数
	 */
	public static final String GROUP_TYPE_FUNCTION = "2";

	/**
	 * 接口文件夹名
	 */
	public static final String PATH_API = "api";

	/**
	 * 函数文件夹名
	 */
	public static final String PATH_FUNCTION = "function";

	/**
	 * 数据源文件夹名
	 */
	public static final String PATH_DATASOURCE = "datasource";

	/**
	 * 备份文件夹名
	 */
	public static final String PATH_BACKUPS = "backups";

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
	 * WebSocket存储的sessionId
	 */
	public static final String WS_DEBUG_SESSION_KEY = "sessionId";

	/**
	 * WebSocket存储的MagicScriptDebugContext
	 */
	public static final String WS_DEBUG_MAGIC_SCRIPT_CONTEXT = "magicScriptContext";

	/**
	 * 脚本中header的变量名
	 */
	public static final String VAR_NAME_HEADER = "header";
	/**
	 * 脚本中query的变量名
	 */
	public static final String VAR_NAME_QUERY = "query";

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
	/**
	 * 脚本中RequestBody的变量名
	 */
	public static final String HEADER_PREFIX_FOR_TEST = "MA-";

	public static final String HEADER_REQUEST_SESSION = "Magic-Request-Session";

	public static final String HEADER_REQUEST_BREAKPOINTS = "Magic-Request-Breakpoints";

	public static final String HEADER_REQUEST_CONTINUE = "Magic-Request-Continue";

	public static final String HEADER_REQUEST_STEP_INTO = "Magic-Request-Step-Into";

	public static final String HEADER_RESPONSE_WITH_MAGIC_API = "Response-With-Magic-API";

	public static final String ATTRIBUTE_MAGIC_USER = "MAGIC_API_ATTRIBUTE_USER";

	public static final String MAGIC_TOKEN_HEADER = "Magic-Token";

	public static final String GROUP_METABASE = "group.json";

	public static final String UPLOAD_MODE_INCREMENT = "increment";

	public static final String UPLOAD_MODE_FULL = "full";

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

}
