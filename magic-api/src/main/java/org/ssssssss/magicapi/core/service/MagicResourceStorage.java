package org.ssssssss.magicapi.core.service;

import org.ssssssss.magicapi.core.resource.Resource;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.nio.charset.StandardCharsets;

public interface MagicResourceStorage<T extends MagicEntity> {

	String separatorWithCRLF = "\r\n================================\r\n";

	String separatorWithLF = "\n================================\n";

	/**
	 * 文件夹名
	 */
	String folder();

	/**
	 * 允许的后缀，为空则不限制
	 */
	String suffix();

	Class<T> magicClass();

	/**
	 * 是否支持path
	 */
	boolean requirePath();

	default boolean requiredScript() {
		return true;
	}

	default boolean allowRoot() {
		return false;
	}

	default T read(byte[] bytes) {
		String content = new String(bytes, StandardCharsets.UTF_8);
		if (requiredScript()) {
			String separator = separatorWithCRLF;
			int index = content.indexOf(separator);
			if (index == -1) {
				separator = separatorWithLF;
				index = content.indexOf(separatorWithLF);
			}
			if (index > -1) {
				T info = JsonUtils.readValue(content.substring(0, index), magicClass());
				info.setScript(content.substring(index + separator.length()));
				return info;
			}
		}
		return JsonUtils.readValue(content, magicClass());
	}

	default byte[] write(MagicEntity entity) {
		entity = entity.copy();
		String script = entity.getScript();
		entity.setScript(null);
		return (JsonUtils.toJsonString(entity) + separatorWithCRLF + script).getBytes(StandardCharsets.UTF_8);

	}

	default T readResource(Resource resource) {
		return read(resource.read());
	}

	String buildMappingKey(T entity);

	default String buildKey(MagicEntity entity) {
		return buildMappingKey((T) entity);
	}

	/**
	 * 校验参数
	 */
	default void validate(T entity) {

	}

	void setMagicResourceService(MagicResourceService magicResourceService);
}
