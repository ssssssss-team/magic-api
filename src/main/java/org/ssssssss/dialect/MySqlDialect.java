package org.ssssssss.dialect;

public class MySqlDialect implements Dialect {

    @Override
    public String getPageSql(String sql, long offset, long limit) {
        return sql + " limit ?,?";
    }
}
