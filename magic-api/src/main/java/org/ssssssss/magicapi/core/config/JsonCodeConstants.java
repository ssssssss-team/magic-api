package org.ssssssss.magicapi.core.config;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.core.exception.InvalidArgumentException;
import org.ssssssss.magicapi.core.model.JsonCode;

public interface JsonCodeConstants {

	JsonCode SUCCESS = new JsonCode(1, Constants.RESPONSE_MESSAGE_SUCCESS);

	JsonCode IS_READ_ONLY = new JsonCode(-2, "当前为只读模式，无法操作");

	JsonCode PERMISSION_INVALID = new JsonCode(-10, "无权限操作.");

	JsonCode GROUP_NOT_FOUND = new JsonCode(1001, "找不到分组信息");

	JsonCode NOT_SUPPORTED_GROUP_TYPE = new JsonCode(1002, "不支持该分组类型");

	JsonCode TARGET_IS_REQUIRED = new JsonCode(1003, "目标网址不能为空");

	JsonCode SECRET_KEY_IS_REQUIRED = new JsonCode(1004, "secretKey不能为空");

	JsonCode MOVE_NAME_CONFLICT = new JsonCode(1005, "移动后名称会重复，请修改名称后在试。");

	JsonCode SRC_GROUP_CONFLICT = new JsonCode(1006, "源对象和分组不能一致");

	JsonCode FILE_NOT_FOUND = new JsonCode(1007, "找不到对应文件或分组");

	JsonCode RESOURCE_LOCKED = new JsonCode(1008, "当前资源已被锁定，请解锁后在操作。");

	JsonCode PATH_CONFLICT = new JsonCode(1009, "该路径已被使用,请换一个路径在试");

	JsonCode RESOURCE_PATH_CONFLICT = new JsonCode(1010, "资源中[%s]有冲突，请检查");

	JsonCode MOVE_PATH_CONFLICT = new JsonCode(1011, "移动后路径会冲突,请换一个路径在试");

	JsonCode SAVE_GROUP_PATH_CONFLICT = new JsonCode(1036, "保存后路径会冲突,请换一个路径在试");

	JsonCode REQUEST_METHOD_REQUIRED = new JsonCode(1012, "请求方法不能为空");

	JsonCode REQUEST_PATH_REQUIRED = new JsonCode(1013, "请求路径不能为空");

	JsonCode FUNCTION_PATH_REQUIRED = new JsonCode(1014, "函数路径不能为空");

	JsonCode FILE_PATH_NOT_EXISTS = new JsonCode(1015, "配置的文件路径不存在，请检查");

	JsonCode REQUEST_PATH_CONFLICT = new JsonCode(1016, "接口[%s(%s)]与应用冲突，无法注册");

	JsonCode SCRIPT_REQUIRED = new JsonCode(1017, "脚本内容不能为空");

	JsonCode NAME_REQUIRED = new JsonCode(1018, "名称不能为空");

	JsonCode PATH_REQUIRED = new JsonCode(1019, "路径不能为空");

	JsonCode DS_URL_REQUIRED = new JsonCode(1020, "jdbcURL不能为空");

	JsonCode DS_KEY_REQUIRED = new JsonCode(1021, "key不能为空");

	JsonCode DS_KEY_CONFLICT = new JsonCode(1022, "数据源key已被使用，请更换后在试");

	JsonCode GROUP_ID_REQUIRED = new JsonCode(1023, "请选择分组");

	JsonCode CRON_ID_REQUIRED = new JsonCode(1024, "cron表达式不能为空");

	JsonCode NAME_INVALID = new JsonCode(1025, "名称不能包含特殊字符，只允许中文、数字、字母以及+_-.()的组合且不能.开头");

	JsonCode DATASOURCE_KEY_INVALID = new JsonCode(1026, "数据源Key不能包含特殊字符，只允许中文、数字、字母以及_组合");

	JsonCode FILE_SAVE_FAILURE = new JsonCode(1027, "保存失败,同一组下分组名称不能重复且不能包含特殊字符。");

	JsonCode PARAMETER_INVALID = new JsonCode(1028, "参数验证失败");

	JsonCode HEADER_INVALID = new JsonCode(1029, "header验证失败");

	JsonCode PATH_VARIABLE_INVALID = new JsonCode(1030, "路径变量验证失败");

	JsonCode BODY_INVALID = new JsonCode(1031, "body验证失败");

	JsonCode FILE_IS_REQUIRED = new JsonCode(1032, "请上传文件");

	JsonCode SIGN_IS_INVALID = new JsonCode(1033, "签名验证失败,请检查秘钥是否正确");

	JsonCode BACKUP_NOT_ENABLED = new JsonCode(1034, "未启用备份，无法操作");

	JsonCode API_NOT_FOUND = new JsonCode(1035, "找不到接口");

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
