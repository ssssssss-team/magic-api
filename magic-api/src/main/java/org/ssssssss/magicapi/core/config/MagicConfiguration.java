package org.ssssssss.magicapi.core.config;

import org.springframework.http.converter.HttpMessageConverter;
import org.ssssssss.magicapi.core.resource.Resource;
import org.ssssssss.magicapi.core.web.RequestHandler;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.core.service.MagicAPIService;
import org.ssssssss.magicapi.backup.service.MagicBackupService;
import org.ssssssss.magicapi.core.service.MagicNotifyService;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;

import java.util.ArrayList;
import java.util.List;

public class MagicConfiguration {

	/**
	 * 拦截器
	 */
	private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();

	/**
	 * 编辑器配置文件
	 */
	private String editorConfig;

	private MagicAPIService magicAPIService;

	private MagicDynamicDataSource magicDynamicDataSource;

	/**
	 * 请求出错时，是否抛出异常
	 */
	private boolean throwException = false;

	/**
	 * 结果处理器
	 */
	private ResultProvider resultProvider;

	private MagicNotifyService magicNotifyService;

	private String instanceId;

	private Resource workspace;

	private List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();

	private AuthorizationInterceptor authorizationInterceptor;

	private MagicBackupService magicBackupService;

	private static MagicResourceService magicResourceService;

	private List<MagicDynamicRegistry<? extends MagicEntity>> magicDynamicRegistries;

	/**
	 * debug 超时时间
	 */
	private int debugTimeout;

	private boolean enableWeb = false;

	public void addRequestInterceptor(RequestInterceptor requestInterceptor) {
		this.requestInterceptors.add(requestInterceptor);
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

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public MagicBackupService getMagicBackupService() {
		return magicBackupService;
	}

	public void setMagicBackupService(MagicBackupService magicBackupService) {
		this.magicBackupService = magicBackupService;
	}

	public MagicDynamicDataSource getMagicDynamicDataSource() {
		return magicDynamicDataSource;
	}

	public void setMagicDynamicDataSource(MagicDynamicDataSource magicDynamicDataSource) {
		this.magicDynamicDataSource = magicDynamicDataSource;
	}

	public static MagicResourceService getMagicResourceService() {
		return MagicConfiguration.magicResourceService;
	}

	public void setMagicResourceService(MagicResourceService magicResourceService) {
		MagicConfiguration.magicResourceService = magicResourceService;
	}

	public List<MagicDynamicRegistry<? extends MagicEntity>> getMagicDynamicRegistries() {
		return magicDynamicRegistries;
	}

	public void setMagicDynamicRegistries(List<MagicDynamicRegistry<? extends MagicEntity>> magicDynamicRegistries) {
		this.magicDynamicRegistries = magicDynamicRegistries;
	}

	/**
	 * 打印banner
	 */
	public void printBanner(List<String> plugins) {
		System.out.println("  __  __                _           _     ____  ___ ");
		System.out.println(" |  \\/  |  __ _   __ _ (_)  ___    / \\   |  _ \\|_ _|");
		System.out.println(" | |\\/| | / _` | / _` || | / __|  / _ \\  | |_) || | ");
		System.out.println(" | |  | || (_| || (_| || || (__  / ___ \\ |  __/ | | ");
		System.out.println(" |_|  |_| \\__,_| \\__, ||_| \\___|/_/   \\_\\|_|   |___|");
		System.out.println("                  |___/                        " + RequestHandler.class.getPackage().getImplementationVersion());
		if(!plugins.isEmpty()){
			System.out.println("集成插件：");
			plugins.stream().peek(it -> System.out.print("- ")).forEach(System.out::println);
		}
	}
}
