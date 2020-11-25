package org.ssssssss.magicapi.provider;

import org.springframework.jdbc.core.ColumnMapRowMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ColumnMapperProvider {

	String name();

	String mapping(String columnName);

	default ColumnMapRowMapper getColumnMapRowMapper(){
		return new ColumnMapRowMapper(){
			@Override
			protected Map<String, Object> createColumnMap(int columnCount) {
				return new LinkedHashMap<>(columnCount);
			}

			@Override
			protected String getColumnKey(String columnName) {
				return mapping(columnName);
			}
		};
	}

}
