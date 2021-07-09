package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.FunctionServiceProvider;
import org.ssssssss.magicapi.provider.MagicAPIService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class MagicFunctionController extends MagicController implements MagicExceptionHandler {

	private final FunctionServiceProvider functionService;

	private final MagicAPIService magicAPIService;

	public MagicFunctionController(MagicConfiguration configuration) {
		super(configuration);
		this.functionService = configuration.getFunctionServiceProvider();
		this.magicAPIService = configuration.getMagicAPIService();
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
	public JsonBean<FunctionInfo> backups(HttpServletRequest request, String id, Long timestamp) {
		isTrue(allowVisit(request, Authorization.VIEW, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(functionService.backupInfo(id, timestamp));
	}

	@RequestMapping("/function/backups")
	@ResponseBody
	public JsonBean<List<Long>> backups(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.VIEW, getFunctionInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(functionService.backupList(id));
	}

	@RequestMapping("/function/move")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> move(HttpServletRequest request, String id, String groupId) {
		isTrue(allowVisit(request, Authorization.SAVE, getFunctionInfo(id)), PERMISSION_INVALID);
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
	
	public FunctionInfo getFunctionInfo(String id){
		FunctionInfo functionInfo = magicAPIService.getFunctionInfo(id);
		notNull(functionInfo, FUNCTION_NOT_FOUND);
		return functionInfo;
	}
}
