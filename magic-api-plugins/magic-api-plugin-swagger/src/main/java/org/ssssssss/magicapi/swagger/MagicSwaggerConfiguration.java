package org.ssssssss.magicapi.swagger;

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
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.impl.RequestMagicDynamicRegistry;
import org.ssssssss.magicapi.swagger.entity.SwaggerEntity;
import org.ssssssss.magicapi.swagger.entity.SwaggerProvider;
import org.ssssssss.magicapi.utils.Mapping;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SwaggerConfig.class)
@ConditionalOnClass(name = "springfox.documentation.swagger.web.SwaggerResourcesProvider")
public class MagicSwaggerConfiguration implements MagicPluginConfiguration {

	private final MagicAPIProperties properties;
	private final SwaggerConfig swaggerConfig;
	private final ApplicationContext applicationContext;

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	public MagicSwaggerConfiguration(MagicAPIProperties properties, SwaggerConfig swaggerConfig, ApplicationContext applicationContext) {
		this.properties = properties;
		this.swaggerConfig = swaggerConfig;
		this.applicationContext = applicationContext;
	}

	@Override
	public Plugin plugin() {
		return new Plugin("Swagger");
	}

	@Bean
	@Primary
	public SwaggerResourcesProvider magicSwaggerResourcesProvider(RequestMagicDynamicRegistry requestMagicDynamicRegistry, MagicResourceService magicResourceService, ServletContext servletContext) throws NoSuchMethodException {
		Mapping mapping = Mapping.create(requestMappingHandlerMapping);
		RequestMappingInfo requestMappingInfo = mapping.paths(swaggerConfig.getLocation()).build();
		SwaggerEntity.License license = new SwaggerEntity.License("MIT", "https://gitee.com/ssssssss-team/magic-api/blob/master/LICENSE");
		SwaggerEntity.Info info = new SwaggerEntity.Info(swaggerConfig.getDescription(), swaggerConfig.getVersion(), swaggerConfig.getTitle(), license, swaggerConfig.getConcat());
		// 构建文档信息
		SwaggerProvider swaggerProvider = new SwaggerProvider(requestMagicDynamicRegistry, magicResourceService, servletContext.getContextPath(), info, properties.isPersistenceResponseBody(), properties.getPrefix());


		// 注册swagger.json
		mapping.register(requestMappingInfo, swaggerProvider, SwaggerProvider.class.getDeclaredMethod("swaggerJson"));

		return () -> {
			List<SwaggerResource> resources = new ArrayList<>();
			// 追加Magic Swagger信息
			resources.add(swaggerResource(swaggerConfig.getName(), swaggerConfig.getLocation()));
			Map<String, SwaggerResourcesProvider> beans = applicationContext.getBeansOfType(SwaggerResourcesProvider.class);
			// 获取已定义的文档信息
			for (Map.Entry<String, SwaggerResourcesProvider> entry : beans.entrySet()) {
				if (!"magicSwaggerResourcesProvider".equalsIgnoreCase(entry.getKey())) {
					resources.addAll(entry.getValue().get());
				}
			}
			return resources;
		};
	}

	/**
	 * 构建 SwaggerResource
	 *
	 * @param name     名字
	 * @param location 位置
	 */
	private SwaggerResource swaggerResource(String name, String location) {
		SwaggerResource resource = new SwaggerResource();
		resource.setName(name);
		resource.setLocation(location);
		resource.setSwaggerVersion("2.0");
		return resource;
	}
}
