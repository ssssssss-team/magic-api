package org.ssssssss.magicapi.model;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.ObjectConvertExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static org.ssssssss.magicapi.model.Constants.*;

/**
 * 请求信息
 *
 * @author mxd
 */
public class RequestEntity {

	private final Long requestTime = System.currentTimeMillis();
	private final String requestId = UUID.randomUUID().toString().replace("-", "");
	private ApiInfo apiInfo;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private boolean requestedFromTest;
	private Map<String, Object> parameters;
	private Map<String, Object> pathVariables;
	private MagicScriptContext magicScriptContext;
	private Object requestBody;

	private Map<String, Object> headers;

	private RequestEntity() {

	}

	public static RequestEntity create() {
		return new RequestEntity();
	}

	public ApiInfo getApiInfo() {
		return apiInfo;
	}

	public RequestEntity info(ApiInfo apiInfo) {
		this.apiInfo = apiInfo;
		return this;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public RequestEntity request(HttpServletRequest request) {
		this.request = request;
		return this;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public RequestEntity response(HttpServletResponse response) {
		this.response = response;
		return this;
	}

	public boolean isRequestedFromTest() {
		return requestedFromTest;
	}

	public RequestEntity requestedFromTest(boolean requestedFromTest) {
		this.requestedFromTest = requestedFromTest;
		return this;
	}

	public boolean isRequestedFromDebug() {
		return requestedFromTest && !getRequestedBreakpoints().isEmpty();
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public RequestEntity parameters(Map<String, Object> parameters) {
		this.parameters = parameters;
		return this;
	}

	public Map<String, Object> getPathVariables() {
		return pathVariables;
	}

	public RequestEntity pathVariables(Map<String, Object> pathVariables) {
		this.pathVariables = pathVariables;
		return this;
	}

	public Long getRequestTime() {
		return requestTime;
	}

	public MagicScriptContext getMagicScriptContext() {
		return magicScriptContext;
	}

	public RequestEntity setMagicScriptContext(MagicScriptContext magicScriptContext) {
		this.magicScriptContext = magicScriptContext;
		return this;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public RequestEntity setHeaders(Map<String, Object> headers) {
		this.headers = headers;
		return this;
	}

	public String getRequestId() {
		return requestId;
	}

	/**
	 * 获取 RequestBody
	 */
	public Object getRequestBody() {
		return this.requestBody;
	}

	public RequestEntity setRequestBody(Object requestBody) {
		this.requestBody = requestBody;
		return this;
	}

	/**
	 * 获取测试scriptId
	 */
	public String getRequestedScriptId() {
		return request.getHeader(HEADER_REQUEST_SCRIPT_ID);
	}

	/**
	 * 获取测试sessionId
	 */
	public String getRequestedSessionId() {
		return request.getHeader(HEADER_REQUEST_SESSION_ID);
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
