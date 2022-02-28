package org.ssssssss.magicapi.spring.boot.starter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.ssssssss.magicapi.backup.service.MagicBackupService;
import org.ssssssss.magicapi.backup.service.MagicDatabaseBackupService;
import org.ssssssss.magicapi.backup.web.MagicBackupController;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.core.config.*;
import org.ssssssss.magicapi.core.exception.MagicAPIException;
import org.ssssssss.magicapi.core.handler.MagicCoordinationHandler;
import org.ssssssss.magicapi.core.handler.MagicDebugHandler;
import org.ssssssss.magicapi.core.handler.MagicWebSocketDispatcher;
import org.ssssssss.magicapi.core.handler.MagicWorkbenchHandler;
import org.ssssssss.magicapi.core.interceptor.*;
import org.ssssssss.magicapi.core.logging.LoggerManager;
import org.ssssssss.magicapi.core.model.DataType;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.resource.DatabaseResource;
import org.ssssssss.magicapi.core.resource.ResourceAdapter;
import org.ssssssss.magicapi.core.service.*;
import org.ssssssss.magicapi.core.service.impl.DefaultMagicAPIService;
import org.ssssssss.magicapi.core.service.impl.DefaultMagicResourceService;
import org.ssssssss.magicapi.core.service.impl.RequestMagicDynamicRegistry;
import org.ssssssss.magicapi.core.web.MagicResourceController;
import org.ssssssss.magicapi.core.web.MagicWorkbenchController;
import org.ssssssss.magicapi.core.web.RequestHandler;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;
import org.ssssssss.magicapi.datasource.service.DataSourceEncryptProvider;
import org.ssssssss.magicapi.datasource.web.MagicDataSourceController;
import org.ssssssss.magicapi.function.service.FunctionMagicDynamicRegistry;
import org.ssssssss.magicapi.jsr223.LanguageProvider;
import org.ssssssss.magicapi.modules.servlet.RequestModule;
import org.ssssssss.magicapi.modules.servlet.ResponseModule;
import org.ssssssss.magicapi.modules.spring.EnvModule;
import org.ssssssss.magicapi.utils.Mapping;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.exception.MagicScriptRuntimeException;
import org.ssssssss.script.functions.DynamicModuleImport;
import org.ssssssss.script.functions.ExtensionMethod;
import org.ssssssss.script.parsing.ast.statement.AsyncCall;
import org.ssssssss.script.reflection.JavaReflection;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * magic-api自动配置类
 *
 * @author mxd
 */
