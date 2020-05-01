package com.ssssssss.dialect;

public interface Dialect {

    default String getCountSql(String sql) {
        return "select count(1) from (" + sql + ") count_";
    }

    String getPageSql(String sql, long offset, long limit);
}
