package org.ssssssss.magicapi.provider.impl;

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
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.controller.MagicDataSourceController;
import org.ssssssss.magicapi.controller.MagicWebSocketDispatcher;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.exception.MagicResourceNotFoundException;
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
import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.runtime.ExitValue;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.ssssssss.magicapi.model.Constants.*;

/**
 * 默认接口实现
 *
 * @author mxd
 */
public class DefaultMagicAPIService implements MagicAPIService, JsonCodeConstants {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMagicAPIService.class);
	private static final ClassLoader CLASSLOADER = MagicDataSourceController.class.getClassLoader();
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
	private final MagicBackupService backupService;

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
								  MagicBackupService backupService,
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
		this.instanceId = instanceId;
		this.backupService = backupService;
		this.datasourceResource = workspace.getDirectory(PATH_DATASOURCE);
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
					return (MagicScriptLambdaFunction) (variables, args) -> {
						MagicScriptContext context = MagicScriptContext.get();
						MagicScriptContext newContext = new MagicScriptContext();
						Map<String, Object> varMap = new LinkedHashMap<>(context.getRootVariables());
						varMap.putAll(variables.getVariables());
						newContext.setScriptName(groupServiceProvider.getScriptName(info.getId(), info.getName(), info.getPath()));
						newContext.putMapIntoContext(varMap);
						try {
							Object value = ScriptManager.executeScript(info.getScript(), newContext);
							if (value instanceof ExitValue) {
								throw new MagicExitException((ExitValue) value);
							}
							return value;
						} finally {
							MagicScriptContext.set(context);
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
		scriptContext.setScriptName(groupServiceProvider.getScriptName(info.getGroupId(), info.getName(), info.getPath()));
		scriptContext.putMapIntoContext(context);
		final Object evalVal;
		try {
			evalVal = ScriptManager.executeScript(info.getScript(), scriptContext);
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
			throw new MagicResourceNotFoundException(String.format("找不到对应接口 [%s:%s]", method, path));
		}
		return execute(info, context);
	}

	@Override
	public Object call(String method, String path, Map<String, Object> context) {
		RequestEntity requestEntity = RequestEntity.empty();
		try {
			return resultProvider.buildResult(requestEntity, execute(method, path, context));
		} catch (MagicResourceNotFoundException e) {
			//找不到对应接口
			return null;
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			return resultProvider.buildResult(requestEntity, root);
		}
	}

	@Override
	public Object invoke(String path, Map<String, Object> context) {
		FunctionInfo functionInfo = magicFunctionManager.getFunctionInfo(path);
		if (functionInfo == null) {
			throw new MagicResourceNotFoundException(String.format("找不到对应函数 [%s]", path));
		}
		MagicScriptContext scriptContext = new MagicScriptContext(context);
		scriptContext.setScriptName(groupServiceProvider.getScriptName(functionInfo.getGroupId(), functionInfo.getName(), functionInfo.getPath()));
		scriptContext.putMapIntoContext(context);
		return ScriptManager.executeScript(functionInfo.getScript(), scriptContext);
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
		int action = NOTIFY_ACTION_UPDATE;
		if (StringUtils.isBlank(info.getId())) {
			// 先判断接口是否存在
			isTrue(!apiServiceProvider.exists(info), API_ALREADY_EXISTS.format(info.getMethod(), info.getPath()));
			isTrue(apiServiceProvider.insert(info), API_SAVE_FAILURE);
			action = NOTIFY_ACTION_ADD;
		} else {
			// 先判断接口是否存在
			isTrue(!apiServiceProvider.existsWithoutId(info), API_ALREADY_EXISTS.format(info.getMethod(), info.getPath()));
			isTrue(apiServiceProvider.update(info), API_SAVE_FAILURE);
			Optional<ApiInfo> optional = mappingHandlerMapping.getApiInfos().stream()
					.filter(it -> it.getId().equals(info.getId()))
					.findFirst();
			if (optional.isPresent() && !optional.get().getScript().equals(info.getScript())) {
				backupService.backup(info);
			}
		}
		// 注册接口
		mappingHandlerMapping.registerMapping(info, true);
		// 通知更新接口
		magicNotifyService.sendNotify(new MagicNotify(instanceId, info.getId(), action, NOTIFY_ACTION_API));
		return info.getId();
	}

	@Override
	public boolean lockApi(String id) {
		return lockWithNotify(apiServiceProvider.lock(id), id, NOTIFY_ACTION_API);
	}

	@Override
	public boolean unlockApi(String id) {
		return lockWithNotify(apiServiceProvider.unlock(id), id, NOTIFY_ACTION_API);
	}

	@Override
	public boolean lockFunction(String id) {
		return lockWithNotify(functionServiceProvider.lock(id), id, NOTIFY_ACTION_FUNCTION);
	}

	@Override
	public boolean unlockFunction(String id) {
		return lockWithNotify(functionServiceProvider.unlock(id), id, NOTIFY_ACTION_FUNCTION);
	}

	private boolean lockWithNotify(boolean success, String id, int type) {
		if (success) {
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, NOTIFY_ACTION_UPDATE, type));
		}
		return success;
	}

	@Override
	public ApiInfo getApiInfo(String id) {
		return apiServiceProvider.get(id);
	}

	@Override
	public List<ApiInfo> apiList() {
		return apiServiceProvider.cachedList();
	}

	@Override
	public boolean deleteApi(String id) {
		if (deleteApiWithoutNotify(id)) {
			// 通知删除接口
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, NOTIFY_ACTION_DELETE, NOTIFY_ACTION_API));
			return true;
		}
		return false;
	}

	private boolean deleteApiWithoutNotify(String id) {
		// 删除成功时在取消注册
		if (apiServiceProvider.delete(id)) {
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
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, NOTIFY_ACTION_UPDATE, NOTIFY_ACTION_API));
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
		int action = NOTIFY_ACTION_UPDATE;
		if (StringUtils.isBlank(functionInfo.getId())) {
			isTrue(!functionServiceProvider.exists(functionInfo), FUNCTION_ALREADY_EXISTS.format(functionInfo.getPath()));
			isTrue(functionServiceProvider.insert(functionInfo), FUNCTION_SAVE_FAILURE);
			action = NOTIFY_ACTION_ADD;
		} else {
			isTrue(!functionServiceProvider.existsWithoutId(functionInfo), FUNCTION_ALREADY_EXISTS.format(functionInfo.getPath()));
			FunctionInfo oldInfo = functionServiceProvider.get(functionInfo.getId());
			isTrue(functionServiceProvider.update(functionInfo), FUNCTION_SAVE_FAILURE);
			if (!oldInfo.getScript().equals(functionInfo.getScript())) {
				backupService.backup(functionInfo);
			}
		}
		magicFunctionManager.register(functionInfo);
		magicNotifyService.sendNotify(new MagicNotify(instanceId, functionInfo.getId(), action, NOTIFY_ACTION_FUNCTION));
		return functionInfo.getId();
	}

	@Override
	public FunctionInfo getFunctionInfo(String id) {
		return functionServiceProvider.get(id);
	}

	@Override
	public List<FunctionInfo> functionList() {
		return functionServiceProvider.cachedList();
	}

	@Override
	public boolean deleteFunction(String id) {
		if (deleteFunctionWithoutNotify(id)) {
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, NOTIFY_ACTION_DELETE, NOTIFY_ACTION_FUNCTION));
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
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, NOTIFY_ACTION_UPDATE, NOTIFY_ACTION_FUNCTION));
			return true;
		}
		return false;
	}

	@Override
	public String createGroup(Group group) {
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId(ROOT_ID);
		}
		notBlank(group.getName(), GROUP_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(group.getName()), NAME_INVALID);
		notBlank(group.getType(), GROUP_TYPE_REQUIRED);
		isTrue(groupServiceProvider.insert(group), GROUP_SAVE_FAILURE);
		if (Objects.equals(group.getType(), GROUP_TYPE_API)) {
			mappingHandlerMapping.loadGroup();
		} else {
			magicFunctionManager.loadGroup();
		}
		magicNotifyService.sendNotify(new MagicNotify(instanceId, group.getId(), NOTIFY_ACTION_ADD, NOTIFY_ACTION_GROUP));
		return group.getId();
	}

	@Override
	public boolean updateGroup(Group group) {
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId(ROOT_ID);
		}
		notBlank(group.getName(), GROUP_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(group.getName()), NAME_INVALID);

		notBlank(group.getType(), GROUP_TYPE_REQUIRED);
		boolean isApiGroup = GROUP_TYPE_API.equals(group.getType());
		boolean isFunctionGroup = GROUP_TYPE_FUNCTION.equals(group.getType());
		if (isApiGroup && mappingHandlerMapping.checkGroup(group)) {
			isTrue(groupServiceProvider.update(group), GROUP_SAVE_FAILURE);
			// 如果数据库修改成功，则修改接口路径
			mappingHandlerMapping.updateGroup(group.getId());

		} else if (isFunctionGroup && magicFunctionManager.checkGroup(group)) {
			isTrue(groupServiceProvider.update(group), GROUP_SAVE_FAILURE);
			// 如果数据库修改成功，则修改接口路径
			magicFunctionManager.updateGroup(group.getId());
		}
		magicNotifyService.sendNotify(new MagicNotify(instanceId, group.getId(), NOTIFY_ACTION_UPDATE, NOTIFY_ACTION_GROUP));
		return true;
	}

	@Override
	public boolean deleteGroup(String groupId) {
		boolean success = deleteGroupWithoutNotify(groupId);
		magicNotifyService.sendNotify(new MagicNotify(instanceId, groupId, NOTIFY_ACTION_DELETE, NOTIFY_ACTION_GROUP));
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
			success = apiServiceProvider.deleteGroup(groupId, children);
			if (success) {
				// 取消注册
				mappingHandlerMapping.deleteGroup(children);
				children.forEach(groupServiceProvider::delete);
			}
		} else {
			// 删除函数
			success = functionServiceProvider.deleteGroup(groupId, children);
			if (success) {
				// 取消注册
				magicFunctionManager.deleteGroup(children);
				children.forEach(groupServiceProvider::delete);
			}
		}
		return success;
	}

	@Override
	public List<Group> groupList(String type) {
		return groupServiceProvider.cachedGroupList(type);
	}

	@Override
	public Group getGroup(String id) {
		Resource groupResource = groupServiceProvider.getGroupResource(id);
		groupResource = groupResource != null ? groupResource.getResource(GROUP_METABASE) : null;
		if (groupResource != null && groupResource.exists()) {
			return groupServiceProvider.readGroup(groupResource);
		}
		return null;
	}

	@Override
	public void registerAllDataSource() {
		datasourceResource.readAll();
		List<Resource> resources = datasourceResource.files(JSON_SUFFIX);
		// 删除旧的数据源
		magicDynamicDataSource.datasourceNodes().stream()
				.filter(it -> it.getId() != null)
				.map(MagicDynamicDataSource.DataSourceNode::getKey)
				.collect(Collectors.toList())
				.forEach(magicDynamicDataSource::delete);
		for (Resource item : resources) {
			registerDataSource(JsonUtils.readValue(item.read(), DataSourceInfo.class));
		}
	}

	private String registerDataSource(DataSourceInfo properties) {
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
	public DataSourceInfo getDataSource(String id) {
		Resource resource = this.datasourceResource.getResource(id + ".json");
		byte[] bytes = resource.read();
		isTrue(bytes != null && bytes.length > 0, DATASOURCE_NOT_FOUND);
		return JsonUtils.readValue(bytes, DataSourceInfo.class);
	}

	@Override
	public List<DataSourceInfo> datasourceList() {
		return magicDynamicDataSource.datasourceNodes().stream().map(it -> {
			DataSourceInfo info = new DataSourceInfo();
			info.put("id", it.getId());    // id为空的则认为是不可修改的
			info.put("key", it.getKey());    // 如果为null 说明是主数据源
			info.put("name", it.getName());
			return info;
		}).collect(Collectors.toList());
	}

	@Override
	public String testDataSource(DataSourceInfo properties) {
		DataSource dataSource = null;
		try {
			properties.remove("id");
			dataSource = createDataSource(properties);
			Connection connection = dataSource.getConnection();
			DataSourceUtils.doCloseConnection(connection, dataSource);
		} catch (Exception e) {
			logger.error("测试数据源连接失败", e);
			return e.getMessage();
		} finally {
			IoUtils.closeDataSource(dataSource);
		}
		return null;
	}

	@Override
	public String saveDataSource(DataSourceInfo properties) {
		String key = properties.get("key");
		// 校验key是否符合规则
		notBlank(key, DATASOURCE_KEY_REQUIRED);
		isTrue(IoUtils.validateFileName(key), DATASOURCE_KEY_INVALID);
		String name = properties.getOrDefault("name", key);
		String id = properties.get("id");
		Stream<String> keyStream;
		int action = NOTIFY_ACTION_UPDATE;
		if (StringUtils.isBlank(id)) {
			action = NOTIFY_ACTION_ADD;
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
		backupService.backup(properties);
		magicNotifyService.sendNotify(new MagicNotify(instanceId, dsId, action, NOTIFY_ACTION_DATASOURCE));
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
		magicNotifyService.sendNotify(new MagicNotify(instanceId, id, NOTIFY_ACTION_DELETE, NOTIFY_ACTION_DATASOURCE));
		return true;
	}

	@Override
	public void upload(InputStream inputStream, String mode) throws IOException {
		ZipResource root = new ZipResource(inputStream);
		Set<String> apiPaths = new LinkedHashSet<>();
		Set<String> functionPaths = new LinkedHashSet<>();
		Set<Group> groups = new LinkedHashSet<>();
		Set<ApiInfo> apiInfos = new LinkedHashSet<>();
		Set<FunctionInfo> functionInfos = new LinkedHashSet<>();
		boolean checked = !UPLOAD_MODE_FULL.equals(mode);
		// 检查上传资源中是否有冲突
		readPaths(groups, apiPaths, functionPaths, apiInfos, functionInfos, "/", root, checked);
		Resource item = root.getResource(GROUP_METABASE);
		if (item.exists()) {
			Group group = groupServiceProvider.readGroup(item);
			// 检查上级分组是否存在
			isTrue(ROOT_ID.equals(group.getParentId()) || groupServiceProvider.getGroupResource(group.getParentId()).exists(), GROUP_NOT_FOUND);
		}
		if (checked) {
			// 检测分组是否有冲突
			groups.forEach(group -> {
				Resource resource;
				if (ROOT_ID.equals(group.getParentId())) {
					resource = workspace.getDirectory(GROUP_TYPE_API.equals(group.getType()) ? PATH_API : PATH_FUNCTION).getDirectory(group.getName());
				} else {
					resource = groupServiceProvider.getGroupResource(group.getId());
				}
				if (resource != null && resource.exists()) {
					Group src = groupServiceProvider.readGroup(resource.getResource(GROUP_METABASE));
					isTrue(src == null || src.getId().equals(group.getId()), GROUP_CONFLICT);
				}
			});
		} else {
			Resource resource = workspace.getDirectory(PATH_API);
			resource.delete();
			resource.mkdir();
			resource = workspace.getDirectory(PATH_FUNCTION);
			resource.delete();
			resource.mkdir();
			resource = workspace.getDirectory(PATH_DATASOURCE);
			resource.delete();
			resource.mkdir();
		}
		for (Group group : groups) {
			Resource groupResource = groupServiceProvider.getGroupResource(group.getId());
			if (groupResource != null && groupResource.exists()) {
				groupServiceProvider.update(group);
			} else {
				groupServiceProvider.insert(group);
			}
		}
		// 保存
		write(apiServiceProvider, apiInfos);
		write(functionServiceProvider, functionInfos);
		// 备份
		apiInfos.forEach(backupService::backup);
		functionInfos.forEach(backupService::backup);
		// 重新注册
		mappingHandlerMapping.registerAllMapping();
		magicFunctionManager.registerAllFunction();
		Resource uploadDatasourceResource = root.getResource(PATH_DATASOURCE + "/");
		if (uploadDatasourceResource.exists()) {
			uploadDatasourceResource.files(".json").forEach(it -> {
				byte[] content = it.read();
				// 保存数据源
				this.datasourceResource.getResource(it.name()).write(content);
				// TODO 备份数据源
			});
		}
		// TODO 会造成闪断，需要上锁处理。
		registerAllDataSource();
		magicNotifyService.sendNotify(new MagicNotify(instanceId));
	}

	@Override
	public void download(String groupId, List<SelectedResource> resources, OutputStream os) throws IOException {
		if (StringUtils.isNotBlank(groupId)) {
			Resource resource = groupServiceProvider.getGroupResource(groupId);
			notNull(resource, GROUP_NOT_FOUND);
			resource.export(os);
		} else if (resources == null || resources.isEmpty()) {
			workspace.export(os, PATH_BACKUPS, "backup");
		} else {
			ZipOutputStream zos = new ZipOutputStream(os);
			for (SelectedResource item : resources) {
				StoreServiceProvider storeServiceProvider = null;
				if ("root".equals(item.getType())) {
					zos.putNextEntry(new ZipEntry(item.getId() + "/"));
					zos.closeEntry();
				} else if ("group".equals(item.getType())) {
					Resource resource = groupServiceProvider.getGroupResource(item.getId());
					zos.putNextEntry(new ZipEntry(resource.getFilePath()));
					zos.closeEntry();
					resource = resource.getResource(GROUP_METABASE);
					zos.putNextEntry(new ZipEntry(resource.getFilePath()));
					zos.write(resource.read());
					zos.closeEntry();
				} else if ("api".equals(item.getType())) {
					storeServiceProvider = apiServiceProvider;
				} else if ("function".equals(item.getType())) {
					storeServiceProvider = functionServiceProvider;
				} else if ("datasource".equals(item.getType())) {
					String filename = item.getId() + ".json";
					Resource resource = datasourceResource.getResource(filename);
					zos.putNextEntry(new ZipEntry(resource.getFilePath()));
					zos.write(resource.read());
					zos.closeEntry();
				}
				if (storeServiceProvider != null) {
					MagicEntity entity = storeServiceProvider.get(item.getId());
					Resource resource = groupServiceProvider.getGroupResource(entity.getGroupId());
					zos.putNextEntry(new ZipEntry(resource.getFilePath() + entity.getName() + ".ms"));
					zos.write(storeServiceProvider.serialize(entity));
					zos.closeEntry();
				}
			}
			zos.close();
		}
	}

	@Override
	public JsonBean<?> push(String target, String secretKey, String mode, List<SelectedResource> resources) {
		notBlank(target, TARGET_IS_REQUIRED);
		notBlank(secretKey, SECRET_KEY_IS_REQUIRED);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			download(null, resources, baos);
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
			case NOTIFY_ACTION_API:
				return processApiNotify(id, action);
			case NOTIFY_ACTION_FUNCTION:
				return processFunctionNotify(id, action);
			case NOTIFY_ACTION_GROUP:
				return processGroupNotify(id, action);
			case NOTIFY_ACTION_DATASOURCE:
				return processDataSourceNotify(id, action);
			case NOTIFY_ACTION_ALL:
				return processAllNotify();
			default:
				break;
		}
		switch (action) {
			case NOTIFY_WS_C_S:
				return processWebSocketMessageReceived(magicNotify.getSessionId(), magicNotify.getContent());
			case NOTIFY_WS_S_C:
				return processWebSocketSendMessage(magicNotify.getSessionId(), magicNotify.getContent());
			default:
				break;
		}
		return false;
	}

	@Override
	public String copyGroup(String srcId, String target) {
		Group src = getGroup(srcId);
		src.setId(null);
		src.setParentId(target);
		src.setName(src.getName() + "(复制)");
		src.setPath(src.getPath() + "_copy");
		String newId = createGroup(src);
		if (GROUP_TYPE_API.equals(src.getType())) {
			apiServiceProvider.listWithScript()
					.stream().filter(it -> srcId.equals(it.getGroupId()))
					.map(ApiInfo::copy)
					.peek(it -> it.setGroupId(newId))
					.peek(it -> it.setId(null))
					.forEach(this::saveApi);
		} else {
			functionServiceProvider.listWithScript()
					.stream().filter(it -> srcId.equals(it.getGroupId()))
					.map(FunctionInfo::copy)
					.peek(it -> it.setGroupId(newId))
					.peek(it -> it.setId(null))
					.forEach(this::saveFunction);
		}
		return newId;
	}

	@Override
	public String getModuleName() {
		return "magic";
	}

	private boolean processWebSocketSendMessage(String sessionId, String content) {
		WebSocketSessionManager.sendBySessionId(sessionId, content);
		return true;
	}

	private boolean processWebSocketMessageReceived(String sessionId, String content) {
		MagicWebSocketDispatcher.processMessageReceived(sessionId, content);
		return true;
	}

	private boolean processApiNotify(String id, int action) {
		// 刷新缓存
		apiServiceProvider.listWithScript();
		if (action == NOTIFY_ACTION_DELETE) {
			mappingHandlerMapping.unregisterMapping(id, true);
		} else {
			mappingHandlerMapping.registerMapping(apiServiceProvider.get(id), true);
		}
		return true;
	}

	private boolean processFunctionNotify(String id, int action) {
		// 刷新缓存
		functionServiceProvider.listWithScript();
		if (action == NOTIFY_ACTION_DELETE) {
			magicFunctionManager.unregister(id);
		} else {
			magicFunctionManager.register(functionServiceProvider.get(id));
		}
		return true;
	}

	private boolean processDataSourceNotify(String id, int action) {
		if (action == NOTIFY_ACTION_DELETE) {
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
		// 新增分组
		if (action == NOTIFY_ACTION_ADD) {
			// 新增时只需要刷新分组缓存即可
			mappingHandlerMapping.loadGroup();
			magicFunctionManager.loadGroup();
			return true;
		}
		// 修改分组，包括移动分组
		if (action == NOTIFY_ACTION_UPDATE) {
			if (!mappingHandlerMapping.updateGroup(id)) {
				return magicFunctionManager.updateGroup(id);
			}
		} else if (action == NOTIFY_ACTION_DELETE) {
			// 删除分组
			TreeNode<Group> treeNode = mappingHandlerMapping.findGroupTree(id);
			if (treeNode == null) {
				// 删除函数分组
				treeNode = magicFunctionManager.findGroupTree(id);
				magicFunctionManager.deleteGroup(treeNode.flat().stream().map(Group::getId).collect(Collectors.toList()));
				// 刷新函数缓存
				functionServiceProvider.listWithScript();
			} else {
				// 删除接口分组
				mappingHandlerMapping.deleteGroup(treeNode.flat().stream().map(Group::getId).collect(Collectors.toList()));
				// 刷新接口缓存
				apiServiceProvider.listWithScript();
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
	private DataSource createDataSource(DataSourceInfo properties) {
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
				return (Class<? extends DataSource>) ClassUtils.forName(datasourceType, CLASSLOADER);
			} catch (Exception e) {
				throw new InvalidArgumentException(DATASOURCE_TYPE_NOT_FOUND.format(datasourceType));
			}
		}
		for (String name : DATA_SOURCE_TYPE_NAMES) {
			try {
				return (Class<? extends DataSource>) ClassUtils.forName(name, CLASSLOADER);
			} catch (Exception ignored) {
				// ignored
			}
		}
		throw new InvalidArgumentException(DATASOURCE_TYPE_NOT_SET);
	}

	private <T extends MagicEntity> void write(StoreServiceProvider<T> provider, Set<T> infos) {
		for (T info : infos) {
			T oldInfo = provider.get(info.getId());
			if (oldInfo != null) {
				provider.update(info);
			} else {
				provider.insert(info);
			}
		}
	}

	private void readPaths(Set<Group> groups, Set<String> apiPaths, Set<String> functionPaths, Set<ApiInfo> apiInfos, Set<FunctionInfo> functionInfos, String parentPath, Resource root, boolean checked) {
		Resource resource = root.getResource(GROUP_METABASE);
		String path = "";
		if (resource.exists()) {
			Group group = JsonUtils.readValue(resource.read(), Group.class);
			groups.add(group);
			path = Objects.toString(group.getPath(), "");
			boolean isApi = GROUP_TYPE_API.equals(group.getType());
			for (Resource file : root.files(".ms")) {
				if (isApi) {
					ApiInfo info = apiServiceProvider.deserialize(file.read());
					if (checked) {
						checkApiConflict(info);
					}
					apiInfos.add(info);
					String apiPath = Objects.toString(info.getMethod(), "GET") + ":" + PathUtils.replaceSlash(parentPath + "/" + path + "/" + info.getPath());
					isTrue(apiPaths.add(apiPath), UPLOAD_PATH_CONFLICT.format(apiPath));
				} else {
					FunctionInfo info = functionServiceProvider.deserialize(file.read());
					if (checked) {
						checkFunctionConflict(info);
					}
					functionInfos.add(info);
					String functionPath = PathUtils.replaceSlash(parentPath + "/" + path + "/" + info.getPath());
					isTrue(functionPaths.add(functionPath), UPLOAD_PATH_CONFLICT.format(functionPath));
				}
			}
		}
		for (Resource directory : root.dirs()) {
			readPaths(groups, apiPaths, functionPaths, apiInfos, functionInfos, PathUtils.replaceSlash(parentPath + "/" + path), directory, checked);
		}
	}

	private ApiInfo checkApiConflict(ApiInfo info) {
		Resource groupResource = groupServiceProvider.getGroupResource(info.getGroupId());
		if (groupResource != null) {
			Resource resource = groupResource.getResource(info.getName() + ".ms");
			if (resource.exists()) {
				ApiInfo oldInfo = apiServiceProvider.deserialize(resource.read());
				isTrue(oldInfo.getId().equals(info.getId()), API_ALREADY_EXISTS.format(info.getMethod(), info.getPath()));
			}
		}
		return info;
	}

	private FunctionInfo checkFunctionConflict(FunctionInfo info) {
		Resource groupResource = groupServiceProvider.getGroupResource(info.getGroupId());
		if (groupResource != null && groupResource.exists()) {
			Resource resource = groupResource.getResource(info.getName() + ".ms");
			if (resource.exists()) {
				FunctionInfo oldInfo = functionServiceProvider.deserialize(resource.read());
				isTrue(oldInfo.getId().equals(info.getId()), FUNCTION_ALREADY_EXISTS.format(info.getName()));
			}
		}
		return info;
	}
}
