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

import java.util.List;

public class MagicGroupController extends MagicController implements MagicExceptionHandler {

	private final MagicAPIService magicAPIService;

	public MagicGroupController(MagicConfiguration configuration) {
		super(configuration);
		this.magicAPIService = configuration.getMagicAPIService();
	}

	/**
	 * 删除分组
	 */
	@RequestMapping("/group/delete")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.DELETE)
	public JsonBean<Boolean> deleteGroup(String groupId) {
		return new JsonBean<>(magicAPIService.deleteGroup(groupId));
	}

	/**
	 * 修改分组
	 */
	@RequestMapping("/group/update")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public synchronized JsonBean<Boolean> groupUpdate(@RequestBody Group group) {
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
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<Group>> groupList(String type) {
		return new JsonBean<>(magicAPIService.groupList(type));
	}

	/**
	 * 创建分组
	 */
	@RequestMapping("/group/create")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<String> createGroup(@RequestBody Group group) {
		return new JsonBean<>(magicAPIService.createGroup(group));
	}
}
