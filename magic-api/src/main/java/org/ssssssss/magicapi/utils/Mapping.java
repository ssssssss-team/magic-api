package org.ssssssss.magicapi.utils;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 接口映射封装，兼容不同版本Spring Boot 处理
 *
 * @author mxd
 */
public class Mapping {

	private final AbstractHandlerMethodMapping<RequestMappingInfo> methodMapping;

	private Mapping(AbstractHandlerMethodMapping<RequestMappingInfo> methodMapping) {
		this.methodMapping = methodMapping;
	}

	public static Mapping create(RequestMappingInfoHandlerMapping mapping) {
		return new Mapping(mapping);
	}

	public Mapping register(RequestMappingInfo requestMappingInfo, Object handler, Method method) {
		this.methodMapping.registerMapping(requestMappingInfo, handler, method);
		return this;
	}

	public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
		return this.methodMapping.getHandlerMethods();
	}

	public Mapping unregister(RequestMappingInfo requestMappingInfo) {
		this.methodMapping.unregisterMapping(requestMappingInfo);
		return this;
	}
}
