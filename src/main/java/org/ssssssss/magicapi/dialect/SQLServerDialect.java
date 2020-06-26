package org.ssssssss.magicapi.dialect;

import org.ssssssss.script.functions.DatabaseQuery;

public class SQLServerDialect implements Dialect {
    @Override
    public String getPageSql(String sql, DatabaseQuery.BoundSql boundSql, long offset, long limit) {
        boundSql.addParameter(offset);
        boundSql.addParameter(limit);
        return sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }
}
