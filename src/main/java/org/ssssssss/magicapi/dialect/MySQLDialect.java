package org.ssssssss.magicapi.dialect;

import org.ssssssss.magicapi.context.RequestContext;

public class MySQLDialect implements Dialect {

    @Override
    public String getPageSql(String sql, RequestContext context, long offset, long limit) {
        context.addParameter(limit);
        context.addParameter(offset);
        return sql + " limit ?,?";
    }
}
