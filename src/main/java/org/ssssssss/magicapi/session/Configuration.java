package org.ssssssss.magicapi.session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.ssssssss.magicapi.utils.Assert;
import org.ssssssss.magicapi.utils.XmlFileLoader;

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
     * Http请求(带RequestBody)处理方法
     */
    private Method requestWithRequestBodyHandleMethod;

    /**
     * xml位置
     */
    private String[] xmlLocations;

    /**
     * 是否自动刷新
     */
    private boolean enableRefresh;

    /**
     * 是否打印banner
     */
    private boolean banner;

    /**
     * 执行出错时是否抛异常
     */
    private boolean throwException = false;

    /**
     * 缓存已加载的statement(request-mapping映射)
     */
    private Map<String, Statement> statementMappingMap = new ConcurrentHashMap<>();

    /**
     * 缓存已加载的statement（ID映射）
     */
    private Map<String, Statement> statementIdMap = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * 根据RequestMapping获取statement对象
     */
    public Statement getStatement(String requestMapping) {
        return statementMappingMap.get(requestMapping);
    }

    /**
     * 根据RequestMapping获取statement对象
     */
    public Statement getStatementById(String id) {
        return statementIdMap.get(id);
    }

    /**
     * 注册Statement成接口，当已存在时，刷新其配置
     */
    public void addStatement(Statement statement) {
        RequestMappingInfo requestMappingInfo = getRequestMappingInfo(statement);
        if (StringUtils.isNotBlank(statement.getId())) {
            // 设置ID与statement的映射
            statementIdMap.put(statement.getId(), statement);
        }
        if (requestMappingInfo == null) {
            return;
        }
        // 如果已经注册过，则先取消注册
        if (statementMappingMap.containsKey(statement.getRequestMapping())) {
            logger.debug("刷新接口:{}", statement.getRequestMapping());
            // 取消注册
            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
        }else{
            logger.debug("注册接口:{}", statement.getRequestMapping());
        }
        // 添加至缓存
        statementMappingMap.put(statement.getRequestMapping(), statement);
        // 注册接口
        requestMappingHandlerMapping.registerMapping(requestMappingInfo,requestHandler,statement.isRequestBody() ? requestWithRequestBodyHandleMethod : requestHandleMethod);
    }

    /**
     * 获取RequestMappingInfo对象
     */
    private RequestMappingInfo getRequestMappingInfo(Statement statement) {
        String requestMapping = statement.getRequestMapping();
        if (StringUtils.isBlank(requestMapping)) {
            return null;
        }
        RequestMappingInfo.Builder builder = RequestMappingInfo.paths(requestMapping);
        if (StringUtils.isNotBlank(statement.getRequestMethod())) {
            RequestMethod requestMethod = RequestMethod.valueOf(statement.getRequestMethod().toUpperCase());
            Assert.isNotNull(requestMethod, String.format("不支持的请求方法:%s", statement.getRequestMethod()));
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

    public void setBanner(boolean banner) {
        this.banner = banner;
    }

    public boolean isThrowException() {
        return throwException;
    }

    public void setRequestWithRequestBodyHandleMethod(Method requestWithRequestBodyHandleMethod) {
        this.requestWithRequestBodyHandleMethod = requestWithRequestBodyHandleMethod;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public void afterPropertiesSet() {
        if(this.banner){

            System.out.println("  __  __                _           _     ____  ___ ");
            System.out.println(" |  \\/  |  __ _   __ _ (_)  ___    / \\   |  _ \\|_ _|");
            System.out.println(" | |\\/| | / _` | / _` || | / __|  / _ \\  | |_) || | ");
            System.out.println(" | |  | || (_| || (_| || || (__  / ___ \\ |  __/ | | ");
            System.out.println(" |_|  |_| \\__,_| \\__, ||_| \\___|/_/   \\_\\|_|   |___|");
            System.out.println("                  |___/                        " + Configuration.class.getPackage().getImplementationVersion());
        }
        if(this.xmlLocations == null){
            logger.error("ssssssss.xml-locations不能为空");
        }else{
            XmlFileLoader loader = new XmlFileLoader(xmlLocations, this);
            loader.run();
            // 如果启动刷新则定时重新加载
            if(enableRefresh){
                logger.info("启动自动刷新ssssssss");
                Executors.newScheduledThreadPool(1).scheduleAtFixedRate(loader,3,3, TimeUnit.SECONDS);
            }
        }
    }
}
