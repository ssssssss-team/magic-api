package org.ssssssss.magicapi.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.convert.ClassImplicitConvert;
import org.ssssssss.script.functions.DynamicAttribute;
import org.ssssssss.script.reflection.JavaInvoker;
import org.ssssssss.script.reflection.JavaReflection;
import org.ssssssss.script.runtime.Variables;

import java.beans.Transient;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * mongo模块
 *
 * @author mxd
 */
@MagicModule("mongo")
public class MongoModule implements ClassImplicitConvert, DynamicAttribute<MongoModule.MongoDataBaseGetter, MongoModule.MongoDataBaseGetter> {

	private static final Logger logger = LoggerFactory.getLogger(MongoModule.class);

	private JavaInvoker<Method> invoker;

	private Object factory;

	public MongoModule(MongoTemplate mongoTemplate) {
		JavaInvoker<Method> mongoDbFactoryInvoker = JavaReflection.getMethod(mongoTemplate, "getMongoDbFactory");
		if(mongoDbFactoryInvoker == null){
			mongoDbFactoryInvoker = JavaReflection.getMethod(mongoTemplate, "getMongoDatabaseFactory");
		}
		if (mongoDbFactoryInvoker != null) {
			try {
				factory = mongoDbFactoryInvoker.invoke0(mongoTemplate, null, Constants.EMPTY_OBJECT_ARRAY);
				invoker = JavaReflection.getMethod(factory, "getDb", StringUtils.EMPTY);
				if (invoker == null) {
					invoker = JavaReflection.getMethod(factory, "getMongoDatabase", StringUtils.EMPTY);
				}
			} catch (Throwable e) {
				logger.error("mongo模块初始化失败", e);
			}
		} else {
			logger.error("mongo模块初始化失败");
		}
		JavaReflection.registerImplicitConvert(this);
	}

	@Override
	@Transient
	public MongoDataBaseGetter getDynamicAttribute(String databaseName) {
		try {
			if (databaseName == null) {
				return null;
			}
			MongoDatabase database = (MongoDatabase) invoker.invoke0(factory, null, new Object[]{databaseName});
			return new MongoDataBaseGetter(database);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean support(Class<?> from, Class<?> to) {
		return Map.class.isAssignableFrom(from) && (Bson.class.isAssignableFrom(to));
	}

	@Override
	public Object convert(Variables variables, Object source, Class<?> target) {
		return new Document((Map<String, Object>) source);
	}

	public static class MongoDataBaseGetter implements DynamicAttribute<MongoCollection<Document>, MongoCollection<Document>> {

		MongoDatabase database;

		public MongoDataBaseGetter(MongoDatabase database) {
			this.database = database;
		}

		@Override
		@Transient
		public MongoCollection<Document> getDynamicAttribute(String key) {
			return database.getCollection(key);
		}
	}
}
