package org.ssssssss.magicapi.modules.db;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 单行结果抽取
 *
 * @author mxd
 */
public class SingleRowResultSetExtractor<T> implements ResultSetExtractor<T> {


	private final boolean singleColumn;

	private final RowMapper<T> mapper;

	private final Class<T> requiredType;

	public SingleRowResultSetExtractor(RowMapper<T> mapper) {
		this(mapper, null, false);
	}

	public SingleRowResultSetExtractor(Class<T> requiredType) {
		this(null, requiredType, true);
	}

	private SingleRowResultSetExtractor(RowMapper<T> mapper, Class<T> requiredType, boolean singleColumn) {
		this.mapper = mapper;
		this.requiredType = requiredType;
		this.singleColumn = singleColumn;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T extractData(ResultSet rs) throws SQLException, DataAccessException {
		if (rs.next()) {
			if (singleColumn) {
				return (T) JdbcUtils.getResultSetValue(rs, 1, requiredType);
			}
			return mapper.mapRow(rs, 0);
		}
		return null;
	}
}
