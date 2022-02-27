package org.ssssssss.magicapi.datasource.service;

import org.ssssssss.magicapi.datasource.model.DataSourceInfo;
import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.util.Objects;

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
		isTrue(IoUtils.validateFileName(entity.getKey()), DATASOURCE_KEY_INVALID);
		boolean noneMatchKey = magicResourceService.listFiles("datasource:0").stream()
				.map(it -> (DataSourceInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, DS_KEY_CONFLICT);
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public DataSourceInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, DataSourceInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
