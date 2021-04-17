package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.TreeNode;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.utils.IoUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MagicGroupController extends MagicController implements MagicExceptionHandler {

	private final GroupServiceProvider groupServiceProvider;

	public MagicGroupController(MagicConfiguration configuration) {
		super(configuration);
		this.groupServiceProvider = configuration.getGroupServiceProvider();
	}

	/**
	 * 删除分组
	 */
	@RequestMapping("/group/delete")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.DELETE)
	public JsonBean<Boolean> deleteGroup(String groupId) {
		boolean isApi = true;
		TreeNode<Group> treeNode = configuration.getGroupServiceProvider().apiGroupTree().findTreeNode(group -> group.getId().equals(groupId));
		if (treeNode == null) {
			treeNode = configuration.getGroupServiceProvider().functionGroupTree().findTreeNode(group -> group.getId().equals(groupId));
			notNull(treeNode, GROUP_NOT_FOUND);
			isApi = false;
		}
		List<String> children = treeNode.flat().stream().map(Group::getId).collect(Collectors.toList());
		boolean success;
		if (isApi) {
			// 删除接口
			if (success = configuration.getMagicApiService().deleteGroup(groupId, children)) {
				// 取消注册
				configuration.getMappingHandlerMapping().deleteGroup(children);
				children.forEach(configuration.getGroupServiceProvider()::delete);
				// 重新加载分组
				configuration.getMappingHandlerMapping().loadGroup();
			}
		} else {
			// 删除函数
			if (success = configuration.getFunctionServiceProvider().deleteGroup(groupId, children)) {
				// 取消注册
				configuration.getMagicFunctionManager().deleteGroup(children);
				children.forEach(configuration.getGroupServiceProvider()::delete);
				// 重新加载分组
				configuration.getMagicFunctionManager().loadGroup();
			}
		}
		return new JsonBean<>(success);
	}

	/**
	 * 修改分组
	 */
	@RequestMapping("/group/update")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public synchronized JsonBean<Boolean> groupUpdate(@RequestBody Group group) {
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		notBlank(group.getName(), GROUP_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(group.getName()), NAME_INVALID);

		notBlank(group.getType(), GROUP_TYPE_REQUIRED);
		boolean isApiGroup = Constants.GROUP_TYPE_API.equals(group.getType());
		boolean isFunctionGroup = Constants.GROUP_TYPE_FUNCTION.equals(group.getType());
		if (isApiGroup && configuration.getMappingHandlerMapping().checkGroup(group)) {
			isTrue(groupServiceProvider.update(group), GROUP_SAVE_FAILURE);
			// 如果数据库修改成功，则修改接口路径
			configuration.getMappingHandlerMapping().updateGroup(group);
			configuration.getMagicApiService().reload(group.getId());
			return new JsonBean<>(true);
		} else if (isFunctionGroup && configuration.getMagicFunctionManager().checkGroup(group)) {
			isTrue(groupServiceProvider.update(group), GROUP_SAVE_FAILURE);
			// 如果数据库修改成功，则修改接口路径
			configuration.getMagicFunctionManager().updateGroup(group);
			configuration.getFunctionServiceProvider().reload(group.getId());
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
		return new JsonBean<>(groupServiceProvider.groupList(type));
	}

	/**
	 * 创建分组
	 */
	@RequestMapping("/group/create")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<String> createGroup(@RequestBody Group group) {
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		notBlank(group.getName(), GROUP_NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(group.getName()), NAME_INVALID);
		notBlank(group.getType(), GROUP_TYPE_REQUIRED);
		isTrue(groupServiceProvider.insert(group), GROUP_SAVE_FAILURE);
		if (Objects.equals(group.getType(), Constants.GROUP_TYPE_API)) {
			configuration.getMappingHandlerMapping().loadGroup();
		} else {
			configuration.getMagicFunctionManager().loadGroup();
		}
		return new JsonBean<>(group.getId());
	}
}
