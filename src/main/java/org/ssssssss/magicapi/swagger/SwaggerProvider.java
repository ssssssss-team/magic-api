package org.ssssssss.magicapi.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.provider.GroupServiceProvider;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生成swagger用的json
 */
public class SwaggerProvider {

	private MappingHandlerMapping mappingHandlerMapping;

	/**
	 * 描述信息
	 */
	private String description;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 版本号
	 */
	private String version;

	private GroupServiceProvider groupServiceProvider;

	public void setMappingHandlerMapping(MappingHandlerMapping mappingHandlerMapping) {
		this.mappingHandlerMapping = mappingHandlerMapping;
	}

	public void setGroupServiceProvider(GroupServiceProvider groupServiceProvider) {
		this.groupServiceProvider = groupServiceProvider;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@ResponseBody
	public SwaggerEntity swaggerJson() {
		List<ApiInfo> infos = mappingHandlerMapping.getApiInfos();
		SwaggerEntity swaggerEntity = new SwaggerEntity();
		SwaggerEntity.License license = new SwaggerEntity.License("MIT", "https://gitee.com/ssssssss-team/magic-api/blob/master/LICENSE");
		swaggerEntity.setInfo(new SwaggerEntity.Info(this.description, this.version, this.title, license));
		ObjectMapper mapper = new ObjectMapper();
		for (ApiInfo info : infos) {
			String groupName = groupServiceProvider.getFullName(info.getGroupId()).replace("/","-");
			swaggerEntity.addTag(groupName, groupServiceProvider.getFullPath(info.getGroupId()));
			SwaggerEntity.Path path = new SwaggerEntity.Path();
			path.addTag(groupName);
			try {
				parseParameters(mapper,info).forEach(path::addParameter);
				path.addResponse("200", mapper.readValue(Objects.toString(info.getResponseBody(), "{}"),Object.class));
			} catch (Exception ignored) {
			}
			path.addConsume("*/*");
			path.addProduce("application/json");
			path.setSummary(info.getName());

			swaggerEntity.addPath(mappingHandlerMapping.getRequestPath(info.getGroupId(), info.getPath()), info.getMethod(), path);
		}
		return swaggerEntity;
	}

	private List<SwaggerEntity.Parameter> parseParameters(ObjectMapper mapper, ApiInfo info) {
		String content = info.getParameter();
		content = content.trim();
		List<SwaggerEntity.Parameter> parameters = new ArrayList<>();
		if (content.startsWith("[")) {    // 0.5.0
			try {
				List<KeyValueDescription> kvs = readKeyValues(mapper, content);
				parameters.addAll(kvs.stream().map(kv -> new SwaggerEntity.Parameter(kv.getName(), "query", "string", kv.getDescription(), kv.getValue())).collect(Collectors.toList()));
				kvs = readKeyValues(mapper, Objects.toString(info.getRequestHeader(),"[]"));
				parameters.addAll(kvs.stream().map(kv -> new SwaggerEntity.Parameter(kv.getName(), "header", "string", kv.getDescription(), kv.getValue())).collect(Collectors.toList()));
				Object object = mapper.readValue(info.getRequestBody(),Object.class);
				if(object instanceof List || object instanceof Map){
					parameters.add(new SwaggerEntity.Parameter("body", "body", object instanceof List ? "array": "object", null, object));
				}
			} catch (Exception ignored) {
			}
		} else {
			try {
				Map map = mapper.readValue(Objects.toString(content, "{}"), Map.class);
				Object request = map.get("request");
				if (request instanceof Map) {
					Map requestMap = (Map) request;
					Set keys = requestMap.keySet();
					for (Object key : keys) {
						parameters.add(new SwaggerEntity.Parameter(key.toString(), "query", "string", key.toString(), requestMap.getOrDefault(key, "")));
					}
				}
				Object header = map.get("header");
				if (header instanceof Map) {
					Map headers = (Map) header;
					Set keys = headers.keySet();
					for (Object key : keys) {
						parameters.add(new SwaggerEntity.Parameter(key.toString(), "header", "string", key.toString(), headers.getOrDefault(key, "")));
					}
				}
				if (map.containsKey("body")) {
					parameters.add(new SwaggerEntity.Parameter("body", "body", null, null, map.get("body")));
				}
			} catch (Exception ignored) {
			}
		}
		return parameters;
	}

	private List<KeyValueDescription> readKeyValues(ObjectMapper mapper, String json) throws IOException {
		return Arrays.asList(mapper.readValue(json, KeyValueDescription[].class));
	}

	static class KeyValueDescription {

		private String name;

		private String value;

		private String description;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

}
