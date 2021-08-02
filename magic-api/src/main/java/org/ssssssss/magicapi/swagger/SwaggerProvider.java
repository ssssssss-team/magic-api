package org.ssssssss.magicapi.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.BaseDefinition;
import org.ssssssss.magicapi.model.DataType;
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
		this.DEFINITION_MAP.clear();
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
				List<Map<String, Object>> parameters = parseParameters(mapper, info);
				hasBody = parameters.stream().anyMatch(it -> VAR_NAME_REQUEST_BODY.equals(it.get("in")));
				BaseDefinition baseDefinition = info.getRequestBodyDefinition();
                if (hasBody && baseDefinition != null) {
                    doProcessDefinition(baseDefinition, info, "root_", "request", 0);
                }
				baseDefinition = info.getResponseBodyDefinition();
				parameters.forEach(path::addParameter);
                if(baseDefinition != null){
					Map responseMap = parseResponse(info);
					if (!responseMap.isEmpty()) {
						path.setResponses(responseMap);
						doProcessDefinition(baseDefinition, info, "root_" + baseDefinition.getName(), "response", 0);
					}
				}else{
					 path.addResponse("200", mapper.readValue(Objects.toString(info.getResponseBody(), BODY_EMPTY), Object.class));
				}

			} catch (Exception ignored) {
			}
			if (hasBody) {
				path.addConsume("application/json");
			} else {
				path.addConsume("*/*");
			}
			path.addProduce("application/json");
			path.setSummary(info.getName());
			path.setDescription(StringUtils.defaultIfBlank(info.getDescription(), info.getName()));

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

	private List<Map<String, Object>> parseParameters(ObjectMapper mapper, ApiInfo info) {
		List<Map<String, Object>> parameters = new ArrayList<>();
		info.getParameters().forEach(it -> parameters.add(SwaggerEntity.createParameter(it.isRequired(), it.getName(), VAR_NAME_QUERY, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		info.getHeaders().forEach(it -> parameters.add(SwaggerEntity.createParameter(it.isRequired(), it.getName(), VAR_NAME_HEADER, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		List<Path> paths = new ArrayList<>(info.getPaths());
		MappingHandlerMapping.findGroups(info.getGroupId())
				.stream()
				.flatMap(it -> it.getPaths().stream())
				.forEach(it -> {
					if (!paths.contains(it)) {
						paths.add(it);
					}
				});
		paths.forEach(it -> parameters.add(SwaggerEntity.createParameter(it.isRequired(), it.getName(), VAR_NAME_PATH_VARIABLE, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		try {
			BaseDefinition baseDefinition = info.getRequestBodyDefinition();
			if (baseDefinition!= null && baseDefinition.getChildren().size() > 0) {
                Map<String, Object> parameter = SwaggerEntity.createParameter(baseDefinition.isRequired(), StringUtils.isNotBlank(baseDefinition.getName()) ? baseDefinition.getName() : VAR_NAME_REQUEST_BODY, VAR_NAME_REQUEST_BODY, baseDefinition.getDataType().getJavascriptType(), baseDefinition.getDescription(), baseDefinition);
				Map<String, Object> schema = new HashMap<>(2);
				String groupName = groupServiceProvider.getFullName(info.getGroupId()).replace("/", "-");
				String voName =  groupName + "«" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + "«request«";
				if (VAR_NAME_REQUEST_BODY_VALUE_TYPE_ARRAY.equalsIgnoreCase(baseDefinition.getDataType().getJavascriptType())) {
					voName += "root_" + (StringUtils.isNotBlank(baseDefinition.getName()) ? baseDefinition.getName() + "_" : "_") +  "»»»";

					Map<String, Object> items = new HashMap<>(2);
					items.put("originalRef", voName);
					items.put("$ref", DEFINITION + voName);
					schema.put("items", items);
					schema.put("type", VAR_NAME_REQUEST_BODY_VALUE_TYPE_ARRAY);
				} else {
					voName += "root_" + baseDefinition.getName() + "»»»";
					schema.put("originalRef", voName);
					schema.put("$ref", DEFINITION + voName);
				}
				parameter.put("schema", schema);
				parameters.add(parameter);
			}else{
				Object object = mapper.readValue(info.getRequestBody(), Object.class);
				if ((object instanceof List || object instanceof Map) && BooleanLiteral.isTrue(object)) {
					parameters.add(SwaggerEntity.createParameter(false, VAR_NAME_REQUEST_BODY, VAR_NAME_REQUEST_BODY, object instanceof List ? VAR_NAME_REQUEST_BODY_VALUE_TYPE_ARRAY : VAR_NAME_REQUEST_BODY_VALUE_TYPE_OBJECT, null, object));
				}
			}

		} catch (Exception ignored) {
		}
		return parameters;
	}

	private Map<String, Object> parseResponse(ApiInfo info) {
		Map<String, Object> result = new HashMap<>();

		BaseDefinition baseDefinition = info.getResponseBodyDefinition();
		if (baseDefinition.getChildren().size() > 0) {
			String groupName = groupServiceProvider.getFullName(info.getGroupId()).replace("/", "-");
			String voName =  groupName + "«" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + "«response«";
			voName += "root_" + baseDefinition.getName() + "»»»";

			Map<String, Object> schema = new HashMap<>(2);
			schema.put("originalRef", voName);
			schema.put("$ref", DEFINITION + voName);

			Map<String, Object> response = new HashMap<>(2);
			response.put("description", "OK");
			response.put("schema", schema);
			result.put("200", response);
		}

		return result;
	}
    private Map<String, Object> doProcessDefinition(BaseDefinition target, ApiInfo info, String parentName, String definitionType, int level) {
        Map<String, Object> result = new HashMap<>(3);
        result.put("description", target.getDescription());
        if (VAR_NAME_REQUEST_BODY_VALUE_TYPE_ARRAY.equalsIgnoreCase(target.getDataType().getJavascriptType())) {
            if (target.getChildren().size() > 0) {
                result.put("items", doProcessDefinition(target.getChildren().get(0), info, parentName + target.getName() + "_", definitionType, level + 1));
            } else {
                result.put("items", Collections.emptyList());
            }
			result.put("type", target.getDataType().getJavascriptType());
        } else if (VAR_NAME_REQUEST_BODY_VALUE_TYPE_OBJECT.equalsIgnoreCase(target.getDataType().getJavascriptType())) {
            String groupName = groupServiceProvider.getFullName(info.getGroupId()).replace("/", "-");
            String voName = groupName + "«" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + (StringUtils.equals("response", definitionType) ? "«response«" : "«request«") + parentName + target.getName()  + "»»»";

			Map<String, Object> definition = new HashMap<>(3);
			Map<String, Map<String, Object>> properties = new HashMap<>(target.getChildren().size());
			for (BaseDefinition obj : target.getChildren()) {
				properties.put(obj.getName(), doProcessDefinition(obj, info, parentName + target.getName() + "_", definitionType, level + 1));
			}
			definition.put("properties", properties);
			definition.put("description", target.getDescription());
			definition.put("type", target.getDataType());

            if (this.DEFINITION_MAP.containsKey(voName)) {
				// TODO 应该不会出现名字都一样的
				voName = voName.replace("»»»", "_" + level + "»»»");
			}

			this.DEFINITION_MAP.put(voName, definition);
            result.put("originalRef", voName);
            result.put("$ref", DEFINITION + voName);

        } else {
            result.put("example", target.getValue());
			result.put("type", target.getDataType());
        }
        return result;
    }
}
