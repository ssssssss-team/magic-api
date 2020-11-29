package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.ApiServiceProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 接口相关操作
 */
public class MagicAPIController extends MagicController {

	private static Logger logger = LoggerFactory.getLogger(MagicAPIController.class);

	private ApiServiceProvider magicApiService;

	public MagicAPIController(MagicConfiguration configuration) {
		super(configuration);
		this.magicApiService = configuration.getMagicApiService();
	}

	/**
	 * 删除接口
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/delete")
	@ResponseBody
	public JsonBean<Boolean> delete(String id, HttpServletRequest request) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DELETE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = magicApiService.delete(id);
			if (success) {    //删除成功时在取消注册
				configuration.getMappingHandlerMapping().unregisterMapping(id, true);
			}
			return new JsonBean<>(success);
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 查询所有接口
	 */
	@RequestMapping("/list")
	@ResponseBody
	public JsonBean<List<ApiInfo>> list() {
		try {
			return new JsonBean<>(magicApiService.list());
		} catch (Exception e) {
			logger.error("查询接口列表失败", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 查询接口详情
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/get")
	@ResponseBody
	public JsonBean<ApiInfo> get(HttpServletRequest request, String id) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DETAIL)) {
			return new JsonBean<>(-10, "无权限执行查看详情方法");
		}
		try {
			return new JsonBean<>(magicApiService.get(id));
		} catch (Exception e) {
			logger.error("查询接口出错");
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 查询历史记录
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/backups")
	@ResponseBody
	public JsonBean<List<Long>> backups(String id) {
		return new JsonBean<>(magicApiService.backupList(id));
	}

	/**
	 * 获取历史记录
	 *
	 * @param id        接口ID
	 * @param timestamp 时间点
	 */
	@RequestMapping("/backup/get")
	@ResponseBody
	public JsonBean<ApiInfo> backups(String id, Long timestamp) {
		return new JsonBean<>(magicApiService.backupInfo(id, timestamp));
	}

	/**
	 * 移动接口
	 */
	@RequestMapping("/api/move")
	@ResponseBody
	public JsonBean<Boolean> apiMove(String id, String groupId, HttpServletRequest request) {
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行保存方法");
		}
		if (!configuration.getGroupServiceProvider().contains(groupId)) {
			return new JsonBean<>(0, "找不到分组信息");
		}
		try {
			if (!configuration.getMappingHandlerMapping().move(id, groupId)) {
				return new JsonBean<>(0, "移动接口失败！");
			} else {
				return new JsonBean<>(magicApiService.move(id, groupId));
			}
		} catch (Exception e) {
			logger.error("移动接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 保存接口
	 *
	 * @param info 接口信息
	 */
	@RequestMapping("/save")
	@ResponseBody
	public JsonBean<String> save(HttpServletRequest request, ApiInfo info) {
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行保存方法");
		}
		try {
			if (StringUtils.isBlank(info.getMethod())) {
				return new JsonBean<>(0, "请求方法不能为空");
			}
			if (StringUtils.isBlank(info.getPath())) {
				return new JsonBean<>(0, "请求路径不能为空");
			}
			if (StringUtils.isBlank(info.getName())) {
				return new JsonBean<>(0, "接口名称不能为空");
			}
			if (StringUtils.isBlank(info.getScript())) {
				return new JsonBean<>(0, "脚本内容不能为空");
			}
			if (configuration.getMappingHandlerMapping().hasRegisterMapping(info)) {
				return new JsonBean<>(0, "该路径已被映射,请换一个请求方法或路径");
			}
			if (StringUtils.isBlank(info.getId())) {
				// 先判断接口是否存在
				if (magicApiService.exists(info.getGroupId(), info.getMethod(), info.getPath())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.insert(info);
			} else {
				// 先判断接口是否存在
				if (magicApiService.existsWithoutId(info.getGroupId(), info.getMethod(), info.getPath(), info.getId())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.update(info);
			}
			magicApiService.backup(info.getId());
			// 注册接口
			configuration.getMappingHandlerMapping().registerMapping(info, true);
			return new JsonBean<>(info.getId());
		} catch (Exception e) {
			logger.error("保存接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

}
