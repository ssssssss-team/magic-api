package org.ssssssss.magicapi.service.impl;

import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.MagicResourceService;
import org.ssssssss.magicapi.utils.PathUtils;

public class FunctionInfoMagicResourceStorage implements MagicResourceStorage<FunctionInfo> {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "function";
	}

	@Override
	public String suffix() {
		return ".ms";
	}

	@Override
	public Class<FunctionInfo> magicClass() {
		return FunctionInfo.class;
	}

	@Override
	public boolean requirePath() {
		return true;
	}


	@Override
	public String buildMappingKey(FunctionInfo info) {
		return PathUtils.replaceSlash(magicResourceService.getGroupPath(info.getGroupId()) + "/" + info.getPath());
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}
}
