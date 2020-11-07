package org.ssssssss.magicapi.spring.boot.starter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.config.*;
import org.ssssssss.magicapi.functions.AssertFunctions;
import org.ssssssss.magicapi.functions.DatabaseQuery;
import org.ssssssss.magicapi.functions.RequestFunctions;
import org.ssssssss.magicapi.functions.ResponseFunctions;
import org.ssssssss.magicapi.logging.LoggerManager;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.provider.impl.DefaultApiServiceProvider;
import org.ssssssss.magicapi.provider.impl.DefaultMagicAPIService;
import org.ssssssss.magicapi.provider.impl.DefaultPageProvider;
import org.ssssssss.magicapi.provider.impl.DefaultResultProvider;
import org.ssssssss.magicapi.utils.ClassScanner;
import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicPackageLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.functions.ExtensionMethod;
import org.ssssssss.script.interpreter.AbstractReflection;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
@ConditionalOnClass({DataSource.class, RequestMappingHandlerMapping.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
public class MagicAPIAutoConfiguration implements WebMvcConfigurer {

	private static Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);
	@Autowired
	@Lazy
	RequestMappingHandlerMapping requestMappingHandlerMapping;

	private MagicAPIProperties properties;

	@Autowired(required = false)
	private List<RequestInterceptor> requestInterceptors;

	@Autowired
	private ApplicationContext springContext;

	private String ALL_CLASS_TXT;

	public MagicAPIAutoConfiguration(MagicAPIProperties properties) {
		this.properties = properties;
		setupSpringSecurity();
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

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String web = properties.getWeb();
		if (web != null) {
			// 当开启了UI界面时，收集日志
			LoggerManager.createMagicAppender();
			// 配置静态资源路径
			registry.addResourceHandler(web + "/**").addResourceLocations("classpath:/magicapi-support/");
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

	/**
	 * 注入接口存储service
	 */
	@ConditionalOnMissingBean(ApiServiceProvider.class)
	@Bean
	public ApiServiceProvider apiServiceProvider(DynamicDataSource dynamicDataSource) {
		logger.info("接口使用数据源：{}", StringUtils.isNotBlank(properties.getDatasource()) ? properties.getDatasource() : "default");
		return new DefaultApiServiceProvider(dynamicDataSource.getDataSource(properties.getDatasource()).getJdbcTemplate());
	}

	/**
	 * 注入API调用Service
	 */
	@ConditionalOnMissingBean(MagicAPIService.class)
	@Bean
	public MagicAPIService magicAPIService(MappingHandlerMapping mappingHandlerMapping, ResultProvider resultProvider) {
		return new DefaultMagicAPIService(mappingHandlerMapping, resultProvider, properties.isThrowException());
	}

	/**
	 * 注入请求处理器
	 */
	@Bean
	public RequestHandler requestHandler(@Autowired(required = false) List<MagicModule> magicModules, //定义的模块集合
										 @Autowired(required = false) List<ExtensionMethod> extensionMethods, //自定义的类型扩展
										 @Autowired(required = false) List<HttpMessageConverter<?>> httpMessageConverters,
										 ApiServiceProvider apiServiceProvider,
										 MagicDynamicDataSource magicDynamicDataSource,
										 // url 映射
										 MappingHandlerMapping mappingHandlerMapping,
										 // JSON结果转换
										 ResultProvider resultProvider) {
		// 设置模块和扩展方法
		setupMagicModules(resultProvider, magicModules, extensionMethods);
		// 构建请求处理器
		RequestHandler requestHandler = new RequestHandler();
		if (this.properties.isBanner()) {
			requestHandler.printBanner();
		}
		requestHandler.setHttpMessageConverters(httpMessageConverters);
		requestHandler.setResultProvider(resultProvider);
		requestHandler.setThrowException(properties.isThrowException());

		WebUIController webUIController = createWebUIController(apiServiceProvider, mappingHandlerMapping);
		webUIController.setMagicDynamicDataSource(magicDynamicDataSource);

		requestHandler.setWebUIController(webUIController);
		requestHandler.setDebugTimeout(properties.getDebugConfig().getTimeout());

		mappingHandlerMapping.setHandler(requestHandler);
		mappingHandlerMapping.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
		mappingHandlerMapping.setMagicApiService(apiServiceProvider);
		// 设置拦截器
		setupRequestInterceptor(webUIController, requestHandler);
		// 注册所有映射
		mappingHandlerMapping.registerAllMapping();
		// 自动刷新
		mappingHandlerMapping.enableRefresh(properties.getRefreshInterval());
		return requestHandler;
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
	public DatabaseQuery databaseQuery(MagicDynamicDataSource dynamicDataSource, ResultProvider resultProvider, PageProvider pageProvider, SqlCache sqlCache) {
		RowMapper<Map<String, Object>> rowMapper;
		// 下划线转驼峰命名
		if (properties.isMapUnderscoreToCamelCase()) {
			logger.info("开启下划线转驼峰命名");
			rowMapper = new ColumnMapRowMapper() {
				@Override
				protected String getColumnKey(String columnName) {
					if (columnName == null || !columnName.contains("_")) {
						return columnName;
					}
					columnName = columnName.toLowerCase();
					boolean upperCase = false;
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < columnName.length(); i++) {
						char ch = columnName.charAt(i);
						if (ch == '_') {
							upperCase = true;
						} else if (upperCase) {
							sb.append(Character.toUpperCase(ch));
							upperCase = false;
						} else {
							sb.append(ch);
						}
					}
					return sb.toString();
				}
			};
		} else {
			rowMapper = new ColumnMapRowMapper();
		}
		DatabaseQuery query = new DatabaseQuery(dynamicDataSource);
		query.setResultProvider(resultProvider);
		query.setPageProvider(pageProvider);
		query.setRowMapper(rowMapper);
		query.setSqlCache(sqlCache);
		return query;
	}

	/**
	 * 注册模块、类型扩展
	 */
	private void setupMagicModules(ResultProvider resultProvider, List<MagicModule> magicModules, List<ExtensionMethod> extensionMethods) {
		// 设置脚本import时 class加载策略
		MagicModuleLoader.setClassLoader((className) -> {
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
		MagicModuleLoader.addModule("log", LoggerFactory.getLogger(MagicScript.class));
		List<String> importModules = properties.getAutoImportModuleList();
		logger.info("注册模块:{} -> {}", "request", RequestFunctions.class);
		MagicModuleLoader.addModule("request", new RequestFunctions());
		logger.info("注册模块:{} -> {}", "response", ResponseFunctions.class);
		MagicModuleLoader.addModule("response", new ResponseFunctions(resultProvider));
		logger.info("注册模块:{} -> {}", "assert", AssertFunctions.class);
		MagicModuleLoader.addModule("assert", AssertFunctions.class);
		if (magicModules != null) {
			for (MagicModule module : magicModules) {
				logger.info("注册模块:{} -> {}", module.getModuleName(), module.getClass());
				MagicModuleLoader.addModule(module.getModuleName(), module);
			}
		}
		Set<String> moduleNames = MagicModuleLoader.getModuleNames();
		for (String moduleName : moduleNames) {
			if (importModules.contains(moduleName)) {
				logger.info("自动导入模块：{}", moduleName);
				MagicScriptEngine.addDefaultImport(moduleName, MagicModuleLoader.loadModule(moduleName));
			}
		}
		List<String> importPackages = properties.getAutoImportPackageList();
		for (String importPackage : importPackages) {
			logger.info("自动导包：{}", importPackage);
			MagicPackageLoader.addPackage(importPackage);
		}
		if (extensionMethods != null) {
			for (ExtensionMethod extension : extensionMethods) {
				List<Class<?>> supports = extension.supports();
				for (Class<?> support : supports) {
					logger.info("注册扩展:{} -> {}", support, extension.getClass());
					AbstractReflection.getInstance().registerExtensionClass(support, extension.getClass());
				}
			}
		}
	}

	/**
	 * 注册请求拦截器
	 */
	private void setupRequestInterceptor(WebUIController controller, RequestHandler requestHandler) {
		// 设置拦截器信息
		if (this.requestInterceptors != null) {
			this.requestInterceptors.forEach(interceptor -> {
				logger.info("注册请求拦截器：{}", interceptor.getClass());
				requestHandler.addRequestInterceptor(interceptor);
				if (controller != null) {
					controller.addRequestInterceptor(interceptor);
				}
			});
		}
	}


	/**
	 * 创建UI对应的后台Controller
	 */
	private WebUIController createWebUIController(ApiServiceProvider apiServiceProvider, MappingHandlerMapping mappingHandlerMapping) {
		if (properties.getWeb() == null) {    //	判断是否开启了UI界面
			return null;
		}
		WebUIController controller = new WebUIController();
		controller.setMagicApiService(apiServiceProvider);
		controller.setMappingHandlerMapping(mappingHandlerMapping);
		SecurityConfig securityConfig = properties.getSecurityConfig();
		controller.setUsername(securityConfig.getUsername());
		controller.setPassword(securityConfig.getPassword());

		// 向页面传递配置信息时不传递用户名密码，增强安全性
		securityConfig.setUsername(null);
		securityConfig.setPassword(null);

		// 构建UI请求处理器
		String base = properties.getWeb();
		Method[] methods = WebUIController.class.getDeclaredMethods();
		for (Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if (requestMapping != null) {
				String[] paths = Stream.of(requestMapping.value()).map(value -> base + value).toArray(String[]::new);
				requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(paths).build(), controller, method);
			}
		}
		return controller;
	}

	/**
	 * 注入动态数据源
	 */
	@Bean
	@ConditionalOnMissingBean(MagicDynamicDataSource.class)
	public MagicDynamicDataSource magicDynamicDataSource(DataSource dataSource) {
		MagicDynamicDataSource dynamicDataSource = new MagicDynamicDataSource();
		dynamicDataSource.put(dataSource);
		return dynamicDataSource;
	}
}
