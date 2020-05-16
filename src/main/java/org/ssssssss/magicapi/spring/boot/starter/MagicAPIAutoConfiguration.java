package org.ssssssss.magicapi.spring.boot.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.executor.RequestExecutor;
import org.ssssssss.magicapi.executor.SqlExecutor;
import org.ssssssss.magicapi.executor.StatementExecutor;
import org.ssssssss.magicapi.expression.ExpressionEngine;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.provider.KeyProvider;
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.magicapi.provider.impl.DefaultPageProvider;
import org.ssssssss.magicapi.provider.impl.UUIDKeyProvider;
import org.ssssssss.magicapi.session.Configuration;
import org.ssssssss.magicapi.session.DynamicDataSource;
import org.ssssssss.magicapi.validator.*;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.List;

@org.springframework.context.annotation.Configuration
@ConditionalOnClass({DataSource.class, RequestMappingHandlerMapping.class})
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(MagicAPIProperties.class)
public class MagicAPIAutoConfiguration {

    private MagicAPIProperties properties;

    @Autowired(required = false)
    private List<IValidator> validators;

    @Autowired(required = false)
    private List<RequestInterceptor> requestInterceptors;

    @Autowired(required = false)
    private List<KeyProvider> keyProviders;

    private static Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);

    public MagicAPIAutoConfiguration(MagicAPIProperties properties) {
        this.properties = properties;
    }

    @ConditionalOnMissingBean(PageProvider.class)
    @Bean
    public PageProvider pageProvider() {
        PageConfig pageConfig = properties.getPageConfig();
        logger.info("未找到分页实现,采用默认分页实现,分页配置:(页码={},页大小={},默认首页={},默认页大小={})", pageConfig.getPage(), pageConfig.getSize(), pageConfig.getDefaultPage(), pageConfig.getDefaultSize());
        return new DefaultPageProvider(pageConfig.getPage(), pageConfig.getSize(), pageConfig.getDefaultPage(), pageConfig.getDefaultSize());
    }

    @ConditionalOnMissingBean(SqlCache.class)
    @Bean
    public SqlCache sqlCache() {
        CacheConfig cacheConfig = properties.getCacheConfig();
        return new DefaultSqlCache(cacheConfig.getCapacity(), cacheConfig.getTtl());
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
            this.validators.forEach(validator -> {
                logger.info("注册验证器：{},class:{}", validator.support(), validator.getClass());
                requestExecutor.addValidator(validator);
            });
        }
        if (this.requestInterceptors != null) {
            this.requestInterceptors.forEach(interceptor -> {
                logger.info("注册请求拦截器：{}", interceptor.getClass());
                requestExecutor.addRequestInterceptor(interceptor);
            });
        }
        return requestExecutor;
    }

    @Bean
    @ConditionalOnMissingBean(DynamicDataSource.class)
    public DynamicDataSource dynamicDataSource(DataSource dataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.put(null, dataSource);
        return dynamicDataSource;
    }

    @Bean
    public ExpressionEngine expressionEngine() {
        return new ExpressionEngine();
    }

    @Bean
    public StatementExecutor statementExecutor(DynamicDataSource dynamicDataSource, PageProvider pageProvider, SqlCache sqlCache, ApplicationContext context) {
        SqlExecutor sqlExecutor = new SqlExecutor(dynamicDataSource);
        CacheConfig cacheConfig = properties.getCacheConfig();
        logger.info("是否开启SQL缓存:{}", cacheConfig.isEnable());
        if (cacheConfig.isEnable()) {
            logger.info("SQL缓存实现:{}", sqlCache.getClass());
            if(sqlCache instanceof DefaultSqlCache){
                logger.info("SQL缓存容量:{},过期时间:{}ms",cacheConfig.getCapacity(),cacheConfig.getTtl());
            }
            sqlExecutor.setSqlCache(sqlCache);
        }
        // 注册UUID生成策略
        sqlExecutor.addKeyProvider(new UUIDKeyProvider());
        if (this.keyProviders != null) {
            // 注册自定义的主键生成策略
            keyProviders.forEach(keyProvider -> {
                logger.info("注册主键生成策略:{},class:{}", keyProvider.getName(), keyProvider.getClass());
            });
        }
        logger.info("开启驼峰命名转换:{}", properties.isMapUnderscoreToCamelCase());
        sqlExecutor.setMapUnderscoreToCamelCase(properties.isMapUnderscoreToCamelCase());
        return new StatementExecutor(sqlExecutor, pageProvider, context);
    }

    @Bean
    public Configuration configuration(StatementExecutor statementExecutor, ExpressionEngine expressionEngine, RequestExecutor requestExecutor, RequestMappingHandlerMapping requestMappingHandlerMapping) throws NoSuchMethodException {
        Configuration configuration = new Configuration();
        configuration.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
        configuration.setRequestHandler(requestExecutor);
        configuration.setXmlLocations(properties.getXmlLocations());
        logger.info("启动XML自动刷新:{}", properties.isEnableRefresh());
        configuration.setEnableRefresh(properties.isEnableRefresh());
        logger.info("启动是否抛出异常:{}", properties.isThrowException());
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
