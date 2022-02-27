package org.ssssssss.magicapi.modules.http;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.annotation.Comment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * http 模块
 *
 * @author mxd
 * @since 1.1.0
 */
@MagicModule("http")
public class HttpModule {

	private final RestTemplate template;
	private final HttpHeaders httpHeaders = new HttpHeaders();
	private Class<?> responseType = Object.class;
	private final MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
	private final MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
	private final Map<String, ?> variables = new HashMap<>();
	private String url;
	private HttpMethod method = HttpMethod.GET;
	private HttpEntity<Object> entity = null;
	private Object requestBody;

	public HttpModule(RestTemplate template) {
		this.template = template;
	}

	public HttpModule(RestTemplate template, String url) {
		this.template = template;
		this.url = url;
	}

	@Comment("创建连接")
	public HttpModule connect(@Comment(name = "url", value = "目标URL") String url) {
		return new HttpModule(template, url);
	}

	@Comment("设置URL参数")
	public HttpModule param(@Comment(name = "key", value = "参数名") String key,
							@Comment(name = "values", value = "参数值") Object... values) {
		if (values != null) {
			for (Object value : values) {
				this.params.add(key, value);
			}
		}
		return this;
	}

	@Comment("批量设置URL参数")
	public HttpModule param(@Comment(name = "values", value = "参数值") Map<String, Object> values) {
		values.forEach((key, value) -> param(key, Objects.toString(value, "")));
		return this;
	}

	@Comment("设置form参数")
	public HttpModule data(@Comment(name = "key", value = "参数名") String key,
						   @Comment(name = "values", value = "参数值") Object... values) {
		if (values != null) {
			for (Object value : values) {
				this.data.add(key, value);
			}
		}
		return this;
	}

	@Comment("批量设置form参数")
	public HttpModule data(@Comment(name = "values", value = "参数值") Map<String, Object> values) {
		values.forEach((key, value) -> data(key, Objects.toString(value, "")));
		return this;
	}

	@Comment("设置header")
	public HttpModule header(@Comment(name = "key", value = "header名") String key,
							 @Comment(name = "value", value = "header值") String value) {
		httpHeaders.add(key, value);
		return this;
	}

	@Comment("批量设置header")
	public HttpModule header(@Comment(name = "values", value = "header值") Map<String, Object> values) {
		values.entrySet()
				.stream()
				.filter(it -> it.getValue() != null)
				.forEach(entry -> header(entry.getKey(), entry.getValue().toString()));
		return this;
	}

	@Comment("设置请求方法，默认GET")
	public HttpModule method(@Comment(name = "method", value = "请求方法") HttpMethod method) {
		this.method = method;
		return this;
	}

	@Comment("设置`RequestBody`")
	public HttpModule body(@Comment(name = "requestBody", value = "`RequestBody`") Object requestBody) {
		this.requestBody = requestBody;
		this.contentType(MediaType.APPLICATION_JSON);
		return this;
	}

	@Comment("自定义`HttpEntity`")
	public HttpModule entity(@Comment(name = "entity", value = "`HttpEntity`") HttpEntity<Object> entity) {
		this.entity = entity;
		return this;
	}

	@Comment("设置`ContentType`")
	public HttpModule contentType(@Comment(name = "contentType", value = "Content-Type值") String contentType) {
		return contentType(MediaType.parseMediaType(contentType));
	}

	@Comment("设置`ContentType`")
	public HttpModule contentType(@Comment(name = "mediaType", value = "Content-Type值") MediaType mediaType) {
		this.httpHeaders.setContentType(mediaType);
		return this;
	}

	@Comment("设置返回值为`byte[]`")
	public HttpModule expectBytes() {
		this.responseType = byte[].class;
		return this;
	}

	@Comment("发送`POST`请求")
	public ResponseEntity<?> post() {
		this.method(HttpMethod.POST);
		return this.execute();
	}

	@Comment("发送`GET`请求")
	public ResponseEntity<?> get() {
		this.method(HttpMethod.GET);
		return this.execute();
	}

	@Comment("发送`PUT`请求")
	public ResponseEntity<?> put() {
		this.method(HttpMethod.PUT);
		return this.execute();
	}

	@Comment("发送`DELETE`请求")
	public ResponseEntity<?> delete() {
		this.method(HttpMethod.DELETE);
		return this.execute();
	}

	@Comment("发送`HEAD`请求")
	public ResponseEntity<?> head() {
		this.method(HttpMethod.HEAD);
		return this.execute();
	}

	@Comment("发送`OPTIONS`请求")
	public ResponseEntity<?> options() {
		this.method(HttpMethod.OPTIONS);
		return this.execute();
	}

	@Comment("发送`TRACE`请求")
	public ResponseEntity<?> trace() {
		this.method(HttpMethod.TRACE);
		return this.execute();
	}

	@Comment("发送`PATCH`请求")
	public ResponseEntity<?> patch() {
		this.method(HttpMethod.PATCH);
		return this.execute();
	}

	@Comment("执行请求")
	public ResponseEntity<?> execute() {
		if (!this.params.isEmpty()) {
			String queryString = this.params.entrySet().stream()
					.map(it -> it.getValue().stream()
							.map(value -> it.getKey() + "=" + value)
							.collect(Collectors.joining("&"))
					).collect(Collectors.joining("&"));
			if (StringUtils.isNotBlank(queryString)) {
				this.url += (this.url.contains("?") ? "&" : "?") + queryString;
			}
		}
		if (!this.data.isEmpty()) {
			this.entity = new HttpEntity<>(this.data, this.httpHeaders);
		} else if (this.entity == null && this.requestBody != null) {
			this.entity = new HttpEntity<>(this.requestBody, this.httpHeaders);
		} else {
			this.entity = new HttpEntity<>(null, this.httpHeaders);
		}
		return template.exchange(url, this.method, entity, responseType, variables);
	}
}
