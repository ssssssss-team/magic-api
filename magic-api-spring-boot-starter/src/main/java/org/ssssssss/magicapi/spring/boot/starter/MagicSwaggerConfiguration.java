package org.ssssssss.magicapi.spring.boot.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.swagger.SwaggerEntity;
import org.ssssssss.magicapi.swagger.SwaggerProvider;
import org.ssssssss.magicapi.utils.Mapping;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Swagger配置类
 */
@Configuration
@AutoConfigureAfter({MagicAPIAutoConfiguration.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
@ConditionalOnClass(name = "springfox.documentation.swagger.web.SwaggerResourcesProvider")
public class MagicSwaggerConfiguration {

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private final MagicAPIProperties properties;

	private ApplicationContext applicationContext;

	public MagicSwaggerConfiguration(MagicAPIProperties properties,ApplicationContext applicationContext) {
		this.properties = properties;
		this.applicationContext = applicationContext;
	}


	@Bean
	@Primary
	public SwaggerResourcesProvider magicSwaggerResourcesProvider(MappingHandlerMapping handlerMapping, GroupServiceProvider groupServiceProvider, ServletContext servletContext) throws NoSuchMethodException {
		SwaggerConfig config = properties.getSwaggerConfig();
		RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(config.getLocation()).build();

		// 构建文档信息
		SwaggerProvider swaggerProvider = new SwaggerProvider();
		swaggerProvider.setGroupServiceProvider(groupServiceProvider);
		swaggerProvider.setMappingHandlerMapping(handlerMapping);
		SwaggerEntity.License license = new SwaggerEntity.License("MIT", "https://gitee.com/ssssssss-team/magic-api/blob/master/LICENSE");
		swaggerProvider.setInfo(new SwaggerEntity.Info(config.getDescription(), config.getVersion(), config.getTitle(), license, config.getConcat()));
		swaggerProvider.setBasePath(servletContext.getContextPath());


		// 注册swagger.json
		Mapping.create(requestMappingHandlerMapping).register(requestMappingInfo, swaggerProvider, SwaggerProvider.class.getDeclaredMethod("swaggerJson"));

		return () -> {
			List<SwaggerResource> resources = new ArrayList<>();
			Map<String, SwaggerResourcesProvider> beans = applicationContext.getBeansOfType(SwaggerResourcesProvider.class);
			// 获取已定义的文档信息
			if (beans != null) {
				for (Map.Entry<String, SwaggerResourcesProvider> entry : beans.entrySet()) {
					if (!"magicSwaggerResourcesProvider".equalsIgnoreCase(entry.getKey())) {
						resources.addAll(entry.getValue().get());
					}
				}
			}
			// 追加Magic Swagger信息
			resources.add(swaggerResource(config.getName(), config.getLocation()));
			return resources;
		};
	}

	/**
	 * 构建 SwaggerResource
	 *
	 * @param name     名字
	 * @param location 位置
	 * @return
	 */
	private SwaggerResource swaggerResource(String name, String location) {
		SwaggerResource resource = new SwaggerResource();
		resource.setName(name);
		resource.setLocation(location);
		resource.setSwaggerVersion("2.0");
		return resource;
	}
}
