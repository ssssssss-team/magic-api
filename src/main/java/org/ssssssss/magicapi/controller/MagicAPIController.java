package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.utils.IoUtils;

import java.util.List;
import java.util.Optional;

/**
 * 接口相关操作
 */
public class MagicAPIController extends MagicController implements MagicExceptionHandler {

	private final ApiServiceProvider magicApiService;

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
	@Valid(readonly = false, authorization = Authorization.DELETE)
	public JsonBean<Boolean> delete(String id) {
		boolean success = magicApiService.delete(id);
		if (success) {    //删除成功时在取消注册
			configuration.getMappingHandlerMapping().unregisterMapping(id, true);
		}
		return new JsonBean<>(success);
	}

	/**
	 * 查询所有接口
	 */
	@RequestMapping("/list")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<ApiInfo>> list() {
		return new JsonBean<>(magicApiService.list());
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
		return new JsonBean<>(magicApiService.get(id));
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
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<ApiInfo> backups(String id, Long timestamp) {
		return new JsonBean<>(magicApiService.backupInfo(id, timestamp));
	}

	/**
	 * 移动接口
	 */
	@RequestMapping("/api/move")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<Boolean> apiMove(String id, String groupId) {
		// 验证分组是否存在
		isTrue(configuration.getGroupServiceProvider().containsApiGroup(groupId), GROUP_NOT_FOUND);
		// 验证移动后名字是否有冲突
		isTrue(magicApiService.allowMove(id, groupId), NAME_CONFLICT);
		// 验证路径是否有冲突
		isTrue(configuration.getMappingHandlerMapping().move(id, groupId), REQUEST_PATH_CONFLICT);

		return new JsonBean<>(magicApiService.move(id, groupId));
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
		// 非空验证
		notBlank(info.getMethod(), REQUEST_METHOD_REQUIRED);
		notBlank(info.getPath(), REQUEST_PATH_REQUIRED);
		notBlank(info.getName(), API_NAME_REQUIRED);
		notBlank(info.getScript(), SCRIPT_REQUIRED);
		// 验证名字
		isTrue(IoUtils.validateFileName(info.getName()), NAME_INVALID);
		// 验证路径是否有冲突
		isTrue(!configuration.getMappingHandlerMapping().hasRegisterMapping(info), REQUEST_PATH_CONFLICT);
		if (StringUtils.isBlank(info.getId())) {
			// 先判断接口是否存在
			isTrue(!magicApiService.exists(info), API_ALREADY_EXISTS.format(info.getMethod(), info.getPath()));

			isTrue(magicApiService.insert(info), API_SAVE_FAILURE);
		} else {
			// 先判断接口是否存在
			isTrue(!magicApiService.existsWithoutId(info), API_ALREADY_EXISTS.format(info.getMethod(), info.getPath()));
			Optional<ApiInfo> optional = configuration.getMappingHandlerMapping().getApiInfos().stream()
					.filter(it -> it.getId().equals(info.getId()))
					.findFirst();
			if (optional.isPresent() && !optional.get().equals(info)) {
				isTrue(magicApiService.update(info), API_SAVE_FAILURE);
				magicApiService.backup(info);
			}
		}
		// 注册接口
		configuration.getMappingHandlerMapping().registerMapping(info, true);
		return new JsonBean<>(info.getId());
	}

}
