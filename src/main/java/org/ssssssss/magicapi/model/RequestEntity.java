package org.ssssssss.magicapi.model;

import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.script.MagicScriptContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RequestEntity {

	private final ApiInfo apiInfo;

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final boolean requestedFromTest;

	private final Map<String, Object> parameters;

	private final Map<String, Object> pathVariables;

	private MagicScriptContext magicScriptContext;

	private Map<String, Object> headers;

	private final Long requestTime = System.currentTimeMillis();

	public RequestEntity(HttpServletRequest request, HttpServletResponse response, boolean requestedFromTest, Map<String, Object> parameters, Map<String, Object> pathVariables) {
		this.request = request;
		this.response = response;
		this.requestedFromTest = requestedFromTest;
		this.parameters = parameters;
		this.pathVariables = pathVariables;
		this.apiInfo = MappingHandlerMapping.getMappingApiInfo(request);
	}

	public ApiInfo getApiInfo() {
		return apiInfo;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public boolean isRequestedFromTest() {
		return requestedFromTest;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public Map<String, Object> getPathVariables() {
		return pathVariables;
	}

	public Long getRequestTime() {
		return requestTime;
	}

	public MagicScriptContext getMagicScriptContext() {
		return magicScriptContext;
	}

	public void setMagicScriptContext(MagicScriptContext magicScriptContext) {
		this.magicScriptContext = magicScriptContext;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}
}
