package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.provider.ColumnMapperProvider;

/**
 * 全大写命名
 */
public class UpperColumnMapperProvider implements ColumnMapperProvider {

	@Override
	public String name() {
		return "upper";
	}

	@Override
	public String mapping(String columnName) {
		return columnName.toUpperCase();
	}

}
