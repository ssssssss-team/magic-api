package org.ssssssss.magicapi.dialect;

import org.ssssssss.magicapi.functions.DatabaseQuery;

public class MySQLDialect implements Dialect {

    @Override
    public String getPageSql(String sql, DatabaseQuery.BoundSql boundSql, long offset, long limit) {
        boundSql.addParameter(offset);
        boundSql.addParameter(limit);
        return sql + " limit ?,?";
    }
}
