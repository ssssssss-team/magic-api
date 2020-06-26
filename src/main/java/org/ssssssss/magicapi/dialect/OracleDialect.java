package org.ssssssss.magicapi.dialect;

import org.ssssssss.script.functions.DatabaseQuery;

public class OracleDialect implements Dialect {

    @Override
    public String getPageSql(String sql, DatabaseQuery.BoundSql boundSql, long offset, long limit) {
        limit = (offset >= 1) ? (offset + limit) : limit;
        boundSql.addParameter(limit);
        boundSql.addParameter(offset);
        return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
                sql + " ) TMP WHERE ROWNUM <= ? ) WHERE ROW_ID > ?";
    }
}
