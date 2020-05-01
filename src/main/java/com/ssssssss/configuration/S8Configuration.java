package com.ssssssss.configuration;

import com.ssssssss.executor.RequestExecutor;
import com.ssssssss.executor.SqlExecutor;
import com.ssssssss.executor.StatementExecutor;
import com.ssssssss.expression.ExpressionEngine;
import com.ssssssss.provider.PageProvider;
import com.ssssssss.provider.impl.DefaultPageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@Configuration
public class S8Configuration {


    @Bean
    public RequestExecutor requestExecutor() {
        return new RequestExecutor();
    }

    @Bean
    public ExpressionEngine expressionEngine() {
        return new ExpressionEngine();
    }

    @ConditionalOnMissingBean(PageProvider.class)
    @Bean
    public PageProvider pageProvider(){
        return new DefaultPageProvider("page","size");
    }

    @Bean
    public StatementExecutor statementExecutor(DataSource dataSource,PageProvider pageProvider) {
        return new StatementExecutor(new SqlExecutor(dataSource),pageProvider);
    }

    @Bean
    public com.ssssssss.session.Configuration configuration(StatementExecutor statementExecutor, ExpressionEngine expressionEngine, RequestExecutor requestExecutor, RequestMappingHandlerMapping requestMappingHandlerMapping) throws NoSuchMethodException {
        com.ssssssss.session.Configuration configuration = new com.ssssssss.session.Configuration();
        configuration.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
        configuration.setRequestHandler(requestExecutor);
        configuration.setXmlLocations("classpath*:ssssssss/*/**.xml");
        configuration.setEnableRefresh(true);
        configuration.setRequestHandleMethod(RequestExecutor.class.getDeclaredMethod("invoke", HttpServletRequest.class));
        requestExecutor.setConfiguration(configuration);
        requestExecutor.setExpressionEngine(expressionEngine);
        requestExecutor.setStatementExecutor(statementExecutor);
        return configuration;
    }
}
