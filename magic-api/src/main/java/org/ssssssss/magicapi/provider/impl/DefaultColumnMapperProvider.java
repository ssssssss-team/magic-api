package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.provider.ColumnMapperProvider;

/**
 * 默认命名（保持原样）
 */
public class DefaultColumnMapperProvider implements ColumnMapperProvider {

	@Override
	public String name() {
		return "default";
	}

	@Override
	public String mapping(String columnName) {
		return columnName;
	}
}
