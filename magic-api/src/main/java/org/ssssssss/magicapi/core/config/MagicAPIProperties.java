package org.ssssssss.magicapi.core.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.ssssssss.magicapi.core.web.RequestHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * magic-api配置信息
 *
 * @author mxd
 */
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

	/**
	 * 远程推送时的秘钥，未配置则不开启
	 *
	 * @since 1.2.1
	 */
	private String secretKey;

	/**
	 * 远程推送的路径，默认为/_magic-api-sync
	 *
	 * @since 1.2.1
	 */
	private String pushPath = "/_magic-api-sync";

	/**
	 * 启动完成之后打印地址
	 */
	private boolean showUrl = true;

	/**
	 * 是否要打印SQL
	 *
	 * @since 1.5.0
	 */
	private boolean showSql = true;

	/**
	 * 请求参数日期格式
	 *
	 * @since 1.5.2
	 */
	private String[] datePattern = new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmmss", "yyyyMMdd"};

	/**
	 * 编译缓存容量
	 *
	 * @since 1.5.3
	 */
	private int compileCacheSize = 500;

	/**
	 * 是否持久化保存ResponseBody
	 *
	 * @since 1.5.3
	 */
	private boolean persistenceResponseBody = true;

	/**
	 * 实例ID，集群环境下，要保证每台机器不同。默认启动后随机生成uuid
	 */
	private String instanceId = UUID.randomUUID().toString();

	@NestedConfigurationProperty
	private Security security = new Security();

	@NestedConfigurationProperty
	private Page page = new Page();

	@NestedConfigurationProperty
	private Cache cache = new Cache();

	@NestedConfigurationProperty
	private Debug debug = new Debug();

	@NestedConfigurationProperty
	private Resource resource = new Resource();

	@NestedConfigurationProperty
	private ResponseCode responseCode = new ResponseCode();

	@NestedConfigurationProperty
	private Crud crud = new Crud();

	@NestedConfigurationProperty
	private Backup backup = new Backup();

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


	public List<String> getAutoImportModuleList() {
		return Arrays.asList(autoImportModule.replaceAll("\\s", "").split(","));
	}

	public List<String> getAutoImportPackageList() {
		if (autoImportPackage == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(autoImportPackage.replaceAll("\\s", "").split(","));
	}

	public String getVersion() {
		return version;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean isThrowException() {
		return throwException;
	}

	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

	public String getAutoImportModule() {
		return autoImportModule;
	}

	public void setAutoImportModule(String autoImportModule) {
		this.autoImportModule = autoImportModule;
	}

	public String getAutoImportPackage() {
		return autoImportPackage;
	}

	public void setAutoImportPackage(String autoImportPackage) {
		this.autoImportPackage = autoImportPackage;
	}

	public boolean isAllowOverride() {
		return allowOverride;
	}

	public void setAllowOverride(boolean allowOverride) {
		this.allowOverride = allowOverride;
	}

	public int getThreadPoolExecutorSize() {
		return threadPoolExecutorSize;
	}

	public void setThreadPoolExecutorSize(int threadPoolExecutorSize) {
		this.threadPoolExecutorSize = threadPoolExecutorSize;
	}

	public String getEditorConfig() {
		return editorConfig;
	}

	public void setEditorConfig(String editorConfig) {
		this.editorConfig = editorConfig;
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

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getPushPath() {
		return pushPath;
	}

	public void setPushPath(String pushPath) {
		this.pushPath = pushPath;
	}

	public boolean isShowUrl() {
		return showUrl;
	}

	public void setShowUrl(boolean showUrl) {
		this.showUrl = showUrl;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public String[] getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String[] datePattern) {
		this.datePattern = datePattern;
	}

	public int getCompileCacheSize() {
		return compileCacheSize;
	}

	public void setCompileCacheSize(int compileCacheSize) {
		this.compileCacheSize = compileCacheSize;
	}

	public boolean isPersistenceResponseBody() {
		return persistenceResponseBody;
	}

	public void setPersistenceResponseBody(boolean persistenceResponseBody) {
		this.persistenceResponseBody = persistenceResponseBody;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public Debug getDebug() {
		return debug;
	}

	public void setDebug(Debug debug) {
		this.debug = debug;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public ResponseCode getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(ResponseCode responseCode) {
		this.responseCode = responseCode;
	}

	public Crud getCrud() {
		return crud;
	}

	public void setCrud(Crud crud) {
		this.crud = crud;
	}

	public Backup getBackup() {
		return backup;
	}

	public void setBackup(Backup backup) {
		this.backup = backup;
	}
}
