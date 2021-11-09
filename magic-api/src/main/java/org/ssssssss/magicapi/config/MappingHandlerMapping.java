package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
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
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.TreeNode;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.utils.Mapping;
import org.ssssssss.magicapi.utils.PathUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 请求映射
 *
 * @author mxd
 */
public class MappingHandlerMapping {

	/**
	 * 已缓存的映射信息
	 */
	private static final Map<String, MappingNode> MAPPINGS = new ConcurrentHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(MappingHandlerMapping.class);
	/**
	 * 接口分组
	 */
	private static TreeNode<Group> groups;
	/**
	 * 请求到达时处理的方法
	 */
	private final Method method = RequestHandler.class.getDeclaredMethod("invoke", HttpServletRequest.class, HttpServletResponse.class, Map.class, Map.class, Map.class);
	/**
	 * 统一接口前缀
	 */
	private final String prefix;
	/**
	 * 是否覆盖应用接口
	 */
	private final boolean allowOverride;
	/**
	 * 缓存已映射的接口信息
	 */
	private final List<ApiInfo> apiInfos = Collections.synchronizedList(new ArrayList<>());

	private Mapping mappingHelper;
	/**
	 * 请求处理器
	 */
	private Object handler;
	/**
	 * 接口信息读取
	 */
	private ApiServiceProvider magicApiService;
	/**
	 * 分组信息读取
	 */
	private GroupServiceProvider groupServiceProvider;

	public MappingHandlerMapping(String prefix, boolean allowOverride) throws NoSuchMethodException {
		this.prefix = prefix;
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
		return MAPPINGS.get(key).getInfo();
	}

	/**
	 * 构建缓存map的key
	 *
	 * @param requestMethod  请求方法
	 * @param requestMapping 请求路径
	 */
	private static String buildMappingKey(String requestMethod, String requestMapping) {
		if (StringUtils.isNotBlank(requestMapping) && !requestMapping.startsWith("/")) {
			requestMapping = "/" + requestMapping;
		}
		return Objects.toString(requestMethod, "GET").toUpperCase() + ":" + requestMapping;
	}

	public static Group findGroup(String groupId) {
		TreeNode<Group> node = groups.findTreeNode(it -> it.getId().equals(groupId));
		return node != null ? node.getNode() : null;
	}

	public static List<Group> findGroups(String groupId) {
		List<Group> groups = new ArrayList<>();
		Group group;
		while (!Constants.ROOT_ID.equals(groupId) && (group = MappingHandlerMapping.findGroup(groupId)) != null) {
			groups.add(group);
			groupId = group.getParentId();
		}
		return groups;
	}

	public TreeNode<Group> findGroupTree(String groupId) {
		return groups.findTreeNode(it -> it.getId().equals(groupId));
	}

