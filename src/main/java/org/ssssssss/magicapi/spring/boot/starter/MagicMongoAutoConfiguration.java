package org.ssssssss.magicapi.spring.boot.starter;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.ssssssss.magicapi.modules.MongoCollectionExtension;
import org.ssssssss.magicapi.modules.MongoFindIterableExtension;
import org.ssssssss.magicapi.modules.MongoModule;
import org.ssssssss.script.reflection.AbstractReflection;

/**
 * mongo配置
 */
@Configuration
@ConditionalOnBean(MongoTemplate.class)
public class MagicMongoAutoConfiguration {

	/**
	 * 注入mongo模块
	 */
	@Bean
	public MongoModule mongoFunctions(MongoTemplate mongoTemplate) {
		AbstractReflection.getInstance().registerExtensionClass(MongoCollection.class, MongoCollectionExtension.class);
		AbstractReflection.getInstance().registerExtensionClass(FindIterable.class, MongoFindIterableExtension.class);
		return new MongoModule(mongoTemplate);
	}
}
