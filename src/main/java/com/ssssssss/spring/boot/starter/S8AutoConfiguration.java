package com.ssssssss.spring.boot.starter;

import com.ssssssss.executor.RequestExecutor;
import com.ssssssss.executor.SqlExecutor;
import com.ssssssss.executor.StatementExecutor;
import com.ssssssss.expression.ExpressionEngine;
import com.ssssssss.provider.PageProvider;
import com.ssssssss.provider.impl.DefaultPageProvider;
import com.ssssssss.session.Configuration;
import com.ssssssss.validator.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.List;

@org.springframework.context.annotation.Configuration
@ConditionalOnClass({DataSource.class, RequestMappingHandlerMapping.class})
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(S8Properties.class)
public class S8AutoConfiguration {

    private S8Properties properties;

    @Autowired(required = false)
    private List<IValidator> validators;

    public S8AutoConfiguration(S8Properties properties) {
        this.properties = properties;
    }

    @ConditionalOnMissingBean(PageProvider.class)
    @Bean
    public PageProvider pageProvider() {
        PageConfig pageConfig = properties.getPageConfig();
        return new DefaultPageProvider(pageConfig.getPage(), pageConfig.getSize(), pageConfig.getDefaultPage(), pageConfig.getDefaultSize());
    }

    @Bean
    public RequestExecutor requestExecutor() {
        RequestExecutor requestExecutor = new RequestExecutor();
        // 非空验证
        requestExecutor.addValidator(new NotNullValidator());
        // 最大长度验证
        requestExecutor.addValidator(new MaxLenValidator());
        // 最小长度验证
        requestExecutor.addValidator(new MinLenValidator());
        // 正则验证
        requestExecutor.addValidator(new RegxValidator());
        if (this.validators != null) {
            this.validators.forEach(requestExecutor::addValidator);
        }
        return requestExecutor;
    }

    @Bean
    public ExpressionEngine expressionEngine() {
        return new ExpressionEngine();
    }

    @Bean
    public StatementExecutor statementExecutor(DataSource dataSource, PageProvider pageProvider) {
        SqlExecutor sqlExecutor = new SqlExecutor(dataSource);
        sqlExecutor.setMapUnderscoreToCamelCase(properties.isMapUnderscoreToCamelCase());
        return new StatementExecutor(sqlExecutor, pageProvider);
    }

    @Bean
    public Configuration configuration(StatementExecutor statementExecutor, ExpressionEngine expressionEngine, RequestExecutor requestExecutor, RequestMappingHandlerMapping requestMappingHandlerMapping) throws NoSuchMethodException {
        Configuration configuration = new Configuration();
        configuration.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
        configuration.setRequestHandler(requestExecutor);
        configuration.setXmlLocations(properties.getXmlLocations());
        configuration.setEnableRefresh(properties.isEnableRefresh());
        configuration.setBanner(properties.isBanner());
        configuration.setRequestHandleMethod(RequestExecutor.class.getDeclaredMethod("invoke", HttpServletRequest.class));
        requestExecutor.setConfiguration(configuration);
        requestExecutor.setExpressionEngine(expressionEngine);
        requestExecutor.setStatementExecutor(statementExecutor);
        return configuration;
    }

}
