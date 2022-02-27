package org.ssssssss.magicapi.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.script.reflection.JavaReflection;

@Configuration
public class MagicMongoConfiguration implements MagicPluginConfiguration {

	@Override
	public Plugin plugin() {
		return new Plugin("Mongo");
	}

	/**
	 * 注入mongo模块
	 */
	@Bean
	@ConditionalOnMissingBean
	public MongoModule mongoFunctions(MongoTemplate mongoTemplate) {
		JavaReflection.registerMethodExtension(MongoCollection.class, new MongoCollectionExtension());
		JavaReflection.registerMethodExtension(FindIterable.class, new MongoFindIterableExtension());
		return new MongoModule(mongoTemplate);
	}
}
