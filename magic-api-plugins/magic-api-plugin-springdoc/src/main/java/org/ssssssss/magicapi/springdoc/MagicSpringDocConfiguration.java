package org.ssssssss.magicapi.springdoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.ssssssss.magicapi.springdoc.entity.SwaggerEntity;
import org.ssssssss.magicapi.springdoc.entity.SwaggerProvider;
import org.ssssssss.magicapi.utils.Mapping;

import jakarta.servlet.ServletContext;

import java.util.*;

@Configuration
@EnableConfigurationProperties(SpringDocConfig.class)
@ConditionalOnProperty(
		name = {"springdoc.api-docs.enabled"},
		matchIfMissing = true
)
public class MagicSpringDocConfiguration implements MagicPluginConfiguration {

	private final MagicAPIProperties properties;
	private final SpringDocConfig springDocConfig;
	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private final ObjectProvider<RequestMagicDynamicRegistry> requestMagicDynamicRegistryObjectProvider;
	private final MagicResourceService magicResourceService;
	private final ServletContext servletContext;

	private boolean createdMapping = false;

	private static Logger logger = LoggerFactory.getLogger(MagicSpringDocConfiguration.class);

	public MagicSpringDocConfiguration(MagicAPIProperties properties, SpringDocConfig springDocConfig, ObjectProvider<RequestMagicDynamicRegistry> requestMagicDynamicRegistryObjectProvider, MagicResourceService magicResourceService, ServletContext servletContext) {
		this.properties = properties;
		this.springDocConfig = springDocConfig;
		this.requestMagicDynamicRegistryObjectProvider = requestMagicDynamicRegistryObjectProvider;
		this.magicResourceService = magicResourceService;
		this.servletContext = servletContext;
	}

	@Override
	public Plugin plugin() {
		return new Plugin("SpringDoc");
	}

	@Bean
	@Primary
	@Lazy
	public SwaggerUiConfigParameters magicSwaggerUiConfigParameters(SwaggerUiConfigProperties swaggerUiConfigProperties) {
		return new SwaggerUiConfigParameters(swaggerUiConfigProperties) {
			@Override
			public Map<String, Object> getConfigParameters() {
				Map<String, Object> params = super.getConfigParameters();
				if (!createdMapping) {
					createdMapping = true;
					try {
						createSwaggerProvider(requestMagicDynamicRegistryObjectProvider, magicResourceService, servletContext);
					} catch (NoSuchMethodException e) {
						logger.error("注册springdoc接口失败", e);
						return params;
					}
				}
				Set<SwaggerUrl> urls = (Set<SwaggerUrl>) params.get("urls");
				if (urls == null) {
					urls = new HashSet<>();
					SwaggerUrl url = new SwaggerUrl("default", (String) params.remove("url"), null);
					urls.add(url);
				} else {
					urls = new HashSet<>(urls);
				}
				urls.add(new SwaggerUrl(springDocConfig.getGroupName(), servletContext.getContextPath() + springDocConfig.getLocation(), null));
				params.put("urls", urls);
				return params;
			}
		};
	}


	private void createSwaggerProvider(ObjectProvider<RequestMagicDynamicRegistry> requestMagicDynamicRegistryObjectProvider, MagicResourceService magicResourceService, ServletContext servletContext) throws NoSuchMethodException {
		Mapping mapping = Mapping.create(requestMappingHandlerMapping);
		RequestMappingInfo requestMappingInfo = mapping.paths(springDocConfig.getLocation()).build();
		SwaggerEntity.License license = new SwaggerEntity.License("MIT", "https://gitee.com/ssssssss-team/magic-api/blob/master/LICENSE");
		SwaggerEntity.Info info = new SwaggerEntity.Info(springDocConfig.getDescription(), springDocConfig.getVersion(), springDocConfig.getTitle(), license, springDocConfig.getConcat());

		//具体参考：https://swagger.io/docs/specification/2-0/authentication/
		Map<String, Object> securityDefinitionMap = new HashMap<>();
		Map<String, Object> securityMap = new HashMap<>();

		if (springDocConfig.getBasicAuth() != null) {
			securityDefinitionMap.put(SwaggerEntity.BasicAuth.KEY_NAME, springDocConfig.getBasicAuth());

			//the Basic and API key security items use an empty array instead.
			securityMap.put(SwaggerEntity.BasicAuth.KEY_NAME, new String[]{});
		}
		if (springDocConfig.getApiKeyAuth() != null) {
			securityDefinitionMap.put(SwaggerEntity.ApiKeyAuth.KEY_NAME, springDocConfig.getApiKeyAuth());

			//the Basic and API key security items use an empty array instead.
			securityMap.put(SwaggerEntity.ApiKeyAuth.KEY_NAME, new String[]{});
		}
		if (springDocConfig.getOauth2() != null) {
			SwaggerEntity.OAuth2 oAuth2 = springDocConfig.getOauth2();
			securityDefinitionMap.put(SwaggerEntity.OAuth2.KEY_NAME, oAuth2);

			Map<String, String> scopes = oAuth2.getScopes();
			if (scopes != null) {
				Set<String> strings = scopes.keySet();
				securityMap.put(SwaggerEntity.OAuth2.KEY_NAME, strings);
			}
		}
		// 构建文档信息
		SwaggerProvider swaggerProvider = new SwaggerProvider(requestMagicDynamicRegistryObjectProvider.getObject(), magicResourceService, servletContext.getContextPath(),
				info, properties.isPersistenceResponseBody(), properties.getPrefix(), securityDefinitionMap, securityMap);
		// 注册swagger.json
		mapping.register(requestMappingInfo, swaggerProvider, SwaggerProvider.class.getDeclaredMethod("swaggerJson"));
	}
}
