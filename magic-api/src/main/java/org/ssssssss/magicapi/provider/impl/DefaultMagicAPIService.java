package org.ssssssss.magicapi.provider.impl;

import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.adapter.resource.ZipResource;
import org.ssssssss.magicapi.config.MagicDynamicDataSource;
import org.ssssssss.magicapi.config.MagicFunctionManager;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.controller.MagicDataSourceController;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.exception.MagicServiceException;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.provider.*;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.PathUtils;
import org.ssssssss.magicapi.utils.SignUtils;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultMagicAPIService implements MagicAPIService, JsonCodeConstants {

	private final static Logger logger = LoggerFactory.getLogger(DefaultMagicAPIService.class);
	private static final ClassLoader classLoader = MagicDataSourceController.class.getClassLoader();
	// copy from DataSourceBuilder
	private static final String[] DATA_SOURCE_TYPE_NAMES = new String[]{
			"com.zaxxer.hikari.HikariDataSource",
			"org.apache.tomcat.jdbc.pool.DataSource",
			"org.apache.commons.dbcp2.BasicDataSource"};
	private final MappingHandlerMapping mappingHandlerMapping;
	private final boolean throwException;
	private final ResultProvider resultProvider;
	private final ApiServiceProvider apiServiceProvider;
	private final FunctionServiceProvider functionServiceProvider;
	private final GroupServiceProvider groupServiceProvider;
	private final MagicDynamicDataSource magicDynamicDataSource;
	private final MagicFunctionManager magicFunctionManager;
	private final MagicNotifyService magicNotifyService;
	private final String instanceId;
	private final Resource workspace;
	private final Resource datasourceResource;

	public DefaultMagicAPIService(MappingHandlerMapping mappingHandlerMapping,
								  ApiServiceProvider apiServiceProvider,
								  FunctionServiceProvider functionServiceProvider,
								  GroupServiceProvider groupServiceProvider,
								  ResultProvider resultProvider,
								  MagicDynamicDataSource magicDynamicDataSource,
								  MagicFunctionManager magicFunctionManager,
								  MagicNotifyService magicNotifyService,
								  String instanceId,
								  Resource workspace,
								  boolean throwException) {
		this.mappingHandlerMapping = mappingHandlerMapping;
		this.apiServiceProvider = apiServiceProvider;
		this.functionServiceProvider = functionServiceProvider;
		this.groupServiceProvider = groupServiceProvider;
		this.resultProvider = resultProvider;
		this.magicDynamicDataSource = magicDynamicDataSource;
		this.magicFunctionManager = magicFunctionManager;
		this.magicNotifyService = magicNotifyService;
		this.workspace = workspace;
		this.throwException = throwException;
		this.instanceId = StringUtils.defaultIfBlank(instanceId, UUID.randomUUID().toString());
		this.datasourceResource = workspace.getDirectory(Constants.PATH_DATASOURCE);
		if (!this.datasourceResource.exists()) {
			this.datasourceResource.mkdir();
		}
		MagicResourceLoader.addFunctionLoader((name) -> {
			int index = name.indexOf(":");
			if (index > -1) {
				String method = name.substring(0, index);
				String path = name.substring(index + 1);
				ApiInfo info = this.mappingHandlerMapping.getApiInfo(method, path);
				if (info != null) {
					return new Expression(new Span("unknown source")) {
						@Override
						public Object evaluate(MagicScriptContext context, Scope scope) {
							return execute(info, scope.getVariables());
						}
					};
				}
			}
			return null;
		});
	}

	private Object execute(ApiInfo info, Map<String, Object> context) {

		// 获取原上下文
		final MagicScriptContext magicScriptContext = MagicScriptContext.get();

		MagicScriptContext scriptContext = new MagicScriptContext();
		scriptContext.putMapIntoContext(context);
		SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
		simpleScriptContext.setAttribute(MagicScript.CONTEXT_ROOT, scriptContext, ScriptContext.ENGINE_SCOPE);
		final Object evalVal;
		try {
			evalVal = ((MagicScript) ScriptManager.compile("MagicScript", info.getScript())).eval(simpleScriptContext);
		} finally {
			// 恢复原接口上下文，修复当前调完其它接口后原接口上下文丢失的问题
			MagicScriptContext.set(magicScriptContext);
		}
		return evalVal;
	}

	@Override
	public Object execute(String method, String path, Map<String, Object> context) {
		ApiInfo info = this.mappingHandlerMapping.getApiInfo(method, path);
		if (info == null) {
			throw new MagicServiceException(String.format("找不到对应接口 [%s:%s]", method, path));
		}
		return execute(info, context);
	}

	@Override
	public Object call(String method, String path, Map<String, Object> context) {
		RequestEntity requestEntity = RequestEntity.empty();
		try {
			return resultProvider.buildResult(requestEntity, execute(method, path, context));
		} catch (MagicServiceException e) {
			return null;    //找不到对应接口
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			return resultProvider.buildResult(requestEntity, root);
		}
	}

	@Override
	public String saveApi(ApiInfo info) {
		// 非空验证
		notBlank(info.getMethod(), REQUEST_METHOD_REQUIRED);
		notBlank(info.getPath(), REQUEST_PATH_REQUIRED);
		notBlank(info.getName(), API_NAME_REQUIRED);
		notBlank(info.getScript(), SCRIPT_REQUIRED);
		// 验证名字
		isTrue(IoUtils.validateFileName(info.getName()), NAME_INVALID);
		// 验证路径是否有冲突
		isTrue(!mappingHandlerMapping.hasRegisterMapping(info), REQUEST_PATH_CONFLICT);
		int action = Constants.NOTIFY_ACTION_UPDATE;
		if (StringUtils.isBlank(info.getId())) {
			// 先判断接口是否存在
			isTrue(!apiServiceProvider.exists(info), API_ALREADY_EXISTS.format(info.getMethod(), info.getPath()));
			isTrue(apiServiceProvider.insert(info), API_SAVE_FAILURE);
			action = Constants.NOTIFY_ACTION_ADD;
		} else {
			// 先判断接口是否存在
			isTrue(!apiServiceProvider.existsWithoutId(info), API_ALREADY_EXISTS.format(info.getMethod(), info.getPath()));
			Optional<ApiInfo> optional = mappingHandlerMapping.getApiInfos().stream()
					.filter(it -> it.getId().equals(info.getId()))
					.findFirst();
			if (optional.isPresent() && !optional.get().equals(info)) {
				isTrue(apiServiceProvider.update(info), API_SAVE_FAILURE);
				apiServiceProvider.backup(info);
			}
		}
		// 注册接口
		mappingHandlerMapping.registerMapping(info, true);
		// 通知更新接口
		magicNotifyService.sendNotify(new MagicNotify(instanceId, info.getId(), action, Constants.NOTIFY_ACTION_API));
		return info.getId();
	}

	@Override
	public ApiInfo getApiInfo(String id) {
		return apiServiceProvider.get(id);
	}

	@Override
	public List<ApiInfo> apiList() {
		return apiServiceProvider.list();
	}

	@Override
	public boolean deleteApi(String id) {
		if (deleteApiWithoutNotify(id)) {
			// 通知删除接口
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, Constants.NOTIFY_ACTION_DELETE, Constants.NOTIFY_ACTION_API));
			return true;
		}
		return false;
	}

	private boolean deleteApiWithoutNotify(String id) {
		if (apiServiceProvider.delete(id)) {    //删除成功时在取消注册
			mappingHandlerMapping.unregisterMapping(id, true);
			return true;
		}
		return false;
	}

	@Override
	public boolean moveApi(String id, String groupId) {
		// 验证分组是否存在
		isTrue(groupServiceProvider.containsApiGroup(groupId), GROUP_NOT_FOUND);
		// 验证移动后名字是否有冲突
		isTrue(apiServiceProvider.allowMove(id, groupId), NAME_CONFLICT);
		// 验证路径是否有冲突
		isTrue(mappingHandlerMapping.move(id, groupId), REQUEST_PATH_CONFLICT);
		if (apiServiceProvider.move(id, groupId)) {
			// 通知更新接口
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, Constants.NOTIFY_ACTION_UPDATE, Constants.NOTIFY_ACTION_API));
			return true;
		}
		return false;
	}

	@Override
	public String saveFunction(FunctionInfo functionInfo) {
		notBlank(functionInfo.getName(), FUNCTION_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(functionInfo.getName()), NAME_INVALID);
		notBlank(functionInfo.getPath(), FUNCTION_PATH_REQUIRED);
		notBlank(functionInfo.getScript(), SCRIPT_REQUIRED);
		isTrue(!magicFunctionManager.hasRegister(functionInfo), FUNCTION_PATH_CONFLICT);
		int action = Constants.NOTIFY_ACTION_UPDATE;
		if (StringUtils.isBlank(functionInfo.getId())) {
			isTrue(!functionServiceProvider.exists(functionInfo), FUNCTION_ALREADY_EXISTS.format(functionInfo.getPath()));
			isTrue(functionServiceProvider.insert(functionInfo), FUNCTION_SAVE_FAILURE);
			action = Constants.NOTIFY_ACTION_ADD;
		} else {
			isTrue(!functionServiceProvider.existsWithoutId(functionInfo), FUNCTION_ALREADY_EXISTS.format(functionInfo.getPath()));
			isTrue(functionServiceProvider.update(functionInfo), FUNCTION_SAVE_FAILURE);
			functionServiceProvider.backup(functionInfo);
		}
		magicFunctionManager.register(functionInfo);
		magicNotifyService.sendNotify(new MagicNotify(instanceId, functionInfo.getId(), action, Constants.NOTIFY_ACTION_FUNCTION));
		return functionInfo.getId();
	}

	@Override
	public FunctionInfo getFunctionInfo(String id) {
		return functionServiceProvider.get(id);
	}

	@Override
	public List<FunctionInfo> functionList() {
		return functionServiceProvider.list();
	}

	@Override
	public boolean deleteFunction(String id) {
		if (deleteFunctionWithoutNotify(id)) {
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, Constants.NOTIFY_ACTION_DELETE, Constants.NOTIFY_ACTION_FUNCTION));
			return true;
		}
		return false;
	}

	private boolean deleteFunctionWithoutNotify(String id) {
		if (functionServiceProvider.delete(id)) {
			magicFunctionManager.unregister(id);
			return true;
		}
		return false;
	}

	@Override
	public boolean moveFunction(String id, String groupId) {
		isTrue(functionServiceProvider.allowMove(id, groupId), NAME_CONFLICT);
		isTrue(magicFunctionManager.move(id, groupId), FUNCTION_PATH_CONFLICT);
		if (functionServiceProvider.move(id, groupId)) {
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, Constants.NOTIFY_ACTION_UPDATE, Constants.NOTIFY_ACTION_FUNCTION));
			return true;
		}
		return false;
	}

	@Override
	public String createGroup(Group group) {
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		notBlank(group.getName(), GROUP_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(group.getName()), NAME_INVALID);
		notBlank(group.getType(), GROUP_TYPE_REQUIRED);
		isTrue(groupServiceProvider.insert(group), GROUP_SAVE_FAILURE);
		if (Objects.equals(group.getType(), Constants.GROUP_TYPE_API)) {
			mappingHandlerMapping.loadGroup();
		} else {
			magicFunctionManager.loadGroup();
		}
		magicNotifyService.sendNotify(new MagicNotify(instanceId, group.getId(), Constants.NOTIFY_ACTION_ADD, Constants.NOTIFY_ACTION_GROUP));
		return group.getId();
	}

	@Override
	public boolean updateGroup(Group group) {
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		notBlank(group.getName(), GROUP_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(group.getName()), NAME_INVALID);

		notBlank(group.getType(), GROUP_TYPE_REQUIRED);
		boolean isApiGroup = Constants.GROUP_TYPE_API.equals(group.getType());
		boolean isFunctionGroup = Constants.GROUP_TYPE_FUNCTION.equals(group.getType());
		if (isApiGroup && mappingHandlerMapping.checkGroup(group)) {
			isTrue(groupServiceProvider.update(group), GROUP_SAVE_FAILURE);
			// 如果数据库修改成功，则修改接口路径
			mappingHandlerMapping.updateGroup(group.getId());

		} else if (isFunctionGroup && magicFunctionManager.checkGroup(group)) {
			isTrue(groupServiceProvider.update(group), GROUP_SAVE_FAILURE);
			// 如果数据库修改成功，则修改接口路径
			magicFunctionManager.updateGroup(group.getId());
		}
		magicNotifyService.sendNotify(new MagicNotify(instanceId, group.getId(), Constants.NOTIFY_ACTION_UPDATE, Constants.NOTIFY_ACTION_GROUP));
		return true;
	}

	@Override
	public boolean deleteGroup(String groupId) {
		boolean success = deleteGroupWithoutNotify(groupId);
		magicNotifyService.sendNotify(new MagicNotify(instanceId, groupId, Constants.NOTIFY_ACTION_DELETE, Constants.NOTIFY_ACTION_GROUP));
		return success;
	}

	private boolean deleteGroupWithoutNotify(String groupId) {
		boolean isApi = true;
		TreeNode<Group> treeNode = groupServiceProvider.apiGroupTree().findTreeNode(group -> group.getId().equals(groupId));
		if (treeNode == null) {
			treeNode = groupServiceProvider.functionGroupTree().findTreeNode(group -> group.getId().equals(groupId));
			notNull(treeNode, GROUP_NOT_FOUND);
			isApi = false;
		}
		List<String> children = treeNode.flat().stream().map(Group::getId).collect(Collectors.toList());
		boolean success;
		if (isApi) {
			// 删除接口
			if (success = apiServiceProvider.deleteGroup(groupId, children)) {
				// 取消注册
				mappingHandlerMapping.deleteGroup(children);
				children.forEach(groupServiceProvider::delete);
			}
		} else {
			// 删除函数
			if (success = functionServiceProvider.deleteGroup(groupId, children)) {
				// 取消注册
				magicFunctionManager.deleteGroup(children);
				children.forEach(groupServiceProvider::delete);
			}
		}
		return success;
	}

	@Override
	public List<Group> groupList(String type) {
		return groupServiceProvider.groupList(type);
	}

	@Override
	public void registerAllDataSource() {
		datasourceResource.readAll();
		List<Resource> resources = datasourceResource.files(".json");
		// 删除旧的数据源
		magicDynamicDataSource.datasourceNodes().stream()
				.filter(it -> it.getId() != null)
				.map(MagicDynamicDataSource.DataSourceNode::getKey)
				.collect(Collectors.toList())
				.forEach(magicDynamicDataSource::delete);
		TypeFactory factory = TypeFactory.defaultInstance();
		for (Resource item : resources) {
			registerDataSource(JsonUtils.readValue(item.read(), factory.constructMapType(HashMap.class, String.class, String.class)));
		}
	}

	private String registerDataSource(Map<String, String> properties) {
		if (properties != null) {
			String key = properties.get("key");
			String name = properties.getOrDefault("name", key);
			String dsId = properties.remove("id");
			int maxRows = ObjectConvertExtension.asInt(properties.get("maxRows"), -1);
			magicDynamicDataSource.put(dsId, key, name, createDataSource(properties), maxRows);
			return key;
		}
		return null;
	}

	@Override
	public Map<String, String> getDataSource(String id) {
		Resource resource = this.datasourceResource.getResource(id + ".json");
		byte[] bytes = resource.read();
		isTrue(bytes != null && bytes.length > 0, DATASOURCE_NOT_FOUND);
		TypeFactory factory = TypeFactory.defaultInstance();
		return JsonUtils.readValue(bytes, factory.constructMapType(LinkedHashMap.class, String.class, String.class));
	}

	@Override
	public List<Map<String, Object>> datasourceList() {
		return magicDynamicDataSource.datasourceNodes().stream().map(it -> {
			Map<String, Object> row = new HashMap<>();
			row.put("id", it.getId());    // id为空的则认为是不可修改的
			row.put("key", it.getKey());    // 如果为null 说明是主数据源
			row.put("name", it.getName());
			return row;
		}).collect(Collectors.toList());
	}

	@Override
	public String testDataSource(Map<String, String> properties) {
		DataSource dataSource = null;
		try {
			dataSource = createDataSource(properties);
			Connection connection = dataSource.getConnection();
			DataSourceUtils.doCloseConnection(connection, dataSource);
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			IoUtils.closeDataSource(dataSource);
		}
		return null;
	}

	@Override
	public String saveDataSource(Map<String, String> properties) {
		String key = properties.get("key");
		// 校验key是否符合规则
		notBlank(key, DATASOURCE_KEY_REQUIRED);
		isTrue(IoUtils.validateFileName(key), DATASOURCE_KEY_INVALID);
		String name = properties.getOrDefault("name", key);
		String id = properties.get("id");
		Stream<String> keyStream;
		int action = Constants.NOTIFY_ACTION_UPDATE;
		if (StringUtils.isBlank(id)) {
			action = Constants.NOTIFY_ACTION_ADD;
			keyStream = magicDynamicDataSource.datasources().stream();
		} else {
			keyStream = magicDynamicDataSource.datasourceNodes().stream()
					.filter(it -> !id.equals(it.getId()))
					.map(MagicDynamicDataSource.DataSourceNode::getKey);
		}
		String dsId = StringUtils.isBlank(id) ? UUID.randomUUID().toString().replace("-", "") : id;
		// 验证是否有冲突
		isTrue(keyStream.noneMatch(key::equals), DATASOURCE_KEY_EXISTS);

		int maxRows = ObjectConvertExtension.asInt(properties.get("maxRows"), -1);
		properties.remove("id");
		// 注册数据源
		magicDynamicDataSource.put(dsId, key, name, createDataSource(properties), maxRows);
		properties.put("id", dsId);
		datasourceResource.getResource(dsId + ".json").write(JsonUtils.toJsonString(properties));
		magicNotifyService.sendNotify(new MagicNotify(instanceId, dsId, action, Constants.NOTIFY_ACTION_DATASOURCE));
		return dsId;
	}

	@Override
	public boolean deleteDataSource(String id) {
		// 查询数据源是否存在
		Optional<MagicDynamicDataSource.DataSourceNode> dataSourceNode = magicDynamicDataSource.datasourceNodes().stream()
				.filter(it -> id.equals(it.getId()))
				.findFirst();
		isTrue(dataSourceNode.isPresent(), DATASOURCE_NOT_FOUND);
		Resource resource = this.datasourceResource.getResource(id + ".json");
		// 删除数据源
		isTrue(resource.delete(), DATASOURCE_NOT_FOUND);
		// 取消注册数据源
		dataSourceNode.ifPresent(it -> magicDynamicDataSource.delete(it.getKey()));
		magicNotifyService.sendNotify(new MagicNotify(instanceId, id, Constants.NOTIFY_ACTION_DELETE, Constants.NOTIFY_ACTION_DATASOURCE));
		return true;
	}

	@Override
	public void upload(InputStream inputStream, String mode) throws IOException {
		ZipResource root = new ZipResource(inputStream);
		Set<String> apiPaths = new HashSet<>();
		Set<String> functionPaths = new HashSet<>();
		Set<Group> groups = new HashSet<>();
		Set<ApiInfo> apiInfos = new HashSet<>();
		Set<FunctionInfo> functionInfos = new HashSet<>();
		// 检查上传资源中是否有冲突
		isTrue(readPaths(groups, apiPaths, functionPaths, apiInfos, functionInfos, "/", root), UPLOAD_PATH_CONFLICT);
		Resource item = root.getResource(Constants.GROUP_METABASE);
		if (item.exists()) {
			Group group = groupServiceProvider.readGroup(item);
			// 检查分组是否存在
			isTrue("0".equals(group.getParentId()) || groupServiceProvider.getGroupResource(group.getParentId()).exists(), GROUP_NOT_FOUND);
		}
		for (Group group : groups) {
			Resource groupResource = groupServiceProvider.getGroupResource(group.getId());
			if (groupResource != null && groupResource.exists()) {
				groupServiceProvider.update(group);
			} else {
				groupServiceProvider.insert(group);
			}
		}
		Resource backups = workspace.getDirectory(Constants.PATH_BACKUPS);
		// 保存
		write(apiServiceProvider, backups, apiInfos);
		write(functionServiceProvider, backups, functionInfos);
		// 重新注册
		mappingHandlerMapping.registerAllMapping();
		magicFunctionManager.registerAllFunction();
		Resource uploadDatasourceResource = root.getResource(Constants.PATH_DATASOURCE + "/");
		List<String> datasourceKeys = new ArrayList<>();
		if (uploadDatasourceResource.exists()) {
			MapType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
			uploadDatasourceResource.files(".json").forEach(it -> {
				byte[] content = it.read();
				// 注册数据源
				datasourceKeys.add(registerDataSource(JsonUtils.readValue(content, mapType)));
				// 保存数据源
				this.datasourceResource.getResource(it.name()).write(content);
			});
		}
		// 全量模式
		if (Constants.UPLOAD_MODE_FULL.equals(mode)) {
			// 删掉多余的分组
			groupServiceProvider.getGroupsWithoutGroups(groups.stream().map(Group::getId).collect(Collectors.toList())).forEach(this::deleteGroupWithoutNotify);
			// 删除多余的接口
			apiServiceProvider.getIdsWithoutIds(apiInfos.stream().map(ApiInfo::getId).collect(Collectors.toList())).forEach(this::deleteApiWithoutNotify);
			// 删除多余的函数
			functionServiceProvider.getIdsWithoutIds(functionInfos.stream().map(FunctionInfo::getId).collect(Collectors.toList())).forEach(this::deleteFunctionWithoutNotify);
			// 删除多余的数据源
			magicDynamicDataSource.datasourceNodes().stream()
					.filter(it -> StringUtils.isNotBlank(it.getId()) && !datasourceKeys.contains(it.getKey()))
					.collect(Collectors.toList())
					.forEach(it -> {
						// 删除数据源
						this.datasourceResource.getResource(it.getId() + ".json").delete();
						this.magicDynamicDataSource.delete(it.getKey());
					});

		}
		magicNotifyService.sendNotify(new MagicNotify(instanceId));
	}

	@Override
	public JsonBean<?> push(String target, String secretKey, String mode) {
		notBlank(target, TARGET_IS_REQUIRED);
		notBlank(secretKey, SECRET_KEY_IS_REQUIRED);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			workspace.export(baos, Constants.PATH_BACKUPS);
		} catch (IOException e) {
			return new JsonBean<>(-1, e.getMessage());
		}
		byte[] bytes = baos.toByteArray();
		long timestamp = System.currentTimeMillis();
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
		param.add("timestamp", timestamp);
		param.add("mode", mode);
		param.add("sign", SignUtils.sign(timestamp, secretKey, mode, bytes));
		param.add("file", new InputStreamResource(new ByteArrayInputStream(bytes)) {
			@Override
			public String getFilename() {
				return "magic-api.zip";
			}

			@Override
			public long contentLength() {
				return bytes.length;
			}
		});
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		return restTemplate.postForObject(target, new HttpEntity<>(param, headers), JsonBean.class);
	}

	@Override
	public boolean processNotify(MagicNotify magicNotify) {
		if (magicNotify == null || instanceId.equals(magicNotify.getFrom())) {
			return false;
		}
		logger.info("收到通知消息:{}", magicNotify);
		String id = magicNotify.getId();
		int action = magicNotify.getAction();
		switch (magicNotify.getType()) {
			case Constants.NOTIFY_ACTION_API:
				return processApiNotify(id, action);
			case Constants.NOTIFY_ACTION_FUNCTION:
				return processFunctionNotify(id, action);
			case Constants.NOTIFY_ACTION_GROUP:
				return processGroupNotify(id, action);
			case Constants.NOTIFY_ACTION_DATASOURCE:
				return processDataSourceNotify(id, action);
			case Constants.NOTIFY_ACTION_ALL:
				return processAllNotify();
		}
		return false;
	}

	@Override
	public String getModuleName() {
		return "magic";
	}

	private boolean processApiNotify(String id, int action) {
		// 刷新缓存
		this.apiList();
		if (action == Constants.NOTIFY_ACTION_DELETE) {
			mappingHandlerMapping.unregisterMapping(id, true);
		} else {
			mappingHandlerMapping.registerMapping(apiServiceProvider.get(id), true);
		}
		return true;
	}

	private boolean processFunctionNotify(String id, int action) {
		// 刷新缓存
		this.functionList();
		if (action == Constants.NOTIFY_ACTION_DELETE) {
			magicFunctionManager.unregister(id);
		} else {
			magicFunctionManager.register(functionServiceProvider.get(id));
		}
		return true;
	}

	private boolean processDataSourceNotify(String id, int action) {
		if (action == Constants.NOTIFY_ACTION_DELETE) {
			// 查询数据源是否存在
			magicDynamicDataSource.datasourceNodes().stream()
					.filter(it -> id.equals(it.getId()))
					.findFirst()
					.ifPresent(it -> magicDynamicDataSource.delete(it.getKey()));
		} else {
			// 刷新数据源缓存
			datasourceResource.readAll();
			// 注册数据源
			registerDataSource(getDataSource(id));
		}
		return true;
	}

	private boolean processGroupNotify(String id, int action) {
		if (action == Constants.NOTIFY_ACTION_ADD) {    // 新增分组
			// 新增时只需要刷新分组缓存即可
			mappingHandlerMapping.loadGroup();
			magicFunctionManager.loadGroup();
			return true;
		}
		if (action == Constants.NOTIFY_ACTION_UPDATE) {    // 修改分组，包括移动分组
			if (!mappingHandlerMapping.updateGroup(id)) {
				return magicFunctionManager.updateGroup(id);
			}
		} else if (action == Constants.NOTIFY_ACTION_DELETE) {    // 删除分组
			TreeNode<Group> treeNode = mappingHandlerMapping.findGroupTree(id);
			if (treeNode == null) {
				// 删除函数分组
				treeNode = magicFunctionManager.findGroupTree(id);
				magicFunctionManager.deleteGroup(treeNode.flat().stream().map(Group::getId).collect(Collectors.toList()));
				// 刷新函数缓存
				this.functionList();
			} else {
				// 删除接口分组
				mappingHandlerMapping.deleteGroup(treeNode.flat().stream().map(Group::getId).collect(Collectors.toList()));
				// 刷新接口缓存
				this.apiList();
			}
		}
		return true;
	}

	private boolean processAllNotify() {
		mappingHandlerMapping.registerAllMapping();
		magicFunctionManager.registerAllFunction();
		registerAllDataSource();
		return true;
	}

	// copy from DataSourceBuilder
	private DataSource createDataSource(Map<String, String> properties) {
		Class<? extends DataSource> dataSourceType = getDataSourceType(properties.get("type"));
		if (!properties.containsKey("driverClassName")
				&& properties.containsKey("url")) {
			String url = properties.get("url");
			String driverClass = DatabaseDriver.fromJdbcUrl(url).getDriverClassName();
			properties.put("driverClassName", driverClass);
		}
		DataSource dataSource = BeanUtils.instantiateClass(dataSourceType);
		ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
		ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
		aliases.addAliases("url", "jdbc-url");
		aliases.addAliases("username", "user");
		Binder binder = new Binder(source.withAliases(aliases));
		binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(dataSource));
		return dataSource;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends DataSource> getDataSourceType(String datasourceType) {
		if (StringUtils.isNotBlank(datasourceType)) {
			try {
				return (Class<? extends DataSource>) ClassUtils.forName(datasourceType, classLoader);
			} catch (Exception e) {
				throw new InvalidArgumentException(DATASOURCE_TYPE_NOT_FOUND.format(datasourceType));
			}
		}
		for (String name : DATA_SOURCE_TYPE_NAMES) {
			try {
				return (Class<? extends DataSource>) ClassUtils.forName(name, classLoader);
			} catch (Exception ignored) {
			}
		}
		throw new InvalidArgumentException(DATASOURCE_TYPE_NOT_SET);
	}

	private <T extends MagicEntity> void write(StoreServiceProvider<T> provider, Resource backups, Set<T> infos) {
		for (T info : infos) {
			Resource resource = groupServiceProvider.getGroupResource(info.getGroupId());
			resource = resource.getResource(info.getName() + ".ms");
			byte[] content = provider.serialize(info);
			resource.write(content);
			Resource directory = backups.getDirectory(info.getId());
			if (!directory.exists()) {
				directory.mkdir();
			}
			directory.getResource(System.currentTimeMillis() + ".ms").write(content);
			resource.write(content);
		}
	}

	private boolean readPaths(Set<Group> groups, Set<String> apiPaths, Set<String> functionPaths, Set<ApiInfo> apiInfos, Set<FunctionInfo> functionInfos, String parentPath, Resource root) {
		Resource resource = root.getResource(Constants.GROUP_METABASE);
		String path = "";
		if (resource.exists()) {
			Group group = JsonUtils.readValue(resource.read(), Group.class);
			groups.add(group);
			path = Objects.toString(group.getPath(), "");
			boolean isApi = Constants.GROUP_TYPE_API.equals(group.getType());
			for (Resource file : root.files(".ms")) {
				boolean conflict;
				if (isApi) {
					ApiInfo info = apiServiceProvider.deserialize(file.read());
					apiInfos.add(info);
					conflict = !apiPaths.add(Objects.toString(info.getMethod(), "GET") + ":" + PathUtils.replaceSlash(parentPath + "/" + path + "/" + info.getPath()));
				} else {
					FunctionInfo info = functionServiceProvider.deserialize(file.read());
					functionInfos.add(info);
					conflict = !functionPaths.add(PathUtils.replaceSlash(parentPath + "/" + path + "/" + info.getPath()));
				}
				if (conflict) {
					return false;
				}
			}
		}
		for (Resource directory : root.dirs()) {
			if (!readPaths(groups, apiPaths, functionPaths, apiInfos, functionInfos, PathUtils.replaceSlash(parentPath + "/" + path), directory)) {
				return false;
			}
		}
		return true;
	}

}
