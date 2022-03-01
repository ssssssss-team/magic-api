package org.ssssssss.magicapi.git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.config.Resource;
import org.ssssssss.magicapi.core.model.Plugin;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(MagicGitProperties.class)
public class MagicGitConfiguration implements MagicPluginConfiguration {

	private final MagicAPIProperties properties;
	private final MagicGitProperties gitProperties;

	public MagicGitConfiguration(MagicAPIProperties properties, MagicGitProperties gitProperties) {
		this.properties = properties;
		this.gitProperties = gitProperties;
	}

	/**
	 * git存储
	 * @author soriee
	 * @date 2022/2/28 19:50
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "git")
	public org.ssssssss.magicapi.core.resource.Resource magicGitResource() throws IOException, GitAPIException {
		Resource resourceConfig = properties.getResource();
		return GitResource.of(resourceConfig, this.gitProperties);
	}


	@Override
	public Plugin plugin() {
		return new Plugin("Git");
	}
}
