package org.ssssssss.magicapi.modules.db.provider;

/**
 * 默认命名（保持原样）
 *
 * @author mxd
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
