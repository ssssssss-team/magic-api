package org.ssssssss.magicapi.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FunctionInfo extends MagicEntity {

	private String path;

	private String description;

	private String parameter;

	private String returnType;

	private String mappingPath;

	private List<String> parameterNames = Collections.emptyList();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
		try {
			this.parameterNames = new ObjectMapper().readTree(parameter).findValues("name")
					.stream().map(JsonNode::asText)
					.collect(Collectors.toList());
		} catch (Throwable ignored) {
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMappingPath() {
		return mappingPath;
	}

	public void setMappingPath(String mappingPath) {
		this.mappingPath = mappingPath;
	}

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FunctionInfo functionInfo = (FunctionInfo) o;
		return Objects.equals(id, functionInfo.id) &&
				Objects.equals(path, functionInfo.path) &&
				Objects.equals(script, functionInfo.script) &&
				Objects.equals(name, functionInfo.name) &&
				Objects.equals(groupId, functionInfo.groupId) &&
				Objects.equals(description, functionInfo.description) &&
				Objects.equals(parameter, functionInfo.parameter) &&
				Objects.equals(returnType, functionInfo.returnType);
	}


	@Override
	public int hashCode() {
		return Objects.hash(id, path, script, name, groupId, parameter, description, returnType);
	}
}
