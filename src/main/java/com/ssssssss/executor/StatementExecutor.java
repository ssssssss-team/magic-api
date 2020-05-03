package com.ssssssss.executor;

import com.ssssssss.context.RequestContext;
import com.ssssssss.dialect.Dialect;
import com.ssssssss.dialect.DialectUtils;
import com.ssssssss.model.Page;
import com.ssssssss.model.PageResult;
import com.ssssssss.provider.PageProvider;
import com.ssssssss.session.SqlStatement;

import java.sql.SQLException;

/**
 * SqlStatement执行器
 */
public class StatementExecutor {

    private SqlExecutor sqlExecutor;

    /**
     * 分页提取器
     */
    private PageProvider pageProvider;

    public StatementExecutor(SqlExecutor sqlExecutor, PageProvider pageProvider) {
        this.sqlExecutor = sqlExecutor;
        this.pageProvider = pageProvider;
    }

    /**
     * 执行SqlStatement
     */
    public Object execute(SqlStatement sqlStatement, RequestContext context) throws SQLException {
        // 获取要执行的SQL
        String sql = sqlStatement.getSqlNode().getSql(context).trim();
        if (sqlStatement.isPagination()) {  //判断是否是分页语句
            // 从Request中提取Page对象
            Page page = pageProvider.getPage(context.getRequest());
            // 执行分页逻辑
            return sqlExecutor.doInConnection(connection -> {
                PageResult<Object> pageResult = new PageResult<>();
                // 获取数据库方言
                Dialect dialect = DialectUtils.getDialectFromUrl(connection.getMetaData().getURL());
                // 获取总条数
                long total = sqlExecutor.queryForOne(connection, dialect.getCountSql(sql), context.getParameters(), Long.class);
                pageResult.setTotal(total);
                // 当条数>0时，执行查询语句，否则不查询以提高性能
                if (total > 0) {
                    // 获取分页语句
                    String pageSql = dialect.getPageSql(sql, page.getOffset(), page.getLimit());
                    // 设置分页参数
                    context.addParameter(page.getLimit());
                    context.addParameter(page.getOffset());
                    // 执行查询
                    pageResult.setList(sqlExecutor.queryForList(connection, pageSql, context.getParameters(), sqlStatement.getReturnType()));
                }
                return pageResult;
            });
        } else {
            // 普通SQL执行
            return sqlExecutor.execute(sqlStatement.getSqlMode(), sql, context.getParameters(), sqlStatement.getReturnType());
        }
    }
}
