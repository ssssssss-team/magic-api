package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.provider.ColumnMapperProvider;

/**
 * 帕斯卡命名转换
 */
public class PascalColumnMapperProvider implements ColumnMapperProvider {

	@Override
	public String name() {
		return "pascal";
	}

	@Override
	public String mapping(String columnName) {
		if (columnName == null || !columnName.contains("_")) {
			return columnName;
		}
		columnName = columnName.toLowerCase();
		boolean upperCase = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < columnName.length(); i++) {
			char ch = columnName.charAt(i);
			if (ch == '_') {
				upperCase = true;
			} else if (upperCase || i == 0) {
				sb.append(Character.toUpperCase(ch));
				upperCase = false;
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
}
