package org.ssssssss.magicapi.spring.boot.starter;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.functions.MongoCollectionExtension;
import org.ssssssss.magicapi.functions.MongoFindIterableExtension;
import org.ssssssss.magicapi.functions.MongoFunctions;
import org.ssssssss.script.reflection.AbstractReflection;

/**
 * mongo配置
 */
@ConditionalOnBean(MongoClient.class)
@Configuration
public class MagicMongoAutoConfiguration {

	/**
	 * 注入mongo模块
	 */
	@Bean
	public MongoFunctions mongoFunctions(MongoClient mongoClient) {
		AbstractReflection.getInstance().registerExtensionClass(MongoCollection.class, MongoCollectionExtension.class);
		AbstractReflection.getInstance().registerExtensionClass(FindIterable.class, MongoFindIterableExtension.class);
		return new MongoFunctions(mongoClient);
	}
}
