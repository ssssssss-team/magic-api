package org.ssssssss.dialect;

import org.ssssssss.context.RequestContext;

public class OracleDialect implements Dialect {

    @Override
    public String getPageSql(String sql, RequestContext context, long offset, long limit) {
        limit = (offset >= 1) ? (offset + limit) : limit;
        context.addParameter(limit);
        context.addParameter(offset);
        return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
                sql + " ) TMP WHERE ROWNUM <= ? ) WHERE ROW_ID > ?";
    }
}
