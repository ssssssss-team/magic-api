package org.ssssssss.magicapi.model;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.exception.InvalidArgumentException;

public interface JsonCodeConstants {

	JsonCode SUCCESS = new JsonCode(1, Constants.RESPONSE_MESSAGE_SUCCESS);

	JsonCode IS_READ_ONLY = new JsonCode(-2, "当前为只读模式，无法操作");

	JsonCode PERMISSION_INVALID = new JsonCode(-10, "无权限操作.");

	JsonCode GROUP_NOT_FOUND = new JsonCode(0, "找不到分组信息");

	JsonCode TARGET_IS_REQUIRED = new JsonCode(0, "目标网址不能为空");

	JsonCode SECRET_KEY_IS_REQUIRED = new JsonCode(0, "secretKey不能为空");

	JsonCode NAME_CONFLICT = new JsonCode(0, "移动后名称会重复，请修改名称后在试。");

	JsonCode REQUEST_PATH_CONFLICT = new JsonCode(0, "该路径已被映射,请换一个请求方法或路径");

	JsonCode FUNCTION_PATH_CONFLICT = new JsonCode(0, "该路径已被映射,请换一个请求方法或路径");

	JsonCode REQUEST_METHOD_REQUIRED = new JsonCode(0, "请求方法不能为空");

	JsonCode REQUEST_PATH_REQUIRED = new JsonCode(0, "请求路径不能为空");

	JsonCode FUNCTION_PATH_REQUIRED = new JsonCode(0, "函数路径不能为空");

	JsonCode SCRIPT_REQUIRED = new JsonCode(0, "脚本内容不能为空");

	JsonCode API_NAME_REQUIRED = new JsonCode(0, "接口名称不能为空");

	JsonCode GROUP_NAME_REQUIRED = new JsonCode(0, "分组名称不能为空");

	JsonCode GROUP_TYPE_REQUIRED = new JsonCode(0, "分组类型不能为空");

	JsonCode FUNCTION_NAME_REQUIRED = new JsonCode(0, "函数名称不能为空");

	JsonCode NAME_INVALID = new JsonCode(0, "名称不能包含特殊字符，只允许中文、数字、字母以及_组合");

	JsonCode DATASOURCE_KEY_INVALID = new JsonCode(0, "数据源Key不能包含特殊字符，只允许中文、数字、字母以及_组合");

	JsonCode API_ALREADY_EXISTS = new JsonCode(0, "接口%s:%s已存在或接口名称重复");

	JsonCode FUNCTION_ALREADY_EXISTS = new JsonCode(0, "函数%s已存在或名称重复");

	JsonCode API_SAVE_FAILURE = new JsonCode(0, "保存失败,请检查接口名称是否重复且不能包含特殊字符。");

	JsonCode FUNCTION_SAVE_FAILURE = new JsonCode(0, "保存失败,请检查函数名称是否重复且不能包含特殊字符。");

	JsonCode GROUP_SAVE_FAILURE = new JsonCode(0, "保存失败,同一组下分组名称不能重复且不能包含特殊字符。");

	JsonCode GROUP_CONFLICT = new JsonCode(-20, "修改分组后，名称或路径会有冲突，请检查！");

	JsonCode PARAMETER_INVALID = new JsonCode(0, "参数验证失败");

	JsonCode HEADER_INVALID = new JsonCode(0, "header验证失败");

	JsonCode PATH_VARIABLE_INVALID = new JsonCode(0, "路径变量验证失败");

	JsonCode BODY_INVALID = new JsonCode(0, "body验证失败");

	JsonCode FILE_IS_REQUIRED = new JsonCode(0, "请上传文件");

	JsonCode SIGN_IS_INVALID = new JsonCode(0, "签名验证失败");

	JsonCode UPLOAD_PATH_CONFLICT = new JsonCode(0, "上传后%s路径会有冲突，请检查");

	JsonCode DEBUG_SESSION_NOT_FOUND = new JsonCode(0, "debug session not found!");

	JsonCode API_NOT_FOUND = new JsonCode(1001, "api not found");

	JsonCode FUNCTION_NOT_FOUND = new JsonCode(1002, "function not found");

	JsonCode DATASOURCE_KEY_REQUIRED = new JsonCode(0, "数据源Key不能为空");

	JsonCode DATASOURCE_KEY_EXISTS = new JsonCode(0, "数据源%s已存在或名称重复");

	JsonCode DATASOURCE_TYPE_NOT_FOUND = new JsonCode(0, "%s not found");
	JsonCode DATASOURCE_NOT_FOUND = new JsonCode(0, "找不到对应的数据源");

	JsonCode DATASOURCE_TYPE_NOT_SET = new JsonCode(0, "请设置数据源类型");

	default void notNull(Object value, JsonCode jsonCode) {
		if (value == null) {
			throw new InvalidArgumentException(jsonCode);
		}
	}

	default void isTrue(boolean value, JsonCode jsonCode) {
		if (!value) {
			throw new InvalidArgumentException(jsonCode);
		}
	}

	default void notBlank(String value, JsonCode jsonCode) {
		isTrue(StringUtils.isNotBlank(value), jsonCode);
	}
}
