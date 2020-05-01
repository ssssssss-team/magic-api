package com.ssssssss.executor;

import com.ssssssss.context.RequestContext;
import com.ssssssss.expression.ExpressionEngine;
import com.ssssssss.model.JsonBean;
import com.ssssssss.session.Configuration;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

public class RequestExecutor {

    private Configuration configuration;

    private StatementExecutor statementExecutor;

    private ExpressionEngine expressionEngine;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setStatementExecutor(StatementExecutor statementExecutor) {
        this.statementExecutor = statementExecutor;
    }

    public void setExpressionEngine(ExpressionEngine expressionEngine) {
        this.expressionEngine = expressionEngine;
    }

    /**
     * http请求入口
     */
    @ResponseBody
    public Object invoke(HttpServletRequest request) throws SQLException {
        // 创建RequestContex对象，供后续使用
        RequestContext requestContext = new RequestContext(request,expressionEngine);
        // 解析requestMapping
        String requestMapping = request.getServletPath();
        if (requestMapping.endsWith("/")) {
            requestMapping = requestMapping.substring(0, requestMapping.length() - 1);
        }
        //执行SQL
        Object value = statementExecutor.execute(configuration.getStatement(requestMapping), requestContext);
        return new JsonBean<>(value);
    }
}
