package org.ssssssss.magicapi.service.impl;

import org.ssssssss.magicapi.model.DataSourceInfo;
import org.ssssssss.magicapi.model.JsonCodeConstants;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.MagicResourceService;

public class DataSourceInfoMagicResourceStorage implements MagicResourceStorage<DataSourceInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "datasource";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<DataSourceInfo> magicClass() {
		return DataSourceInfo.class;
	}

	@Override
	public boolean requirePath() {
		return false;
	}

	@Override
	public boolean requiredScript() {
		return false;
	}

	@Override
	public boolean allowRoot() {
		return true;
	}

	@Override
	public String buildMappingKey(DataSourceInfo info) {
		return info.getKey();
	}

	@Override
	public void validate(DataSourceInfo entity) {
		notBlank(entity.getUrl(), DS_URL_REQUIRED);
		notBlank(entity.getKey(), DS_KEY_REQUIRED);
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}
}
