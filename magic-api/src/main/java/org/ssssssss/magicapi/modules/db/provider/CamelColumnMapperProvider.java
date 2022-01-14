package org.ssssssss.magicapi.modules.db.provider;

/**
 * 驼峰命名转换
 *
 * @author mxd
 */
public class CamelColumnMapperProvider implements ColumnMapperProvider {

	@Override
	public String name() {
		return "camel";
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
			} else if (upperCase) {
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
			if (Character.isUpperCase(ch)) {
				sb.append("_");
			}
			sb.append(Character.toLowerCase(ch));
		}
		return sb.toString();
	}
}
