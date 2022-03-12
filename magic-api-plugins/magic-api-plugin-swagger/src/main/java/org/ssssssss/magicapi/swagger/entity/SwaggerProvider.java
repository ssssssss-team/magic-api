package org.ssssssss.magicapi.swagger.entity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.ApiInfo;
import org.ssssssss.magicapi.core.model.BaseDefinition;
import org.ssssssss.magicapi.core.model.DataType;
import org.ssssssss.magicapi.core.model.Path;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.impl.RequestMagicDynamicRegistry;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.PathUtils;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.ssssssss.magicapi.core.config.Constants.*;

/**
 * 生成swagger用的json
 *
 * @author mxd
 */
public class SwaggerProvider {

	/**
	 * swagger Model定义路径前缀
	 */
	private static final String DEFINITION = "#/definitions/";
	/**
	 * body空对象
	 */
	private static final String BODY_EMPTY = "{}";

	private final Map<String, Object> DEFINITION_MAP = new ConcurrentHashMap<>();

	private final RequestMagicDynamicRegistry requestMagicDynamicRegistry;

	private final MagicResourceService magicResourceService;
	/**
	 * 基础路径
	 */
	private final String basePath;
	private final SwaggerEntity.Info info;
	private final boolean persistenceResponseBody;
	private final String prefix;

	public SwaggerProvider(RequestMagicDynamicRegistry requestMagicDynamicRegistry, MagicResourceService magicResourceService, String basePath, SwaggerEntity.Info info, boolean persistenceResponseBody, String prefix) {
		this.requestMagicDynamicRegistry = requestMagicDynamicRegistry;
		this.magicResourceService = magicResourceService;
		this.basePath = basePath;
		this.info = info;
		this.persistenceResponseBody = persistenceResponseBody;
		this.prefix = StringUtils.defaultIfBlank(prefix, "") + "/";
	}

