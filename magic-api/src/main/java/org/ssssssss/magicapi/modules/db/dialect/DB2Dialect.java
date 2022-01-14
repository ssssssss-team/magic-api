package org.ssssssss.magicapi.modules.db.dialect;


import org.ssssssss.magicapi.modules.db.BoundSql;

/**
 * DB2方言
 *
 * @author mxd
 */
public class DB2Dialect implements Dialect {
	@Override
	public boolean match(String jdbcUrl) {
		return jdbcUrl.contains(":db2:");
	}

	@Override
	public String getPageSql(String sql, BoundSql boundSql, long offset, long limit) {
		boundSql.addParameter(offset + 1);
		boundSql.addParameter(offset + limit);
		return "SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( " + sql +
				" ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN ? AND ?";
	}
}
