package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.Backup;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.JsonBean;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口相关操作
 *
 * @author mxd
 */
public class MagicAPIController extends MagicController implements MagicExceptionHandler {

	public MagicAPIController(MagicConfiguration configuration) {
		super(configuration);
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
		ApiInfo apiInfo = getApiInfo(id);
		isTrue(allowVisit(request, Authorization.DELETE, apiInfo), PERMISSION_INVALID);
		isTrue(!Constants.LOCK.equals(apiInfo.getLock()), RESOURCE_LOCKED);
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
	public JsonBean<List<Backup>> backupList(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.VIEW, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicBackupService.backupById(id)
				.stream()
				.sorted(Comparator.comparing(Backup::getCreateDate).reversed())
				.collect(Collectors.toList()));
	}

	/**
	 * 获取历史记录
	 *
	 * @param id        接口ID
	 * @param timestamp 时间点
	 */
	@RequestMapping("/backup/get")
	@ResponseBody
	public JsonBean<Backup> backups(HttpServletRequest request, String id, Long timestamp) {
		isTrue(allowVisit(request, Authorization.VIEW, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicBackupService.backupInfo(id, timestamp));
	}

	/**
	 * 移动接口
	 */
	@RequestMapping("/api/move")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> apiMove(HttpServletRequest request, String id, String groupId) {
		ApiInfo apiInfo = getApiInfo(id).copy();
		// 新的分组ID
		apiInfo.setGroupId(groupId);
		isTrue(allowVisit(request, Authorization.SAVE, apiInfo), PERMISSION_INVALID);
		isTrue(!Constants.LOCK.equals(apiInfo.getLock()), RESOURCE_LOCKED);
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
		if (StringUtils.isNotBlank(info.getId())) {
			ApiInfo oldInfo = getApiInfo(info.getId());
			isTrue(!Constants.LOCK.equals(oldInfo.getLock()), RESOURCE_LOCKED);
		}
		return new JsonBean<>(magicAPIService.saveApi(info));
	}

	/**
	 * 锁定接口
	 */
	@RequestMapping("/lock")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> lock(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.LOCK, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.lockApi(id));
	}

	/**
	 * 解锁接口
	 */
	@RequestMapping("/unlock")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> unlock(HttpServletRequest request, String id) {
		isTrue(allowVisit(request, Authorization.UNLOCK, getApiInfo(id)), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.unlockApi(id));
	}

	private ApiInfo getApiInfo(String id) {
		ApiInfo apiInfo = magicAPIService.getApiInfo(id);
		notNull(apiInfo, API_NOT_FOUND);
		return apiInfo;
	}
}
