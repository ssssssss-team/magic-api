package org.ssssssss.magicapi.modules.db.dialect;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.modules.db.BoundSql;

/**
 * SQLServer2005 方言
 *
 * @author mxd
 */
public class SQLServer2005Dialect implements Dialect {

	@Override
	public boolean match(String jdbcUrl) {
		return jdbcUrl.contains(":sqlserver:");
	}

	@Override
	public String getPageSql(String sql, BoundSql boundSql, long offset, long limit) {
		StringBuilder pagingBuilder = new StringBuilder();
		String orderby = getOrderByPart(sql);
		String distinctStr = "";

		String loweredString = sql.toLowerCase();
		String sqlPartString = sql;
		if (loweredString.trim().startsWith("select")) {
			int index = 6;
			if (loweredString.startsWith("select distinct")) {
				distinctStr = "DISTINCT ";
				index = 15;
			}
			sqlPartString = sqlPartString.substring(index);
		}
		pagingBuilder.append(sqlPartString);

		// if no ORDER BY is specified use fake ORDER BY field to avoid errors
		if (StringUtils.isEmpty(orderby)) {
			orderby = "ORDER BY CURRENT_TIMESTAMP";
		}

		StringBuilder result = new StringBuilder();
		result.append("WITH query AS (SELECT ")
				.append(distinctStr)
				.append("TOP 100 PERCENT ")
				.append(" ROW_NUMBER() OVER (")
				.append(orderby)
				.append(") as __row_number__, ")
				.append(pagingBuilder)
				.append(") SELECT * FROM query WHERE __row_number__ BETWEEN ? AND ?")
				.append(" ORDER BY __row_number__");
		boundSql.addParameter(offset + 1);
		boundSql.addParameter(offset + limit);
		return result.toString();
	}

	private String getOrderByPart(String sql) {
		String loweredString = sql.toLowerCase();
		int orderByIndex = loweredString.indexOf("order by");
		if (orderByIndex != -1) {
			// if we find a new "order by" then we need to ignore
			// the previous one since it was probably used for a subquery
			return sql.substring(orderByIndex);
		} else {
			return "";
		}
	}
}
