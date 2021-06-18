package org.ssssssss.magicapi.dialect;


import org.ssssssss.magicapi.modules.BoundSql;

import java.sql.Connection;
import java.sql.SQLException;

public interface Dialect {

	/**
	 * 根据jdbcUrl匹配
	 */
	default boolean match(String jdbcUrl){
		return false;
	}

	/**
	 * 根据Connection匹配
	 */
	default boolean match(Connection connection) throws SQLException {
		return match(connection.getMetaData().getURL());
	}

	/**
	 * 获取查总数的sql
	 */
	default String getCountSql(String sql) {
		return "select count(1) from (" + sql + ") count_";
	}

	/**
	 * 获取分页sql
	 */
	String getPageSql(String sql, BoundSql boundSql, long offset, long limit);
}
