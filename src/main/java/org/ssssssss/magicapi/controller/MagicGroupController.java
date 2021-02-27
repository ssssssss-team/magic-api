package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.TreeNode;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.utils.IoUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MagicGroupController extends MagicController {

	private static Logger logger = LoggerFactory.getLogger(MagicGroupController.class);

	private GroupServiceProvider groupServiceProvider;

	public MagicGroupController(MagicConfiguration configuration) {
		super(configuration);
		this.groupServiceProvider = configuration.getGroupServiceProvider();
	}

	/**
	 * 删除分组
	 */
	@RequestMapping("/group/delete")
	@ResponseBody
	public JsonBean<Boolean> deleteGroup(String groupId, HttpServletRequest request) {
		if(configuration.getWorkspace().readonly()){
			return new JsonBean<>(0, "当前为只读模式,无法删除");
		}
		if (!allowVisit(request, RequestInterceptor.Authorization.DELETE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean isApi = true;
			TreeNode<Group> treeNode = configuration.getGroupServiceProvider().apiGroupTree().findTreeNode(group -> group.getId().equals(groupId));
			if (treeNode == null) {
				treeNode = configuration.getGroupServiceProvider().functionGroupTree().findTreeNode(group -> group.getId().equals(groupId));
				if (treeNode == null) {
					return new JsonBean<>(0, "分组不存在!");
				}
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
		} catch (Exception e) {
			logger.error("删除分组出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 修改分组
	 */
	@RequestMapping("/group/update")
	@ResponseBody
	public synchronized JsonBean<Boolean> groupUpdate(Group group, HttpServletRequest request) {
		if(configuration.getWorkspace().readonly()){
			return new JsonBean<>(0, "当前为只读模式,无法修改");
		}
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		if (StringUtils.isBlank(group.getName())) {
			return new JsonBean<>(0, "分组名称不能为空");
		}
		if (!IoUtils.validateFileName(group.getName())) {
			return new JsonBean<>(0, "分组名称不能包含特殊字符，只允许中文、数字、字母以及_组合");
		}
		if (StringUtils.isBlank(group.getType())) {
			return new JsonBean<>(0, "分组类型不能为空");
		}
		if (groupServiceProvider.exists(group)) {
			return new JsonBean<>(-20, "修改分组后，名称会有冲突，请检查！");
		}
		try {
			boolean isApiGroup = "1".equals(group.getType());
			boolean isFunctionGroup = "2".equals(group.getType());
			if (isApiGroup && configuration.getMappingHandlerMapping().checkGroup(group)) {
				if (groupServiceProvider.update(group)) {    // 如果数据库修改成功，则修改接口路径
					configuration.getMappingHandlerMapping().updateGroup(group);
				}else{
					return new JsonBean<>(0, "修改分组失败,同一组下分组名称不能重复且不能包含特殊字符。");
				}
				return new JsonBean<>(true);
			} else if (isFunctionGroup && configuration.getMagicFunctionManager().checkGroup(group)) {
				if (groupServiceProvider.update(group)) {    // 如果数据库修改成功，则修改接口路径
					configuration.getMagicFunctionManager().updateGroup(group);
				}else{
					return new JsonBean<>(0, "修改分组失败,同一组下分组名称不能重复且不能包含特殊字符。");
				}
				return new JsonBean<>(true);
			}
			return new JsonBean<>(-20, "修改分组后，路径会有冲突，请检查！");
		} catch (Exception e) {
			logger.error("修改分组出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 查询所有分组
	 */
	@RequestMapping("/group/list")
	@ResponseBody
	public JsonBean<List<Group>> groupList(String type) {
		try {
			return new JsonBean<>(groupServiceProvider.groupList(type));
		} catch (Exception e) {
			logger.error("查询分组列表失败", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 创建分组
	 */
	@RequestMapping("/group/create")
	@ResponseBody
	public JsonBean<String> createGroup(Group group, HttpServletRequest request) {
		if(configuration.getWorkspace().readonly()){
			return new JsonBean<>(0, "当前为只读模式,无法创建");
		}
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行保存方法");
		}
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		if (StringUtils.isBlank(group.getName())) {
			return new JsonBean<>(0, "分组名称不能为空");
		}
		if (!IoUtils.validateFileName(group.getName())) {
			return new JsonBean<>(0, "分组名称不能包含特殊字符，只允许中文、数字、字母以及_组合");
		}
		if (StringUtils.isBlank(group.getType())) {
			return new JsonBean<>(0, "分组类型不能为空");
		}
		try {
			if(groupServiceProvider.insert(group)){
				if (Objects.equals(group.getType(), "1")) {
					configuration.getMappingHandlerMapping().loadGroup();
				} else {
					configuration.getMagicFunctionManager().loadGroup();
				}
				return new JsonBean<>(group.getId());
			}else {
				return new JsonBean<>(-1,"保存分组失败,同一组下分组名称不能重复且不能包含特殊字符。");
			}
		} catch (Exception e) {
			logger.error("保存分组出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}
}
