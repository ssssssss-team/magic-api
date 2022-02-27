package org.ssssssss.magicapi.modules.db.provider;

/**
 * 帕斯卡命名转换
 *
 * @author mxd
 */
public class PascalColumnMapperProvider implements ColumnMapperProvider {

	@Override
	public String name() {
		return "pascal";
	}

	@Override
	public String mapping(String columnName) {
		if (columnName == null) {
			return null;
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

	@Override
	public String unmapping(String name) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (i > 0 && Character.isUpperCase(ch)) {
				sb.append("_");
			}
			sb.append(Character.toLowerCase(ch));
		}
		return sb.toString();
	}
}
