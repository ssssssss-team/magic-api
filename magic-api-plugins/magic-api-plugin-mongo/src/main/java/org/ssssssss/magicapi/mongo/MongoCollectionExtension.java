package org.ssssssss.magicapi.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.ssssssss.script.annotation.Comment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MongoCollection方法扩展
 *
 * @author mxd
 */
public class MongoCollectionExtension {

	@Comment("执行批量插入操作")
	public void insert(MongoCollection<Document> collection,
					   @Comment(name = "maps", value = "要插入的集合") List<Map<String, Object>> maps) {
		collection.insertMany(maps.stream().map(Document::new).collect(Collectors.toList()));
	}

	@Comment("执行单条插入操作")
	public void insert(MongoCollection<Document> collection,
					   @Comment(name = "map", value = "执行插入数据") Map<String, Object> map) {
		insert(collection, Collections.singletonList(map));
	}

	@Comment("执行查询操作")
	public FindIterable<Document> find(MongoCollection<Document> collection,
									   @Comment(name = "query", value = "查询条件") Map<String, Object> query) {
		return collection.find(new Document(query));
	}

	@Comment("修改操作，返回修改数量")
	public long update(MongoCollection<Document> collection,
					   @Comment(name = "query", value = "查询条件") Map<String, Object> query,
					   @Comment(name = "update", value = "修改值") Map<String, Object> update) {
		return collection.updateOne(new Document(query), new Document(update)).getModifiedCount();
	}

	@Comment("批量修改，返回修改数量")
	public long updateMany(MongoCollection<Document> collection,
						   @Comment(name = "query", value = "修改条件") Map<String, Object> query,
						   @Comment(name = "update", value = "修改值") Map<String, Object> update) {
		return collection.updateMany(new Document(query), new Document(update)).getModifiedCount();
	}

	@Comment("批量修改，返回修改数量")
	public long updateMany(MongoCollection<Document> collection,
						   @Comment(name = "query", value = "查询条件") Map<String, Object> query,
						   @Comment(name = "update", value = "修改值") Map<String, Object> update,
						   @Comment(name = "filters", value = "过滤条件") Map<String, Object> filters) {
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

	@Comment("查询数量")
	public long count(MongoCollection<Document> collection,
					  @Comment(name = "query", value = "查询") Map<String, Object> query) {
		return collection.countDocuments(new Document(query));
	}

	@Comment("批量删除，返回删除条数")
	public long remove(MongoCollection<Document> collection,
					   @Comment(name = "query", value = "删除条件") Map<String, Object> query) {
		return collection.deleteMany(new Document(query)).getDeletedCount();
	}

	@Comment("删除一条，返回删除条数")
	public long removeOne(MongoCollection<Document> collection,
						  @Comment(name = "query", value = "删除条件") Map<String, Object> query) {
		return collection.deleteOne(new Document(query)).getDeletedCount();
	}
}
