package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.provider.ColumnMapperProvider;

/**
 * 全小写命名
 */
public class LowerColumnMapperProvider implements ColumnMapperProvider {

	@Override
	public String name() {
		return "lower";
	}

	@Override
	public String mapping(String columnName) {
		return columnName.toLowerCase();
	}
}
