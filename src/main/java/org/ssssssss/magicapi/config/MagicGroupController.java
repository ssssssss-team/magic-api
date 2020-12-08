package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.GroupServiceProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
		if (!allowVisit(request, RequestInterceptor.Authorization.DELETE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = configuration.getMagicApiService().deleteGroup(groupId);
			if (success) {    //删除成功时取消注册
				configuration.getMappingHandlerMapping().deleteGroup(groupId);
			}
			success = this.groupServiceProvider.delete(groupId);
			if(success){
				configuration.getMappingHandlerMapping().loadGroup();
			}
			// 删除分组
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
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		if (StringUtils.isBlank(group.getName())) {
			return new JsonBean<>(0, "分组名称不能为空");
		}
		if (StringUtils.isBlank(group.getType())) {
			return new JsonBean<>(0, "分组类型不能为空");
		}
		try {
			boolean isApiGroup = "1".equals(group.getType());
			if (!isApiGroup || configuration.getMappingHandlerMapping().checkGroup(group)) {
				boolean success = groupServiceProvider.update(group);
				if (success && isApiGroup) {    // 如果数据库修改成功，则修改接口路径
					configuration.getMappingHandlerMapping().updateGroup(group);
				}
				return new JsonBean<>(success);
			}
			return new JsonBean<>(-20, "修改分组后，接口路径会有冲突，请检查！");
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
	public JsonBean<List<Group>> groupList() {
		try {
			return new JsonBean<>(groupServiceProvider.groupList());
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
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行保存方法");
		}
		if (StringUtils.isBlank(group.getParentId())) {
			group.setParentId("0");
		}
		if (StringUtils.isBlank(group.getName())) {
			return new JsonBean<>(0, "分组名称不能为空");
		}
		if (StringUtils.isBlank(group.getType())) {
			return new JsonBean<>(0, "分组类型不能为空");
		}
		try {
			groupServiceProvider.insert(group);
			configuration.getMappingHandlerMapping().loadGroup();
			return new JsonBean<>(group.getId());
		} catch (Exception e) {
			logger.error("保存分组出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}
}
