package org.ssssssss.magicapi.dialect;

import org.ssssssss.magicapi.context.RequestContext;

public class DB2Dialect implements Dialect {
    @Override
    public String getPageSql(String sql, RequestContext context, long offset, long limit) {
        context.addParameter(offset + 1);
        context.addParameter(offset + limit);
        return "SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( " + sql +
                " ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN ? AND ?";
    }
}
