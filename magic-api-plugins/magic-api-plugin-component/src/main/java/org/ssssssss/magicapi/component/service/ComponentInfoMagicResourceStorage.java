package org.ssssssss.magicapi.component.service;

import org.ssssssss.magicapi.component.model.ComponentInfo;
import org.ssssssss.magicapi.core.exception.InvalidArgumentException;
import org.ssssssss.magicapi.core.model.JsonCode;
import org.ssssssss.magicapi.core.service.AbstractPathMagicResourceStorage;

import java.util.UUID;

public class ComponentInfoMagicResourceStorage extends AbstractPathMagicResourceStorage<ComponentInfo> {

	@Override
	public String folder() {
		return "component";
	}

	@Override
	public Class<ComponentInfo> magicClass() {
		return ComponentInfo.class;
	}

	@Override
	public void validate(ComponentInfo entity) {
	}

	@Override
	public String buildMappingKey(ComponentInfo info) {
		return buildMappingKey(info, magicResourceService.getGroupPath(info.getGroupId()));
	}

	@Override
	public boolean requirePath() {
		return false;
	}

}
