package org.ssssssss.magicapi.model;

import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.script.MagicScriptContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
public class RequestEntity {

	private  ApiInfo apiInfo;

	private  HttpServletRequest request;

	private  HttpServletResponse response;

	private  boolean requestedFromTest;

	private  Map<String, Object> parameters;

	private  Map<String, Object> pathVariables;
	private  Long requestTime = System.currentTimeMillis();
	private MagicScriptContext magicScriptContext;
	private Map<String, Object> headers;
	private RequestEntity() {

	}

	public RequestEntity(HttpServletRequest request, HttpServletResponse response, boolean requestedFromTest, Map<String, Object> parameters, Map<String, Object> pathVariables) {
		this.request = request;
		this.response = response;
		this.requestedFromTest = requestedFromTest;
		this.parameters = parameters;
		this.pathVariables = pathVariables;
		ApiInfo info = MappingHandlerMapping.getMappingApiInfo(request);
		this.apiInfo = info != null ? info.copy() : null;
	}

	public RequestEntity(ApiInfo apiInfo, HttpServletRequest request, HttpServletResponse response, boolean requestedFromTest, Map<String, Object> parameters, Map<String, Object> pathVariables) {
		this.apiInfo = apiInfo;
		this.request = request;
		this.response = response;
		this.requestedFromTest = requestedFromTest;
		this.parameters = parameters;
		this.pathVariables = pathVariables;
	}

	public RequestEntity(HttpServletRequest request, HttpServletResponse response, boolean requestedFromTest, Map<String, Object> parameters, Map<String, Object> pathVariables, MagicScriptContext magicScriptContext, Map<String, Object> headers) {
		ApiInfo info = MappingHandlerMapping.getMappingApiInfo(request);
		this.apiInfo = info != null ? info.copy() : null;
		this.request = request;
		this.response = response;
		this.requestedFromTest = requestedFromTest;
		this.parameters = parameters;
		this.pathVariables = pathVariables;
		this.magicScriptContext = magicScriptContext;
		this.headers = headers;
	}

	public static RequestEntity empty() {
		return new RequestEntity();
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

	public void setApiInfo(ApiInfo apiInfo) {
		this.apiInfo = apiInfo;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setRequestedFromTest(boolean requestedFromTest) {
		this.requestedFromTest = requestedFromTest;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public void setPathVariables(Map<String, Object> pathVariables) {
		this.pathVariables = pathVariables;
	}


}
