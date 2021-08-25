package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.Backup;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.FunctionServiceProvider;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.MagicBackupService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class MagicFunctionController extends MagicController implements MagicExceptionHandler {


	public MagicFunctionController(MagicConfiguration configuration) {
		super(configuration);
	}

	@RequestMapping("/function/list")
	@ResponseBody
	public JsonBean<List<FunctionInfo>> list(HttpServletRequest request) {
		return new JsonBean<>(magicAPIService.functionList()
				.stream()
				.filter(it -> allowVisit(request, Authorization.VIEW, it))
				.collect(Collectors.toList())
		);
	}

	@RequestMapping("/function/get")
	@ResponseBody
	public JsonBean<FunctionInfo> get(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.VIEW, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.getFunctionInfo(id));
	}

	@RequestMapping("/function/backup/get")
	@ResponseBody
	public JsonBean<Backup> backups(HttpServletRequest request, String id, Long timestamp) {
		isTrue(allowVisit(request, Authorization.VIEW, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicBackupService.backupInfo(id, timestamp));
	}

	@RequestMapping("/function/backups")
	@ResponseBody
	public JsonBean<List<Backup>> backupList(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.VIEW, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicBackupService.backupById(id));
	}

	@RequestMapping("/function/move")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> move(HttpServletRequest request, String id, String groupId) {
		FunctionInfo functionInfo = getFunctionInfo(id);
		functionInfo.setGroupId(groupId);
		isTrue(allowVisit(request, Authorization.SAVE, functionInfo), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.moveFunction(id, groupId));
	}


	@RequestMapping("/function/save")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<String> save(HttpServletRequest request, @RequestBody FunctionInfo functionInfo) {
		isTrue(allowVisit(request, Authorization.SAVE, functionInfo), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.saveFunction(functionInfo));
	}

	@RequestMapping("/function/delete")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> delete(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.DELETE, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.deleteFunction(id));
	}

	public FunctionInfo getFunctionInfo(String id) {
		FunctionInfo functionInfo = magicAPIService.getFunctionInfo(id);
		notNull(functionInfo, FUNCTION_NOT_FOUND);
		return functionInfo;
	}
}
