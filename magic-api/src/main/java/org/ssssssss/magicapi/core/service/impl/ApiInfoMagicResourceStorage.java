package org.ssssssss.magicapi.core.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.core.model.ApiInfo;
import org.ssssssss.magicapi.core.service.AbstractPathMagicResourceStorage;
import org.ssssssss.magicapi.utils.PathUtils;

public class ApiInfoMagicResourceStorage extends AbstractPathMagicResourceStorage<ApiInfo> {

	private String prefix;

	public ApiInfoMagicResourceStorage(String prefix) {
		this.prefix = StringUtils.defaultIfBlank(prefix, "") + "/";
	}

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
		return info.getMethod().toUpperCase() + ":" + PathUtils.replaceSlash(this.prefix + buildMappingKey(info, magicResourceService.getGroupPath(info.getGroupId())));
	}

	@Override
	public void validate(ApiInfo entity) {
		notBlank(entity.getMethod(), REQUEST_METHOD_REQUIRED);
		super.validate(entity);
	}
}
