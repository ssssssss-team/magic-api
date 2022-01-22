package org.ssssssss.magicapi.elasticsearch;

import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Map;

public class ElasticSearchConnection extends ElasticSearchRest {

	public ElasticSearchConnection(RestClient restClient, String endpoint) {
		super(restClient);
		super.endpoint(endpoint);
	}

	public ElasticSearchConnection parameter(String key, String value) {
		if (value != null) {
			parameters.put(key, value);
		}
		return this;
	}

	public ElasticSearchConnection parameters(Map<String, String> params) {
		if (params != null) {
			parameters.putAll(params);
		}
		return this;
	}

	public Object put(Object data) throws IOException {
		return processResponse(json(data).doPut());
	}

	public Object delete() throws IOException {
		return processResponse(doDelete());
	}

	public Object delete(Object data) throws IOException {
		return processResponse(json(data).doDelete());
	}

	public Object post(Object data) throws IOException {
		return processResponse(json(data).doPost());
	}

	public Object get() throws IOException {
		return processResponse(doGet());
	}

}
