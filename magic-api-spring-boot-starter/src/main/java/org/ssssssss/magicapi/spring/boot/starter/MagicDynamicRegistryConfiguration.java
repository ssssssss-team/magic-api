package org.ssssssss.magicapi.spring.boot.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import org.ssssssss.magicapi.core.interceptor.DefaultResultProvider;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;
import org.ssssssss.magicapi.core.service.impl.ApiInfoMagicResourceStorage;
import org.ssssssss.magicapi.core.service.impl.RequestMagicDynamicRegistry;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;
import org.ssssssss.magicapi.datasource.service.DataSourceInfoMagicResourceStorage;
import org.ssssssss.magicapi.datasource.service.DataSourceMagicDynamicRegistry;
import org.ssssssss.magicapi.function.service.FunctionInfoMagicResourceStorage;
import org.ssssssss.magicapi.function.service.FunctionMagicDynamicRegistry;
import org.ssssssss.magicapi.utils.Mapping;

@Configuration
@AutoConfigureAfter(MagicModuleConfiguration.class)
public class MagicDynamicRegistryConfiguration {


	private final MagicAPIProperties properties;

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;


	public MagicDynamicRegistryConfiguration(MagicAPIProperties properties) {
		this.properties = properties;
	}

	@Bean
	@ConditionalOnMissingBean
	public ApiInfoMagicResourceStorage apiInfoMagicResourceStorage() {
		return new ApiInfoMagicResourceStorage(properties.getPrefix());
	}

	@Bean
	@ConditionalOnMissingBean
	public RequestMagicDynamicRegistry magicRequestMagicDynamicRegistry(ApiInfoMagicResourceStorage apiInfoMagicResourceStorage) throws NoSuchMethodException {
		return new RequestMagicDynamicRegistry(apiInfoMagicResourceStorage, Mapping.create(requestMappingHandlerMapping, properties.getWeb()), properties.isAllowOverride(), properties.getPrefix());
	}

	@Bean
	@ConditionalOnMissingBean
	public FunctionInfoMagicResourceStorage functionInfoMagicResourceStorage() {
		return new FunctionInfoMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public FunctionMagicDynamicRegistry functionMagicDynamicRegistry(FunctionInfoMagicResourceStorage functionInfoMagicResourceStorage) {
		return new FunctionMagicDynamicRegistry(functionInfoMagicResourceStorage);
	}

	@Bean
	@ConditionalOnMissingBean
	public DataSourceInfoMagicResourceStorage dataSourceInfoMagicResourceStorage() {
		return new DataSourceInfoMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public DataSourceMagicDynamicRegistry dataSourceMagicDynamicRegistry(DataSourceInfoMagicResourceStorage dataSourceInfoMagicResourceStorage, MagicDynamicDataSource magicDynamicDataSource) {
		return new DataSourceMagicDynamicRegistry(dataSourceInfoMagicResourceStorage, magicDynamicDataSource);
	}

}
