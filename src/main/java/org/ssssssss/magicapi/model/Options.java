package org.ssssssss.magicapi.model;

public enum Options {

	WRAP_REQUEST_PARAMETERS("包装请求参数到一个变量中", "wrap_request_parameter");

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
