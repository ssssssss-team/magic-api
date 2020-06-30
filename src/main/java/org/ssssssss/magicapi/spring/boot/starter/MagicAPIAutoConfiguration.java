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
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.magicapi.provider.impl.DefaultPageProvider;
import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.magicapi.functions.AssertFunctions;
import org.ssssssss.magicapi.functions.DatabaseQuery;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
@ConditionalOnClass({DataSource.class, RequestMappingHandlerMapping.class})
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(MagicAPIProperties.class)
public class MagicAPIAutoConfiguration implements WebMvcConfigurer {

	private static Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);
	@Autowired
	RequestMappingHandlerMapping requestMappingHandlerMapping;

	private MagicAPIProperties properties;

	@Autowired(required = false)
	private List<RequestInterceptor> requestInterceptors;

	@Autowired
	private ApplicationContext springContext;

	public MagicAPIAutoConfiguration(MagicAPIProperties properties) {
		this.properties = properties;
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

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String web = properties.getWeb();
		if (web != null) {
			// 配置静态资源路径
			registry.addResourceHandler(web + "/**").addResourceLocations("classpath:/magicapi-support/");
			try {
				// 默认首页设置
				requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(web).build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("redirectIndex", HttpServletRequest.class));
				// 读取配置
				requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(web + "/config.json").build(), this, MagicAPIAutoConfiguration.class.getDeclaredMethod("readConfig"));
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

	@ConditionalOnMissingBean(SqlCache.class)
	@Bean
	public SqlCache sqlCache() {
		CacheConfig cacheConfig = properties.getCacheConfig();
		logger.info("未找到SQL缓存实现，采用默认缓存实现(LRU+TTL)，缓存配置:(容量={},TTL={})", cacheConfig.getCapacity(), cacheConfig.getTtl());
		return new DefaultSqlCache(cacheConfig.getCapacity(), cacheConfig.getTtl());
	}

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
		return handlerMapping;
	}

	@Bean
	public MagicApiService magicApiService(DynamicDataSource dynamicDataSource) {
		return new MagicApiService(dynamicDataSource.getJdbcTemplate());
	}

	@Bean
	public RequestHandler requestHandler(MagicApiService magicApiService, DynamicDataSource dynamicDataSource, PageProvider pageProvider, MappingHandlerMapping mappingHandlerMapping, SqlCache sqlCache) {
		RowMapper<Map<String, Object>> rowMapper;
		if (properties.isMapUnderscoreToCamelCase()) {
			rowMapper = new ColumnMapRowMapper() {
				@Override
				protected String getColumnKey(String columnName) {
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
		MagicModuleLoader.addModule("log", LoggerFactory.getLogger(MagicScript.class));
		MagicModuleLoader.addModule("assert", AssertFunctions.class);
		DatabaseQuery query = new DatabaseQuery(dynamicDataSource);
		query.setPageProvider(pageProvider);
		query.setRowMapper(rowMapper);
		query.setSqlCache(sqlCache);
		MagicScriptEngine.addDefaultImport("db", query);
		Method[] methods = WebUIController.class.getDeclaredMethods();
		WebUIController controller = new WebUIController();
		controller.setDebugTimeout(properties.getDebugConfig().getTimeout());
		controller.setMagicApiService(magicApiService);
		controller.setMappingHandlerMapping(mappingHandlerMapping);
		if (this.properties.isBanner()) {
			controller.printBanner();
		}
		String base = properties.getWeb();
		for (Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if (requestMapping != null) {
				String[] paths = Stream.of(requestMapping.value()).map(value -> base + value).toArray(String[]::new);
				requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(paths).build(), controller, method);
			}
		}
		RequestHandler requestHandler = new RequestHandler();
		requestHandler.setThrowException(properties.isThrowException());
		if (this.requestInterceptors != null) {
			this.requestInterceptors.forEach(interceptor -> {
				logger.info("注册请求拦截器：{}", interceptor.getClass());
				requestHandler.addRequestInterceptor(interceptor);
			});
		}
		mappingHandlerMapping.setHandler(requestHandler);
		mappingHandlerMapping.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
		mappingHandlerMapping.setMagicApiService(magicApiService);
		mappingHandlerMapping.registerAllMapping();
		return requestHandler;
	}

	@Bean
	@ConditionalOnMissingBean(DynamicDataSource.class)
	public DynamicDataSource dynamicDataSource(DataSource dataSource) {
		DynamicDataSource dynamicDataSource = new DynamicDataSource();
		dynamicDataSource.put(dataSource);
		return dynamicDataSource;
	}
}
