package org.ssssssss.magicapi.swagger.entity;

import java.util.*;

/**
 * Swagger接口信息
 *
 * @author mxd
 */
public class SwaggerEntity {

	private String swagger = "2.0";

	private String host;

	private String basePath;

	private Info info;

	private final Set<Tag> tags = new TreeSet<>(Comparator.comparing(Tag::getName));

	private final Map<String, Object> definitions = new HashMap<>();

	private final Map<String, Map<String, Path>> paths = new HashMap<>();

	private static Map<String, Object> doProcessSchema(Object target) {
		Map<String, Object> result = new HashMap<>(3);
		result.put("type", getType(target));
		if (target instanceof List) {
			List<?> targetList = (List<?>) target;
			if (targetList.size() > 0) {
				result.put("items", doProcessSchema(targetList.get(0)));
			} else {
				result.put("items", Collections.emptyList());
			}
		} else if (target instanceof Map) {
			Set<Map.Entry> entries = ((Map) target).entrySet();
			Map<String, Map<String, Object>> properties = new HashMap<>(entries.size());
			for (Map.Entry entry : entries) {
				properties.put(Objects.toString(entry.getKey()), doProcessSchema(entry.getValue()));
			}
			result.put("properties", properties);
		} else {
			result.put("example", target == null ? "" : target);
			result.put("description", target == null ? "" : target);
		}
		return result;
	}

	private static String getType(Object object) {
		if (object instanceof Number) {
			return "number";
		}
		if (object instanceof String) {
			return "string";
		}
		if (object instanceof Boolean) {
			return "boolean";
		}
		if (object instanceof List) {
			return "array";
		}
		if (object instanceof Map) {
			return "object";
		}
		return "string";
	}

	public static Map<String, Object> createParameter(boolean required, String name, String in, String type, String description, Object example) {
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("required", required);
		parameter.put("name", name);
		parameter.put("in", in);
		parameter.put("description", description);

		if ("body".equalsIgnoreCase(in)) {
			Map<String, Object> schema = new HashMap<>();
			schema.put("type", type);
			schema.put("example", example);
			parameter.put("schema", schema);
		} else {
			parameter.put("x-example", example);
			parameter.put("type", type);
		}
		return parameter;
	}

	public Info getInfo() {
		return info;
	}

	public void setInfo(Info info) {
		this.info = info;
	}

	public void addPath(String path, String method, Path pathInfo) {
		Map<String, Path> map = paths.computeIfAbsent(path, k -> new HashMap<>());
		map.put(method.toLowerCase(), pathInfo);
	}

	public void addTag(String name, String description) {
		this.tags.add(new Tag(name, description));
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSwagger() {
		return swagger;
	}

	public void setSwagger(String swagger) {
		this.swagger = swagger;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public Map<String, Object> getDefinitions() {
		return definitions;
	}

	public void addDefinitions(String path, Object definition) {
		definitions.put(path, definition);
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public Map<String, Map<String, Path>> getPaths() {
		return paths;
	}

	public static class Concat {

		private String name;

		private String url;

		private String email;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}
	}

	public static class Info {

		private String description;

		private String version;

		private String title;

		private License license;

		private Concat concat;

		public Info(String description, String version, String title, License license, Concat concat) {
			this.description = description;
			this.version = version;
			this.title = title;
			this.license = license;
			this.concat = concat;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public License getLicense() {
			return license;
		}

		public void setLicense(License license) {
			this.license = license;
		}

		public Concat getConcat() {
			return concat;
		}

		public void setConcat(Concat concat) {
			this.concat = concat;
		}
	}

	public static class Path {

		private List<String> tags = new ArrayList<>();

		private String summary;

		private String description;

		private final String operationId;

		private List<String> produces = new ArrayList<>();

		private List<String> consumes = new ArrayList<>();

		private List<Map<String, Object>> parameters = new ArrayList<>();

		private Map<String, Object> responses = new HashMap<>();

		public Path(String operationId) {
			this.operationId = operationId;
		}

		public void addProduce(String produce) {
			this.produces.add(produce);
		}

		public void addConsume(String consume) {
			this.consumes.add(consume);
		}

		public void addParameter(Map<String, Object> parameter) {
			this.parameters.add(parameter);
		}

		public String getOperationId() {
			return operationId;
		}

		public void addResponse(String status, Object object) {
			Map<String, Object> response = new HashMap<>();
			response.put("description", "OK");
			response.put("schema", doProcessSchema(object));
			response.put("example", object);
			this.responses.put(status, response);
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public void addTag(String tag) {
			this.tags.add(tag);
		}

		public String getSummary() {
			return summary;
		}

		public void setSummary(String summary) {
			this.summary = summary;
		}

		public List<String> getProduces() {
			return produces;
		}

		public void setProduces(List<String> produces) {
			this.produces = produces;
		}

		public List<String> getConsumes() {
			return consumes;
		}

		public void setConsumes(List<String> consumes) {
			this.consumes = consumes;
		}

		public List<Map<String, Object>> getParameters() {
			return parameters;
		}

		public void setParameters(List<Map<String, Object>> parameters) {
			this.parameters = parameters;
		}

		public Map<String, Object> getResponses() {
			return responses;
		}

		public void setResponses(Map<String, Object> responses) {
			this.responses = responses;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class Parameter {

		private String name;

		private String in;

		private boolean required = false;

		private String type;

		private Object schema;

		private String description;

		private Object example;

		public Parameter(boolean required, String name, String in, String type, String description, Object example) {
			this.name = name;
			this.in = in;
			this.type = type;
			this.description = description;
			this.required = required;
			if ("body".equalsIgnoreCase(in)) {
				this.schema = "";
			} else {
				this.example = example;
				/*
				 * fix swagger文档使用knife4j时无法显示接口详情的问题（query类型参数）
				 * schema 需设置为空字符串，否则请求参数中数据类型字段显示不正确
				 */
				this.schema = "";
			}
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getIn() {
			return in;
		}

		public void setIn(String in) {
			this.in = in;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Object getSchema() {
			return schema;
		}

		public void setSchema(Object schema) {
			this.schema = schema;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Object getExample() {
			return example;
		}

		public void setExample(Object example) {
			this.example = example;
		}
	}

	public static class Tag {

		private String name;

		private String description;

		public Tag(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Tag tag = (Tag) o;
			return Objects.equals(name, tag.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}


	public static class License {

		private String name;

		private String url;

		public License(String name, String url) {
			this.name = name;
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}
}
