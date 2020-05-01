package com.ssssssss.executor;

import com.ssssssss.context.RequestContext;
import com.ssssssss.dialect.Dialect;
import com.ssssssss.dialect.DialectUtils;
import com.ssssssss.model.Page;
import com.ssssssss.model.PageResult;
import com.ssssssss.provider.PageProvider;
import com.ssssssss.session.SqlStatement;

import java.sql.SQLException;

public class StatementExecutor {

    private SqlExecutor sqlExecutor;

    private PageProvider pageProvider;

    public StatementExecutor(SqlExecutor sqlExecutor, PageProvider pageProvider) {
        this.sqlExecutor = sqlExecutor;
        this.pageProvider = pageProvider;
    }

    public Object execute(SqlStatement sqlStatement, RequestContext context) throws SQLException {
        String sql = sqlStatement.getSqlNode().getSql(context).trim();
        if(sqlStatement.isPagination()){
            Page page = pageProvider.getPage(context.getRequest());
            return sqlExecutor.doInConnection(connection -> {
                PageResult<Object> pageResult = new PageResult<>();
                Dialect dialect = DialectUtils.getDialectFromUrl(connection.getMetaData().getURL());
                long total = sqlExecutor.queryForNumber(connection,dialect.getCountSql(sql),context.getParameters(),Long.class);
                pageResult.setTotal(total);
                if(total > 0){
                    String pageSql = dialect.getPageSql(sql, page.getOffset(), page.getLimit());
                    context.addParameter(page.getLimit());
                    context.addParameter(page.getOffset());
                    pageResult.setList(sqlExecutor.queryForList(connection,pageSql,context.getParameters(),sqlStatement.getReturnType()));
                }
                return pageResult;
            });
        }else{
            return sqlExecutor.execute(sqlStatement.getSqlMode(), sql, context.getParameters(), sqlStatement.getReturnType());
        }
    }
}
