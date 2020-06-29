package org.ssssssss.magicapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ApiInfo {

	private String id;

	private String method;

	private String path;

	private String script;

	private String name;

	private String groupName;

	private String parameter;

	private String option;

	private Map optionMap;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
		try {
			this.optionMap = new ObjectMapper().readValue(option, Map.class);
		} catch (Throwable ignored) {
		}
	}

	public Object getOptionValue(String key) {
		return this.optionMap != null ? this.optionMap.get(key) : null;
	}
}
