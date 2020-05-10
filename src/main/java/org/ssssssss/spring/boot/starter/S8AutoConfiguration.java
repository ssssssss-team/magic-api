package org.ssssssss.spring.boot.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.executor.RequestExecutor;
import org.ssssssss.executor.SqlExecutor;
import org.ssssssss.executor.StatementExecutor;
import org.ssssssss.expression.ExpressionEngine;
import org.ssssssss.interceptor.RequestInterceptor;
import org.ssssssss.provider.KeyProvider;
import org.ssssssss.provider.PageProvider;
import org.ssssssss.provider.impl.DefaultPageProvider;
import org.ssssssss.provider.impl.UUIDKeyProvider;
import org.ssssssss.session.Configuration;
import org.ssssssss.session.DynamicDataSource;
import org.ssssssss.validator.*;

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

    @Autowired(required = false)
    private List<RequestInterceptor> requestInterceptors;

    @Autowired(required = false)
    private List<KeyProvider> keyProviders;

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
        if(this.requestInterceptors != null){
            this.requestInterceptors.forEach(requestExecutor::addRequestInterceptor);
        }
        return requestExecutor;
    }

    @Bean
    public ExpressionEngine expressionEngine() {
        return new ExpressionEngine();
    }

    @Bean
    @ConditionalOnMissingBean(DynamicDataSource.class)
    public DynamicDataSource dynamicDataSource(DataSource dataSource){
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.put(null,dataSource);
        return dynamicDataSource;
    }

    @Bean
    public StatementExecutor statementExecutor(DynamicDataSource dynamicDataSource, PageProvider pageProvider, ApplicationContext context) {
        SqlExecutor sqlExecutor = new SqlExecutor(dynamicDataSource);
        // 注册UUID生成策略
        sqlExecutor.addKeyProvider(new UUIDKeyProvider());
        if(this.keyProviders != null){
            // 注册自定义的主键生成策略
            keyProviders.forEach(sqlExecutor::addKeyProvider);
        }
        sqlExecutor.setMapUnderscoreToCamelCase(properties.isMapUnderscoreToCamelCase());
        return new StatementExecutor(sqlExecutor, pageProvider, context);
    }

    @Bean
    public Configuration configuration(StatementExecutor statementExecutor, ExpressionEngine expressionEngine, RequestExecutor requestExecutor, RequestMappingHandlerMapping requestMappingHandlerMapping) throws NoSuchMethodException {
        Configuration configuration = new Configuration();
        configuration.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
        configuration.setRequestHandler(requestExecutor);
        configuration.setXmlLocations(properties.getXmlLocations());
        configuration.setEnableRefresh(properties.isEnableRefresh());
        configuration.setThrowException(properties.isThrowException());
        configuration.setBanner(properties.isBanner());
        configuration.setRequestWithRequestBodyHandleMethod(RequestExecutor.class.getDeclaredMethod("invoke", HttpServletRequest.class, Object.class));
        configuration.setRequestHandleMethod(RequestExecutor.class.getDeclaredMethod("invoke", HttpServletRequest.class));
        statementExecutor.setConfiguration(configuration);
        requestExecutor.setConfiguration(configuration);
        requestExecutor.setExpressionEngine(expressionEngine);
        requestExecutor.setStatementExecutor(statementExecutor);
        return configuration;
    }

}
