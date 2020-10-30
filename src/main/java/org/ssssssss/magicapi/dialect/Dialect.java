package org.ssssssss.magicapi.dialect;


import org.ssssssss.magicapi.functions.BoundSql;

public interface Dialect {

    /**
     * 获取查总数的sql
     */
    default String getCountSql(String sql) {
        return "select count(1) from (" + sql + ") count_";
    }

    /**
     * 获取分页sql
     */
    String getPageSql(String sql, BoundSql boundSql, long offset, long limit);
}