	@ResponseBody
	public SwaggerEntity swaggerJson() {
		this.DEFINITION_MAP.clear();
		List<ApiInfo> infos = requestMagicDynamicRegistry.mappings();
		SwaggerEntity swaggerEntity = new SwaggerEntity();
		swaggerEntity.setInfo(info);
		swaggerEntity.setBasePath(this.basePath);
		for (ApiInfo info : infos) {
			String groupName = magicResourceService.getGroupName(info.getGroupId()).replace("/", "-");
			String requestPath = PathUtils.replaceSlash(this.prefix + magicResourceService.getGroupPath(info.getGroupId()) + "/" + info.getPath());
			SwaggerEntity.Path path = new SwaggerEntity.Path(info.getId());
			path.addTag(groupName);
			boolean hasBody = false;
			try {
				List<Map<String, Object>> parameters = parseParameters(info);
				hasBody = parameters.stream().anyMatch(it -> VAR_NAME_REQUEST_BODY.equals(it.get("in")));
				BaseDefinition baseDefinition = info.getRequestBodyDefinition();
				if (hasBody && baseDefinition != null) {
					doProcessDefinition(baseDefinition, info, groupName, "root_", "request", 0);
				}
				parameters.forEach(path::addParameter);
				if (this.persistenceResponseBody) {
					baseDefinition = info.getResponseBodyDefinition();
					if (baseDefinition != null) {
						Map responseMap = parseResponse(info);
						if (!responseMap.isEmpty()) {
							path.setResponses(responseMap);
							doProcessDefinition(baseDefinition, info, groupName, "root_" + baseDefinition.getName(), "response", 0);
						}
					} else {
						path.addResponse("200", JsonUtils.readValue(Objects.toString(info.getResponseBody(), BODY_EMPTY), Object.class));
					}
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

	private List<Map<String, Object>> parseParameters(ApiInfo info) {
		List<Map<String, Object>> parameters = new ArrayList<>();
		info.getParameters().forEach(it -> parameters.add(SwaggerEntity.createParameter(it.isRequired(), it.getName(), VAR_NAME_QUERY, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		info.getHeaders().forEach(it -> parameters.add(SwaggerEntity.createParameter(it.isRequired(), it.getName(), VAR_NAME_HEADER, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		List<Path> paths = new ArrayList<>(info.getPaths());
		MagicConfiguration.getMagicResourceService().getGroupsByFileId(info.getId())
				.stream()
				.flatMap(it -> it.getPaths().stream())
				.filter(it -> !paths.contains(it))
				.forEach(paths::add);
		paths.forEach(it -> parameters.add(SwaggerEntity.createParameter(it.isRequired(), it.getName(), VAR_NAME_PATH_VARIABLE, it.getDataType().getJavascriptType(), it.getDescription(), it.getValue())));
		try {
			BaseDefinition baseDefinition = info.getRequestBodyDefinition();
			if (baseDefinition != null && !CollectionUtils.isEmpty(baseDefinition.getChildren())) {
				Map<String, Object> parameter = SwaggerEntity.createParameter(baseDefinition.isRequired(), StringUtils.isNotBlank(baseDefinition.getName()) ? baseDefinition.getName() : VAR_NAME_REQUEST_BODY, VAR_NAME_REQUEST_BODY, baseDefinition.getDataType().getJavascriptType(), baseDefinition.getDescription(), baseDefinition);
				Map<String, Object> schema = new HashMap<>(2);
				String groupName = magicResourceService.getGroupName(info.getGroupId()).replace("/", "-");
				String voName = groupName + "«" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + "«request«";
				if (DataType.Array == baseDefinition.getDataType()) {
					voName += "root_" + (StringUtils.isNotBlank(baseDefinition.getName()) ? baseDefinition.getName() + "_" : "_") + "»»»";

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
			} else if (StringUtils.isNotBlank(info.getRequestBody())) {
				Object object = JsonUtils.readValue(info.getResponseBody(), Object.class);
				boolean isListOrMap = (object instanceof List || object instanceof Map);
				if (isListOrMap && BooleanLiteral.isTrue(object)) {
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
		if (!CollectionUtils.isEmpty(baseDefinition.getChildren())) {
			String groupName = magicResourceService.getGroupName(info.getGroupId()).replace("/", "-");
			String voName = groupName + "«" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + "«response«";
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

	private Map<String, Object> doProcessDefinition(BaseDefinition target, ApiInfo info, String groupName, String parentName, String definitionType, int level) {
		Map<String, Object> result = new HashMap<>(4);
		result.put("description", target.getDescription());
		if (DataType.Array == target.getDataType()) {
			if (!CollectionUtils.isEmpty(target.getChildren())) {
				result.put("items", doProcessDefinition(target.getChildren().get(0), info, groupName, parentName + target.getName() + "_", definitionType, level + 1));
			} else {
				result.put("items", Collections.emptyList());
			}
			result.put("type", target.getDataType().getJavascriptType());
		} else if (DataType.Object == target.getDataType() || DataType.Any == target.getDataType()) {
			String voName = groupName + "«" + info.getPath().replaceFirst("/", "").replaceAll("/", "_") + (StringUtils.equals("response", definitionType) ? "«response«" : "«request«") + parentName + target.getName() + "»»»";

			Map<String, Object> definition = new HashMap<>(4);
			Map<String, Map<String, Object>> properties = new HashMap<>(target.getChildren().size());
			Set<String> requiredSet = new HashSet<>(target.getChildren().size());
			for (BaseDefinition obj : target.getChildren()) {
				properties.put(obj.getName(), doProcessDefinition(obj, info, groupName, parentName + target.getName() + "_", definitionType, level + 1));
				if (obj.isRequired()) {
					requiredSet.add(obj.getName());
				}
			}
			definition.put("properties", properties);
			definition.put("description", target.getDescription());
			definition.put("type", target.getDataType().getJavascriptType());
			definition.put("required", requiredSet);
			if (this.DEFINITION_MAP.containsKey(voName)) {
				// TODO 应该不会出现名字都一样的
				voName = voName.replace("»»»", "_" + level + "»»»");
			}

			this.DEFINITION_MAP.put(voName, definition);
			result.put("originalRef", voName);
			result.put("$ref", DEFINITION + voName);

		} else {
			result.put("example", target.getValue());
			result.put("type", target.getDataType().getJavascriptType());
		}
		return result;
	}
}
