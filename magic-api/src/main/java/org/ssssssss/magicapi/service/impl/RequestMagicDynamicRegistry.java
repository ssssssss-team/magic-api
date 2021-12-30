package org.ssssssss.magicapi.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.ssssssss.magicapi.controller.RequestHandler;
import org.ssssssss.magicapi.event.FileEvent;
import org.ssssssss.magicapi.event.GroupEvent;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.utils.Mapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class RequestMagicDynamicRegistry extends AbstractMagicDynamicRegistry<ApiInfo> {

	private final Mapping mapping;

	private Object handler;

	private final Method method = RequestHandler.class.getDeclaredMethod("invoke", HttpServletRequest.class, HttpServletResponse.class, Map.class, Map.class, Map.class);

	private static final Logger logger = LoggerFactory.getLogger(RequestMagicDynamicRegistry.class);

	public RequestMagicDynamicRegistry(MagicResourceStorage<ApiInfo> magicResourceStorage, Mapping mapping) throws NoSuchMethodException {
		super(magicResourceStorage);
		this.mapping = mapping;
	}

	public void setHandler(Object handler) {
		this.handler = handler;
	}

	@EventListener(condition = "#event.type == 'api'")
	public void onFileEvent(FileEvent event) {
		processEvent(event);
	}

	@EventListener(condition = "#event.type == 'api'")
	public void onGroupEvent(GroupEvent event) {
		processEvent(event);
	}

	public ApiInfo getApiInfoFromRequest(HttpServletRequest request) {
		String mappingKey = Objects.toString(request.getMethod(), "GET").toUpperCase() + ":" + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		return getMapping(mappingKey);
	}

	@Override
	public boolean register(MappingNode<ApiInfo> mappingNode) {
		String mappingKey = mappingNode.getMappingKey();
		logger.debug("注册接口[{}({})]", mappingNode.getEntity().getName(), mappingKey);
		int index = mappingKey.indexOf(":");
		String requestMethod = mappingKey.substring(0, index);
		String path = mappingKey.substring(index + 1);
		mappingNode.setMappingData(mapping.register(requestMethod, path, handler, method));
		return true;
	}

	@Override
	protected void unregister(MappingNode<ApiInfo> mappingNode) {
		logger.debug("取消注册接口[{}({})]", mappingNode.getEntity().getName(), mappingNode.getMappingKey());
		mapping.unregister((RequestMappingInfo) mappingNode.getMappingData());
	}

}
