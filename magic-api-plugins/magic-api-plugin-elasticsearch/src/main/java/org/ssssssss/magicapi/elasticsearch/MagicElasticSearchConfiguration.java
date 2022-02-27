package org.ssssssss.magicapi.elasticsearch;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;

@Configuration
public class MagicElasticSearchConfiguration implements MagicPluginConfiguration {

	@Override
	public Plugin plugin() {
		return new Plugin("ElasticSearch");
	}

	@Bean
	@ConditionalOnBean(RestHighLevelClient.class)
	public ElasticSearchModule elasticSearchModule(RestHighLevelClient restHighLevelClient){
		return new ElasticSearchModule(restHighLevelClient.getLowLevelClient());
	}
}
