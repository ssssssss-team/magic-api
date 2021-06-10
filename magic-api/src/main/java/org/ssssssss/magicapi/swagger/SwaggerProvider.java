package org.ssssssss.magicapi.swagger;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.BaseDefinition;
import org.ssssssss.magicapi.model.Path;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.ssssssss.magicapi.model.Constants.*;

/**
 * 生成swagger用的json
 */
public class SwaggerProvider {

	private MappingHandlerMapping mappingHandlerMapping;

	/**
	 * 基础路径
	 */
	private String basePath;

	private GroupServiceProvider groupServiceProvider;

	private SwaggerEntity.Info info;

	/**
	 * swagger Model定义路径前缀
	 */
    private static final String DEFINITION = "#/definitions/";
    /**
	 * body空对象
     */
    private static final String BODY_EMPTY = "{}";

    private final Map<String, Object> DEFINITION_MAP = new ConcurrentHashMap<>();

	public void setMappingHandlerMapping(MappingHandlerMapping mappingHandlerMapping) {
		this.mappingHandlerMapping = mappingHandlerMapping;
	}

	public void setGroupServiceProvider(GroupServiceProvider groupServiceProvider) {
		this.groupServiceProvider = groupServiceProvider;
	}

	public void setInfo(SwaggerEntity.Info info) {
		this.info = info;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@ResponseBody
	public SwaggerEntity swaggerJson() {
		List<ApiInfo> infos = mappingHandlerMapping.getApiInfos();
		SwaggerEntity swaggerEntity = new SwaggerEntity();
		swaggerEntity.setInfo(info);
		swaggerEntity.setBasePath(this.basePath);
		ObjectMapper mapper = new ObjectMapper();
		for (ApiInfo info : infos) {
			String groupName = groupServiceProvider.getFullName(info.getGroupId()).replace("/", "-");
			String requestPath = "/" + mappingHandlerMapping.getRequestPath(info.getGroupId(), info.getPath());
			SwaggerEntity.Path path = new SwaggerEntity.Path();
			path.addTag(groupName);
			boolean hasBody = false;
			try {
				List<SwaggerEntity.Parameter> parameters = parseParameters(mapper, info);
				hasBody = parameters.stream().anyMatch(it -> VAR_NAME_REQUEST_BODY.equals(it.getIn()));
                if (hasBody) {
                    BaseDefinition baseDefinition = JSONUtil.toBean(info.getRequestBody(), BaseDefinition.class);
                    doProcessDefinition(baseDefinition, info);
                }
				parameters.forEach(path::addParameter);
				path.addResponse("200", mapper.readValue(Objects.toString(info.getResponseBody(), BODY_EMPTY), Object.class));
			} catch (Exception ignored) {
			}
			if (hasBody) {
				path.addConsume("application/json");
			} else {
				path.addConsume("*/*");
			}
			path.addProduce("application/json");
			path.setSummary(StringUtils.defaultIfBlank(info.getDescription(), info.getName()));

			swaggerEntity.addPath(requestPath, info.getMethod(), path);
		}

		if (this.DEFINITION_MAP.size() > 0) {
			Set<Map.Entry> entries = ((Map) this.DEFINITION_MAP).entrySet();
			for (Map.Entry entry : entries) {
				swaggerEntity.addDefinitions(Objects.toString(entry.getKey()), entry.getValue());
			}
		}

		return swaggerEntity;
	}

	private List<SwaggerEntity.Parameter> parseParameters(ObjectMapper mapper, ApiInfo info) {
		List<SwaggerEntity.Parameter> parameters = new ArrayList<>();
		info.getParameters().forEach(it -> parameters.add(new SwaggerEntity.Parameter(it.isRequired(), it.getName(), VAR_NAME_QUERY, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		info.getHeaders().forEach(it -> parameters.add(new SwaggerEntity.Parameter(it.isRequired(), it.getName(), VAR_NAME_HEADER, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		List<Path> paths = new ArrayList<>(info.getPaths());
		MappingHandlerMapping.findGroups(info.getGroupId())
				.stream()
				.flatMap(it -> it.getPaths().stream())
				.forEach(it -> {
					if (!paths.contains(it)) {
						paths.add(it);
					}
				});
		paths.forEach(it -> parameters.add(new SwaggerEntity.Parameter(it.isRequired(), it.getName(), VAR_NAME_PATH_VARIABLE, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		try {
			if (StringUtils.isNotBlank(info.getRequestBody()) && !BODY_EMPTY.equals(info.getRequestBody().replaceAll("\\s", ""))) {
				BaseDefinition baseDefinition = JSONUtil.toBean(info.getRequestBody(), BaseDefinition.class);
				if (BooleanLiteral.isTrue(baseDefinition)) {
					SwaggerEntity.Parameter parameter = new SwaggerEntity.Parameter(true, StringUtils.isNotBlank(baseDefinition.getName()) ? baseDefinition.getName() : VAR_NAME_REQUEST_BODY, VAR_NAME_REQUEST_BODY, baseDefinition.getDataType().getJavascriptType(), baseDefinition.getDescription(), baseDefinition);

					Map<String, Object> schema = new HashMap<>();
					String groupName = groupServiceProvider.getFullName(info.getGroupId()).replace("/", "-");
					String voName =  groupName + "<" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + "<" + baseDefinition.getName() + ">>";
					schema.put("originalRef", voName);
					schema.put("$ref", DEFINITION + voName);
					parameter.setSchema(schema);
					parameters.add(parameter);
				}
			}

		} catch (Exception ignored) {
		}
		return parameters;
	}

    private Map<String, Object> doProcessDefinition(BaseDefinition target, ApiInfo info) {
        Map<String, Object> result = new HashMap<>(4);
        result.put("type", target.getDataType().getJavascriptType());
        result.put("description", target.getDescription());
        if (VAR_NAME_REQUEST_BODY_VALUE_TYPE_ARRAY.equalsIgnoreCase(target.getDataType().getJavascriptType())) {
            if (target.getChildren().size() > 0) {
                result.put("items", doProcessDefinition(target.getChildren().get(0), info));
            } else {
                result.put("items", Collections.emptyList());
            }

        } else if (VAR_NAME_REQUEST_BODY_VALUE_TYPE_OBJECT.equalsIgnoreCase(target.getDataType().getJavascriptType())) {
            String groupName = groupServiceProvider.getFullName(info.getGroupId()).replace("/", "-");
            String voName = groupName + "<" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + "<" + target.getName() + ">>";
            result.put("originalRef", voName);
            result.put("$ref", DEFINITION + voName);

            Map<String, Object> definition = new HashMap<>(4);
            Map<String, Map<String, Object>> properties = new HashMap<>(target.getChildren().size());
            for (BaseDefinition obj : target.getChildren()) {
                properties.put(obj.getName(), doProcessDefinition(obj, info));
            }
            definition.put("properties", properties);
            definition.put("description", target.getDescription());

            this.DEFINITION_MAP.put(voName, definition);
        } else {
            result.put("example", target.getValue());
        }
        return result;
    }
}
