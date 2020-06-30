package org.ssssssss.magicapi.dialect;

import org.ssssssss.magicapi.functions.DatabaseQuery;

public class PostgreSQLDialect implements Dialect {
    @Override
    public String getPageSql(String sql, DatabaseQuery.BoundSql boundSql, long offset, long limit) {
        boundSql.addParameter(limit);
        boundSql.addParameter(offset);
        return sql + " limit ? offset ?";
    }
}
