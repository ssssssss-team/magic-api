package org.ssssssss.magicapi.spring.boot.starter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.adapter.ColumnMapperAdapter;
import org.ssssssss.magicapi.adapter.DialectAdapter;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.adapter.ResourceAdapter;
import org.ssssssss.magicapi.adapter.resource.DatabaseResource;
import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.config.*;
import org.ssssssss.magicapi.controller.*;
import org.ssssssss.magicapi.dialect.Dialect;
import org.ssssssss.magicapi.exception.MagicAPIException;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.DefaultAuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.interceptor.SQLInterceptor;
import org.ssssssss.magicapi.logging.LoggerManager;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.modules.*;
import org.ssssssss.magicapi.provider.*;
import org.ssssssss.magicapi.provider.impl.*;
import org.ssssssss.magicapi.utils.ClassScanner;
import org.ssssssss.magicapi.utils.Mapping;
import org.ssssssss.magicapi.utils.PathUtils;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.functions.ExtensionMethod;
import org.ssssssss.script.parsing.ast.statement.AsyncCall;
import org.ssssssss.script.reflection.AbstractReflection;
import org.ssssssss.script.reflection.JavaReflection;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

@Configuration
@ConditionalOnClass({RequestMappingHandlerMapping.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
@Import({MagicRedisAutoConfiguration.class, MagicMongoAutoConfiguration.class, MagicSwaggerConfiguration.class, MagicJsonAutoConfiguration.class})
public class MagicAPIAutoConfiguration implements WebMvcConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);

	/**
	 * 请求拦截器
	 */
	private final List<RequestInterceptor> requestInterceptors;

	/**
	 * SQL拦截器
	 */
	private final List<SQLInterceptor> sqlInterceptors;

	/**
	 * 自定义的类型扩展
	 */
	private final List<ExtensionMethod> extensionMethods;

	/**
	 * 内置的消息转换
	 */
	private final List<HttpMessageConverter<?>> httpMessageConverters;

	/**
	 * 自定义的方言
	 */
	private final List<Dialect> dialects;

	/**
	 * 自定义的列名转换
	 */
	private final List<ColumnMapperProvider> columnMapperProviders;


	private final AuthorizationInterceptor authorizationInterceptor;

	/**
	 * 自定义的函数
	 */
	private final List<MagicFunction> magicFunctions;

	private final Environment environment;

	private final MagicCorsFilter magicCorsFilter = new MagicCorsFilter();

	private final MagicAPIProperties properties;

	private final ApplicationContext applicationContext;

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private final RestTemplate restTemplate;

	private String ALL_CLASS_TXT;

	public MagicAPIAutoConfiguration(MagicAPIProperties properties,
									 ObjectProvider<List<Dialect>> dialectsProvider,
									 ObjectProvider<List<RequestInterceptor>> requestInterceptorsProvider,
									 ObjectProvider<List<SQLInterceptor>> sqlInterceptorsProvider,
									 ObjectProvider<List<ExtensionMethod>> extensionMethodsProvider,
									 ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConvertersProvider,
									 ObjectProvider<List<ColumnMapperProvider>> columnMapperProvidersProvider,
									 ObjectProvider<List<MagicFunction>> magicFunctionsProvider,
									 ObjectProvider<RestTemplate> restTemplateProvider,
									 ObjectProvider<AuthorizationInterceptor> authorizationInterceptorProvider,
									 Environment environment,
									 ApplicationContext applicationContext
									 ) {
		this.properties = properties;
		this.dialects = dialectsProvider.getIfAvailable(Collections::emptyList);
		this.requestInterceptors = requestInterceptorsProvider.getIfAvailable(Collections::emptyList);
		this.sqlInterceptors = sqlInterceptorsProvider.getIfAvailable(Collections::emptyList);
		this.extensionMethods = extensionMethodsProvider.getIfAvailable(Collections::emptyList);
		this.httpMessageConverters = httpMessageConvertersProvider.getIfAvailable(Collections::emptyList);
		this.columnMapperProviders = columnMapperProvidersProvider.getIfAvailable(Collections::emptyList);
		this.magicFunctions = magicFunctionsProvider.getIfAvailable(Collections::emptyList);
		this.restTemplate = restTemplateProvider.getIfAvailable(this::createRestTemplate);
		this.authorizationInterceptor = authorizationInterceptorProvider.getIfAvailable(this::createAuthorizationInterceptor);
		this.environment = environment;
		this.applicationContext = applicationContext;
	}

	private String redirectIndex(HttpServletRequest request) {
		if (request.getRequestURI().endsWith("/")) {
			return "redirect:./index.html";
		}
		return "redirect:" + properties.getWeb() + "/index.html";
	}

	@ResponseBody
	private MagicAPIProperties readConfig() {
		return properties;
	}

	@ResponseBody
	private String readClass() {
		if (ALL_CLASS_TXT == null) {
			try {
				ALL_CLASS_TXT = StringUtils.join(ClassScanner.scan(), "\r\n");
			} catch (Throwable t) {
				logger.warn("扫描Class失败", t);
				ALL_CLASS_TXT = "";
			}
		}
		return ALL_CLASS_TXT;
	}

	/**
	 * 注入动态数据源
	 */
	@Bean
	@ConditionalOnMissingBean(MagicDynamicDataSource.class)
	public MagicDynamicDataSource magicDynamicDataSource(@Autowired(required = false) DataSource dataSource) {
		MagicDynamicDataSource dynamicDataSource = new MagicDynamicDataSource();
		if (dataSource != null) {
			dynamicDataSource.put(dataSource);
		} else {
			logger.warn("当前数据源未配置");
		}
		return dynamicDataSource;
	}

	@Bean
	@ConditionalOnMissingBean(Resource.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "database")
	public Resource magicDatabaseResource(MagicDynamicDataSource magicDynamicDataSource) {
		ResourceConfig resourceConfig = properties.getResource();
		MagicDynamicDataSource.DataSourceNode dataSourceNode = magicDynamicDataSource.getDataSource(resourceConfig.getDatasource());
		if (dataSourceNode == null) {
			throw new IllegalArgumentException(String.format("找不到数据源:%s", resourceConfig.getDatasource()));
		}
		return new DatabaseResource(new JdbcTemplate(dataSourceNode.getDataSource()), resourceConfig.getTableName(), resourceConfig.getPrefix(), resourceConfig.isReadonly());
	}

	@Bean
	@ConditionalOnMissingBean(Resource.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "file", matchIfMissing = true)
	public Resource magicResource() throws IOException {
		ResourceConfig resourceConfig = properties.getResource();
		return ResourceAdapter.getResource(resourceConfig.getLocation(), resourceConfig.isReadonly());
	}


	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String web = properties.getWeb();
		if (web != null) {
			// 当开启了UI界面时，收集日志
			LoggerManager.createMagicAppender();
			// 配置静态资源路径
			registry.addResourceHandler(web + "/**").addResourceLocations("classpath:/magic-editor/");
			try {
				Mapping.create(requestMappingHandlerMapping)
						// 默认首页设置
						.register(RequestMappingInfo.paths(web).build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("redirectIndex", HttpServletRequest.class))
						// 读取配置
						.register(RequestMappingInfo.paths(web + "/config.json").build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("readConfig"))
						// 读取配置
						.register(RequestMappingInfo.paths(web + "/classes.txt").produces("text/plain").build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("readClass"));
			} catch (NoSuchMethodException ignored) {
			}
		}
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new MagicWebRequestInterceptor(properties.isSupportCrossDomain() ? magicCorsFilter : null, authorizationInterceptor))
				.addPathPatterns("/**");
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
	@ConditionalOnMissingBean(PageProvider.class)
	public PageProvider pageProvider() {
		PageConfig pageConfig = properties.getPageConfig();
		logger.info("未找到分页实现,采用默认分页实现,分页配置:(页码={},页大小={},默认首页={},默认页大小={})", pageConfig.getPage(), pageConfig.getSize(), pageConfig.getDefaultPage(), pageConfig.getDefaultSize());
		return new DefaultPageProvider(pageConfig.getPage(), pageConfig.getSize(), pageConfig.getDefaultPage(), pageConfig.getDefaultSize());
	}

	/**
	 * 注入结果构建方法
	 */
	@Bean
	@ConditionalOnMissingBean(ResultProvider.class)
	public ResultProvider resultProvider() {
		return new DefaultResultProvider(properties.getResponse());
	}

	/**
	 * 注入SQL缓存实现
	 */
	@Bean
	@ConditionalOnMissingBean(SqlCache.class)
	public SqlCache sqlCache() {
		CacheConfig cacheConfig = properties.getCacheConfig();
		logger.info("未找到SQL缓存实现，采用默认缓存实现(LRU+TTL)，缓存配置:(容量={},TTL={})", cacheConfig.getCapacity(), cacheConfig.getTtl());
		return new DefaultSqlCache(cacheConfig.getCapacity(), cacheConfig.getTtl());
	}

	/**
	 * 注入接口映射
	 */
	@Bean
	public MappingHandlerMapping mappingHandlerMapping() throws NoSuchMethodException {
		String prefix = StringUtils.isNotBlank(properties.getPrefix()) ? PathUtils.replaceSlash("/" + properties.getPrefix() + "/") : null;
		return new MappingHandlerMapping(prefix, properties.isAllowOverride());
	}


	@Bean
	@ConditionalOnMissingBean(FunctionServiceProvider.class)
	public FunctionServiceProvider functionServiceProvider(GroupServiceProvider groupServiceProvider, Resource magicResource) {
		return new DefaultFunctionServiceProvider(groupServiceProvider, magicResource);
	}

	/**
	 * 注入分组存储service
	 */
	@Bean
	@ConditionalOnMissingBean(GroupServiceProvider.class)
	public GroupServiceProvider groupServiceProvider(Resource magicResource) {
		return new DefaultGroupServiceProvider(magicResource);
	}

	/**
	 * 注入接口存储service
	 */
	@Bean
	@ConditionalOnMissingBean(ApiServiceProvider.class)
	public ApiServiceProvider apiServiceProvider(GroupServiceProvider groupServiceProvider, Resource magicResource) {
		return new DefaultApiServiceProvider(groupServiceProvider, magicResource);
	}

	@Bean
	@ConditionalOnMissingBean(MagicNotifyService.class)
	public MagicNotifyService magicNotifyService() {
		return magicNotify -> {
		};
	}

	@Bean
	public MagicFunctionManager magicFunctionManager(GroupServiceProvider groupServiceProvider, FunctionServiceProvider functionServiceProvider) {
		return new MagicFunctionManager(groupServiceProvider, functionServiceProvider);
	}


	/**
	 * 注入API调用Service
	 */
	@Bean
	public MagicAPIService magicAPIService(MappingHandlerMapping mappingHandlerMapping,
										   ApiServiceProvider apiServiceProvider,
										   FunctionServiceProvider functionServiceProvider,
										   GroupServiceProvider groupServiceProvider,
										   ResultProvider resultProvider,
										   MagicFunctionManager magicFunctionManager,
										   MagicNotifyService magicNotifyService) {
		return new DefaultMagicAPIService(mappingHandlerMapping, apiServiceProvider, functionServiceProvider, groupServiceProvider, resultProvider, magicFunctionManager, magicNotifyService, properties.getClusterConfig().getInstanceId(), properties.isThrowException());
	}

	private void setupSpringSecurity() {
		Class<?> clazz = null;
		try {
			clazz = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
		} catch (ClassNotFoundException ignored) {
		}
		if (clazz != null) {
			try {
				Method method = clazz.getDeclaredMethod("setStrategyName", String.class);
				method.setAccessible(true);
				method.invoke(clazz, "MODE_INHERITABLETHREADLOCAL");
				logger.info("自动适配 Spring Security 成功");
			} catch (Exception ignored) {
				logger.info("自动适配 Spring Security 失败");
			}
		}
	}

	/**
	 * 注入数据库查询模块
	 */
	@Bean
	@ConditionalOnBean({DataSource.class})
	public SQLModule magicSqlModule(MagicDynamicDataSource dynamicDataSource,
									ResultProvider resultProvider,
									PageProvider pageProvider,
									SqlCache sqlCache) {
		SQLModule sqlModule = new SQLModule(dynamicDataSource);
		sqlModule.setResultProvider(resultProvider);
		sqlModule.setPageProvider(pageProvider);
		sqlModule.setSqlInterceptors(sqlInterceptors);
		ColumnMapperAdapter columnMapperAdapter = new ColumnMapperAdapter();
		this.columnMapperProviders.stream().filter(mapperProvider -> !"default".equals(mapperProvider.name())).forEach(columnMapperAdapter::add);
		columnMapperAdapter.setDefault(properties.getSqlColumnCase());
		sqlModule.setColumnMapperProvider(columnMapperAdapter);
		sqlModule.setColumnMapRowMapper(columnMapperAdapter.getDefaultColumnMapRowMapper());
		sqlModule.setRowMapColumnMapper(columnMapperAdapter.getDefaultRowMapColumnMapper());
		sqlModule.setSqlCache(sqlCache);
		DialectAdapter dialectAdapter = new DialectAdapter();
		dialects.forEach(dialectAdapter::add);
		sqlModule.setDialectAdapter(dialectAdapter);
		return sqlModule;
	}

	/**
	 * 注册模块、类型扩展
	 */
	private void setupMagicModules(ResultProvider resultProvider,
								   List<MagicModule> magicModules,
								   List<ExtensionMethod> extensionMethods,
								   List<LanguageProvider> languageProviders) {
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
		MagicResourceLoader.addModule("log", LoggerFactory.getLogger(MagicScript.class));
		List<String> importModules = properties.getAutoImportModuleList();
		logger.info("注册模块:{} -> {}", "env", EnvModule.class);
		MagicResourceLoader.addModule("env", new EnvModule(environment));
		logger.info("注册模块:{} -> {}", "request", RequestModule.class);
		MagicResourceLoader.addModule("request", new RequestModule());
		logger.info("注册模块:{} -> {}", "response", ResponseModule.class);
		MagicResourceLoader.addModule("response", new ResponseModule(resultProvider));
		logger.info("注册模块:{} -> {}", "assert", AssertModule.class);
		MagicResourceLoader.addModule("assert", new AssertModule());
		magicModules.forEach(module -> {
			logger.info("注册模块:{} -> {}", module.getModuleName(), module.getClass());
			MagicResourceLoader.addModule(module.getModuleName(), module);
		});
		if (MagicResourceLoader.loadModule("http") == null) {
			logger.info("注册模块:{} -> {}", "http", HttpModule.class);
			MagicResourceLoader.addModule("http", new HttpModule(this.restTemplate));
		}
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
			AbstractReflection.getInstance().registerMethodExtension(support, extension);
		}));
	}

	@Bean
	public JSR223LanguageProvider jsr223LanguageProvider() {
		return new JSR223LanguageProvider();
	}

	@Bean
	public MagicConfiguration magicConfiguration(List<MagicModule> magicModules,
												 List<LanguageProvider> languageProviders,
												 @Autowired(required = false) MagicDynamicDataSource magicDynamicDataSource,
												 Resource magicResource,
												 ResultProvider resultProvider,
												 MagicAPIService magicAPIService,
												 ApiServiceProvider apiServiceProvider,
												 GroupServiceProvider groupServiceProvider,
												 MappingHandlerMapping mappingHandlerMapping,
												 FunctionServiceProvider functionServiceProvider,
												 MagicFunctionManager magicFunctionManager) {
		logger.info("magic-api工作目录:{}", magicResource);
		setupSpringSecurity();
		AsyncCall.setThreadPoolExecutorSize(properties.getThreadPoolExecutorSize());
		// 设置响应结果的code值
		ResponseCodeConfig responseCodeConfig = properties.getResponseCodeConfig();
		Constants.RESPONSE_CODE_SUCCESS = responseCodeConfig.getSuccess();
		Constants.RESPONSE_CODE_INVALID = responseCodeConfig.getInvalid();
		Constants.RESPONSE_CODE_EXCEPTION = responseCodeConfig.getException();
		// 设置模块和扩展方法
		setupMagicModules(resultProvider, magicModules, extensionMethods, languageProviders);
		MagicConfiguration configuration = new MagicConfiguration();
		configuration.setMagicAPIService(magicAPIService);
		configuration.setApiServiceProvider(apiServiceProvider);
		configuration.setGroupServiceProvider(groupServiceProvider);
		configuration.setMappingHandlerMapping(mappingHandlerMapping);
		configuration.setFunctionServiceProvider(functionServiceProvider);
		SecurityConfig securityConfig = properties.getSecurityConfig();
		configuration.setUsername(securityConfig.getUsername());
		configuration.setPassword(securityConfig.getPassword());
		configuration.setDebugTimeout(properties.getDebugConfig().getTimeout());
		configuration.setHttpMessageConverters(Optional.ofNullable(httpMessageConverters).orElse(Collections.emptyList()));
		configuration.setResultProvider(resultProvider);
		configuration.setThrowException(properties.isThrowException());
		configuration.setMagicDynamicDataSource(magicDynamicDataSource);
		configuration.setEditorConfig(properties.getEditorConfig());
		configuration.setWorkspace(magicResource);
		configuration.setAuthorizationInterceptor(authorizationInterceptor);
		// 注册函数
		this.magicFunctions.forEach(JavaReflection::registerFunction);
		// 向页面传递配置信息时不传递用户名密码，增强安全性
		securityConfig.setUsername(null);
		securityConfig.setPassword(null);

		// 构建UI请求处理器
		String base = properties.getWeb();
		mappingHandlerMapping.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
		MagicDataSourceController dataSourceController = new MagicDataSourceController(configuration);
		if (base != null) {
			configuration.setEnableWeb(true);
			List<MagicController> controllers = new ArrayList<>(Arrays.asList(
					new MagicAPIController(configuration),
					dataSourceController,
					new MagicWorkbenchController(configuration),
					new MagicGroupController(configuration),
					new MagicFunctionController(configuration)
			));
			controllers.forEach(item -> mappingHandlerMapping.registerController(item, base));
		}
		dataSourceController.registerDataSource();
		// 设置拦截器信息
		this.requestInterceptors.forEach(interceptor -> {
			logger.info("注册请求拦截器：{}", interceptor.getClass());
			configuration.addRequestInterceptor(interceptor);
		});

		if (this.properties.isBanner()) {
			configuration.printBanner();
		}
		configuration.setMagicFunctionManager(magicFunctionManager);
		// 注册函数加载器
		magicFunctionManager.registerFunctionLoader();
		// 注册所有函数a
		magicFunctionManager.registerAllFunction();
		// 自动刷新
		magicFunctionManager.enableRefresh(properties.getRefreshInterval());
		mappingHandlerMapping.setHandler(new RequestHandler(configuration));
		mappingHandlerMapping.setMagicApiService(apiServiceProvider);
		mappingHandlerMapping.setGroupServiceProvider(groupServiceProvider);
		// 注册所有映射
		mappingHandlerMapping.registerAllMapping();
		int refreshInterval = properties.getRefreshInterval();
		// 自动刷新
		mappingHandlerMapping.enableRefresh(refreshInterval);
		if (refreshInterval > 0) {
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(dataSourceController::registerDataSource, refreshInterval, refreshInterval, TimeUnit.SECONDS);
		}
		return configuration;
	}

	public AuthorizationInterceptor createAuthorizationInterceptor() {
		SecurityConfig securityConfig = properties.getSecurityConfig();
		return new DefaultAuthorizationInterceptor(securityConfig.getUsername(), securityConfig.getPassword());
	}

	private RestTemplate createRestTemplate(){
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8) {
			{
				setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
			}

			@Override
			public boolean supports(Class<?> clazz) {
				return true;
			}
		});
		return restTemplate;
	}

}
