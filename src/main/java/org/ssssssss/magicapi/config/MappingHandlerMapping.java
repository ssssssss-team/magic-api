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
import org.ssssssss.magicapi.controller.RequestHandler;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.TreeNode;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.utils.PathUtils;

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
	private static Map<String, MappingNode> mappings = new ConcurrentHashMap<>();

	private static Logger logger = LoggerFactory.getLogger(MappingHandlerMapping.class);

	/**
	 * spring中的请求映射处理器
	 */
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	/**
	 * 请求处理器
	 */
	private Object handler;

	/**
	 * 请求到达时处理的方法
	 */
	private Method method = RequestHandler.class.getDeclaredMethod("invoke", HttpServletRequest.class, HttpServletResponse.class, Map.class, Map.class);

	/**
	 * 接口信息读取
	 */
	private ApiServiceProvider magicApiService;

	/**
	 * 分组信息读取
	 */
	private GroupServiceProvider groupServiceProvider;

	/**
	 * 统一接口前缀
	 */
	private String prefix;

	/**
	 * 接口分组
	 */
	private TreeNode<Group> groups;

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
	private static ApiInfo getMappingApiInfo(String key) {
		return mappings.get(key).getInfo();
	}

	/**
	 * 构建缓存map的key
	 *
	 * @param requestMethod  请求方法
	 * @param requestMapping 请求路径
	 */
	private static String buildMappingKey(String requestMethod, String requestMapping) {

		if (!StringUtils.isEmpty(requestMapping) && !requestMapping.startsWith("/")) {
			requestMapping = "/" + requestMapping;
		}
		return requestMethod.toUpperCase() + ":" + requestMapping;
	}

	public void setRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		this.requestMappingHandlerMapping = requestMappingHandlerMapping;
	}

	public void setHandler(Object handler) {
		this.handler = handler;
	}

	public void setMagicApiService(ApiServiceProvider magicApiService) {
		this.magicApiService = magicApiService;
	}

	public void setGroupServiceProvider(GroupServiceProvider groupServiceProvider) {
		this.groupServiceProvider = groupServiceProvider;
	}

	public List<ApiInfo> getApiInfos() {
		return apiInfos;
	}

	/**
	 * 加载所有分组
	 */
	public synchronized void loadGroup() {
		groups = groupServiceProvider.apiGroupList();
	}

	/**
	 * 注册请求
	 */
	public void registerAllMapping() {
		try {
			loadGroup();
			List<ApiInfo> list = magicApiService.listWithScript();
			if (list != null) {
				list = list.stream().filter(it -> groupServiceProvider.getFullPath(it.getGroupId()) != null).collect(Collectors.toList());
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
		MappingNode mappingNode = mappings.get(buildMappingKey(method, requestMapping));
		return mappingNode == null ? null : mappingNode.getInfo();
	}

	private boolean hasConflict(TreeNode<Group> group, String newPath) {
		// 获取要移动的接口
		List<ApiInfo> infos = apiInfos.stream().filter(info -> Objects.equals(info.getGroupId(), group.getNode().getId())).collect(Collectors.toList());
		String groupPath = Objects.toString(group.getNode().getPath(), "");
		// 判断是否有冲突
		for (ApiInfo info : infos) {
			String path = concatPath(newPath, groupPath + "/" + info.getPath());
			String mappingKey = buildMappingKey(info.getMethod(), path);
			if (mappings.containsKey(mappingKey)) {
				return true;
			}
			if (!allowOverride) {
				Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
				if (handlerMethods != null) {
					if (handlerMethods.get(getRequestMapping(info.getMethod(), path)) != null) {
						return true;
					}
				}
			}
		}
		for (TreeNode<Group> child : group.getChildren()) {
			if (hasConflict(child, newPath + "/" + groupPath)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检测是否允许修改
	 */
	public boolean checkGroup(Group group) {
		TreeNode<Group> oldTree = groups.findTreeNode((item) -> item.getId().equals(group.getId()));
		// 如果只改了名字，则不做任何操作
		if (Objects.equals(oldTree.getNode().getParentId(), group.getParentId()) &&
				Objects.equals(oldTree.getNode().getPath(), group.getPath())) {
			return true;
		}
		// 新的接口分组路径
		String newPath = groupServiceProvider.getFullPath(group.getParentId());
		// 检测冲突
		return !hasConflict(oldTree, newPath);
	}

	/**
	 * 删除分组
	 */
	public void deleteGroup(List<String> groupIds) {
		// 找到对应的所有接口
		List<ApiInfo> deleteInfos = apiInfos.stream().filter(info -> groupIds.contains(info.getGroupId())).collect(Collectors.toList());
		for (ApiInfo info : deleteInfos) {
			unregisterMapping(info.getId(), true);
		}
		// 全部删除
		apiInfos.removeAll(deleteInfos);
	}

	/**
	 * 修改分组
	 */
	public void updateGroup(Group group) {
		loadGroup();    // 重新加载分组
		TreeNode<Group> groupTreeNode = groups.findTreeNode((item) -> item.getId().equals(group.getId()));
		recurseUpdateGroup(groupTreeNode, true);
	}

	private void recurseUpdateGroup(TreeNode<Group> node, boolean updateGroupId) {
		apiInfos.stream().filter(info -> Objects.equals(info.getGroupId(), node.getNode().getId())).forEach(info -> {
			unregisterMapping(info.getId(), false);
			if (updateGroupId) {
				info.setGroupId(node.getNode().getId());
			}
			registerMapping(info, false);
		});
		for (TreeNode<Group> child : node.getChildren()) {
			recurseUpdateGroup(child, false);
		}
	}

	/**
	 * 判断是否已注册
	 */
	public boolean hasRegisterMapping(ApiInfo info) {
		if (info.getId() != null) {
			MappingNode mappingNode = mappings.get(info.getId());
			ApiInfo oldInfo = mappingNode == null ? null : mappingNode.getInfo();
			if (oldInfo != null
					&& Objects.equals(oldInfo.getGroupId(), info.getGroupId())
					&& Objects.equals(oldInfo.getMethod(), info.getMethod())
					&& Objects.equals(oldInfo.getPath(), info.getPath())) {
				return false;
			}
		}
		String mappingKey = getMappingKey(info);
		if (mappings.containsKey(mappingKey)) {
			if(mappings.get(mappingKey).getInfo().getId().equals(info.getId())){
				return false;
			}
			return true;
		}
		if (!allowOverride) {
			Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
			if (handlerMethods != null) {
				return handlerMethods.get(getRequestMapping(info)) != null;
			}
		}
		return false;
	}

	/**
	 * 接口移动
	 */
	public boolean move(String id, String groupId) {
		MappingNode mappingNode = mappings.get(id);
		if (mappingNode == null) {
			return false;
		}
		ApiInfo copy = mappingNode.getInfo().copy();
		copy.setGroupId(groupId);
		if (hasRegisterMapping(copy)) {
			return false;
		}
		unregisterMapping(id,true);
		registerMapping(copy, true);
		return true;
	}

	/**
	 * 注册请求映射
	 */
	public void registerMapping(ApiInfo info, boolean delete) {
		// 先判断是否已注册，如果已注册，则先取消注册在进行注册。
		MappingNode mappingNode = mappings.get(info.getId());
		ApiInfo oldInfo = mappingNode == null ? null : mappingNode.getInfo();
		if(mappingNode == null){
			mappingNode = new MappingNode(info);
		}
		String newMappingKey = getMappingKey(info);
		mappingNode.setMappingKey(newMappingKey);
		if (oldInfo != null) {
			String oldMappingKey = getMappingKey(oldInfo);
			// URL 路径一致时，刷新脚本内容即可
			if (Objects.equals(oldMappingKey, newMappingKey)) {
				if (!info.equals(oldInfo)) {
					mappingNode.setInfo(info);
					mappings.get(newMappingKey).setInfo(info);
					if (delete) {
						refreshCache(info);
					}
					logger.info("刷新接口:{},{}", info.getName(), newMappingKey);
				}
				return;
			}
			// URL不一致时，需要取消注册旧接口，重新注册新接口
			logger.info("取消注册接口:{},{}", oldInfo.getName(), oldMappingKey);
			// 取消注册
			mappings.remove(oldMappingKey);
			requestMappingHandlerMapping.unregisterMapping(getRequestMapping(oldInfo));
		}
		// 注册
		RequestMappingInfo requestMapping = getRequestMapping(info);
		mappingNode.setRequestMappingInfo(requestMapping);
		mappingNode.setInfo(info);
		// 如果与应用冲突
		if (!overrideApplicationMapping(requestMapping)) {
			logger.error("接口{},{}与应用冲突，无法注册", info.getName(), newMappingKey);
			return;
		}
		logger.info("注册接口:{},{}", info.getName(), newMappingKey);
		mappings.put(info.getId(), mappingNode);
		mappings.put(newMappingKey, mappingNode);
		registerMapping(requestMapping, handler, method);
		if (delete) {   // 刷新缓存
			refreshCache(info);
		}
	}

	private void refreshCache(ApiInfo info) {
		apiInfos.removeIf(i -> i.getId().equalsIgnoreCase(info.getId()));
		apiInfos.add(info);
	}

	private void registerMapping(RequestMappingInfo requestMapping, Object handler, Method method) {
		requestMappingHandlerMapping.registerMapping(requestMapping, handler, method);
	}

	/**
	 * 取消注册请求映射
	 */
	public void unregisterMapping(String id, boolean delete) {
		MappingNode mappingNode = mappings.remove(id);
		if (mappingNode != null) {
			ApiInfo info = mappingNode.getInfo();
			logger.info("取消注册接口:{}", info.getName());
			mappings.remove(mappingNode.getMappingKey());
			requestMappingHandlerMapping.unregisterMapping(mappingNode.getRequestMappingInfo());
			if (delete) {   //刷新缓存
				apiInfos.removeIf(i -> i.getId().equalsIgnoreCase(info.getId()));
			}
		}
	}

	/**
	 * 根据接口信息获取绑定map的key
	 */
	private String getMappingKey(ApiInfo info) {
		return buildMappingKey(info.getMethod(), getRequestPath(info.getGroupId(), info.getPath()));
	}

	/**
	 * 处理前缀
	 *
	 * @param groupId 分组ID
	 * @param path    请求路径
	 */
	public String getRequestPath(String groupId, String path) {
		return concatPath(groupServiceProvider.getFullPath(groupId), path);

	}

	private String concatPath(String groupPath, String path) {
		path = groupPath + "/" + path;
		if (prefix != null) {
			path = prefix + "/" + path;
		}
		return PathUtils.replaceSlash(path);
	}

	/**
	 * 覆盖应用接口
	 */
	private boolean overrideApplicationMapping(RequestMappingInfo requestMapping) {
		if (requestMappingHandlerMapping.getHandlerMethods().containsKey(requestMapping)) {
			if (!allowOverride) {
				// 不允许覆盖
				return false;
			}
			logger.warn("取消注册应用接口:{}", requestMapping);
			// 取消注册原接口
			requestMappingHandlerMapping.unregisterMapping(requestMapping);
		}
		return true;
	}

	/**
	 * 根据接口信息构建 RequestMappingInfo
	 */
	private RequestMappingInfo getRequestMapping(ApiInfo info) {
		return RequestMappingInfo.paths(getRequestPath(info.getGroupId(), info.getPath())).methods(RequestMethod.valueOf(info.getMethod().toUpperCase())).build();
	}

	/**
	 * 根据接口信息构建 RequestMappingInfo
	 */
	private RequestMappingInfo getRequestMapping(String method, String path) {
		return RequestMappingInfo.paths(path).methods(RequestMethod.valueOf(method.toUpperCase())).build();
	}

	public void enableRefresh(int interval) {
		if (interval > 0) {
			logger.info("启动自动刷新magic-api");
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::registerAllMapping, interval, interval, TimeUnit.SECONDS);
		}
	}

	static class MappingNode{

		private ApiInfo info;

		private String mappingKey;

		private RequestMappingInfo requestMappingInfo;

		public MappingNode(ApiInfo info) {
			this.info = info;
		}

		public ApiInfo getInfo() {
			return info;
		}

		public void setInfo(ApiInfo info) {
			this.info = info;
		}

		public String getMappingKey() {
			return mappingKey;
		}

		public void setMappingKey(String mappingKey) {
			this.mappingKey = mappingKey;
		}

		public RequestMappingInfo getRequestMappingInfo() {
			return requestMappingInfo;
		}

		public void setRequestMappingInfo(RequestMappingInfo requestMappingInfo) {
			this.requestMappingInfo = requestMappingInfo;
		}
	}
}
