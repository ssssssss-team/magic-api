package org.ssssssss.magicapi.dialect;


import org.ssssssss.magicapi.modules.BoundSql;

public interface Dialect {

    /**
     * 根据jdbcUrl匹配
     */
    boolean match(String jdbcUrl);

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
