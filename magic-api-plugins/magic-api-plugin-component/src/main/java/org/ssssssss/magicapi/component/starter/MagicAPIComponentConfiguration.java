package org.ssssssss.magicapi.component.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.component.service.ComponentInfoMagicResourceStorage;
import org.ssssssss.magicapi.component.service.ComponentMagicDynamicRegistry;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;

@Configuration
public class MagicAPIComponentConfiguration implements MagicPluginConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ComponentInfoMagicResourceStorage componentInfoMagicResourceStorage() {
		return new ComponentInfoMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public ComponentMagicDynamicRegistry componentMagicDynamicRegistry(ComponentInfoMagicResourceStorage componentInfoMagicResourceStorage) {
		return new ComponentMagicDynamicRegistry(componentInfoMagicResourceStorage);
	}

	@Override
	public Plugin plugin() {
		 return new Plugin("组件", "MagicComponent", "magic-component.1.0.0.iife.js");
	}

}
