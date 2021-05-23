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

import java.util.List;

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
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<FunctionInfo>> list() {
		return new JsonBean<>(magicAPIService.functionList());
	}

	@RequestMapping("/function/get")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<FunctionInfo> get(String id) {
		return new JsonBean<>(magicAPIService.getFunctionInfo(id));
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
		return new JsonBean<>(magicAPIService.moveFunction(id, groupId));
	}


	@RequestMapping("/function/save")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<String> save(@RequestBody FunctionInfo functionInfo) {
		return new JsonBean<>(magicAPIService.saveFunction(functionInfo));
	}

	@RequestMapping("/function/delete")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.DELETE)
	public JsonBean<Boolean> delete(String id) {
		return new JsonBean<>(magicAPIService.deleteFunction(id));
	}
}
