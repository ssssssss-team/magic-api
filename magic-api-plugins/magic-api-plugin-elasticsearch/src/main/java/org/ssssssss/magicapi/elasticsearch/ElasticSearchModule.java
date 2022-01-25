package org.ssssssss.magicapi.elasticsearch;

import org.elasticsearch.client.RestClient;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.annotation.Comment;

@MagicModule("elasticsearch")
public class ElasticSearchModule {

	private static final String DOC = "_doc";

	private final RestClient restClient;

	public ElasticSearchModule(RestClient restClient) {
		this.restClient = restClient;
	}

	@Comment(value = "ElasticSearch REST API")
	public ElasticSearchConnection rest(String url){
		return new ElasticSearchConnection(this.restClient, url);
	}

	public ElasticSearchIndex index(String indexName){
		return new ElasticSearchIndex(this.restClient, indexName, DOC);
	}
}
