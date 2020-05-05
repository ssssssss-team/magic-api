package org.ssssssss.dialect;

import org.ssssssss.context.RequestContext;

public class SQLServerDialect implements Dialect {
    @Override
    public String getPageSql(String sql, RequestContext context, long offset, long limit) {
        context.addParameter(offset);
        context.addParameter(limit);
        return sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }
}
