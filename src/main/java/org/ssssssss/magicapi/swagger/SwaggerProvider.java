package org.ssssssss.magicapi.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.provider.GroupServiceProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
			String groupName = groupServiceProvider.getFullName(info.getGroupId());
			swaggerEntity.addTag(groupName, groupServiceProvider.getFullPath(info.getGroupId()));
			SwaggerEntity.Path path = new SwaggerEntity.Path();
			path.addTag(groupName);
			try {
				path.addResponse("200", mapper.readValue(Objects.toString(info.getOutput(), "{}"), Object.class));
			} catch (IOException ignored) {
			}
			path.addConsume("*/*");
			path.addProduce("application/json");
			path.setSummary(info.getName());
			try {
				Map map = mapper.readValue(Objects.toString(info.getParameter(), "{}"), Map.class);
				Object request = map.get("request");
				if (request instanceof Map) {
					Map requestMap = (Map) request;
					Set keys = requestMap.keySet();
					for (Object key : keys) {
						path.addParameter(new SwaggerEntity.Parameter(key.toString(), "query", "string", requestMap.getOrDefault(key, "")));
					}
				}
				Object header = map.get("header");
				if (header instanceof Map) {
					Map headers = (Map) header;
					Set keys = headers.keySet();
					for (Object key : keys) {
						path.addParameter(new SwaggerEntity.Parameter(key.toString(), "header", "string", headers.getOrDefault(key, "")));
					}
				}
				if (map.containsKey("body")) {
					path.addParameter(new SwaggerEntity.Parameter("body", "body", null, map.get("body")));
				}
			} catch (IOException ignored) {
			}
			swaggerEntity.addPath(mappingHandlerMapping.getRequestPath(info.getGroupId(), info.getPath()), info.getMethod(), path);
		}
		return swaggerEntity;
	}


}
