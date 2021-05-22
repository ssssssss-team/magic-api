package org.ssssssss.magicapi.config;

import org.springframework.http.converter.HttpMessageConverter;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.controller.RequestHandler;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.provider.*;

import java.util.ArrayList;
import java.util.List;

public class MagicConfiguration {

	/**
	 * 拦截器
	 */
	private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
	/**
	 * 接口映射
	 */
	private MappingHandlerMapping mappingHandlerMapping;
	/**
	 * 函数管理
	 */
	private MagicFunctionManager magicFunctionManager;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 编辑器配置文件
	 */
	private String editorConfig;
	/**
	 * 接口查询Service
	 */
	private ApiServiceProvider apiServiceProvider;

	/**
	 * 分组查询Service
	 */
	private GroupServiceProvider groupServiceProvider;

	/**
	 * 函数查询Service
	 */
	private FunctionServiceProvider functionServiceProvider;

	/**
	 * 动态数据源
	 */
	private MagicDynamicDataSource magicDynamicDataSource;

	private MagicAPIService magicAPIService;

	/**
	 * 请求出错时，是否抛出异常
	 */
	private boolean throwException = false;

	/**
	 * 结果处理器
	 */
	private ResultProvider resultProvider;

	private Resource workspace;

	private List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();

	private AuthorizationInterceptor authorizationInterceptor;

	private MagicNotifyService magicNotifyService;

	/**
	 * debug 超时时间
	 */
	private int debugTimeout;

	private boolean enableWeb = false;

	public void addRequestInterceptor(RequestInterceptor requestInterceptor) {
		this.requestInterceptors.add(requestInterceptor);
	}

	public MappingHandlerMapping getMappingHandlerMapping() {
		return mappingHandlerMapping;
	}

	public void setMappingHandlerMapping(MappingHandlerMapping mappingHandlerMapping) {
		this.mappingHandlerMapping = mappingHandlerMapping;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AuthorizationInterceptor getAuthorizationInterceptor() {
		return authorizationInterceptor;
	}

	public void setAuthorizationInterceptor(AuthorizationInterceptor authorizationInterceptor) {
		this.authorizationInterceptor = authorizationInterceptor;
	}

	public List<RequestInterceptor> getRequestInterceptors() {
		return requestInterceptors;
	}

	public ApiServiceProvider getApiServiceProvider() {
		return apiServiceProvider;
	}

	public void setApiServiceProvider(ApiServiceProvider apiServiceProvider) {
		this.apiServiceProvider = apiServiceProvider;
	}

	public GroupServiceProvider getGroupServiceProvider() {
		return groupServiceProvider;
	}

	public void setGroupServiceProvider(GroupServiceProvider groupServiceProvider) {
		this.groupServiceProvider = groupServiceProvider;
	}

	public MagicDynamicDataSource getMagicDynamicDataSource() {
		return magicDynamicDataSource;
	}

	public void setMagicDynamicDataSource(MagicDynamicDataSource magicDynamicDataSource) {
		this.magicDynamicDataSource = magicDynamicDataSource;
	}

	public boolean isThrowException() {
		return throwException;
	}

	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

	public ResultProvider getResultProvider() {
		return resultProvider;
	}

	public void setResultProvider(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	public List<HttpMessageConverter<?>> getHttpMessageConverters() {
		return httpMessageConverters;
	}

	public void setHttpMessageConverters(List<HttpMessageConverter<?>> httpMessageConverters) {
		this.httpMessageConverters = httpMessageConverters;
	}

	public int getDebugTimeout() {
		return debugTimeout;
	}

	public void setDebugTimeout(int debugTimeout) {
		this.debugTimeout = debugTimeout;
	}

	public boolean isEnableWeb() {
		return enableWeb;
	}

	public void setEnableWeb(boolean enableWeb) {
		this.enableWeb = enableWeb;
	}

	public FunctionServiceProvider getFunctionServiceProvider() {
		return functionServiceProvider;
	}

	public void setFunctionServiceProvider(FunctionServiceProvider functionServiceProvider) {
		this.functionServiceProvider = functionServiceProvider;
	}

	public MagicFunctionManager getMagicFunctionManager() {
		return magicFunctionManager;
	}

	public void setMagicFunctionManager(MagicFunctionManager magicFunctionManager) {
		this.magicFunctionManager = magicFunctionManager;
	}

	public String getEditorConfig() {
		return editorConfig;
	}

	public void setEditorConfig(String editorConfig) {
		this.editorConfig = editorConfig;
	}

	public Resource getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Resource workspace) {
		this.workspace = workspace;
	}

	public MagicAPIService getMagicAPIService() {
		return magicAPIService;
	}

	public void setMagicAPIService(MagicAPIService magicAPIService) {
		this.magicAPIService = magicAPIService;
	}

	public MagicNotifyService getMagicNotifyService() {
		return magicNotifyService;
	}

	public void setMagicNotifyService(MagicNotifyService magicNotifyService) {
		this.magicNotifyService = magicNotifyService;
	}

	/**
	 * 打印banner
	 */
	public void printBanner() {
		System.out.println("  __  __                _           _     ____  ___ ");
		System.out.println(" |  \\/  |  __ _   __ _ (_)  ___    / \\   |  _ \\|_ _|");
		System.out.println(" | |\\/| | / _` | / _` || | / __|  / _ \\  | |_) || | ");
		System.out.println(" | |  | || (_| || (_| || || (__  / ___ \\ |  __/ | | ");
		System.out.println(" |_|  |_| \\__,_| \\__, ||_| \\___|/_/   \\_\\|_|   |___|");
		System.out.println("                  |___/                        " + RequestHandler.class.getPackage().getImplementationVersion());
	}
}
