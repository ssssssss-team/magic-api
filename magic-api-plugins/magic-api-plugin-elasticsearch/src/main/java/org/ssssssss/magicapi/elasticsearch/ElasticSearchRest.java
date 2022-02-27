package org.ssssssss.magicapi.elasticsearch;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ElasticSearchRest {

	private final RestClient restClient;

	private String method;

	private String endpoint = "/";

	private HttpEntity entity;

	protected final Map<String, String> parameters = new HashMap<>();

	public ElasticSearchRest(RestClient restClient) {
		this.restClient = restClient;
	}

	ElasticSearchRest endpoint(String endpoint){
		this.endpoint = endpoint;
		return this;
	}

	Response doGet() throws IOException {
		this.method = "GET";
		return execute();
	}

	Response doPost() throws IOException {
		this.method = "POST";
		return execute();
	}

	Response doDelete() throws IOException {
		this.method = "DELETE";
		return execute();
	}

	Response doPut() throws IOException {
		this.method = "PUT";
		return execute();
	}

	ElasticSearchRest json(Object data){
		if(data == null){
			return this;
		}
		String json = null;
		if(data instanceof CharSequence){
			json = data.toString();
		} else {
			json = JsonUtils.toJsonString(data);
		}
		if(json != null){
			this.entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
		}
		return this;
	}

	private Response execute() throws IOException {
		Request request = new Request(method, this.endpoint);
		request.addParameters(parameters);
		request.setEntity(entity);
		return this.restClient.performRequest(request);
	}

	Object processResponse(Response response) throws IOException {
		int code = response.getStatusLine().getStatusCode();
		if (code >= 200 && code < 300) {    // 2xx
			HttpEntity entity = response.getEntity();
			String resp = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			ContentType contentType = ContentType.get(entity);
			if (Objects.equals(ContentType.APPLICATION_JSON.getMimeType(), contentType.getMimeType())) {
				return JsonUtils.readValue(resp, Object.class);
			}
		}
		return response;
	}
}
