package org.ssssssss.magicapi.adapter;

import org.springframework.jdbc.core.RowMapper;
import org.ssssssss.magicapi.provider.ColumnMapperProvider;

import java.util.HashMap;
import java.util.Map;

public class ColumnMapperAdapter {

	private Map<String, RowMapper<Map<String, Object>>> mappers = new HashMap<>();

	private RowMapper<Map<String, Object>> defaultMapper;

	public void add(ColumnMapperProvider columnMapperProvider) {
		mappers.put(columnMapperProvider.name(), columnMapperProvider.getColumnMapRowMapper());
	}

	public void setDefault(ColumnMapperProvider columnMapperProvider) {
		this.defaultMapper = columnMapperProvider.getColumnMapRowMapper();
		add(columnMapperProvider);
	}

	public void setDefault(String name) {
		this.defaultMapper = get(name);
	}

	public RowMapper<Map<String, Object>> getDefault() {
		return this.defaultMapper;
	}

	public RowMapper<Map<String, Object>> get(String name) {
		return mappers.getOrDefault(name, defaultMapper);
	}
}
