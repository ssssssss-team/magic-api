package org.ssssssss.magicapi.functions;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.ssssssss.magicapi.config.MagicModule;

/**
 * 应用模块，用于加载 spring 的 context 和配置
 *
 * @author L.cm
 */
public class ApplicationFunctions implements MagicModule {
    /**
     * Spring 应用名 prop key
     */
    private static final String SPRING_APP_NAME_KEY = "spring.application.name";
    private final ApplicationContext context;
    private final Environment environment;

    public ApplicationFunctions(ApplicationContext context) {
        this.context = context;
        this.environment = context.getEnvironment();
    }

    /**
     * 应用名称${spring.application.name}
     *
     * @return 应用名
     */
    public String getName() {
        return this.environment.getProperty(SPRING_APP_NAME_KEY);
    }

    /**
     * 获取 bean
     *
     * @param name name
     * @return Bean
     */
    public Object getBean(String name) {
        return this.context.getBean(name);
    }

    /**
     * 获取配置
     *
     * @param key key
     * @return value
     */
    public String getProp(String key) {
        return this.environment.getProperty(key);
    }

    /**
     * 获取配置
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return value
     */
    public String getProp(String key, String defaultValue) {
        return this.environment.getProperty(key, defaultValue);
    }

    @Override
    public String getModuleName() {
        return "application";
    }
}
