package org.ssssssss.magicapi.modules.db.dialect;


import org.ssssssss.magicapi.modules.db.BoundSql;

/**
 * Oracle方言
 *
 * @author mxd
 */
public class OracleDialect implements Dialect {

	@Override
	public boolean match(String jdbcUrl) {
		return jdbcUrl.contains(":oracle:");
	}

	@Override
	public String getPageSql(String sql, BoundSql boundSql, long offset, long limit) {
		limit = (offset >= 1) ? (offset + limit) : limit;
		boundSql.addParameter(limit);
		boundSql.addParameter(offset);
		return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
				sql + " ) TMP WHERE ROWNUM <= ? ) WHERE ROW_ID > ?";
	}
}
