package org.ssssssss.magicapi.spring.boot.starter;

/**
 * Swagger 配置
 */
public class SwaggerConfig {

	/**
	 * 资源名称
	 */
	private String name = "MagicAPI接口";

	/**
	 * 资源位置
	 */
	private String location = "/v2/api-docs/magic-api/swagger2.json";

	/**
	 * 文档标题
	 */
	private String title = "MagicAPI Swagger Docs";

	/**
	 * 文档描述
	 */
	private String description = "MagicAPI 接口信息";

	/**
	 * 文档版本
	 */
	private String version = "1.0";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
}
