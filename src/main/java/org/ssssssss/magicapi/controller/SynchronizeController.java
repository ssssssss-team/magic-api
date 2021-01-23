package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.SyncConfig;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.SynchronizeRequest;
import org.ssssssss.magicapi.model.SynchronizeResponse;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.GroupServiceProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SynchronizeController extends MagicController {

	private SyncConfig syncConfig;

	private ApiServiceProvider agiApiService;

	private GroupServiceProvider groupService;


	public SynchronizeController(MagicConfiguration configuration) {
		super(configuration);
		this.syncConfig = configuration.getSyncConfig();
		this.agiApiService = configuration.getMagicApiService();
		this.groupService = configuration.getGroupServiceProvider();
	}

	private boolean validateSecret(String secret) {
		return StringUtils.isNotBlank(syncConfig.getSecret()) && Objects.equals(secret, syncConfig.getSecret());
	}

	@RequestMapping("/_synchronize")
	@ResponseBody
	public JsonBean<SynchronizeResponse> doSynchronize(@RequestBody SynchronizeRequest synchronizeRequest) {
		if (!validateSecret(synchronizeRequest.getSecret())) {
			return new JsonBean<>(-100, "秘钥不正确");
		}
		// 查询该环境下的信息
		List<SynchronizeRequest.Info> oldInfos = agiApiService.listForSync(synchronizeRequest.getGroupId(), synchronizeRequest.getApiId());
		List<SynchronizeRequest.Info> newInfos = synchronizeRequest.getInfos();
		oldInfos.forEach(it -> it.setGroupPath(groupService.getFullPath(it.getGroupId())));
		// 对比差异
		Map<String, SynchronizeRequest.Info> oldInfoMap = oldInfos.stream().collect(Collectors.toMap(SynchronizeRequest.Info::getId, it -> it));
		Map<String, SynchronizeRequest.Info> newInfoMap = newInfos.stream().collect(Collectors.toMap(SynchronizeRequest.Info::getId, it -> it));
		SynchronizeResponse response = new SynchronizeResponse();
		newInfos.forEach(info -> {
			SynchronizeRequest.Info oldInfo = oldInfoMap.get(info.getId());
			if (oldInfo == null) {
				// 如果找不到，则是新增
				response.addAdded(info);
			} else if (!Objects.equals(info.getUpdateTime(), oldInfo.getUpdateTime())) {
				// 修改时间不同，则是修改
				response.addUpdated(oldInfo);
			}
		});
		// 找出删除项
		oldInfos.stream().filter(it -> !newInfoMap.containsKey(it.getId())).forEach(response::addRemoved);
		return new JsonBean<>(response);
	}


//	@RequestMapping("/_synchronize/pull")
//	@ResponseBody
//	public JsonBean<Void> doPull(SynchronizeRequest synchronizeRequest) {
//		if (!validateSecret(synchronizeRequest)) {
//			return new JsonBean<>(-100, "秘钥不正确");
//		}
//		return null;
//	}
//
//	@RequestMapping("/_synchronize/push")
//	@ResponseBody
//	public JsonBean<Void> doPush(SynchronizeRequest synchronizeRequest) {
//		if (!validateSecret(synchronizeRequest)) {
//			return new JsonBean<>(-100, "秘钥不正确");
//		}
//		return null;
//	}


}
