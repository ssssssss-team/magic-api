package org.ssssssss.magicapi.elasticsearch;

import org.elasticsearch.client.RestClient;
import org.ssssssss.magicapi.core.config.MagicModule;
import org.ssssssss.script.annotation.Comment;

public class ElasticSearchModule implements MagicModule {

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

	@Override
	public String getModuleName() {
		return "elasticsearch";
	}
}
