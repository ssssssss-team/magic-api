package org.ssssssss.magicapi.spring.boot.starter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.ssssssss.magicapi.controller.RequestHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "magic-api")
public class MagicAPIProperties {

	/**
	 * 版本号
	 */
	private final String version = RequestHandler.class.getPackage().getImplementationVersion();
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
	 * 自动导入的模块,多个用","分隔
	 *
	 * @since 0.3.2
	 */
	private String autoImportModule = "db";
	/**
	 * 可自动导入的包（目前只支持以.*结尾的通配符），多个用","分隔
	 *
	 * @since 0.4.0
	 */
	private String autoImportPackage;
	/**
	 * 自动刷新间隔，单位为秒，默认不开启
	 *
	 * @since 0.3.4
	 */
	@Deprecated
	private int refreshInterval = 0;
	/**
	 * 是否允许覆盖应用接口，默认为false
	 *
	 * @since 0.4.0
	 */
	private boolean allowOverride = false;
	/**
	 * SQL列名转换
	 *
	 * @since 0.5.0
	 */
	private String sqlColumnCase = "default";
	/**
	 * 线程核心数，需要>0，<=0时采用默认配置，即CPU核心数 * 2
	 *
	 * @since 0.4.5
	 */
	private int threadPoolExecutorSize = 0;
	/**
	 * 编辑器配置文件路径(js)
	 *
	 * @since 0.6.1
	 */
	private String editorConfig;
	/**
	 * 是否启用跨域支持
	 *
	 * @since 1.0.0
	 */
	private boolean supportCrossDomain = true;

	/**
	 * JSON响应结构表达式
	 *
	 * @since 1.0.0
	 */
	private String response;


	@NestedConfigurationProperty
	private SecurityConfig securityConfig = new SecurityConfig();

	@NestedConfigurationProperty
	private PageConfig pageConfig = new PageConfig();

	@NestedConfigurationProperty
	private CacheConfig cacheConfig = new CacheConfig();

	@NestedConfigurationProperty
	private DebugConfig debugConfig = new DebugConfig();

	@NestedConfigurationProperty
	private SwaggerConfig swaggerConfig = new SwaggerConfig();

	@NestedConfigurationProperty
	private ResourceConfig resource = new ResourceConfig();

	@NestedConfigurationProperty
	private ResponseCodeConfig responseCodeConfig = new ResponseCodeConfig();

	@NestedConfigurationProperty
	private ClusterConfig clusterConfig = new ClusterConfig();

	public String getEditorConfig() {
		return editorConfig;
	}

	public void setEditorConfig(String editorConfig) {
		this.editorConfig = editorConfig;
	}

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

	public String getSqlColumnCase() {
		return sqlColumnCase;
	}

	public void setSqlColumnCase(String sqlColumnCase) {
		this.sqlColumnCase = sqlColumnCase;
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

	public SecurityConfig getSecurityConfig() {
		return securityConfig;
	}

	public void setSecurityConfig(SecurityConfig securityConfig) {
		this.securityConfig = securityConfig;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
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

	public void setAutoImportModule(String autoImport) {
		this.autoImportModule = autoImport;
	}

	public List<String> getAutoImportModuleList() {
		return Arrays.asList(autoImportModule.replaceAll("\\s", "").split(","));
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

	public String getAutoImportPackage() {
		return autoImportPackage;
	}

	public void setAutoImportPackage(String autoImportPackage) {
		this.autoImportPackage = autoImportPackage;
	}

	public List<String> getAutoImportPackageList() {
		if (autoImportPackage == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(autoImportPackage.replaceAll("\\s", "").split(","));
	}

	public int getThreadPoolExecutorSize() {
		return threadPoolExecutorSize;
	}

	public void setThreadPoolExecutorSize(int threadPoolExecutorSize) {
		this.threadPoolExecutorSize = threadPoolExecutorSize;
	}

	public String getVersion() {
		return version;
	}


	public ResourceConfig getResource() {
		return resource;
	}

	public void setResource(ResourceConfig resource) {
		this.resource = resource;
	}

	public boolean isSupportCrossDomain() {
		return supportCrossDomain;
	}

	public void setSupportCrossDomain(boolean supportCrossDomain) {
		this.supportCrossDomain = supportCrossDomain;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public ResponseCodeConfig getResponseCodeConfig() {
		return responseCodeConfig;
	}

	public void setResponseCodeConfig(ResponseCodeConfig responseCodeConfig) {
		this.responseCodeConfig = responseCodeConfig;
	}

	public ClusterConfig getClusterConfig() {
		return clusterConfig;
	}

	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}
}
