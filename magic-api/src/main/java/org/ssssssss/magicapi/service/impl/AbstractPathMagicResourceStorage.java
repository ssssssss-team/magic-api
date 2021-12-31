package org.ssssssss.magicapi.service.impl;

import org.ssssssss.magicapi.model.PathMagicEntity;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.MagicResourceService;
import org.ssssssss.magicapi.utils.PathUtils;

public abstract class AbstractPathMagicResourceStorage<T extends PathMagicEntity> implements MagicResourceStorage<T> {


	protected MagicResourceService magicResourceService;

	@Override
	public String suffix() {
		return ".ms";
	}

	@Override
	public boolean requirePath() {
		return true;
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public String buildScriptName(T entity) {
		String fullGroupName = magicResourceService.getGroupName(entity.getGroupId());
		String fullGroupPath = magicResourceService.getGroupPath(entity.getGroupId());
		return PathUtils.replaceSlash(String.format("/%s/%s(/%s/%s)", fullGroupName, entity.getName(), fullGroupPath, entity.getPath()));
	}
}
