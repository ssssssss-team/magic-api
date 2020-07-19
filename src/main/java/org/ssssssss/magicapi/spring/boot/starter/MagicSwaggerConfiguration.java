package org.ssssssss.magicapi.spring.boot.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.swagger.SwaggerProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger配置类
 */
@Configuration
@AutoConfigureAfter({MagicAPIAutoConfiguration.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
public class MagicSwaggerConfiguration {

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private MagicAPIProperties properties;

	public MagicSwaggerConfiguration(MagicAPIProperties properties) {
		this.properties = properties;
	}

	@Bean
	@Primary
	public SwaggerResourcesProvider swaggerResourcesProvider(@Autowired(required = false) SwaggerResourcesProvider provider, MappingHandlerMapping handlerMapping) throws NoSuchMethodException {

		SwaggerConfig config = properties.getSwaggerConfig();
		RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(config.getLocation()).build();

		// 构建文档信息
		SwaggerProvider swaggerProvider = new SwaggerProvider();
		swaggerProvider.setMappingHandlerMapping(handlerMapping);
		swaggerProvider.setTitle(config.getTitle());
		swaggerProvider.setDescription(config.getDescription());
		swaggerProvider.setVersion(config.getVersion());

		// 注册swagger.json
		requestMappingHandlerMapping.registerMapping(requestMappingInfo, swaggerProvider, SwaggerProvider.class.getDeclaredMethod("swaggerJson"));

		return () -> {
			List<SwaggerResource> resources = new ArrayList<>();
			// 获取已定义的文档信息
			if (provider != null) {
				resources.addAll(provider.get());
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
