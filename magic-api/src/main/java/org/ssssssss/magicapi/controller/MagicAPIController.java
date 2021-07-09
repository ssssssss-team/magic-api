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

import javax.servlet.http.HttpServletRequest;
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
	@Valid(readonly = false)
	public JsonBean<Boolean> delete(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.VIEW, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.deleteApi(id));
	}

	/**
	 * 查询所有接口
	 */
	@RequestMapping("/list")
	@ResponseBody
	public JsonBean<List<ApiInfo>> list(HttpServletRequest request) {
		return new JsonBean<>(magicAPIService.apiList()
				.stream()
				.filter(it -> allowVisit(request, Authorization.VIEW, it))
				.map(ApiInfo::simple)
				.collect(Collectors.toList())
		);
	}

	/**
	 * 查询接口详情
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/get")
	@ResponseBody
	public JsonBean<ApiInfo> get(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.VIEW, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.getApiInfo(id));
	}

	/**
	 * 查询历史记录
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/backups")
	@ResponseBody
	public JsonBean<List<Long>> backups(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.VIEW, getApiInfo(id)), PERMISSION_INVALID);
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
	public JsonBean<ApiInfo> backups(HttpServletRequest request, String id, Long timestamp) {
		isTrue(allowVisit(request, Authorization.VIEW, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(apiServiceProvider.backupInfo(id, timestamp));
	}

	/**
	 * 移动接口
	 */
	@RequestMapping("/api/move")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> apiMove(HttpServletRequest request, String id, String groupId) {
		isTrue(allowVisit(request, Authorization.SAVE, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.moveApi(id, groupId));
	}

	/**
	 * 保存接口
	 */
	@RequestMapping("/save")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<String> save(HttpServletRequest request, @RequestBody ApiInfo info) {
		isTrue(allowVisit(request, Authorization.SAVE, info), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.saveApi(info));
	}

	private ApiInfo getApiInfo(String id){
		ApiInfo apiInfo = magicAPIService.getApiInfo(id);
		notNull(apiInfo, API_NOT_FOUND);
		return apiInfo;
	}
}
