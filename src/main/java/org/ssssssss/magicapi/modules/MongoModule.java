package org.ssssssss.magicapi.modules;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.script.reflection.AbstractReflection;
import org.ssssssss.script.reflection.JavaInvoker;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * mongo模块
 */
public class MongoModule extends HashMap<String, Object> implements MagicModule {

	private final MongoTemplate mongoTemplate;

	private JavaInvoker<Method> invoker;

	public MongoModule(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		Object factory = mongoTemplate.getMongoDbFactory();
		invoker = AbstractReflection.getInstance().getMethod(factory, "getDb", StringUtils.EMPTY);
		if (invoker == null) {
			invoker = AbstractReflection.getInstance().getMethod(factory, "getMongoDatabase", StringUtils.EMPTY);
		}
	}

	@Override
	public Object get(Object databaseName) {
		return databaseName == null ? null : new HashMap<String, MongoCollection<Document>>() {
			@Override
			public MongoCollection<Document> get(Object collection) {
				if (collection == null) {
					return null;
				}
				try {
					MongoDatabase database = (MongoDatabase) invoker.invoke0(mongoTemplate.getMongoDbFactory(), null, databaseName.toString());
					return database.getCollection(collection.toString());
				} catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				}
			}
		};
	}

	@Override
	public String getModuleName() {
		return "mongo";
	}
}
