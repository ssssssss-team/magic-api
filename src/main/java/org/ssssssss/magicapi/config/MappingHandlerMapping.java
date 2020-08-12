package org.ssssssss.magicapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.provider.ApiServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	private Method method = RequestHandler.class.getDeclaredMethod("invoke", HttpServletRequest.class, HttpServletResponse.class, Map.class, Map.class);

	/**
	 * 接口信息读取
	 */
	private ApiServiceProvider magicApiService;
	/**
	 * 统一接口前缀
	 */
	private String prefix;

	/**
	 * 是否覆盖应用接口
	 */
	private boolean allowOverride = false;

	/**
	 * 缓存已映射的接口信息
	 */
	private List<ApiInfo> apiInfos = Collections.synchronizedList(new ArrayList<>());

	public MappingHandlerMapping() throws NoSuchMethodException {
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setAllowOverride(boolean allowOverride) {
		this.allowOverride = allowOverride;
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
		//TODO 判断 requestMapping 是否已 “/” 开头
		if (!StringUtils.isEmpty(requestMapping) && !requestMapping.startsWith("/")) {
			requestMapping = "/" + requestMapping;
		}
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
		try {
			List<ApiInfo> list = magicApiService.listWithScript();
			if (list != null) {
				for (ApiInfo info : list) {
					try {
						// 当接口存在时，刷新缓存
						registerMapping(info, true);
					} catch (Exception e) {
						logger.error("接口:{}注册失败", info.getName(), e);
					}
				}
				List<String> resistedList = list.stream().map(ApiInfo::getId).collect(Collectors.toList());
				Iterator<ApiInfo> iterator = apiInfos.iterator();
				while (iterator.hasNext()) {
					String oldId = iterator.next().getId();
					// 当接口不存在时，取消注册接口
					if (!resistedList.contains(oldId)) {
						unregisterMapping(oldId, false);
						iterator.remove();
					}
				}
			}
		} catch (Exception e) {
			logger.info("注册接口映射失败", e);
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
	 * 判断是否已注册
	 */
	public boolean hasRegisterMapping(ApiInfo info) {
		if (info.getId() != null) {
			ApiInfo oldInfo = mappings.get(info.getId());
			if (oldInfo != null
					&& Objects.equals(oldInfo.getGroupPrefix(), info.getGroupPrefix())
					&& Objects.equals(oldInfo.getMethod(), info.getMethod())
					&& Objects.equals(oldInfo.getPath(), info.getPath())) {
				return false;
			}
		}
		if(mappings.containsKey(getMappingKey(info))){
			return true;
		}
		if(!allowOverride){
			Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
			if (handlerMethods != null) {
				return handlerMethods.get(getRequestMapping(info)) != null;
			}
		}
		return false;
	}

	/**
	 * 注册请求映射
	 */
	public void registerMapping(ApiInfo info, boolean delete) {
		// 先判断是否已注册，如果已注册，则先取消注册在进行注册。
		ApiInfo oldInfo = mappings.get(info.getId());
		String newMappingKey = getMappingKey(info);
		if (oldInfo != null) {
			String oldMappingKey = getMappingKey(oldInfo);
			// URL 路径一致时，刷新脚本内容即可
			if (Objects.equals(oldMappingKey, newMappingKey)) {
				if (!info.equals(oldInfo)) {
					mappings.put(info.getId(), info);
					mappings.put(newMappingKey, info);
					logger.info("刷新接口:{}", info.getName());
				}
				return;
			}
			// URL不一致时，需要取消注册旧接口，重新注册新接口
			logger.info("取消注册接口:{}", oldInfo.getName());
			// 取消注册
			mappings.remove(oldMappingKey);
			requestMappingHandlerMapping.unregisterMapping(getRequestMapping(oldInfo));
		}
		// 注册
		RequestMappingInfo requestMapping = getRequestMapping(info);
		// 如果与应用冲突
		if (requestMappingHandlerMapping.getHandlerMethods().containsKey(requestMapping)) {
			if (!allowOverride) {
				// 不允许覆盖
				logger.error("接口{}与应用冲突，无法注册", info.getName());
				return;
			}
			logger.warn("取消注册应用接口:{}", requestMapping);
			// 取消注册原接口
			requestMappingHandlerMapping.unregisterMapping(requestMapping);
		}
		logger.info("注册接口:{}", info.getName());
		mappings.put(info.getId(), info);
		mappings.put(newMappingKey, info);
		requestMappingHandlerMapping.registerMapping(requestMapping, handler, method);
		if (delete) {   // 刷新缓存
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
			if (delete) {   //刷新缓存
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
	 * @param groupPrefix 分组前缀
	 * @param path        请求路径
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

	public void enableRefresh(int interval) {
		if (interval > 0) {
			logger.info("启动自动刷新magic-api");
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::registerAllMapping, interval, interval, TimeUnit.SECONDS);
		}
	}
}