	public void setRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		this.mappingHelper = Mapping.create(requestMappingHandlerMapping);
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
		groups = groupServiceProvider.apiGroupTree();
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
		MappingNode mappingNode = MAPPINGS.get(buildMappingKey(method, concatPath("", requestMapping)));
		return mappingNode == null ? null : mappingNode.getInfo();
	}

	private boolean hasConflict(TreeNode<Group> group, String newPath) {
		// 获取要移动的接口
		List<ApiInfo> infos = apiInfos.stream().filter(info -> Objects.equals(info.getGroupId(), group.getNode().getId())).collect(Collectors.toList());
		// 判断是否有冲突
		for (ApiInfo info : infos) {
			String path = concatPath(newPath, "/" + info.getPath());
			String mappingKey = buildMappingKey(info.getMethod(), path);
			MappingNode mappingNode = MAPPINGS.get(mappingKey);
			if (mappingNode != null) {
				if (mappingNode.getInfo().equals(info)) {
					continue;
				}
				return true;
			}
			if (!allowOverride) {
				Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.mappingHelper.getHandlerMethods();
				if (handlerMethods.get(getRequestMapping(info.getMethod(), path)) != null) {
					return true;
				}
			}
		}
		for (TreeNode<Group> child : group.getChildren()) {
			if (hasConflict(child, newPath + "/" + Objects.toString(child.getNode().getPath(), ""))) {
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
		// 如果没移动目录且没改路径，则只需要判断名字是否冲突
		boolean parentIdEquals = Objects.equals(oldTree.getNode().getParentId(), group.getParentId());
		boolean nameEquals = Objects.equals(oldTree.getNode().getName(), group.getName());
		if (parentIdEquals && Objects.equals(oldTree.getNode().getPath(), group.getPath())) {
			return nameEquals || !groupServiceProvider.exists(group);
		}
		// 检测名字是否冲突
		boolean requiredChecked = (!parentIdEquals || !nameEquals);
		if (requiredChecked && groupServiceProvider.exists(group)) {
			return false;
		}
		// 新的接口分组路径
		String newPath = groupServiceProvider.getFullPath(group.getParentId());
		// 检测冲突
		return !hasConflict(oldTree, newPath + "/" + Objects.toString(group.getPath(), ""));
	}

	public boolean hasRegister(Set<String> paths) {
		return paths.stream().anyMatch(MAPPINGS::containsKey);
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
		// 刷新分组缓存
		loadGroup();
	}

	/**
	 * 修改分组
	 */
	public boolean updateGroup(String groupId) {
		loadGroup();    // 重新加载分组
		TreeNode<Group> groupTreeNode = groups.findTreeNode((item) -> item.getId().equals(groupId));
		recurseUpdateGroup(groupTreeNode, true);
		return magicApiService.reload(groupId);
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
			MappingNode mappingNode = MAPPINGS.get(info.getId());
			ApiInfo oldInfo = mappingNode == null ? null : mappingNode.getInfo();
			if (oldInfo != null
					&& Objects.equals(oldInfo.getGroupId(), info.getGroupId())
					&& Objects.equals(oldInfo.getMethod(), info.getMethod())
					&& Objects.equals(oldInfo.getPath(), info.getPath())) {
				return false;
			}
		}
		String mappingKey = getMappingKey(info);
		if (MAPPINGS.containsKey(mappingKey)) {
			return !MAPPINGS.get(mappingKey).getInfo().getId().equals(info.getId());
		}
		if (!allowOverride) {
			Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.mappingHelper.getHandlerMethods();
			return handlerMethods.get(getRequestMapping(info)) != null;
		}
		return false;
	}

	/**
	 * 接口移动
	 */
	public boolean move(String id, String groupId) {
		MappingNode mappingNode = MAPPINGS.get(id);
		if (mappingNode == null) {
			return false;
		}
		ApiInfo copy = mappingNode.getInfo().copy();
		copy.setGroupId(groupId);
		if (hasRegisterMapping(copy)) {
			return false;
		}
		unregisterMapping(id, true);
		registerMapping(copy, true);
		return true;
	}

	/**
	 * 注册请求映射
	 */
	public void registerMapping(ApiInfo info, boolean delete) {
		if (info == null) {
			return;
		}
		// 先判断是否已注册，如果已注册，则先取消注册在进行注册。
		MappingNode mappingNode = MAPPINGS.get(info.getId());
		String newMappingKey = getMappingKey(info);
		if (mappingNode != null) {
			ApiInfo oldInfo = mappingNode.getInfo();
			String oldMappingKey = mappingNode.getMappingKey();
			// URL 路径一致时，刷新脚本内容即可
			if (Objects.equals(oldMappingKey, newMappingKey)) {
				if (!info.equals(oldInfo)) {
					mappingNode.setInfo(info);
					MAPPINGS.get(newMappingKey).setInfo(info);
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
			MAPPINGS.remove(oldMappingKey);
			mappingHelper.unregister(getRequestMapping(oldInfo));
		}
		mappingNode = new MappingNode(info);
		mappingNode.setMappingKey(newMappingKey);
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
		MAPPINGS.put(info.getId(), mappingNode);
		MAPPINGS.put(newMappingKey, mappingNode);
		registerMapping(requestMapping, handler, method);
		if (delete) {
			// 刷新缓存
			refreshCache(info);
		}
	}

	private void refreshCache(ApiInfo info) {
		apiInfos.removeIf(i -> i.getId().equalsIgnoreCase(info.getId()));
		apiInfos.add(info);
	}

	private void registerMapping(RequestMappingInfo requestMapping, Object handler, Method method) {
		mappingHelper.register(requestMapping, handler, method);
	}

	/**
	 * 取消注册请求映射
	 */
	public void unregisterMapping(String id, boolean delete) {
		MappingNode mappingNode = MAPPINGS.remove(id);
		if (mappingNode != null) {
			ApiInfo info = mappingNode.getInfo();
			logger.info("取消注册接口:{}", info.getName());
			MAPPINGS.remove(mappingNode.getMappingKey());
			mappingHelper.unregister(mappingNode.getRequestMappingInfo());
			if (delete) {
				// 刷新缓存
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

	public void registerController(Object target, String base) {
		Method[] methods = target.getClass().getDeclaredMethods();
		for (Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if (requestMapping != null) {
				String[] paths = Stream.of(requestMapping.value()).map(value -> base + value).toArray(String[]::new);
				mappingHelper.register(RequestMappingInfo.paths(paths).build(), target, method);
			}
		}
	}

	private String concatPath(String groupPath, String path) {
		path = groupPath + "/" + path;
		if (prefix != null) {
			path = prefix + "/" + path;
		}
		path = PathUtils.replaceSlash(path);
		if (path.startsWith("/")) {
			return path.substring(1);
		}
		return path;
	}

	/**
	 * 覆盖应用接口
	 */
	private boolean overrideApplicationMapping(RequestMappingInfo requestMapping) {
		if (mappingHelper.getHandlerMethods().containsKey(requestMapping)) {
			if (!allowOverride) {
				// 不允许覆盖
				return false;
			}
			logger.warn("取消注册应用接口:{}", requestMapping);
			// 取消注册原接口
			mappingHelper.unregister(requestMapping);
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

	static class MappingNode {

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
