package org.ssssssss.magicapi.context;

public class RequestContextHolder {

	private static final ThreadLocal<RequestContext> THREAD_LOCAL = new ThreadLocal<>();

	public static RequestContext get() {
		return THREAD_LOCAL.get();
	}

	public static void set(RequestContext requestContext) {
		THREAD_LOCAL.set(requestContext);
	}

	public static void remove() {
		THREAD_LOCAL.remove();
	}
}
