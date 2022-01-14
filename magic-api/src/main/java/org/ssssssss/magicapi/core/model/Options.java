package org.ssssssss.magicapi.core.model;

/**
 * 接口选项信息
 *
 * @author mxd
 */
public enum Options {

	/**
	 * 包装请求参数到一个变量中
	 */
	WRAP_REQUEST_PARAMETERS("包装请求参数到一个变量中", "wrap_request_parameter"),

	/**
	 * 配置默认数据源的key
	 */
	DEFAULT_DATA_SOURCE("配置默认数据源的key", "default_data_source"),

	/**
	 * 允许拥有该权限的访问
	 */
	PERMISSION("允许拥有该权限的访问", "permission"),

	/**
	 * 允许拥有该角色的访问
	 */
	ROLE("允许拥有该角色的访问", "role"),

	/**
	 * 该接口需要登录才允许访问
	 */
	REQUIRE_LOGIN("该接口需要登录才允许访问", "require_login", "true"),

	/**
	 * 该接口需要不登录也可访问
	 */
	ANONYMOUS("该接口需要不登录也可访问", "anonymous", "true"),

	/**
	 * 不接收未经定义的参数
	 */
	DISABLED_UNKNOWN_PARAMETER("不接收未经定义的参数", "disabled_unknown_parameter", "true");

	private final String name;
	private final String value;
	private final String defaultValue;

	Options(String name, String value) {
		this(name, value, null);
	}

	Options(String name, String value, String defaultValue) {
		this.name = name;
		this.value = value;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
