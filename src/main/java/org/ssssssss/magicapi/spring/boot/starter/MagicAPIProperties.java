package org.ssssssss.magicapi.spring.boot.starter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "magic-api")
public class MagicAPIProperties {

	/**
	 * web页面入口
	 */
	private String web;

	/**
	 * 接口路径前缀
	 */
	private String prefix;

	/**
	 * 打印banner
	 */
	private boolean banner = true;

	/**
	 * 是否抛出异常
	 */
	private boolean throwException = false;

	/**
	 * 接口保存的数据源
	 */
	private String datasource;

	/**
	 * 自动导入的模块,多个用","分隔
	 * @since 0.3.2
	 */
	private String autoImportModule = "db";

	/**
	 * 自动刷新间隔，单位为秒，默认不开启
	 * @since 0.3.4
	 */
	private int refreshInterval = 0;

	/**
	 * 是否允许覆盖应用接口，默认为false
	 * @since 0.4.0
	 */
	private boolean allowOverride = false;

	/**
	 * 驼峰命名转换
	 */
	private boolean mapUnderscoreToCamelCase = true;

	@NestedConfigurationProperty
	private PageConfig pageConfig = new PageConfig();

	@NestedConfigurationProperty
	private CacheConfig cacheConfig = new CacheConfig();

	@NestedConfigurationProperty
	private DebugConfig debugConfig = new DebugConfig();

	@NestedConfigurationProperty
	private SwaggerConfig swaggerConfig = new SwaggerConfig();

	public String getWeb() {
		if (StringUtils.isBlank(web)) {
			return null;
		}
		if (web.endsWith("/**")) {
			return web.substring(0, web.length() - 3);
		}
		if (web.endsWith("/*")) {
			return web.substring(0, web.length() - 2);
		}
		if (web.endsWith("/")) {
			return web.substring(0, web.length() - 1);
		}
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public boolean isMapUnderscoreToCamelCase() {
		return mapUnderscoreToCamelCase;
	}

	public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
		this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
	}

	public boolean isBanner() {
		return banner;
	}

	public void setBanner(boolean banner) {
		this.banner = banner;
	}

	public PageConfig getPageConfig() {
		return pageConfig;
	}

	public void setPageConfig(PageConfig pageConfig) {
		this.pageConfig = pageConfig;
	}

	public boolean isThrowException() {
		return throwException;
	}

	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

	public CacheConfig getCacheConfig() {
		return cacheConfig;
	}

	public void setCacheConfig(CacheConfig cacheConfig) {
		this.cacheConfig = cacheConfig;
	}

	public DebugConfig getDebugConfig() {
		return debugConfig;
	}

	public void setDebugConfig(DebugConfig debugConfig) {
		this.debugConfig = debugConfig;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public SwaggerConfig getSwaggerConfig() {
		return swaggerConfig;
	}

	public void setSwaggerConfig(SwaggerConfig swaggerConfig) {
		this.swaggerConfig = swaggerConfig;
	}

	public String getAutoImportModule() {
		return autoImportModule;
	}

	public List<String> getAutoImportModuleList() {
		return Arrays.asList(autoImportModule.replaceAll("\\s","").split(","));
	}

	public void setAutoImportModule(String autoImport) {
		this.autoImportModule = autoImport;
	}

	public int getRefreshInterval() {
		return refreshInterval;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	public boolean isAllowOverride() {
		return allowOverride;
	}

	public void setAllowOverride(boolean allowOverride) {
		this.allowOverride = allowOverride;
	}
}
