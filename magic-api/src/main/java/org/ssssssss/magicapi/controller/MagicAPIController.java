package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.MagicAPIService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口相关操作
 */
public class MagicAPIController extends MagicController implements MagicExceptionHandler {

	private final ApiServiceProvider apiServiceProvider;

	private final MagicAPIService magicAPIService;

	public MagicAPIController(MagicConfiguration configuration) {
		super(configuration);
		this.apiServiceProvider = configuration.getApiServiceProvider();
		this.magicAPIService = configuration.getMagicAPIService();
	}

	/**
	 * 删除接口
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/delete")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.DELETE)
	public JsonBean<Boolean> delete(String id) {
		return new JsonBean<>(magicAPIService.deleteApi(id));
	}

	/**
	 * 查询所有接口
	 */
	@RequestMapping("/list")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<ApiInfo>> list() {
		return new JsonBean<>(magicAPIService.apiList().stream().map(ApiInfo::simple).collect(Collectors.toList()));
	}

	/**
	 * 查询接口详情
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/get")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<ApiInfo> get(String id) {
		return new JsonBean<>(magicAPIService.getApiInfo(id));
	}

	/**
	 * 查询历史记录
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/backups")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<Long>> backups(String id) {
		return new JsonBean<>(apiServiceProvider.backupList(id));
	}

	/**
	 * 获取历史记录
	 *
	 * @param id        接口ID
	 * @param timestamp 时间点
	 */
	@RequestMapping("/backup/get")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<ApiInfo> backups(String id, Long timestamp) {
		return new JsonBean<>(apiServiceProvider.backupInfo(id, timestamp));
	}

	/**
	 * 移动接口
	 */
	@RequestMapping("/api/move")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<Boolean> apiMove(String id, String groupId) {
		return new JsonBean<>(magicAPIService.moveApi(id, groupId));
	}

	/**
	 * 保存接口
	 *
	 * @param info 接口信息
	 */
	@RequestMapping("/save")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<String> save(@RequestBody ApiInfo info) {
		return new JsonBean<>(magicAPIService.saveApi(info));
	}

}
