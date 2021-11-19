package org.ssssssss.magicapi.utils;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 接口映射封装，兼容不同版本Spring Boot 处理
 *
 * @author mxd
 */
public class Mapping {

	private final AbstractHandlerMethodMapping<RequestMappingInfo> methodMapping;

	private final RequestMappingInfo.BuilderConfiguration config;

	private static final boolean HAS_GET_PATTERN_PARSER = JavaReflection.getMethod(RequestMappingHandlerMapping.class, "getPatternParser") != null;

	private Mapping(AbstractHandlerMethodMapping<RequestMappingInfo> methodMapping, RequestMappingInfo.BuilderConfiguration config) {
		this.methodMapping = methodMapping;
		this.config = config;
	}

	public static Mapping create(RequestMappingHandlerMapping mapping) {
		if(HAS_GET_PATTERN_PARSER){
			RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();
			config.setTrailingSlashMatch(mapping.useTrailingSlashMatch());
			config.setContentNegotiationManager(mapping.getContentNegotiationManager());
			if (mapping.getPatternParser() != null) {
				config.setPatternParser(mapping.getPatternParser());
			} else {
				config.setPathMatcher(mapping.getPathMatcher());
			}
			return new Mapping(mapping, config);
		}
		return new Mapping(mapping, null);
	}

	public RequestMappingInfo.Builder paths(String ... paths){
		RequestMappingInfo.Builder builder = RequestMappingInfo.paths(paths);
		if(this.config != null){
			return builder.options(this.config);
		}
		return builder;
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
