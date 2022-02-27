package org.ssssssss.magicapi.modules.db.dialect;


import org.ssssssss.magicapi.modules.db.BoundSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * 数据库方言接口
 *
 * @author mxd
 */
public interface Dialect {

	Pattern REPLACE_ORDER_BY = Pattern.compile("order\\s+by\\s+[^,\\s]+(\\s+asc|\\s+desc)?(\\s*,\\s*[^,\\s]+(\\s+asc|\\s+desc)?)*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/**
	 * 根据jdbcUrl匹配
	 *
	 * @param jdbcUrl jdbc链接
	 * @return 是否匹配
	 */
	default boolean match(String jdbcUrl) {
		return false;
	}

	/**
	 * 根据Connection匹配
	 *
	 * @param connection jdbc连接
	 * @return 是否匹配
	 * @throws SQLException 匹配失败时抛出的异常
	 */
	default boolean match(Connection connection) throws SQLException {
		return match(connection.getMetaData().getURL());
	}

	/**
	 * 获取查总数的sql
	 *
	 * @param sql 原始SQL
	 * @return 分页 count SQL
	 */
	default String getCountSql(String sql) {
		return "select count(1) from (" + REPLACE_ORDER_BY.matcher(sql).replaceAll("") + ") count_";
	}

	/**
	 * 获取分页sql
	 *
	 * @param sql      原始SQL
	 * @param boundSql boundSql对象
	 * @param offset   跳过条数
	 * @param limit    限制条数
	 * @return 返回分页SQL
	 */
	String getPageSql(String sql, BoundSql boundSql, long offset, long limit);
}
