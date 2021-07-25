package org.ssssssss.magicapi.model;

import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.ObjectConvertExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static org.ssssssss.magicapi.model.Constants.HEADER_REQUEST_BREAKPOINTS;
import static org.ssssssss.magicapi.model.Constants.HEADER_REQUEST_SESSION;

public class RequestEntity {

	private ApiInfo apiInfo;

	private HttpServletRequest request;

	private HttpServletResponse response;

	private boolean requestedFromTest;

	private Map<String, Object> parameters;

	private Map<String, Object> pathVariables;

	private final Long requestTime = System.currentTimeMillis();

	private final String requestId = UUID.randomUUID().toString().replace("-", "");

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

	public void setApiInfo(ApiInfo apiInfo) {
		this.apiInfo = apiInfo;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public boolean isRequestedFromTest() {
		return requestedFromTest;
	}

	public boolean isRequestedFromDebug(){
		return requestedFromTest && !getRequestedBreakpoints().isEmpty();
	}

	public void setRequestedFromTest(boolean requestedFromTest) {
		this.requestedFromTest = requestedFromTest;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public Map<String, Object> getPathVariables() {
		return pathVariables;
	}

	public void setPathVariables(Map<String, Object> pathVariables) {
		this.pathVariables = pathVariables;
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

	public String getRequestId() {
		return requestId;
	}

	/**
	 * 获取测试sessionId
	 */
	public String getRequestedSessionId() {
		return request.getHeader(HEADER_REQUEST_SESSION);
	}

	/**
	 * 获得断点
	 */
	public List<Integer> getRequestedBreakpoints() {
		String breakpoints = request.getHeader(HEADER_REQUEST_BREAKPOINTS);
		if (breakpoints != null) {
			return Arrays.stream(breakpoints.split(","))
					.map(val -> ObjectConvertExtension.asInt(val, -1))
					.filter(it -> it > 0)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
