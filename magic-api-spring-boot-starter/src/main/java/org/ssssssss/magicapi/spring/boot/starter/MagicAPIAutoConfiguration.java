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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
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
import org.ssssssss.magicapi.interceptor.*;
import org.ssssssss.magicapi.logging.LoggerManager;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.DataType;
import org.ssssssss.magicapi.model.Options;
import org.ssssssss.magicapi.modules.*;
import org.ssssssss.magicapi.provider.*;
import org.ssssssss.magicapi.provider.impl.*;
import org.ssssssss.magicapi.utils.ClassScanner;
import org.ssssssss.magicapi.utils.Mapping;
import org.ssssssss.magicapi.utils.PathUtils;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.exception.MagicScriptRuntimeException;
import org.ssssssss.script.functions.DynamicModuleImport;
import org.ssssssss.script.functions.ExtensionMethod;
import org.ssssssss.script.parsing.ast.statement.AsyncCall;
import org.ssssssss.script.reflection.JavaReflection;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * magic-api自动配置类
 *
 * @author mxd
 */
@Configuration
@ConditionalOnClass({RequestMappingHandlerMapping.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
@Import({MagicRedisAutoConfiguration.class, MagicMongoAutoConfiguration.class, MagicSwaggerConfiguration.class, MagicJsonAutoConfiguration.class, ApplicationUriPrinter.class})
@EnableWebSocket
public class MagicAPIAutoConfiguration implements WebMvcConfigurer, WebSocketConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);

	/**
	 * 请求拦截器
	 */
	private final ObjectProvider<List<RequestInterceptor>> requestInterceptorsProvider;

	/**
	 * SQL拦截器
	 */
	private final ObjectProvider<List<SQLInterceptor>> sqlInterceptorsProvider;

	/**
	 * 单表API拦截器
	 */
	private final ObjectProvider<List<NamedTableInterceptor>> namedTableInterceptorsProvider;

	/**
	 * 自定义的类型扩展
	 */
	private final ObjectProvider<List<ExtensionMethod>> extensionMethodsProvider;

	/**
	 * 内置的消息转换
	 */
	private final ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConvertersProvider;

	/**
	 * 自定义的方言
	 */
	private final ObjectProvider<List<Dialect>> dialectsProvider;

	/**
	 * 自定义的列名转换
	 */
	private final ObjectProvider<List<ColumnMapperProvider>> columnMapperProvidersProvider;


	private final ObjectProvider<AuthorizationInterceptor> authorizationInterceptorProvider;

	/**
	 * 自定义的函数
	 */
	private final ObjectProvider<List<MagicFunction>> magicFunctionsProvider;

	private final ObjectProvider<MagicNotifyService> magicNotifyServiceProvider;

	private final Environment environment;

	private final MagicCorsFilter magicCorsFilter = new MagicCorsFilter();

	private final MagicAPIProperties properties;

	private final ApplicationContext applicationContext;

	private boolean registerMapping = false;

	private boolean registerInterceptor = false;

	private boolean registerWebsocket = false;

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired(required = false)
	private MultipartResolver multipartResolver;

	private String allClassTxt;
	private DefaultAuthorizationInterceptor defaultAuthorizationInterceptor;

	public MagicAPIAutoConfiguration(MagicAPIProperties properties,
									 ObjectProvider<List<Dialect>> dialectsProvider,
									 ObjectProvider<List<RequestInterceptor>> requestInterceptorsProvider,
									 ObjectProvider<List<SQLInterceptor>> sqlInterceptorsProvider,
									 ObjectProvider<List<ExtensionMethod>> extensionMethodsProvider,
									 ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConvertersProvider,
									 ObjectProvider<List<ColumnMapperProvider>> columnMapperProvidersProvider,
									 ObjectProvider<List<MagicFunction>> magicFunctionsProvider,
									 ObjectProvider<MagicNotifyService> magicNotifyServiceProvider,
									 ObjectProvider<AuthorizationInterceptor> authorizationInterceptorProvider,
									 ObjectProvider<List<NamedTableInterceptor>> namedTableInterceptorsProvider,
									 Environment environment,
									 ApplicationContext applicationContext
	) {
		this.properties = properties;
		this.dialectsProvider = dialectsProvider;
		this.requestInterceptorsProvider = requestInterceptorsProvider;
		this.sqlInterceptorsProvider = sqlInterceptorsProvider;
		this.extensionMethodsProvider = extensionMethodsProvider;
		this.httpMessageConvertersProvider = httpMessageConvertersProvider;
		this.columnMapperProvidersProvider = columnMapperProvidersProvider;
		this.magicFunctionsProvider = magicFunctionsProvider;
		this.magicNotifyServiceProvider = magicNotifyServiceProvider;
		this.authorizationInterceptorProvider = authorizationInterceptorProvider;
		this.namedTableInterceptorsProvider = namedTableInterceptorsProvider;
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
		if (allClassTxt == null) {
			try {
				allClassTxt = ClassScanner.compress(ClassScanner.scan());
			} catch (Throwable t) {
				logger.warn("扫描Class失败", t);
				allClassTxt = "";
			}
		}
		return allClassTxt;
	}

	@Bean
	@ConditionalOnMissingBean(HttpModule.class)
	public HttpModule magicHttpModule() {
		return new HttpModule(createRestTemplate());
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
		if (magicDynamicDataSource.isEmpty()) {
			throw new MagicAPIException("当前未配置数据源，如已配置，请引入 spring-boot-starter-jdbc 后在试!");
		}
		MagicDynamicDataSource.DataSourceNode dataSourceNode = magicDynamicDataSource.getDataSource(resourceConfig.getDatasource());
		return new DatabaseResource(new JdbcTemplate(dataSourceNode.getDataSource()), resourceConfig.getTableName(), resourceConfig.getPrefix(), resourceConfig.isReadonly());
	}

	@Bean
	@ConditionalOnMissingBean(Resource.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "file", matchIfMissing = true)
	public Resource magicResource() throws IOException {
		ResourceConfig resourceConfig = properties.getResource();
		return ResourceAdapter.getResource(resourceConfig.getLocation(), resourceConfig.isReadonly());
	}

	@Bean
	@ConditionalOnMissingBean(MagicBackupService.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "backup-config.resource-type", havingValue = "database")
	public MagicBackupService magicDatabaseBackupService(MagicDynamicDataSource magicDynamicDataSource) {
		BackupConfig backupConfig = properties.getBackupConfig();
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
			try {
				Mapping mapping = Mapping.create(requestMappingHandlerMapping);
						// 默认首页设置
				mapping.register(mapping.paths(web).build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("redirectIndex", HttpServletRequest.class))
						// 读取配置
						.register(mapping.paths(web + "/config.json").build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("readConfig"))
						// 读取配置
						.register(mapping.paths(web + "/classes.txt").produces("text/plain").build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("readClass"));
			} catch (NoSuchMethodException ignored) {
			}
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
		logger.info("未配置集群通知服务，本实例不会推送通知，集群环境下可能会有问题，如需开启，请配置magic-api.cluster-config.enable=true，若开启后本提示还在，请检查 spring-boot-starter-data-redis 是否引入");
		return magicNotify -> {
		};
	}

	@Bean
	@ConditionalOnMissingBean(MagicBackupService.class)
	@ConditionalOnProperty(prefix = "magic-api", name = "backup-config.resource-type", havingValue = "file", matchIfMissing = true)
	public MagicBackupService magicFileBackupService() {
		return new MagicFileBackupService(new File(properties.getBackupConfig().getLocation()));
	}

	@Bean
	public MagicFunctionManager magicFunctionManager(GroupServiceProvider groupServiceProvider, FunctionServiceProvider functionServiceProvider) {
		return new MagicFunctionManager(groupServiceProvider, functionServiceProvider);
	}

	/**
	 * 注入API调用Service
	 */
	@Bean
	@ConditionalOnMissingBean
	public MagicAPIService magicAPIService(MappingHandlerMapping mappingHandlerMapping,
										   ApiServiceProvider apiServiceProvider,
										   FunctionServiceProvider functionServiceProvider,
										   GroupServiceProvider groupServiceProvider,
										   ResultProvider resultProvider,
										   MagicDynamicDataSource magicDynamicDataSource,
										   MagicFunctionManager magicFunctionManager,
										   Resource workspace,
										   MagicBackupService magicBackupService) {
		return new DefaultMagicAPIService(mappingHandlerMapping, apiServiceProvider, functionServiceProvider, groupServiceProvider, resultProvider, magicDynamicDataSource, magicFunctionManager, magicNotifyServiceProvider.getObject(), properties.getClusterConfig().getInstanceId(), workspace, magicBackupService, properties.isThrowException());
	}

	/**
	 * 注入数据库查询模块
	 */
	@Bean
	@ConditionalOnBean({MagicDynamicDataSource.class})
	public SQLModule magicSqlModule(MagicDynamicDataSource dynamicDataSource,
									ResultProvider resultProvider,
									PageProvider pageProvider,
									SqlCache sqlCache) {
		SQLModule sqlModule = new SQLModule(dynamicDataSource);
		if (!dynamicDataSource.isEmpty()) {
			sqlModule.setDataSourceNode(dynamicDataSource.getDataSource());
		}
		sqlModule.setResultProvider(resultProvider);
		sqlModule.setPageProvider(pageProvider);
		List<SQLInterceptor> sqlInterceptors = sqlInterceptorsProvider.getIfAvailable(ArrayList::new);
		if (properties.isShowSql()) {
			sqlInterceptors.add(new DefaultSqlInterceptor());
		}
		sqlModule.setSqlInterceptors(sqlInterceptors);
		sqlModule.setNamedTableInterceptors(namedTableInterceptorsProvider.getIfAvailable(Collections::emptyList));
		ColumnMapperAdapter columnMapperAdapter = new ColumnMapperAdapter();
		this.columnMapperProvidersProvider.getIfAvailable(Collections::emptyList).stream().filter(mapperProvider -> !"default".equals(mapperProvider.name())).forEach(columnMapperAdapter::add);
		columnMapperAdapter.setDefault(properties.getSqlColumnCase());
		sqlModule.setColumnMapperProvider(columnMapperAdapter);
		sqlModule.setColumnMapRowMapper(columnMapperAdapter.getDefaultColumnMapRowMapper());
		sqlModule.setRowMapColumnMapper(columnMapperAdapter.getDefaultRowMapColumnMapper());
		sqlModule.setSqlCache(sqlCache);
		DialectAdapter dialectAdapter = new DialectAdapter();
		dialectsProvider.getIfAvailable(Collections::emptyList).forEach(dialectAdapter::add);
		sqlModule.setDialectAdapter(dialectAdapter);
		sqlModule.setLogicDeleteColumn(properties.getCrudConfig().getLogicDeleteColumn());
		sqlModule.setLogicDeleteValue(properties.getCrudConfig().getLogicDeleteValue());
		return sqlModule;
	}

	/**
	 * 注册模块、类型扩展
	 */
	private void setupMagicModules(MagicDynamicDataSource dynamicDataSource,
                                   SQLModule sqlModule,
                                   ResultProvider resultProvider,
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
		MagicResourceLoader.addModule("log", new DynamicModuleImport(Logger.class, context -> LoggerFactory.getLogger(Objects.toString(context.getScriptName(),"Unknown"))));
		List<String> importModules = properties.getAutoImportModuleList();
		logger.info("注册模块:{} -> {}", "env", EnvModule.class);
		MagicResourceLoader.addModule("env", new EnvModule(environment));
		logger.info("注册模块:{} -> {}", "request", RequestModule.class);
		MagicResourceLoader.addModule("request", new RequestModule(multipartResolver));
		logger.info("注册模块:{} -> {}", "response", ResponseModule.class);
		MagicResourceLoader.addModule("response", new ResponseModule(resultProvider));
		logger.info("注册模块:{} -> {}", "assert", AssertModule.class);
		MagicResourceLoader.addModule("assert", new AssertModule());
		magicModules.forEach(module -> {
			logger.info("注册模块:{} -> {}", module.getModuleName(), module.getClass());
			MagicResourceLoader.addModule(module.getModuleName(), module);
		});
        MagicResourceLoader.addModule(sqlModule.getModuleName(), new DynamicModuleImport(SQLModule.class, context -> {
			String dataSourceKey = context.getString(Options.DEFAULT_DATA_SOURCE.getValue());
			if(StringUtils.isEmpty(dataSourceKey)) return sqlModule;
			SQLModule newSqlModule = sqlModule.cloneSQLModule();
			newSqlModule.setDataSourceNode(dynamicDataSource.getDataSource(dataSourceKey));
            return newSqlModule;
        }));
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
	public JSR223LanguageProvider jsr223LanguageProvider() {
		return new JSR223LanguageProvider();
	}

	@Bean
	public MagicConfiguration magicConfiguration(MagicDynamicDataSource dynamicDataSource,
                                                 SQLModule sqlModule,
                                                 List<MagicModule> magicModules,
												 List<LanguageProvider> languageProviders,
												 Resource magicResource,
												 ResultProvider resultProvider,
												 MagicAPIService magicAPIService,
												 ApiServiceProvider apiServiceProvider,
												 GroupServiceProvider groupServiceProvider,
												 MappingHandlerMapping mappingHandlerMapping,
												 FunctionServiceProvider functionServiceProvider,
												 MagicNotifyService magicNotifyService,
												 MagicFunctionManager magicFunctionManager,
												 MagicBackupService magicBackupService) throws NoSuchMethodException {
		logger.info("magic-api工作目录:{}", magicResource);
		AsyncCall.setThreadPoolExecutorSize(properties.getThreadPoolExecutorSize());
		DataType.DATE_PATTERNS = properties.getDatePattern();
		MagicScript.setCompileCache(properties.getCompileCacheSize());
		// 设置响应结果的code值
		ResponseCodeConfig responseCodeConfig = properties.getResponseCodeConfig();
		Constants.RESPONSE_CODE_SUCCESS = responseCodeConfig.getSuccess();
		Constants.RESPONSE_CODE_INVALID = responseCodeConfig.getInvalid();
		Constants.RESPONSE_CODE_EXCEPTION = responseCodeConfig.getException();
		// 设置模块和扩展方法
		setupMagicModules(dynamicDataSource, sqlModule, resultProvider, magicModules, extensionMethodsProvider.getIfAvailable(Collections::emptyList), languageProviders);
		MagicConfiguration configuration = new MagicConfiguration();
		configuration.setMagicAPIService(magicAPIService);
		configuration.setMagicNotifyService(magicNotifyService);
		configuration.setInstanceId(properties.getClusterConfig().getInstanceId());
		configuration.setApiServiceProvider(apiServiceProvider);
		configuration.setGroupServiceProvider(groupServiceProvider);
		configuration.setMappingHandlerMapping(mappingHandlerMapping);
		configuration.setFunctionServiceProvider(functionServiceProvider);
		configuration.setMagicBackupService(magicBackupService);
		SecurityConfig securityConfig = properties.getSecurityConfig();
		configuration.setDebugTimeout(properties.getDebugConfig().getTimeout());
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

		// 构建UI请求处理器
		String base = properties.getWeb();
		mappingHandlerMapping.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
		MagicDataSourceController dataSourceController = new MagicDataSourceController(configuration);
		MagicWorkbenchController magicWorkbenchController = new MagicWorkbenchController(configuration, properties.getSecretKey());
		if (base != null) {
			configuration.setEnableWeb(true);
			List<MagicController> controllers = new ArrayList<>(Arrays.asList(
					new MagicAPIController(configuration),
					dataSourceController,
					magicWorkbenchController,
					new MagicGroupController(configuration),
					new MagicFunctionController(configuration)
			));
			controllers.forEach(item -> mappingHandlerMapping.registerController(item, base));
		}
		// 注册接收推送的接口
		if (StringUtils.isNotBlank(properties.getSecretKey())) {
			Mapping mapping = Mapping.create(requestMappingHandlerMapping);
			RequestMappingInfo requestMappingInfo = mapping.paths(properties.getPushPath()).build();
			Method method = MagicWorkbenchController.class.getDeclaredMethod("receivePush", MultipartFile.class, String.class, Long.class, String.class);
			mapping.register(requestMappingInfo, magicWorkbenchController, method);
		}
		// 注册数据源
		magicAPIService.registerAllDataSource();
		// 设置拦截器信息
		this.requestInterceptorsProvider.getIfAvailable(Collections::emptyList).forEach(interceptor -> {
			logger.info("注册请求拦截器：{}", interceptor.getClass());
			configuration.addRequestInterceptor(interceptor);
		});
		// 打印banner
		if (this.properties.isBanner()) {
			configuration.printBanner();
		}
		configuration.setMagicFunctionManager(magicFunctionManager);
		// 注册函数加载器
		magicFunctionManager.registerFunctionLoader();
		// 注册所有函数
		magicFunctionManager.registerAllFunction();
		mappingHandlerMapping.setHandler(new RequestHandler(configuration));
		mappingHandlerMapping.setMagicApiService(apiServiceProvider);
		mappingHandlerMapping.setGroupServiceProvider(groupServiceProvider);
		// 注册所有映射
		mappingHandlerMapping.registerAllMapping();
		// 备份清理
		if (properties.getBackupConfig().getMaxHistory() > 0) {
			long interval = properties.getBackupConfig().getMaxHistory() * 86400000L;
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
		SecurityConfig securityConfig = properties.getSecurityConfig();
		defaultAuthorizationInterceptor = new DefaultAuthorizationInterceptor(securityConfig.getUsername(), securityConfig.getPassword());
		return defaultAuthorizationInterceptor;
	}

	private RestTemplate createRestTemplate() {
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

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
		String web = properties.getWeb();
		MagicNotifyService magicNotifyService = magicNotifyServiceProvider.getObject();
		WebSocketSessionManager.setMagicNotifyService(magicNotifyService);
		if (web != null && !registerWebsocket) {
			registerWebsocket = true;
			MagicWebSocketDispatcher dispatcher = new MagicWebSocketDispatcher(properties.getClusterConfig().getInstanceId(), magicNotifyService, Arrays.asList(
					new MagicDebugHandler(),
					new MagicWorkbenchHandler(authorizationInterceptorProvider.getIfAvailable(this::createAuthorizationInterceptor))
			));
			WebSocketHandlerRegistration registration = webSocketHandlerRegistry.addHandler(dispatcher, web + "/console");
			if (properties.isSupportCrossDomain()) {
				registration.setAllowedOrigins("*");
			}
		}
	}
}
