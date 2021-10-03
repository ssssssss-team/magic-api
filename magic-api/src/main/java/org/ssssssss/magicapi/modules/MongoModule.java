package org.ssssssss.magicapi.modules;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.utils.Invoker;
import org.ssssssss.script.reflection.JavaReflection;

import java.util.HashMap;

/**
 * mongo模块
 *
 * @author mxd
 */
public class MongoModule extends HashMap<String, Object> implements MagicModule {

	private static final Logger logger = LoggerFactory.getLogger(MongoModule.class);

	private final MongoTemplate mongoTemplate;
	private final Invoker mongoDbFactoryInvoker;
	private Invoker invoker;

	public MongoModule(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		mongoDbFactoryInvoker = Invoker.from(JavaReflection.getMethod(this.mongoTemplate, "getMongoDbFactory"));
		if (mongoDbFactoryInvoker != null) {
			try {
				Object factory = mongoDbFactoryInvoker.invoke(this.mongoTemplate, null, Constants.EMPTY_OBJECT_ARRAY);
				invoker = Invoker.from(JavaReflection.getMethod(factory, "getDb", StringUtils.EMPTY));
				if (invoker == null) {
					invoker = Invoker.from(JavaReflection.getMethod(factory, "getMongoDatabase", StringUtils.EMPTY));
				}
			} catch (Throwable e) {
				logger.error("mongo模块初始化失败", e);
			}
		} else {
			logger.error("mongo模块初始化失败");
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
					Object factory = mongoDbFactoryInvoker.invoke(mongoTemplate, null, Constants.EMPTY_OBJECT_ARRAY);
					MongoDatabase database = (MongoDatabase) invoker.invoke(factory, null, new Object[]{databaseName.toString()});
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
