package org.ssssssss.magicapi.modules.db.provider;

/**
 * 全大写命名
 *
 * @author mxd
 */
public class UpperColumnMapperProvider implements ColumnMapperProvider {

	@Override
	public String name() {
		return "upper";
	}

	@Override
	public String mapping(String columnName) {
		return columnName == null ? null : columnName.toUpperCase();
	}

}
