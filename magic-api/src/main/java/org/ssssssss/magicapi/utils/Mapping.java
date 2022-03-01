package org.ssssssss.magicapi.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Mapping {

	private final AbstractHandlerMethodMapping<RequestMappingInfo> methodMapping;

	private final String base;

	private final RequestMappingInfo.BuilderConfiguration config;

	private static final boolean HAS_GET_PATTERN_PARSER = JavaReflection.getMethod(RequestMappingHandlerMapping.class, "getPatternParser") != null;

	private Mapping(AbstractHandlerMethodMapping<RequestMappingInfo> methodMapping, RequestMappingInfo.BuilderConfiguration config, String base) {
		this.methodMapping = methodMapping;
		this.config = config;
		this.base = StringUtils.defaultIfBlank(base, "");
	}

	public static Mapping create(RequestMappingHandlerMapping mapping) {
		return create(mapping, null);
	}

	public static Mapping create(RequestMappingHandlerMapping mapping, String base) {
		if (HAS_GET_PATTERN_PARSER) {
			RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();
			config.setTrailingSlashMatch(mapping.useTrailingSlashMatch());
			config.setContentNegotiationManager(mapping.getContentNegotiationManager());
			if (mapping.getPatternParser() != null) {
				config.setPatternParser(mapping.getPatternParser());
			} else {
				config.setPathMatcher(mapping.getPathMatcher());
			}
			return new Mapping(mapping, config, base);
		}
		return new Mapping(mapping, null, base);
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

	public RequestMappingInfo register(String requestMethod, String path, Object handler, Method method) {
		RequestMappingInfo info = paths(path).methods(RequestMethod.valueOf(requestMethod.toUpperCase())).build();
		register(info, handler, method);
		return info;
	}

	public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
		return this.methodMapping.getHandlerMethods();
	}

	public Mapping unregister(RequestMappingInfo info) {
		this.methodMapping.unregisterMapping(info);
		return this;
	}

	public Mapping registerController(Object target) {
		Method[] methods = target.getClass().getDeclaredMethods();
		for (Method method : methods) {
			RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
			if (requestMapping != null) {
				String[] paths = Stream.of(requestMapping.value()).map(value -> PathUtils.replaceSlash(base + value)).toArray(String[]::new);
				this.register(paths(paths).build(), target, method);
			}
		}
		return this;
	}
}
