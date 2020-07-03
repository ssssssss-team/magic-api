package org.ssssssss.magicapi.functions;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MongoCollection方法扩展
 */
public class MongoCollectionExtension {

	public static void insert(MongoCollection<Document> collection, List<Map<String, Object>> maps) {
		collection.insertMany(maps.stream().map(Document::new).collect(Collectors.toList()));
	}

	public static void insert(MongoCollection<Document> collection, Map<String, Object> map) {
		insert(collection, Collections.singletonList(map));
	}

	public static FindIterable<Document> find(MongoCollection<Document> collection, Map<String, Object> query) {
		return collection.find(new Document(query));
	}

	public static long update(MongoCollection<Document> collection, Map<String, Object> query, Map<String, Object> update) {
		return collection.updateOne(new Document(query), new Document(update)).getModifiedCount();
	}

	public static long updateMany(MongoCollection<Document> collection, Map<String, Object> query, Map<String, Object> update) {
		return collection.updateMany(new Document(query), new Document(update)).getModifiedCount();
	}

	public static long updateMany(MongoCollection<Document> collection, Map<String, Object> query, Map<String, Object> update, Map<String, Object> filters) {
		UpdateOptions updateOptions = new UpdateOptions();
		if (filters != null && !filters.isEmpty()) {
			Object upsert = filters.get("upsert");
			if (upsert != null) {
				filters.remove("upsert");
				updateOptions.upsert(Boolean.parseBoolean(upsert.toString()));
			}
			Object bypassDocumentValidation = filters.get("bypassDocumentValidation");
			if (bypassDocumentValidation != null) {
				filters.remove("bypassDocumentValidation");
				updateOptions.bypassDocumentValidation(Boolean.parseBoolean(bypassDocumentValidation.toString()));
			}
			List<Document> arrayFilters = filters.entrySet().stream().map(entry -> new Document(entry.getKey(), entry.getValue())).collect(Collectors.toList());
			updateOptions.arrayFilters(arrayFilters);
		}
		return collection.updateMany(new Document(query), new Document(update), updateOptions).getModifiedCount();
	}

	public static long count(MongoCollection<Document> collection, Map<String, Object> query) {
		return collection.count(new Document(query));
	}

	public static long remove(MongoCollection<Document> collection, Map<String, Object> query) {
		return collection.deleteMany(new Document(query)).getDeletedCount();
	}

	public static long removeOne(MongoCollection<Document> collection, Map<String, Object> query) {
		return collection.deleteOne(new Document(query)).getDeletedCount();
	}
}
