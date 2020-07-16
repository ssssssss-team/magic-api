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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求映射
 */
public class MappingHandlerMapping {

	/**
	 * 已缓存的映射信息
	 */
	private static Map<String, ApiInfo> mappings = new ConcurrentHashMap<>();

	private static Logger logger = LoggerFactory.getLogger(MappingHandlerMapping.class);

	/**
	 * spring中的请求映射处理器
	 */
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	/**
	 * 请求处理器
	 */
	private RequestHandler handler;

	/**
	 * 请求到达时处理的方法
	 */
	private Method method = RequestHandler.class.getDeclaredMethod("invoke", HttpServletRequest.class, HttpServletResponse.class, Map.class, Map.class, Map.class);

	/**
	 * 接口信息读取
	 */
	private ApiServiceProvider magicApiService;
	/**
	 * 统一接口前缀
	 */
	private String prefix;

	private List<ApiInfo> apiInfos = Collections.synchronizedList(new ArrayList<>());

	public MappingHandlerMapping() throws NoSuchMethodException {
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * 根据request获取对应的接口信息
	 */
	public static ApiInfo getMappingApiInfo(HttpServletRequest request) {
		NativeWebRequest webRequest = new ServletWebRequest(request);
		// 找到注册的路径
		String requestMapping = (String) webRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		// 根据请求方法和路径获取接口信息
		return getMappingApiInfo(buildMappingKey(request.getMethod(), requestMapping));
	}

	/**
	 * 根据绑定的key获取接口信息
	 */
	public static ApiInfo getMappingApiInfo(String key) {
		return mappings.get(key);
	}

	/**
	 * 构建缓存map的key
	 *
	 * @param requestMethod  请求方法
	 * @param requestMapping 请求路径
	 * @return
	 */
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

	public List<ApiInfo> getApiInfos() {
		return apiInfos;
	}

	/**
	 * 注册请求
	 */
	public void registerAllMapping() {
		List<ApiInfo> list = magicApiService.listWithScript();
		if (list != null) {
			apiInfos.addAll(list);
			for (ApiInfo info : list) {
				registerMapping(info, false);
			}
		}
	}

	/**
	 * 根据请求方法和路径获取接口信息
	 *
	 * @param method         请求方法
	 * @param requestMapping 请求路径
	 */
	public ApiInfo getApiInfo(String method, String requestMapping) {
		return mappings.get(buildMappingKey(method, requestMapping));
	}

	public void updateGroupPrefix(String oldGroupName, String newGroupName, String prefix) {
		for (ApiInfo info : apiInfos) {
			if (oldGroupName.equals(info.getGroupName())) {
				unregisterMapping(info.getId(), false);
				info.setGroupName(newGroupName);
				info.setGroupPrefix(prefix);
				registerMapping(info, false);
			}
		}
	}

	/**
	 * 注册请求映射
	 *
	 * @param info
	 */
	public void registerMapping(ApiInfo info, boolean delete) {
		// 先判断是否已注册，如果已注册，则先取消注册在进行注册。
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
		if (delete) {
			apiInfos.removeIf(i -> i.getId().equalsIgnoreCase(info.getId()));
			apiInfos.add(info);
		}
	}

	/**
	 * 取消注册请求映射
	 */
	public void unregisterMapping(String id, boolean delete) {
		ApiInfo info = mappings.remove(id);
		if (info != null) {
			logger.info("取消注册接口:{}", info.getName());
			mappings.remove(getMappingKey(info));
			requestMappingHandlerMapping.unregisterMapping(getRequestMapping(info));
			if (delete) {
				apiInfos.removeIf(i -> i.getId().equalsIgnoreCase(info.getId()));
			}
		}
	}

	/**
	 * 根据接口信息获取绑定map的key
	 */
	private String getMappingKey(ApiInfo info) {
		return buildMappingKey(info.getMethod(), getRequestPath(info.getGroupPrefix(), info.getPath()));
	}

	/**
	 * 处理前缀
	 *
	 * @param path 请求路径
	 */
	public String getRequestPath(String groupPrefix, String path) {
		groupPrefix = groupPrefix == null ? "" : groupPrefix;
		while (groupPrefix.endsWith("/")) {
			groupPrefix = groupPrefix.substring(0, groupPrefix.length() - 1);
		}
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		path = groupPrefix + "/" + path;
		if (prefix != null) {
			path = prefix + (path.startsWith("/") ? path.substring(1) : path);
		}
		return path;
	}

	/**
	 * 根据接口信息构建 RequestMappingInfo
	 */
	private RequestMappingInfo getRequestMapping(ApiInfo info) {
		return RequestMappingInfo.paths(getRequestPath(info.getGroupPrefix(), info.getPath())).methods(RequestMethod.valueOf(info.getMethod().toUpperCase())).build();
	}

}
