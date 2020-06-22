package org.ssssssss.magicapi.spring.boot.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.cache.DefaultSqlCache;
import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.config.DynamicDataSource;
import org.ssssssss.magicapi.config.RequestExecutor;
import org.ssssssss.magicapi.config.RequestInterceptor;
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.magicapi.provider.impl.DefaultPageProvider;

import javax.sql.DataSource;
import java.util.List;

@org.springframework.context.annotation.Configuration
@ConditionalOnClass({DataSource.class, RequestMappingHandlerMapping.class})
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(MagicAPIProperties.class)
public class MagicAPIAutoConfiguration {

    private MagicAPIProperties properties;

    @Autowired(required = false)
    private List<RequestInterceptor> requestInterceptors;

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
}
