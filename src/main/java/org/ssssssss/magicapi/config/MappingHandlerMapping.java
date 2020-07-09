package org.ssssssss.magicapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.provider.ApiServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MappingHandlerMapping {

	private static Map<String, ApiInfo> mappings = new ConcurrentHashMap<>();
	private static Logger logger = LoggerFactory.getLogger(MappingHandlerMapping.class);
	private RequestMappingHandlerMapping requestMappingHandlerMapping;
	private RequestHandler handler;
	private Method method = RequestHandler.class.getDeclaredMethod("invoke", HttpServletRequest.class, HttpServletResponse.class, Map.class, Map.class, Map.class);
	private ApiServiceProvider magicApiService;
	private String prefix;

	public MappingHandlerMapping() throws NoSuchMethodException {
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public static ApiInfo getMappingApiInfo(HttpServletRequest request) {
		NativeWebRequest webRequest = new ServletWebRequest(request);
		String requestMapping = (String) webRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		return getMappingApiInfo(buildMappingKey(request.getMethod(), requestMapping));
	}

	public static ApiInfo getMappingApiInfo(String key) {
		return mappings.get(key);
	}

	public static String buildMappingKey(String requestMethod, String requestMapping) {
		return requestMethod.toUpperCase() + ":" + requestMapping;
	}

	public void setRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		this.requestMappingHandlerMapping = requestMappingHandlerMapping;
	}

	public void setHandler(RequestHandler handler) {
		this.handler = handler;
	}

	public void setMagicApiService(ApiServiceProvider magicApiService) {
		this.magicApiService = magicApiService;
	}

	public void registerAllMapping() {
		List<ApiInfo> list = magicApiService.listWithScript();
		if (list != null) {
			for (ApiInfo info : list) {
				registerMapping(info);
			}
		}
	}

	public ApiInfo getApiInfo(String method, String requestMapping) {
		return mappings.get(buildMappingKey(method, requestMapping));
	}

	/**
	 * 注册请求映射
	 *
	 * @param info
	 */
	public void registerMapping(ApiInfo info) {
		if (mappings.containsKey(info.getId())) {
			ApiInfo oldInfo = mappings.get(info.getId());
			logger.info("取消注册接口:{}", oldInfo.getName());
			// 取消注册
			mappings.remove(getMappingKey(info));
			requestMappingHandlerMapping.unregisterMapping(getRequestMapping(oldInfo));
		}
		logger.info("注册接口:{}", info.getName());
		// 注册
		RequestMappingInfo requestMapping = getRequestMapping(info);
		mappings.put(info.getId(), info);
		mappings.put(getMappingKey(info), info);
		requestMappingHandlerMapping.registerMapping(requestMapping, handler, method);
	}

	/**
	 * 取消注册请求映射
	 *
	 * @param id
	 */
	public void unregisterMapping(String id) {
		ApiInfo info = mappings.remove(id);
		if (info != null) {
			logger.info("取消注册接口:{}", info.getName());
			mappings.remove(getMappingKey(info));
			requestMappingHandlerMapping.unregisterMapping(getRequestMapping(info));
		}
	}

	private String getMappingKey(ApiInfo info) {
		return buildMappingKey(info.getMethod(), getRequestPath(info.getPath()));
	}

	private String getRequestPath(String path) {
		if (prefix != null) {
			path = prefix + (path.startsWith("/") ? path.substring(1) : path);
		}
		return path;
	}

	private RequestMappingInfo getRequestMapping(ApiInfo info) {
		return RequestMappingInfo.paths(getRequestPath(info.getPath())).methods(RequestMethod.valueOf(info.getMethod().toUpperCase())).build();
	}

}
