package org.ssssssss.magicapi.context;

import org.ssssssss.magicapi.model.RequestEntity;

import javax.servlet.http.HttpServletRequest;

public class RequestContext {

	private static final ThreadLocal<RequestEntity> REQUEST_ENTITY_THREAD_LOCAL = new InheritableThreadLocal<>();

	public static HttpServletRequest getHttpServletRequest() {
		RequestEntity requestEntity = REQUEST_ENTITY_THREAD_LOCAL.get();
		return requestEntity == null ? null : requestEntity.getRequest();
	}

	public static RequestEntity getRequestEntity() {
		return REQUEST_ENTITY_THREAD_LOCAL.get();
	}

	public static void setRequestEntity(RequestEntity requestEntity) {
		REQUEST_ENTITY_THREAD_LOCAL.set(requestEntity);
	}

	public static void remove() {
		REQUEST_ENTITY_THREAD_LOCAL.remove();
	}

}
