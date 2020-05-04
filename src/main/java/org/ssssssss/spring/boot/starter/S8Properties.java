package org.ssssssss.spring.boot.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "ssssssss")
public class S8Properties {

    /**
     * xml文件位置
     */
    private String[] xmlLocations;

    /**
     * 启动自动刷新
     */
    private boolean enableRefresh = true;

    /**
     * 打印banner
     */
    private boolean banner = true;

    /**
     * 驼峰命名转换
     */
    private boolean mapUnderscoreToCamelCase = true;

    @NestedConfigurationProperty
    private PageConfig pageConfig = new PageConfig();

    public String[] getXmlLocations() {
        return xmlLocations;
    }

    public void setXmlLocations(String[] xmlLocations) {
        this.xmlLocations = xmlLocations;
    }

    public boolean isEnableRefresh() {
        return enableRefresh;
    }

    public void setEnableRefresh(boolean enableRefresh) {
        this.enableRefresh = enableRefresh;
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
}
