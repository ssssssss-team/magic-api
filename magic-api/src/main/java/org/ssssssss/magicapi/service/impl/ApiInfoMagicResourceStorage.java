package org.ssssssss.magicapi.service.impl;

import org.ssssssss.magicapi.model.ApiInfo;

public class ApiInfoMagicResourceStorage extends AbstractPathMagicResourceStorage<ApiInfo> {

	@Override
	public String folder() {
		return "api";
	}

	@Override
	public Class<ApiInfo> magicClass() {
		return ApiInfo.class;
	}

	@Override
	public String buildMappingKey(ApiInfo info) {
		return info.getMethod().toUpperCase() + ":" + buildMappingKey(info, magicResourceService.getGroupPath(info.getGroupId()));
	}

}
