package org.ssssssss.magicapi.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.util.*;

/**
 * 接口信息
 */
public class ApiInfo extends MagicEntity {

	/**
	 * 请求方法
	 */
	private String method = "GET";

	/**
	 * 请求路径
	 */
	private String path;

	/**
	 * 设置的请求参数
	 */
	private List<Parameter> parameters = Collections.emptyList();

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
	 * 接口选项json
	 */
	private transient JsonNode jsonNode;

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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setParameter(String parameter) {
		if (parameter != null) {
			parameter = parameter.trim();
			if (parameter.startsWith("[")) {    // v0.5.0+
				this.parameters = JsonUtils.readValue(Objects.toString(parameter, "[]"), new TypeReference<List<Parameter>>() {
				});
			} else {
				Map map = JsonUtils.readValue(Objects.toString(parameter, "{}"), Map.class);
				Object request = map.get("request");
				if (request instanceof Map) {
					Map requestMap = (Map) request;
					Set keys = requestMap.keySet();
					this.parameters = new ArrayList<>();
					for (Object key : keys) {
						this.parameters.add(new Parameter(key.toString(), Objects.toString(requestMap.get(key), "")));
					}
				}
				Object header = map.get("header");
				if (header instanceof Map) {
					Map headers = (Map) header;
					Set keys = headers.keySet();
					this.headers = new ArrayList<>();
					for (Object key : keys) {
						this.headers.add(new Header(key.toString(), Objects.toString(headers.get(key), "")));
					}
				}
				if (map.containsKey("body")) {
					this.requestBody = Objects.toString(map.get("body"), null);
				}
			}
		}
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

	public Map<String, String> getOptionMap() {
		Map<String, String> map = new HashMap<>();
		if (this.jsonNode == null) {
			return null;
		} else if (this.jsonNode.isArray()) {
			for (JsonNode node : this.jsonNode) {
				map.put(node.get("name").asText(), node.get("value").asText());
			}
		} else {
			this.jsonNode.fieldNames().forEachRemaining(it -> map.put(it, this.jsonNode.get(it).asText()));
		}
		MappingHandlerMapping.findGroups(this.groupId)
				.stream()
				.flatMap(it -> it.getOptions().stream())
				.forEach(option -> {
					if (!map.containsKey(option.getName())) {
						map.put(option.getName(), String.valueOf(option.getValue()));
					}
				});
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

	public void setOption(String option) {
		this.option = option;
		try {
			this.jsonNode = new ObjectMapper().readTree(option);
		} catch (Throwable ignored) {
		}
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public void setRequestHeader(String requestHeader) {
		this.headers = JsonUtils.readValue(Objects.toString(requestHeader, "[]"), new TypeReference<List<Header>>() {
		});
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public void setOptionValue(String optionValue) {
		this.setOption(optionValue);
	}

	public String getOptionValue(Options options) {
		return getOptionValue(options.getValue());
	}

	public String getOptionValue(String key) {
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
			JsonNode node = this.jsonNode.get(key);
			if (node != null) {
				return node.asText();
			}
		}
		return MappingHandlerMapping.findGroups(this.groupId)
				.stream()
				.flatMap(it -> it.getOptions().stream())
				.filter(it -> key.equals(it.getName()))
				.findFirst()
				.map(it -> Objects.toString(it.getValue(), null)).orElse(null);
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

	public ApiInfo simple(){
		ApiInfo target = new ApiInfo();
		target.setId(this.getId());
		target.setName(this.getName());
		target.setGroupId(this.getGroupId());
		target.setPath(this.getPath());
		target.setMethod(this.getMethod());
		target.setLock(this.getLock());
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
				Objects.equals(option, apiInfo.option) &&
				Objects.equals(requestBody, apiInfo.requestBody) &&
				Objects.equals(headers, apiInfo.headers) &&
				Objects.equals(description, apiInfo.description) &&
				Objects.equals(requestBodyDefinition, apiInfo.requestBodyDefinition) &&
				Objects.equals(responseBodyDefinition, apiInfo.responseBodyDefinition);
	}


	@Override
	public int hashCode() {
		return Objects.hash(id, method, path, script, name, groupId, parameters, option, requestBody, headers, responseBody, description, requestBodyDefinition, responseBodyDefinition);
	}

	public ApiInfo copy() {
		ApiInfo info = new ApiInfo();
		info.setId(this.id);
		info.setMethod(this.method);
		info.setName(this.name);
		info.setPath(this.path);
		info.setScript(this.script);
		info.setGroupId(this.groupId);
		info.setParameters(this.parameters);
		info.jsonNode = this.jsonNode;
		info.setRequestBody(this.requestBody);
		info.setHeaders(this.headers);
		info.setResponseBody(this.responseBody);
		info.setDescription(this.description);
		info.setPaths(this.paths);
		info.setRequestBodyDefinition(this.requestBodyDefinition);
		info.setResponseBodyDefinition(this.responseBodyDefinition);
		return info;
	}
}
