package org.ssssssss.magicapi.functions;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.ssssssss.magicapi.config.MagicModule;

import java.util.HashMap;

public class MongoFunctions extends HashMap<String,Object> implements MagicModule {

	private MongoClient mongoClient;

	public MongoFunctions(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	@Override
	public Object get(Object database) {
		return database == null ? null : new HashMap<String, MongoCollection>(){
			@Override
			public MongoCollection get(Object collection) {
				return collection == null ? null : mongoClient.getDatabase(database.toString()).getCollection(collection.toString());
			}
		};
	}

	@Override
	public String getModuleName() {
		return "mongo";
	}
}
