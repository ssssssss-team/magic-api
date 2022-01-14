package org.ssssssss.magicapi.spring.boot.starter;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.modules.elasticsearch.ElasticSearchModule;

@Configuration
@ConditionalOnMissingBean(ElasticSearchModule.class)
@ConditionalOnClass(RestHighLevelClient.class)
public class MagicElasticSearchAutoConfiguration {

	@Bean
	@ConditionalOnBean(RestHighLevelClient.class)
	public ElasticSearchModule elasticSearchModule(RestHighLevelClient restHighLevelClient){
		return new ElasticSearchModule(restHighLevelClient.getLowLevelClient());
	}
}
