package org.ssssssss.magicapi.provider.impl;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.config.MagicFunctionManager;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.exception.MagicServiceException;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.provider.*;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultMagicAPIService implements MagicAPIService, JsonCodeConstants {

	private final MappingHandlerMapping mappingHandlerMapping;

	private final boolean throwException;

	private final ResultProvider resultProvider;

	private final ApiServiceProvider apiServiceProvider;

	private final FunctionServiceProvider functionServiceProvider;

	private final GroupServiceProvider groupServiceProvider;

	private final MagicFunctionManager magicFunctionManager;

	private final MagicNotifyService magicNotifyService;

	private final String instanceId;

	public DefaultMagicAPIService(MappingHandlerMapping mappingHandlerMapping, ApiServiceProvider apiServiceProvider, FunctionServiceProvider functionServiceProvider, GroupServiceProvider groupServiceProvider, ResultProvider resultProvider, MagicFunctionManager magicFunctionManager, MagicNotifyService magicNotifyService, String instanceId, boolean throwException) {
		this.mappingHandlerMapping = mappingHandlerMapping;
		this.apiServiceProvider = apiServiceProvider;
		this.functionServiceProvider = functionServiceProvider;
		this.groupServiceProvider = groupServiceProvider;
		this.resultProvider = resultProvider;
		this.magicFunctionManager = magicFunctionManager;
		this.magicNotifyService = magicNotifyService;
		this.throwException = throwException;
		this.instanceId = StringUtils.defaultIfBlank(instanceId, UUID.randomUUID().toString());
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
		boolean success = apiServiceProvider.delete(id);
		if (success) {    //删除成功时在取消注册
			mappingHandlerMapping.unregisterMapping(id, true);
			// 通知删除接口
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, Constants.NOTIFY_ACTION_DELETE, Constants.NOTIFY_ACTION_API));
		}
		return success;
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
		boolean success = functionServiceProvider.delete(id);
		if (success) {
			magicFunctionManager.unregister(id);
			magicNotifyService.sendNotify(new MagicNotify(instanceId, id, Constants.NOTIFY_ACTION_DELETE, Constants.NOTIFY_ACTION_FUNCTION));
		}
		return success;
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
			return true;
		} else if (isFunctionGroup && magicFunctionManager.checkGroup(group)) {
			isTrue(groupServiceProvider.update(group), GROUP_SAVE_FAILURE);
			// 如果数据库修改成功，则修改接口路径
			magicFunctionManager.updateGroup(group.getId());
			return true;
		}
		magicNotifyService.sendNotify(new MagicNotify(instanceId, group.getId(), Constants.NOTIFY_ACTION_UPDATE, Constants.NOTIFY_ACTION_GROUP));
		return false;
	}

	@Override
	public boolean deleteGroup(String groupId) {
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
		magicNotifyService.sendNotify(new MagicNotify(instanceId, groupId, Constants.NOTIFY_ACTION_DELETE, Constants.NOTIFY_ACTION_GROUP));
		return success;
	}

	@Override
	public List<Group> groupList(String type) {
		return groupServiceProvider.groupList(type);
	}

	@Override
	public boolean processNotify(MagicNotify magicNotify) {
		if (magicNotify == null || instanceId.equals(magicNotify.getFrom())) {
			return false;
		}
		String id = magicNotify.getId();
		int action = magicNotify.getAction();
		switch (magicNotify.getType()) {
			case Constants.NOTIFY_ACTION_API:
				return processApiNotify(id, action);
			case Constants.NOTIFY_ACTION_FUNCTION:
				return processFunctionNotify(id, action);
			case Constants.NOTIFY_ACTION_GROUP:
				return processGroupNotify(id, action);
		}
		return false;
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
			TreeNode<Group> treeNode = groupServiceProvider.apiGroupTree().findTreeNode(group -> group.getId().equals(id));
			if (treeNode == null) {
				// 删除函数分组
				treeNode = groupServiceProvider.functionGroupTree().findTreeNode(group -> group.getId().equals(id));
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


	@Override
	public String getModuleName() {
		return "magic";
	}
}
