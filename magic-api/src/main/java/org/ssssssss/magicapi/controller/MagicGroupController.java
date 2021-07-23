package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.MagicAPIService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class MagicGroupController extends MagicController implements MagicExceptionHandler {

	public MagicGroupController(MagicConfiguration configuration) {
		super(configuration);
	}

	/**
	 * 删除分组
	 */
	@RequestMapping("/group/delete")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<Boolean> deleteGroup(HttpServletRequest request, String groupId) {
		Group group = magicAPIService.getGroup(groupId);
		notNull(group, GROUP_NOT_FOUND);
		isTrue(allowVisit(request, Authorization.DELETE, group), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.deleteGroup(groupId));
	}

	/**
	 * 修改分组
	 */
	@RequestMapping("/group/update")
	@ResponseBody
	@Valid(readonly = false)
	public synchronized JsonBean<Boolean> groupUpdate(HttpServletRequest request, @RequestBody Group group) {
		isTrue(allowVisit(request, Authorization.SAVE, group), PERMISSION_INVALID);
		if (magicAPIService.updateGroup(group)) {
			return new JsonBean<>(true);
		}
		return new JsonBean<>(GROUP_CONFLICT);
	}

	/**
	 * 查询所有分组
	 */
	@RequestMapping("/group/list")
	@ResponseBody
	public JsonBean<List<Group>> groupList(HttpServletRequest request, String type) {
		return new JsonBean<>(magicAPIService.groupList(type)
				.stream()
				.filter(it -> allowVisit(request, Authorization.VIEW, it))
				.collect(Collectors.toList())
		);
	}

	/**
	 * 创建分组
	 */
	@RequestMapping("/group/create")
	@ResponseBody
	@Valid(readonly = false)
	public JsonBean<String> createGroup(HttpServletRequest request, @RequestBody Group group) {
		isTrue(allowVisit(request, Authorization.SAVE, group), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.createGroup(group));
	}
}
