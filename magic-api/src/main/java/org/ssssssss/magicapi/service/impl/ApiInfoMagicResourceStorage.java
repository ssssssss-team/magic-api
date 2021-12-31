package org.ssssssss.magicapi.service.impl;

import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.utils.PathUtils;

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
		return info.getMethod().toUpperCase() + ":" + PathUtils.replaceSlash("/" + magicResourceService.getGroupPath(info.getGroupId()) + "/" + info.getPath());
	}
}
