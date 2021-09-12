package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.Backup;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.JsonBean;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
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
		return new JsonBean<>(magicBackupService.backupById(id)
				.stream()
				.sorted(Comparator.comparing(Backup::getCreateDate).reversed())
				.collect(Collectors.toList()));
	}

	@RequestMapping("/function/move")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> move(HttpServletRequest request, String id, String groupId) {
		FunctionInfo functionInfo = getFunctionInfo(id);
		functionInfo.setGroupId(groupId);
		isTrue(allowVisit(request, Authorization.SAVE, functionInfo), PERMISSION_INVALID);
		isTrue(!Constants.LOCK.equals(functionInfo.getLock()), RESOURCE_LOCKED);
		return new JsonBean<>(magicAPIService.moveFunction(id, groupId));
	}


	@RequestMapping("/function/save")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<String> save(HttpServletRequest request, @RequestBody FunctionInfo functionInfo) {
		isTrue(allowVisit(request, Authorization.SAVE, functionInfo), PERMISSION_INVALID);
		if (StringUtils.isNotBlank(functionInfo.getId())) {
			FunctionInfo oldInfo = getFunctionInfo(functionInfo.getId());
			isTrue(!Constants.LOCK.equals(oldInfo.getLock()), RESOURCE_LOCKED);
		}
		return new JsonBean<>(magicAPIService.saveFunction(functionInfo));
	}

	@RequestMapping("/function/delete")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> delete(HttpServletRequest request, String id) {
		FunctionInfo info = getFunctionInfo(id);
		isTrue(allowVisit(request, Authorization.DELETE, info), PERMISSION_INVALID);
		isTrue(!Constants.LOCK.equals(info.getLock()), RESOURCE_LOCKED);
		return new JsonBean<>(magicAPIService.deleteFunction(id));
	}

	/**
	 * 锁定函数
	 */
	@RequestMapping("/function/lock")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> lock(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.LOCK, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.lockFunction(id));
	}

	/**
	 * 解锁函数
	 */
	@RequestMapping("/function/unlock")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> unlock(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.UNLOCK, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.unlockFunction(id));
	}

	public FunctionInfo getFunctionInfo(String id) {
		FunctionInfo functionInfo = magicAPIService.getFunctionInfo(id);
		notNull(functionInfo, FUNCTION_NOT_FOUND);
		return functionInfo;
	}
}
