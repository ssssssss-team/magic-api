package org.ssssssss.magicapi.modules.db;

import org.springframework.jdbc.core.RowMapper;
import org.ssssssss.magicapi.modules.db.provider.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 列名转换适配器
 *
 * @author mxd
 */
public class ColumnMapperAdapter {

	private final Map<String, RowMapper<Map<String, Object>>> columnMapRowMappers = new HashMap<>();

	private final Map<String, Function<String, String>> rowMapColumnMappers = new HashMap<>();

	private RowMapper<Map<String, Object>> mapRowColumnMapper;

	private Function<String, String> rowMapColumnMapper;

	public ColumnMapperAdapter() {
		setDefault(new DefaultColumnMapperProvider());
		add(new CamelColumnMapperProvider());
		add(new PascalColumnMapperProvider());
		add(new LowerColumnMapperProvider());
		add(new UpperColumnMapperProvider());
	}

	public void add(ColumnMapperProvider columnMapperProvider) {
		columnMapRowMappers.put(columnMapperProvider.name(), columnMapperProvider.getColumnMapRowMapper());
		rowMapColumnMappers.put(columnMapperProvider.name(), columnMapperProvider.getRowMapColumnMapper());
	}

	public void setDefault(ColumnMapperProvider columnMapperProvider) {
		this.mapRowColumnMapper = columnMapperProvider.getColumnMapRowMapper();
		this.rowMapColumnMapper = columnMapperProvider.getRowMapColumnMapper();
		add(columnMapperProvider);
	}

	public void setDefault(String name) {
		this.mapRowColumnMapper = getColumnMapRowMapper(name);
		this.rowMapColumnMapper = getRowMapColumnMapper(name);
	}

	public RowMapper<Map<String, Object>> getDefaultColumnMapRowMapper() {
		return this.mapRowColumnMapper;
	}

	public Function<String, String> getDefaultRowMapColumnMapper() {
		return this.rowMapColumnMapper;
	}

	public RowMapper<Map<String, Object>> getColumnMapRowMapper(String name) {
		return columnMapRowMappers.getOrDefault(name, mapRowColumnMapper);
	}

	public Function<String, String> getRowMapColumnMapper(String name) {
		return rowMapColumnMappers.getOrDefault(name, rowMapColumnMapper);
	}
}
