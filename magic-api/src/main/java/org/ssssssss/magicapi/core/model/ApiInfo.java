package org.ssssssss.magicapi.core.model;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 接口信息
 */
public class ApiInfo extends PathMagicEntity {

	/**
	 * 请求方法
	 */
	private String method = "GET";

	/**
	 * 设置的请求参数
	 */
	private List<Parameter> parameters = Collections.emptyList();

	/**
	 * 设置的接口选项
	 */
	private List<Option> options = new ArrayList<>();

	/**
	 * 请求体
	 */
	private String requestBody;

	/**
	 * 请求头
	 */
	private List<Header> headers = Collections.emptyList();

	/**
	 * 路径变量
	 */
	private List<Path> paths = Collections.emptyList();

	/**
	 * 输出结果
	 */
	private String responseBody;

	/**
	 * 接口描述
	 */
	private String description;

	/**
	 * 请求体属性
	 */
	private BaseDefinition requestBodyDefinition;

	/**
	 * 输出结果属性
	 */
	private BaseDefinition responseBodyDefinition;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
	}

	public Map<String, String> options() {
		Map<String, String> map = this.options.stream()
				.collect(Collectors.toMap(BaseDefinition::getName, it -> String.valueOf(it.getValue()), (o, n) -> n));
		MagicConfiguration.getMagicResourceService().getGroupsByFileId(this.groupId)
				.stream()
				.flatMap(it -> it.getOptions().stream())
				.forEach(option -> {
					if (!map.containsKey(option.getName())) {
						map.put(option.getName(), String.valueOf(option.getValue()));
					}
				});
		return map;
	}

	// 兼容1.x处理。
	public void setOptionMap(Map<String, Object> optionMap) {

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOption(List<Option> options) {
		this.options = options;
	}

	public void setOption(String json) {
		this.options = JsonUtils.readValue(Objects.toString(json, "[]"), new TypeReference<List<Option>>() {
		});
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public String getOptionValue(Options options) {
		return getOptionValue(options.getValue());
	}

	public String getOptionValue(String key) {
		return this.options.stream()
				.filter(it -> key.equals(it.getName()))
				.findFirst()
				.map(it -> Objects.toString(it.getValue(), null))
				.orElseGet(() -> MagicConfiguration.getMagicResourceService().getGroupsByFileId(this.id)
						.stream()
						.flatMap(it -> it.getOptions().stream())
						.filter(it -> key.equals(it.getName()))
						.findFirst()
						.map(it -> Objects.toString(it.getValue(), null)).orElse(null)
				);

	}

	public BaseDefinition getRequestBodyDefinition() {
		return requestBodyDefinition;
	}

	public void setRequestBodyDefinition(BaseDefinition requestBodyDefinition) {
		this.requestBodyDefinition = requestBodyDefinition;
	}

	public BaseDefinition getResponseBodyDefinition() {
		return responseBodyDefinition;
	}

	public void setResponseBodyDefinition(BaseDefinition responseBodyDefinition) {
		this.responseBodyDefinition = responseBodyDefinition;
	}

	public ApiInfo simple() {
		ApiInfo target = new ApiInfo();
		super.simple(target);
		target.setMethod(this.getMethod());
		return target;
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
				Objects.equals(paths, apiInfo.paths) &&
				Objects.equals(groupId, apiInfo.groupId) &&
				Objects.equals(parameters, apiInfo.parameters) &&
				Objects.equals(options, apiInfo.options) &&
				Objects.equals(requestBody, apiInfo.requestBody) &&
				Objects.equals(headers, apiInfo.headers) &&
				Objects.equals(description, apiInfo.description) &&
				Objects.equals(requestBodyDefinition, apiInfo.requestBodyDefinition) &&
				Objects.equals(responseBodyDefinition, apiInfo.responseBodyDefinition);
	}


	@Override
	public int hashCode() {
		return Objects.hash(id, method, path, script, name, groupId, parameters, options, requestBody, headers, description, requestBodyDefinition, responseBodyDefinition);
	}

	@Override
	public ApiInfo copy() {
		ApiInfo info = new ApiInfo();
		copyTo(info);
		info.setMethod(this.method);
		info.setParameters(this.parameters);
		info.setRequestBody(this.requestBody);
		info.setOption(this.options);
		info.setHeaders(this.headers);
		info.setResponseBody(this.responseBody);
		info.setDescription(this.description);
		info.setPaths(this.paths);
		info.setRequestBodyDefinition(this.requestBodyDefinition);
		info.setResponseBodyDefinition(this.responseBodyDefinition);
		return info;
	}
}
