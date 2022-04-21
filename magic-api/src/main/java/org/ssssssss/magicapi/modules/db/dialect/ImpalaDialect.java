package org.ssssssss.magicapi.modules.db.dialect;

import org.ssssssss.magicapi.modules.db.BoundSql;

/**
 * @author YuePeng
 * date 2022/4/22 00:10
 */
public class ImpalaDialect implements Dialect {

    @Override
    public boolean match(String jdbcUrl) {
        return jdbcUrl.contains(":impala:");
    }

    @Override
    public String getPageSql(String sql, BoundSql boundSql, long offset, long limit) {
        boundSql.addParameter(limit);
        boundSql.addParameter(offset);
        return sql + " order by null limit ? offset ?";
    }
}
