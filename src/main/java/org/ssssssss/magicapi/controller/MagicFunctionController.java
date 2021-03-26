package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.FunctionServiceProvider;
import org.ssssssss.magicapi.utils.IoUtils;

import java.util.List;

public class MagicFunctionController extends MagicController implements MagicExceptionHandler {

	private final FunctionServiceProvider functionService;

	public MagicFunctionController(MagicConfiguration configuration) {
		super(configuration);
		this.functionService = configuration.getFunctionServiceProvider();
	}

	@RequestMapping("/function/list")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<FunctionInfo>> list() {
		return new JsonBean<>(functionService.list());
	}

	@RequestMapping("/function/get")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<FunctionInfo> get(String id) {
		return new JsonBean<>(functionService.get(id));
	}

	@RequestMapping("/function/backup/get")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<FunctionInfo> backups(String id, Long timestamp) {
		return new JsonBean<>(functionService.backupInfo(id, timestamp));
	}

	@RequestMapping("/function/backups")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<Long>> backups(String id) {
		return new JsonBean<>(functionService.backupList(id));
	}

	@RequestMapping("/function/move")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<Boolean> move(String id, String groupId) {
		isTrue(functionService.allowMove(id, groupId), NAME_CONFLICT);
		isTrue(configuration.getMagicFunctionManager().move(id, groupId), FUNCTION_PATH_CONFLICT);
		return new JsonBean<>(functionService.move(id, groupId));
	}


	@RequestMapping("/function/save")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<String> save(@RequestBody FunctionInfo functionInfo) {
		notBlank(functionInfo.getName(), FUNCTION_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(functionInfo.getName()), NAME_INVALID);
		notBlank(functionInfo.getPath(), FUNCTION_PATH_REQUIRED);
		notBlank(functionInfo.getScript(), SCRIPT_REQUIRED);
		isTrue(!configuration.getMagicFunctionManager().hasRegister(functionInfo), FUNCTION_PATH_CONFLICT);

		if (StringUtils.isBlank(functionInfo.getId())) {
			isTrue(!functionService.exists(functionInfo), FUNCTION_ALREADY_EXISTS.format(functionInfo.getPath()));
			isTrue(functionService.insert(functionInfo), FUNCTION_SAVE_FAILURE);
		} else {
			isTrue(!functionService.existsWithoutId(functionInfo), FUNCTION_ALREADY_EXISTS.format(functionInfo.getPath()));
			isTrue(functionService.update(functionInfo), FUNCTION_SAVE_FAILURE);
			functionService.backup(functionInfo);
		}
		configuration.getMagicFunctionManager().register(functionInfo);
		return new JsonBean<>(functionInfo.getId());
	}

	@RequestMapping("/function/delete")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.DELETE)
	public JsonBean<Boolean> delete(String id) {
		boolean success = functionService.delete(id);
		if (success) {
			configuration.getMagicFunctionManager().unregister(id);
		}
		return new JsonBean<>(success);
	}
}
