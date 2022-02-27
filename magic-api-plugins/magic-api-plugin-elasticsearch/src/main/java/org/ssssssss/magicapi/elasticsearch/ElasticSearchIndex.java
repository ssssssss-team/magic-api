package org.ssssssss.magicapi.elasticsearch;

import org.elasticsearch.client.RestClient;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.annotation.Comment;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ElasticSearchIndex {

	private final RestClient restClient;

	private final String name;

	private final String type;


	public ElasticSearchIndex(RestClient restClient, String name, String type) {
		this.restClient = restClient;
		this.name = name;
		this.type = type;
	}

	@Comment("根据`_id`保存，当存在时更新，不存在时插入")
	public Object save(@Comment(value = "_id", name = "_id")String _id, @Comment(value = "保存对象", name = "data")Object data) throws IOException {
		return connect("/%s/%s/%s", this.name, this.type, _id).post(data);
	}

	@Comment("不指定`_id`插入")
	public Object insert(@Comment(value = "插入对象", name = "data")Object data) throws IOException {
		return connect("/%s/%s", this.name, this.type).post(data);
	}

	@Comment("指定`_id`插入，当`_id`存在时不会更新")
	public Object insert(@Comment(value = "_id", name = "_id")String _id, @Comment(value = "插入对象", name = "data")Object data) throws IOException {
		return connect("/%s/%s/%s/_create", this.name, this.type, _id).post(data);
	}

	@Comment("根据`id`删除")
	public Object delete(@Comment(value = "id", name = "id")String id) throws IOException {
		return connect("/%s/%s/%s", this.name, this.type, id).delete();
	}

	@Comment("批量保存，当包含`id`时，则使用该列值匹配保存")
	public Object bulkSave(@Comment(value = "保存内容", name = "list") List<Map<String, Object>> list) throws IOException {
		StringBuilder builder = new StringBuilder();
		list.forEach(item -> {
			Object id = item.get("id");
			if(id != null){
				builder.append(String.format("{ \"index\":{ \"_id\": \"%s\" } }\r\n", id));
			} else {
				builder.append("{ \"index\":{} }\r\n");
			}
			builder.append(JsonUtils.toJsonStringWithoutPretty(item));
			builder.append("\r\n");
		});
		return connect("/%s/%s/_bulk", this.name, this.type).post(builder.toString());
	}

	@Comment("根据`_id`修改")
	public Object update(@Comment(value = "_id", name = "_id")String _id, @Comment(value = "修改项", name = "data")Object data) throws IOException {
		return connect("/%s/%s/%s", this.name, this.type, _id).post(Collections.singletonMap("doc", data));
	}

	@Comment("搜索")
	public Object search(@Comment(value = "搜索`DSL`语句", name = "dsl")Map<String, Object> dsl) throws IOException {
		return connect("/%s/_search", this.name).post(dsl);
	}

	private ElasticSearchConnection connect(String format, Object... args) {
		return new ElasticSearchConnection(this.restClient, String.format(format, args));
	}
}
