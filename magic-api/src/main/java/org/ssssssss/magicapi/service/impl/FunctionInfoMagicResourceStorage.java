package org.ssssssss.magicapi.service.impl;

import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.utils.PathUtils;

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
		return PathUtils.replaceSlash("/" + magicResourceService.getGroupPath(info.getGroupId()) + "/" + info.getPath());
	}
}
