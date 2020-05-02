package com.ssssssss.session;

import com.ssssssss.utils.Assert;
import com.ssssssss.utils.XmlFileLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * S8配置类
 */
public class Configuration implements InitializingBean {

    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * Http请求处理器
     */
    private Object requestHandler;

    /**
     * Http请求处理方法
     */
    private Method requestHandleMethod;

    /**
     * xml位置
     */
    private String[] xmlLocations;

    /**
     * 是否自动刷新
     */
    private boolean enableRefresh;

    /**
     * 缓存已加载的SqlStatement
     */
    private Map<String,SqlStatement> statementMap = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * 根据RequestMapping获取SqlStatement对象
     */
    public SqlStatement getStatement(String requestMapping){
        return statementMap.get(requestMapping);
    }

    /**
     * 注册sql语句成接口，当已存在时，刷新其配置
     */
    public void addStatement(SqlStatement sqlStatement){
        RequestMappingInfo requestMappingInfo = getRequestMappingInfo(sqlStatement);
        // 如果已经注册过，则先取消注册
        if(statementMap.containsKey(sqlStatement.getRequestMapping())){
            logger.debug("刷新接口:{}",sqlStatement.getRequestMapping());
            // 取消注册
            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
        }else{
            logger.debug("注册接口:{}",sqlStatement.getRequestMapping());
        }
        // 添加至缓存
        statementMap.put(sqlStatement.getRequestMapping(),sqlStatement);
        // 注册接口
        requestMappingHandlerMapping.registerMapping(requestMappingInfo,requestHandler,requestHandleMethod);
    }

    /**
     * 获取RequestMappingInfo对象
     */
    private RequestMappingInfo getRequestMappingInfo(SqlStatement sqlStatement){
        String requestMapping = sqlStatement.getRequestMapping();
        Assert.isNotBlank(requestMapping,"request-mapping 不能为空！");
        RequestMappingInfo.Builder builder = RequestMappingInfo.paths(requestMapping);
        if(StringUtils.isNotBlank(sqlStatement.getRequestMethod())){
            RequestMethod requestMethod = RequestMethod.valueOf(sqlStatement.getRequestMethod().toUpperCase());
            Assert.isNotNull(requestMethod,String.format("不支持的请求方法:%s",sqlStatement.getRequestMethod()));
            builder.methods(requestMethod);
        }
        return builder.build();
    }

    public void setRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    public void setRequestHandler(Object requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void setRequestHandleMethod(Method requestHandleMethod) {
        this.requestHandleMethod = requestHandleMethod;
    }

    public void setXmlLocations(String[] xmlLocations) {
        this.xmlLocations = xmlLocations;
    }

    public void setEnableRefresh(boolean enableRefresh) {
        this.enableRefresh = enableRefresh;
    }

    @Override
    public void afterPropertiesSet() {
        if(this.xmlLocations == null){
            logger.error("ssssssss.xml-locations不能为空");
        }else{
            XmlFileLoader loader = new XmlFileLoader(xmlLocations, this);
            loader.run();
            // 如果启动刷新则定时重新加载
            if(enableRefresh){
                Executors.newScheduledThreadPool(1).scheduleAtFixedRate(loader,3,3, TimeUnit.SECONDS);
            }
        }
    }
}
