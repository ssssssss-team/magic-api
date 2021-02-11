package org.ssssssss.magicapi.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 接口信息
 */
public class ApiInfo {

	/**
	 * 接口ID
	 */
	private String id;

	/**
	 * 请求方法
	 */
	private String method;

	/**
	 * 请求路径
	 */
	private String path;

	/**
	 * 脚本内容
	 */
	private String script;

	/**
	 * 接口名称
	 */
	private String name;

	/**
	 * 分组ID
	 */
	private String groupId;

	/**
	 * 设置的请求参数
	 */
	private String parameter;

	/**
	 * 设置的接口选项
	 */
	private String option;

	/**
	 * 请求体
	 */
	private String requestBody;

	/**
	 * 请求头
	 */
	private String requestHeader;

	/**
	 * 输出结果
	 */
	private String responseBody;

	/**
	 * 接口描述
	 */
	private String description;

	/**
	 * 最后更新时间
	 */
	private Long updateTime;

	/**
	 * 接口选项json
	 */
	private JsonNode jsonNode;

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

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getResponseBody() {
		return responseBody;
	}


	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public String getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(String requestHeader) {
		this.requestHeader = requestHeader;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Map<String, Object> getOptionMap() {
		Map<String, Object> map = new HashMap<>();
		if (this.jsonNode == null) {
			return null;
		} else if (this.jsonNode.isArray()) {
			for (JsonNode node : this.jsonNode) {
				map.put(node.get("name").asText(), node.get("value").asText());
			}
		} else {
			this.jsonNode.fieldNames().forEachRemaining(it -> map.put(it, this.jsonNode.get(it)));
		}
		return map;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOption() {
		return option;
	}

	public void setOptionValue(String optionValue) {
		this.setOption(optionValue);
	}

	public void setOption(String option) {
		this.option = option;
		try {
			this.jsonNode = new ObjectMapper().readTree(option);
		} catch (Throwable ignored) {
		}
	}


	public Object getOptionValue(String key) {
		if (this.jsonNode == null) {
			return null;
		}
		if (this.jsonNode.isArray()) {
			for (JsonNode node : this.jsonNode) {
				if (node.isObject() && Objects.equals(key, node.get("name").asText())) {
					return node.get("value").asText();
				}
			}
		} else if (this.jsonNode.isObject()) {
			return this.jsonNode.get(key).asText();
		}
		return null;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ApiInfo apiInfo = (ApiInfo) o;
		return Objects.equals(id, apiInfo.id) &&
				Objects.equals(method, apiInfo.method) &&
				Objects.equals(path, apiInfo.path) &&
				Objects.equals(script, apiInfo.script) &&
				Objects.equals(name, apiInfo.name) &&
				Objects.equals(groupId, apiInfo.groupId) &&
				Objects.equals(parameter, apiInfo.parameter) &&
				Objects.equals(option, apiInfo.option) &&
				Objects.equals(requestBody, apiInfo.requestBody) &&
				Objects.equals(requestHeader, apiInfo.requestHeader) &&
				Objects.equals(responseBody, apiInfo.responseBody) &&
				Objects.equals(description, apiInfo.description);
	}


	@Override
	public int hashCode() {
		return Objects.hash(id, method, path, script, name, groupId, parameter, option, requestBody, requestHeader, responseBody, description);
	}

	public ApiInfo copy() {
		ApiInfo info = new ApiInfo();
		info.setId(this.id);
		info.setMethod(this.method);
		info.setName(this.name);
		info.setPath(this.path);
		info.setScript(this.script);
		info.setGroupId(this.groupId);
		info.setParameter(parameter);
		info.setOption(this.option);
		info.setRequestBody(this.requestBody);
		info.setRequestHeader(this.requestHeader);
		info.setResponseBody(this.responseBody);
		info.setDescription(this.description);
		return info;
	}
}
