package org.ssssssss.magicapi.swagger;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.ServletContext;
import java.util.*;

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

		//具体参考：https://swagger.io/docs/specification/2-0/authentication/
		Map<String, Object> securityDefinitionMap = new HashMap<>();
		Map<String, Object> securityMap = new HashMap<>();

		if (swaggerConfig.getBasicAuth() != null) {
			securityDefinitionMap.put(SwaggerEntity.BasicAuth.KEY_NAME, swaggerConfig.getBasicAuth());

			//the Basic and API key security items use an empty array instead.
			securityMap.put(SwaggerEntity.BasicAuth.KEY_NAME, new String[]{});
		}
		if (swaggerConfig.getApiKeyAuth() != null) {
			securityDefinitionMap.put(SwaggerEntity.ApiKeyAuth.KEY_NAME, swaggerConfig.getApiKeyAuth());

			//the Basic and API key security items use an empty array instead.
			securityMap.put(SwaggerEntity.ApiKeyAuth.KEY_NAME, new String[]{});
		}
		if (swaggerConfig.getOauth2() != null) {
			SwaggerEntity.OAuth2 oAuth2 = swaggerConfig.getOauth2();
			securityDefinitionMap.put(SwaggerEntity.OAuth2.KEY_NAME, oAuth2);

			Map<String, String> scopes = oAuth2.getScopes();
			if (scopes != null) {
				Set<String> strings = scopes.keySet();
				securityMap.put(SwaggerEntity.OAuth2.KEY_NAME, strings);
			}
		}

		// 构建文档信息
		SwaggerProvider swaggerProvider = new SwaggerProvider(requestMagicDynamicRegistry, magicResourceService, servletContext.getContextPath(),
				info, properties.isPersistenceResponseBody(), properties.getPrefix(), securityDefinitionMap, securityMap);


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
