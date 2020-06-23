package org.ssssssss.magicapi.spring.boot.starter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "magic-api")
public class MagicAPIProperties {

    /**
     * web页面入口
     */
    private String web;

    /**
     * 打印banner
     */
    private boolean banner = true;

    /**
     * 是否抛出异常
     */
    private boolean throwException = false;

    /**
     * 驼峰命名转换
     */
    private boolean mapUnderscoreToCamelCase = true;

    @NestedConfigurationProperty
    private PageConfig pageConfig = new PageConfig();

    @NestedConfigurationProperty
    private CacheConfig cacheConfig = new CacheConfig();

    public String getWeb() {
        if(StringUtils.isBlank(web)){
            return null;
        }
        if(web.endsWith("/**")){
            return web.substring(0,web.length() - 3);
        }
        if(web.endsWith("/*")){
            return web.substring(0,web.length() - 2);
        }
        if(web.endsWith("/")){
            return web.substring(0,web.length() - 1);
        }
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public boolean isBanner() {
        return banner;
    }

    public void setBanner(boolean banner) {
        this.banner = banner;
    }

    public PageConfig getPageConfig() {
        return pageConfig;
    }

    public void setPageConfig(PageConfig pageConfig) {
        this.pageConfig = pageConfig;
    }

    public boolean isThrowException() {
        return throwException;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }
}
