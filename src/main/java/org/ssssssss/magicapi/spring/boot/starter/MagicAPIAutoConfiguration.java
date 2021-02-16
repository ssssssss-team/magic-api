package org.ssssssss.magicapi.spring.boot.starter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.adapter.ColumnMapperAdapter;
import org.ssssssss.magicapi.adapter.DialectAdapter;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.adapter.ResourceAdapter;
import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.config.*;
import org.ssssssss.magicapi.controller.*;
import org.ssssssss.magicapi.dialect.*;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.interceptor.SQLInterceptor;
import org.ssssssss.magicapi.logging.LoggerManager;
import org.ssssssss.magicapi.modules.*;
import org.ssssssss.magicapi.provider.*;
import org.ssssssss.magicapi.provider.impl.*;
import org.ssssssss.magicapi.utils.ClassScanner;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.functions.ExtensionMethod;
import org.ssssssss.script.parsing.ast.statement.AsyncCall;
import org.ssssssss.script.reflection.AbstractReflection;
import org.ssssssss.script.reflection.JavaReflection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

@Configuration
@ConditionalOnClass({RequestMappingHandlerMapping.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
public class MagicAPIAutoConfiguration implements WebMvcConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);

	@Autowired
	private MagicAPIProperties properties;

	@Autowired(required = false)
	private List<RequestInterceptor> requestInterceptors = Collections.emptyList();

	@Autowired(required = false)
	private List<SQLInterceptor> sqlInterceptors = Collections.emptyList();

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	private ApplicationContext springContext;

	/**
	 * 自定义的类型扩展
	 */
	@Autowired(required = false)
	private List<ExtensionMethod> extensionMethods = Collections.emptyList();

	/**
	 * 内置的消息转换
	 */
	@Autowired(required = false)
	private List<HttpMessageConverter<?>> httpMessageConverters = Collections.emptyList();

	/**
	 * 自定义的方言
	 */
	@Autowired(required = false)
	private List<Dialect> dialects = Collections.emptyList();

	/**
	 * 自定义的列名转换
	 */
	@Autowired(required = false)
	List<ColumnMapperProvider> columnMapperProviders = Collections.emptyList();

	/**
	 * 自定义的函数
	 */
	@Autowired(required = false)
	List<MagicFunction> magicFunctions = Collections.emptyList();

	@Autowired
	private Environment environment;

	@Autowired
	ApiServiceProvider apiServiceProvider;

	@Autowired
	GroupServiceProvider groupServiceProvider;

	@Autowired
	FunctionServiceProvider functionServiceProvider;

	@Autowired(required = false)
	MagicDynamicDataSource magicDynamicDataSource;

	@Autowired
	MappingHandlerMapping mappingHandlerMapping;

	@Autowired
	ResultProvider resultProvider;

	@Autowired
	MagicCorsFilter magicCorsFilter;

	private String ALL_CLASS_TXT;

	public MagicAPIAutoConfiguration() {
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

	@Bean
	public Resource magicWorkspaceResource() throws IOException {
		Resource resource = ResourceAdapter.getResource(properties.getWorkspace());
		logger.info("magic-api工作目录:{}",resource.getAbsolutePath());
		return resource;
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
				// 默认首页设置
				requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(web).build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("redirectIndex", HttpServletRequest.class));
				// 读取配置
				requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(web + "/config.json").build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("readConfig"));
				// 读取配置
				requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(web + "/classes.txt").produces("text/plain").build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("readClass"));
			} catch (NoSuchMethodException ignored) {
			}
		}
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		String web = properties.getWeb();
		if (web != null) {
			registry.addInterceptor(new HandlerInterceptor() {
				@Override
				public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
					if (handler instanceof HandlerMethod) {
						handler = ((HandlerMethod) handler).getBean();
					}
					if (handler instanceof MagicController) {
						magicCorsFilter.process(request, response);
					}
					return true;
				}
			}).addPathPatterns("/**");
		}
	}

	@Bean
	public FilterRegistrationBean magicCorsFilterRegistrationBean() {
		FilterRegistrationBean<MagicCorsFilter> registration = new FilterRegistrationBean<>(magicCorsFilter);
		registration.addUrlPatterns("/*");
		registration.setName("Magic Cors Filter");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

	@Bean
	public MagicCorsFilter magicCorsFilter() {
		return new MagicCorsFilter();
	}

	@ConditionalOnMissingBean(PageProvider.class)
	@Bean
	public PageProvider pageProvider() {
		PageConfig pageConfig = properties.getPageConfig();
		logger.info("未找到分页实现,采用默认分页实现,分页配置:(页码={},页大小={},默认首页={},默认页大小={})", pageConfig.getPage(), pageConfig.getSize(), pageConfig.getDefaultPage(), pageConfig.getDefaultSize());
		return new DefaultPageProvider(pageConfig.getPage(), pageConfig.getSize(), pageConfig.getDefaultPage(), pageConfig.getDefaultSize());
	}

	/**
	 * 注入结果构建方法
	 */
	@ConditionalOnMissingBean(ResultProvider.class)
	@Bean
	public ResultProvider resultProvider() {
		return new DefaultResultProvider();
	}

	/**
	 * 注入SQL缓存实现
	 */
	@ConditionalOnMissingBean(SqlCache.class)
	@Bean
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
		MappingHandlerMapping handlerMapping = new MappingHandlerMapping();
		if (StringUtils.isNotBlank(properties.getPrefix())) {
			String prefix = properties.getPrefix().trim();
			if (!prefix.startsWith("/")) {
				prefix = "/" + prefix;
			}
			if (!prefix.endsWith("/")) {
				prefix = prefix + "/";
			}
			handlerMapping.setPrefix(prefix);
		}
		handlerMapping.setAllowOverride(properties.isAllowOverride());
		return handlerMapping;
	}


	@Bean
	@ConditionalOnMissingBean(FunctionServiceProvider.class)
	public FunctionServiceProvider functionServiceProvider(GroupServiceProvider groupServiceProvider,Resource magicWorkspaceResource) {
		return new DefaultFunctionServiceProvider(groupServiceProvider, magicWorkspaceResource);
	}

	/**
	 * 注入分组存储service
	 */
	@Bean
	@ConditionalOnMissingBean(GroupServiceProvider.class)
	public GroupServiceProvider groupServiceProvider(Resource magicWorkspaceResource) {
		return new DefaultGroupServiceProvider(magicWorkspaceResource);
	}

	/**
	 * 注入接口存储service
	 */
	@ConditionalOnMissingBean(ApiServiceProvider.class)
	@Bean
	public ApiServiceProvider apiServiceProvider(GroupServiceProvider groupServiceProvider,Resource magicWorkspaceResource) {
		return new DefaultApiServiceProvider(groupServiceProvider, magicWorkspaceResource);
	}


	/**
	 * 注入API调用Service
	 */
	@Bean
	public MagicAPIService magicAPIService(MappingHandlerMapping mappingHandlerMapping, ResultProvider resultProvider) {
		return new DefaultMagicAPIService(mappingHandlerMapping, resultProvider, properties.isThrowException());
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
	@ConditionalOnBean({MagicDynamicDataSource.class})
	public SQLModule magicSqlModule(MagicDynamicDataSource dynamicDataSource, ResultProvider resultProvider, PageProvider pageProvider, SqlCache sqlCache) {
		SQLModule sqlModule = new SQLModule(dynamicDataSource);
		sqlModule.setResultProvider(resultProvider);
		sqlModule.setPageProvider(pageProvider);
		sqlModule.setSqlInterceptors(sqlInterceptors);
		ColumnMapperAdapter columnMapperAdapter = new ColumnMapperAdapter();
		columnMapperAdapter.setDefault(new DefaultColumnMapperProvider());
		columnMapperAdapter.add(new CamelColumnMapperProvider());
		columnMapperAdapter.add(new PascalColumnMapperProvider());
		columnMapperAdapter.add(new LowerColumnMapperProvider());
		columnMapperAdapter.add(new UpperColumnMapperProvider());
		for (ColumnMapperProvider mapperProvider : this.columnMapperProviders) {
			if (!"default".equals(mapperProvider.name())) {
				columnMapperAdapter.add(mapperProvider);
			}
		}
		columnMapperAdapter.setDefault(properties.getSqlColumnCase());
		sqlModule.setColumnMapperProvider(columnMapperAdapter);
		sqlModule.setColumnMapRowMapper(columnMapperAdapter.getDefaultColumnMapRowMapper());
		sqlModule.setRowMapColumnMapper(columnMapperAdapter.getDefaultRowMapColumnMapper());
		sqlModule.setSqlCache(sqlCache);
		DialectAdapter dialectAdapter = new DialectAdapter();
		dialectAdapter.add(new MySQLDialect());
		dialectAdapter.add(new OracleDialect());
		dialectAdapter.add(new PostgreSQLDialect());
		dialectAdapter.add(new ClickhouseDialect());
		dialectAdapter.add(new DB2Dialect());
		dialectAdapter.add(new SQLServerDialect());
		dialectAdapter.add(new SQLServer2005Dialect());
		dialectAdapter.add(new DmDialect());
		dialects.forEach(dialectAdapter::add);
		sqlModule.setDialectAdapter(dialectAdapter);
		return sqlModule;
	}

	/**
	 * 注册模块、类型扩展
	 */
	private void setupMagicModules(ResultProvider resultProvider, List<MagicModule> magicModules, List<ExtensionMethod> extensionMethods) {
		// 设置脚本import时 class加载策略
		MagicResourceLoader.setClassLoader((className) -> {
			try {
				return springContext.getBean(className);
			} catch (Exception e) {
				Class<?> clazz = null;
				try {
					clazz = Class.forName(className);
					return springContext.getBean(clazz);
				} catch (Exception ex) {
					return clazz;
				}
			}
		});
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
		MagicResourceLoader.addModule("assert", AssertModule.class);
		for (MagicModule module : magicModules) {
			logger.info("注册模块:{} -> {}", module.getModuleName(), module.getClass());
			MagicResourceLoader.addModule(module.getModuleName(), module);
		}
		Set<String> moduleNames = MagicResourceLoader.getModuleNames();
		for (String moduleName : moduleNames) {
			if (importModules.contains(moduleName)) {
				logger.info("自动导入模块：{}", moduleName);
				MagicScriptEngine.addDefaultImport(moduleName, MagicResourceLoader.loadModule(moduleName));
			}
		}
		List<String> importPackages = properties.getAutoImportPackageList();
		for (String importPackage : importPackages) {
			logger.info("自动导包：{}", importPackage);
			MagicResourceLoader.addPackage(importPackage);
		}
		for (ExtensionMethod extension : extensionMethods) {
			List<Class<?>> supports = extension.supports();
			for (Class<?> support : supports) {
				logger.info("注册扩展:{} -> {}", support, extension.getClass());
				AbstractReflection.getInstance().registerMethodExtension(support, extension);
			}
		}
	}


	/**
	 * 注入动态数据源
	 */
	@Bean
	@ConditionalOnMissingBean(MagicDynamicDataSource.class)
	@ConditionalOnBean({DataSource.class})
	public MagicDynamicDataSource magicDynamicDataSource(DataSource dataSource) {
		MagicDynamicDataSource dynamicDataSource = new MagicDynamicDataSource();
		dynamicDataSource.put(dataSource);
		return dynamicDataSource;
	}

	@Bean
	public MagicConfiguration magicConfiguration(@Autowired List<MagicModule> magicModules) {
		logger.info("magic-api workspace:{}",properties.getWorkspace());
		setupSpringSecurity();
		AsyncCall.setThreadPoolExecutorSize(properties.getThreadPoolExecutorSize());
		// 设置模块和扩展方法
		setupMagicModules(resultProvider, magicModules, extensionMethods);
		MagicConfiguration configuration = new MagicConfiguration();
		configuration.setMagicApiService(apiServiceProvider);
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
		// 注册函数
		this.magicFunctions.forEach(JavaReflection::registerFunction);
		// 向页面传递配置信息时不传递用户名密码，增强安全性
		securityConfig.setUsername(null);
		securityConfig.setPassword(null);

		// 构建UI请求处理器
		String base = properties.getWeb();
		mappingHandlerMapping.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
		if (base != null) {
			configuration.setEnableWeb(true);
			List<MagicController> controllers = new ArrayList<>(Arrays.asList(
					new MagicAPIController(configuration),
					new MagicConfigController(configuration),
					new MagicWorkbenchController(configuration),
					new MagicGroupController(configuration),
					new MagicFunctionController(configuration)
			));
			controllers.forEach(item -> mappingHandlerMapping.registerController(item, base));
		}
		// 设置拦截器信息
		this.requestInterceptors.forEach(interceptor -> {
			logger.info("注册请求拦截器：{}", interceptor.getClass());
			configuration.addRequestInterceptor(interceptor);
		});

		if (this.properties.isBanner()) {
			configuration.printBanner();
		}
		MagicFunctionManager magicFunctionManager = new MagicFunctionManager(groupServiceProvider, functionServiceProvider);
		configuration.setMagicFunctionManager(magicFunctionManager);
		// 注册函数加载器
		magicFunctionManager.registerFunctionLoader();
		// 注册所有函数
		magicFunctionManager.registerAllFunction();
		// 自动刷新
		magicFunctionManager.enableRefresh(properties.getRefreshInterval());
		mappingHandlerMapping.setHandler(new RequestHandler(configuration));
		mappingHandlerMapping.setMagicApiService(apiServiceProvider);
		mappingHandlerMapping.setGroupServiceProvider(groupServiceProvider);
		// 注册所有映射
		mappingHandlerMapping.registerAllMapping();
		// 自动刷新
		mappingHandlerMapping.enableRefresh(properties.getRefreshInterval());
		return configuration;
	}

}
