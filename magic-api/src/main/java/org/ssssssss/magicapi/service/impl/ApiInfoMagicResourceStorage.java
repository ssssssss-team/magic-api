package org.ssssssss.magicapi.service.impl;

import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.MagicResourceService;
import org.ssssssss.magicapi.utils.PathUtils;

public class ApiInfoMagicResourceStorage implements MagicResourceStorage<ApiInfo> {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "api";
	}

	@Override
	public String suffix() {
		return ".ms";
	}

	@Override
	public Class<ApiInfo> magicClass() {
		return ApiInfo.class;
	}

	@Override
	public boolean requirePath() {
		return true;
	}


	@Override
	public String buildMappingKey(ApiInfo info) {
		return info.getMethod().toUpperCase() + ":" + PathUtils.replaceSlash("/" + magicResourceService.getGroupPath(info.getGroupId()) + "/" + info.getPath());
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}
}