@Configuration
@ConditionalOnClass({RequestMappingHandlerMapping.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
@Import({MagicJsonAutoConfiguration.class, ApplicationUriPrinter.class, MagicModuleConfiguration.class, MagicDynamicRegistryConfiguration.class})
@EnableWebSocket
@AutoConfigureAfter(MagicPluginConfiguration.class)
public class MagicAPIAutoConfiguration implements WebMvcConfigurer, WebSocketConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);

	/**
	 * 请求拦截器
	 */
	private final ObjectProvider<List<RequestInterceptor>> requestInterceptorsProvider;


	/**
	 * 自定义的类型扩展
	 */
	private final ObjectProvider<List<ExtensionMethod>> extensionMethodsProvider;

	/**
	 * 内置的消息转换
	 */
	private final ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConvertersProvider;


	private final ObjectProvider<AuthorizationInterceptor> authorizationInterceptorProvider;

	/**
	 * 自定义的函数
	 */
	private final ObjectProvider<List<MagicFunction>> magicFunctionsProvider;

	private final ObjectProvider<List<MagicPluginConfiguration>> magicPluginsProvider;

	private final ObjectProvider<MagicNotifyService> magicNotifyServiceProvider;

	private final ObjectProvider<List<MagicDynamicRegistry<? extends MagicEntity>>> magicDynamicRegistriesProvider;

	private final ObjectProvider<List<MagicResourceStorage<? extends MagicEntity>>> magicResourceStoragesProvider;

	private final ObjectProvider<DataSourceEncryptProvider> dataSourceEncryptProvider;

	private final MagicCorsFilter magicCorsFilter = new MagicCorsFilter();

	private final MagicAPIProperties properties;

	private final ApplicationContext applicationContext;

	private boolean registerMapping = false;

	private boolean registerInterceptor = false;

	private boolean registerWebsocket = false;

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private DefaultAuthorizationInterceptor defaultAuthorizationInterceptor;

	public MagicAPIAutoConfiguration(MagicAPIProperties properties,
									 ObjectProvider<List<RequestInterceptor>> requestInterceptorsProvider,
									 ObjectProvider<List<ExtensionMethod>> extensionMethodsProvider,
									 ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConvertersProvider,
									 ObjectProvider<List<MagicFunction>> magicFunctionsProvider,
									 ObjectProvider<List<MagicPluginConfiguration>> magicPluginsProvider,
									 ObjectProvider<MagicNotifyService> magicNotifyServiceProvider,
									 ObjectProvider<AuthorizationInterceptor> authorizationInterceptorProvider,
									 ObjectProvider<DataSourceEncryptProvider> dataSourceEncryptProvider,
									 ObjectProvider<List<MagicDynamicRegistry<? extends MagicEntity>>> magicDynamicRegistriesProvider,
									 ObjectProvider<List<MagicResourceStorage<? extends MagicEntity>>> magicResourceStoragesProvider,
									 ApplicationContext applicationContext
	) {
		this.properties = properties;
		this.requestInterceptorsProvider = requestInterceptorsProvider;
		this.extensionMethodsProvider = extensionMethodsProvider;
		this.httpMessageConvertersProvider = httpMessageConvertersProvider;
		this.magicFunctionsProvider = magicFunctionsProvider;
		this.magicPluginsProvider = magicPluginsProvider;
		this.magicNotifyServiceProvider = magicNotifyServiceProvider;
		this.authorizationInterceptorProvider = authorizationInterceptorProvider;
		this.dataSourceEncryptProvider = dataSourceEncryptProvider;
		this.magicDynamicRegistriesProvider = magicDynamicRegistriesProvider;
		this.magicResourceStoragesProvider = magicResourceStoragesProvider;
		this.applicationContext = applicationContext;
	}

	@Bean
	@ConditionalOnMissingBean(org.ssssssss.magicapi.core.resource.Resource.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "database")
	public org.ssssssss.magicapi.core.resource.Resource magicDatabaseResource(MagicDynamicDataSource magicDynamicDataSource) {
		Resource resourceConfig = properties.getResource();
		if (magicDynamicDataSource.isEmpty()) {
			throw new MagicAPIException("当前未配置数据源，如已配置，请引入 spring-boot-starter-jdbc 后在试!");
		}
		MagicDynamicDataSource.DataSourceNode dataSourceNode = magicDynamicDataSource.getDataSource(resourceConfig.getDatasource());
		return new DatabaseResource(new JdbcTemplate(dataSourceNode.getDataSource()), resourceConfig.getTableName(), resourceConfig.getPrefix(), resourceConfig.isReadonly());
	}

	@Bean
	@ConditionalOnMissingBean(org.ssssssss.magicapi.core.resource.Resource.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "file", matchIfMissing = true)
	public org.ssssssss.magicapi.core.resource.Resource magicResource() throws IOException {
		Resource resourceConfig = properties.getResource();
		return ResourceAdapter.getResource(resourceConfig.getLocation(), resourceConfig.isReadonly());
	}

	@Bean
	@ConditionalOnMissingBean(MagicBackupService.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "backup.enable", havingValue = "true")
	public MagicBackupService magicDatabaseBackupService(MagicDynamicDataSource magicDynamicDataSource) {
		Backup backupConfig = properties.getBackup();
		MagicDynamicDataSource.DataSourceNode dataSourceNode = magicDynamicDataSource.getDataSource(backupConfig.getDatasource());
		return new MagicDatabaseBackupService(new JdbcTemplate(dataSourceNode.getDataSource()), backupConfig.getTableName());
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String web = properties.getWeb();
		if (web != null && !registerMapping) {
			registerMapping = true;
			// 当开启了UI界面时，收集日志
			LoggerManager.createMagicAppender();
			// 配置静态资源路径
			registry.addResourceHandler(web + "/**").addResourceLocations("classpath:/magic-editor/");
		}
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (!registerInterceptor) {
			registerInterceptor = true;
			registry.addInterceptor(new MagicWebRequestInterceptor(properties.isSupportCrossDomain() ? magicCorsFilter : null, authorizationInterceptorProvider.getIfAvailable(this::createAuthorizationInterceptor)))
					.addPathPatterns("/**");
		}
	}

	@Bean
	@ConditionalOnProperty(prefix = "magic-api", value = "support-cross-domain", havingValue = "true", matchIfMissing = true)
	public FilterRegistrationBean<MagicCorsFilter> magicCorsFilterRegistrationBean() {
		FilterRegistrationBean<MagicCorsFilter> registration = new FilterRegistrationBean<>(magicCorsFilter);
		registration.addUrlPatterns("/*");
		registration.setName("Magic Cors Filter");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

	@Bean
	@ConditionalOnMissingBean
	public MagicResourceService magicResourceService(org.ssssssss.magicapi.core.resource.Resource workspace) {
		return new DefaultMagicResourceService(workspace, magicResourceStoragesProvider.getObject(), applicationContext);
	}


	@Bean
	@ConditionalOnMissingBean(MagicNotifyService.class)
	public MagicNotifyService magicNotifyService() {
		logger.info("未配置集群通知服务，本实例不会推送通知，集群环境下可能会有问题，如需开启，请引用magic-api-plugin-cluster插件");
		return magicNotify -> {
		};
	}

	/**
	 * 注入API调用Service
	 */
	@Bean
	@ConditionalOnMissingBean
	public MagicAPIService magicAPIService(ResultProvider resultProvider, MagicResourceService magicResourceService, RequestMagicDynamicRegistry requestMagicDynamicRegistry, FunctionMagicDynamicRegistry functionMagicDynamicRegistry) {
		return new DefaultMagicAPIService(resultProvider, properties.getInstanceId(), magicResourceService, requestMagicDynamicRegistry, functionMagicDynamicRegistry, properties.isThrowException(), applicationContext);
	}

	/**
	 * 注册模块、类型扩展
	 */
	private void setupMagicModules(List<ExtensionMethod> extensionMethods, List<LanguageProvider> languageProviders) {
		// 设置脚本import时 class加载策略
		MagicResourceLoader.setClassLoader((className) -> {
			try {
				return applicationContext.getBean(className);
			} catch (Exception e) {
				Class<?> clazz = null;
				try {
					clazz = Class.forName(className);
					return applicationContext.getBean(clazz);
				} catch (Exception ex) {
					if (clazz == null) {
						throw new MagicScriptRuntimeException(new ClassNotFoundException(className));
					}
					return clazz;
				}
			}
		});
		MagicResourceLoader.addScriptLanguageLoader(language -> languageProviders.stream()
				.filter(it -> it.support(language))
				.findFirst().<BiFunction<Map<String, Object>, String, Object>>map(languageProvider -> (context, script) -> {
					try {
						return languageProvider.execute(language, script, context);
					} catch (Exception e) {
						throw new MagicAPIException(e.getMessage(), e);
					}
				}).orElse(null)
		);
		logger.info("注册模块:{} -> {}", "log", Logger.class);
		MagicResourceLoader.addModule("log", new DynamicModuleImport(Logger.class, context -> LoggerFactory.getLogger(Objects.toString(context.getScriptName(), "Unknown"))));
		List<String> importModules = properties.getAutoImportModuleList();
		applicationContext.getBeansWithAnnotation(MagicModule.class).values().forEach(module -> {
			String moduleName = module.getClass().getAnnotation(MagicModule.class).value();
			logger.info("注册模块:{} -> {}", moduleName, module.getClass());
			MagicResourceLoader.addModule(moduleName, module);
		});
		MagicResourceLoader.getModuleNames().stream().filter(importModules::contains).forEach(moduleName -> {
			logger.info("自动导入模块：{}", moduleName);
			MagicScriptEngine.addDefaultImport(moduleName, MagicResourceLoader.loadModule(moduleName));
		});
		properties.getAutoImportPackageList().forEach(importPackage -> {
			logger.info("自动导包：{}", importPackage);
			MagicResourceLoader.addPackage(importPackage);
		});
		extensionMethods.forEach(extension -> extension.supports().forEach(support -> {
			logger.info("注册扩展:{} -> {}", support, extension.getClass());
			JavaReflection.registerMethodExtension(support, extension);
		}));
	}

	@Bean
	public MagicConfiguration magicConfiguration(List<LanguageProvider> languageProviders,
												 org.ssssssss.magicapi.core.resource.Resource magicResource,
												 ResultProvider resultProvider,
												 MagicResourceService magicResourceService,
												 MagicAPIService magicAPIService,
												 MagicNotifyService magicNotifyService,
												 RequestMagicDynamicRegistry requestMagicDynamicRegistry,
												 @Autowired(required = false) MagicBackupService magicBackupService) throws NoSuchMethodException {
		logger.info("magic-api工作目录:{}", magicResource);
		AsyncCall.setThreadPoolExecutorSize(properties.getThreadPoolExecutorSize());
		DataType.DATE_PATTERNS = properties.getDatePattern();
		MagicScript.setCompileCache(properties.getCompileCacheSize());
		// 设置响应结果的code值
		ResponseCode responseCodeConfig = properties.getResponseCode();
		Constants.RESPONSE_CODE_SUCCESS = responseCodeConfig.getSuccess();
		Constants.RESPONSE_CODE_INVALID = responseCodeConfig.getInvalid();
		Constants.RESPONSE_CODE_EXCEPTION = responseCodeConfig.getException();
		// 设置模块和扩展方法
		setupMagicModules(extensionMethodsProvider.getIfAvailable(Collections::emptyList), languageProviders);
		MagicConfiguration configuration = new MagicConfiguration();
		configuration.setMagicAPIService(magicAPIService);
		configuration.setMagicNotifyService(magicNotifyService);
		configuration.setInstanceId(properties.getInstanceId());
		configuration.setMagicResourceService(magicResourceService);
		configuration.setMagicDynamicRegistries(magicDynamicRegistriesProvider.getObject());
		configuration.setMagicBackupService(magicBackupService);
		Security securityConfig = properties.getSecurityConfig();
		configuration.setDebugTimeout(properties.getDebug().getTimeout());
		configuration.setHttpMessageConverters(httpMessageConvertersProvider.getIfAvailable(Collections::emptyList));
		configuration.setResultProvider(resultProvider);
		configuration.setThrowException(properties.isThrowException());
		configuration.setEditorConfig(properties.getEditorConfig());
		configuration.setWorkspace(magicResource);
		configuration.setAuthorizationInterceptor(authorizationInterceptorProvider.getIfAvailable(this::createAuthorizationInterceptor));
		// 注册函数
		this.magicFunctionsProvider.getIfAvailable(Collections::emptyList).forEach(JavaReflection::registerFunction);
		// 向页面传递配置信息时不传递用户名密码，增强安全性
		securityConfig.setUsername(null);
		securityConfig.setPassword(null);
		requestMagicDynamicRegistry.setHandler(new RequestHandler(configuration, requestMagicDynamicRegistry));
		List<MagicPluginConfiguration> pluginConfigurations = magicPluginsProvider.getIfAvailable(Collections::emptyList);
		List<Plugin> plugins = pluginConfigurations.stream().map(MagicPluginConfiguration::plugin).collect(Collectors.toList());
		// 构建UI请求处理器
		String base = properties.getWeb();
		Mapping mapping = Mapping.create(requestMappingHandlerMapping, base);
		MagicWorkbenchController magicWorkbenchController = new MagicWorkbenchController(configuration, properties, plugins);
		if (base != null) {
			configuration.setEnableWeb(true);
			mapping.registerController(magicWorkbenchController)
					.registerController(new MagicResourceController(configuration))
					.registerController(new MagicDataSourceController(configuration))
					.registerController(new MagicBackupController(configuration));
			pluginConfigurations.forEach(it -> it.controllerRegister().register(mapping, configuration));
		}
		// 注册接收推送的接口
		if (StringUtils.isNotBlank(properties.getSecretKey())) {
			mapping.register(mapping.paths(properties.getPushPath()).methods(RequestMethod.POST).build(), magicWorkbenchController, MagicWorkbenchController.class.getDeclaredMethod("receivePush", MultipartFile.class, String.class, Long.class, String.class));
		}
		// 设置拦截器信息
		this.requestInterceptorsProvider.getIfAvailable(Collections::emptyList).forEach(interceptor -> {
			logger.info("注册请求拦截器：{}", interceptor.getClass());
			configuration.addRequestInterceptor(interceptor);
		});
		// 打印banner
		if (this.properties.isBanner()) {
			configuration.printBanner(plugins.stream().map(Plugin::getName).collect(Collectors.toList()));
		}
		if (magicBackupService == null) {
			logger.error("当前备份设置未配置，强烈建议配置备份设置，以免代码丢失。");
		}
		// 备份清理
		if (properties.getBackup().isEnable() && properties.getBackup().getMaxHistory() > 0 && magicBackupService != null) {
			long interval = properties.getBackup().getMaxHistory() * 86400000L;
			// 1小时执行1次
			new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "magic-api-clean-task")).scheduleAtFixedRate(() -> {
				try {
					long count = magicBackupService.removeBackupByTimestamp(System.currentTimeMillis() - interval);
					if (count > 0) {
						logger.info("已删除备份记录{}条", count);
					}
				} catch (Exception e) {
					logger.error("删除备份记录时出错", e);
				}
			}, 1, 1, TimeUnit.HOURS);
		}
		return configuration;
	}

	public AuthorizationInterceptor createAuthorizationInterceptor() {
		if (defaultAuthorizationInterceptor != null) {
			return defaultAuthorizationInterceptor;
		}
		Security securityConfig = properties.getSecurityConfig();
		defaultAuthorizationInterceptor = new DefaultAuthorizationInterceptor(securityConfig.getUsername(), securityConfig.getPassword());
		return defaultAuthorizationInterceptor;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
		String web = properties.getWeb();
		MagicNotifyService magicNotifyService = magicNotifyServiceProvider.getObject();
		WebSocketSessionManager.setMagicNotifyService(magicNotifyService);
		if (web != null && !registerWebsocket) {
			registerWebsocket = true;
			MagicWebSocketDispatcher dispatcher = new MagicWebSocketDispatcher(properties.getInstanceId(), magicNotifyService, Arrays.asList(
					new MagicDebugHandler(),
					new MagicCoordinationHandler(),
					new MagicWorkbenchHandler(authorizationInterceptorProvider.getIfAvailable(this::createAuthorizationInterceptor))
			));
			WebSocketHandlerRegistration registration = webSocketHandlerRegistry.addHandler(dispatcher, web + "/console");
			if (properties.isSupportCrossDomain()) {
				registration.setAllowedOrigins("*");
			}
		}
	}
}
