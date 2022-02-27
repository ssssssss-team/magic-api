package org.ssssssss.magicapi.function.service;

import org.ssssssss.magicapi.function.model.FunctionInfo;
import org.ssssssss.magicapi.core.service.AbstractPathMagicResourceStorage;

public class FunctionInfoMagicResourceStorage extends AbstractPathMagicResourceStorage<FunctionInfo> {

	@Override
	public String folder() {
		return "function";
	}

	@Override
	public Class<FunctionInfo> magicClass() {
		return FunctionInfo.class;
	}

	@Override
	public String buildMappingKey(FunctionInfo info) {
		return buildMappingKey(info, magicResourceService.getGroupPath(info.getGroupId()));
	}

	@Override
	public void validate(FunctionInfo entity) {
		notBlank(entity.getPath(), FUNCTION_PATH_REQUIRED);
	}
}
