package org.ssssssss.magicapi.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContext {

	private static final ThreadLocal<RequestAttribute> REQUEST_ATTRIBUTE_THREAD_LOCAL = new InheritableThreadLocal<>();

	public static void setRequestAttribute(HttpServletRequest request, HttpServletResponse response) {
		REQUEST_ATTRIBUTE_THREAD_LOCAL.set(new RequestAttribute(request, response));
	}

	public static HttpServletRequest getHttpServletRequest() {
		RequestAttribute requestAttribute = REQUEST_ATTRIBUTE_THREAD_LOCAL.get();
		return requestAttribute == null ? null : requestAttribute.request;
	}

	public static HttpServletResponse getHttpServletResponse() {
		RequestAttribute requestAttribute = REQUEST_ATTRIBUTE_THREAD_LOCAL.get();
		return requestAttribute == null ? null : requestAttribute.response;
	}

	public static void remove() {
		REQUEST_ATTRIBUTE_THREAD_LOCAL.remove();
	}


	private static class RequestAttribute {

		private final HttpServletRequest request;

		private final HttpServletResponse response;

		public RequestAttribute(HttpServletRequest request, HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}
	}
}
