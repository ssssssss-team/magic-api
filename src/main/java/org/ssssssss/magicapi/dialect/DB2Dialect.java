package org.ssssssss.magicapi.dialect;

import org.ssssssss.script.functions.DatabaseQuery;

public class DB2Dialect implements Dialect {
    @Override
    public String getPageSql(String sql, DatabaseQuery.BoundSql boundSql, long offset, long limit) {
        boundSql.addParameter(offset + 1);
        boundSql.addParameter(offset + limit);
        return "SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( " + sql +
                " ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN ? AND ?";
    }
}
