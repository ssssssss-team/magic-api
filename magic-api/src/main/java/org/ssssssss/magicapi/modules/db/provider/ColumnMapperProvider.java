package org.ssssssss.magicapi.modules.db.provider;

import org.springframework.jdbc.core.ColumnMapRowMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 列名转换接口
 *
 * @author mxd
 */
public interface ColumnMapperProvider {

	/**
	 * 获取转换器名称
	 *
	 * @return 转换器名称
	 */
	String name();

	/**
	 * 转换方法
	 *
	 * @param columnName 列名
	 * @return 转换后的方法
	 */
	String mapping(String columnName);

	/**
	 * 反向转换
	 *
	 * @param name 转换后的值
	 * @return 列名
	 */
	default String unmapping(String name) {
		return name;
	}

	/**
	 * 获取 RowMapColumnMapper
	 *
	 * @return RowMapColumnMapper
	 */
	default Function<String, String> getRowMapColumnMapper() {
		return this::unmapping;
	}

	/**
	 * 获取 ColumnMapRowMapper
	 *
	 * @return ColumnMapRowMapper
	 */
	default ColumnMapRowMapper getColumnMapRowMapper() {
		return new ColumnMapRowMapper() {
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
