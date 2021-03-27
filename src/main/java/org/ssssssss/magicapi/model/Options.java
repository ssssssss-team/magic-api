package org.ssssssss.magicapi.model;

public enum Options {

	WRAP_REQUEST_PARAMETERS("包装请求参数到一个变量中", "wrap_request_parameter"),
	PERMISSION("允许拥有该权限的访问","permission"),
	ROLE("允许拥有该角色的访问","role");

	private final String name;
	private final String value;

	Options(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
