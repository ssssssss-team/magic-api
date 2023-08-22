package org.ssssssss.magicapi.nebula;


import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.utils.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Nebula自动配置类
 */

@Configuration
@EnableConfigurationProperties(NebulaPoolProperties.class)
public class MagicNebulaConfiguration  implements MagicPluginConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MagicNebulaConfiguration.class);

    private NebulaPoolProperties nebulaPoolProperties;

    private final MagicAPIProperties properties;

    public MagicNebulaConfiguration(MagicAPIProperties properties, NebulaPoolProperties nebulaPoolProperties) {
        this.properties = properties;
        this.nebulaPoolProperties = nebulaPoolProperties;
    }

    /**
     * 创建nebula pool
     * @param nebulaPoolProperties
     * @return
     */
    @Bean
    public NebulaPool nebulaPool(@Autowired NebulaPoolProperties nebulaPoolProperties) {
        Session session = null;
        try {

            NebulaPoolConfig nebulaPoolConfig = buildNebulaPoolConfig(nebulaPoolProperties);
            Assert.isNotBlank(nebulaPoolProperties.getHostAddress(), "nebula.hostAddress 不能为空, 格式为 ip:port,ip:port 配置多个地址用逗号分隔");
            String[] hostAddress = nebulaPoolProperties.getHostAddress().split(",");
            List<HostAddress> addresses = Arrays.stream(hostAddress).map(address -> {
                String[] ipAndPort = address.split(":");
                Assert.isTrue(ipAndPort.length == 2, "nebula.hostAddress 格式错误, 格式为 ip:port,ip:port 配置多个地址用逗号分隔");
                return new HostAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
            }).collect(Collectors.toList());

            NebulaPool pool = new NebulaPool();
            pool.init(addresses, nebulaPoolConfig);
            session = pool.getSession(nebulaPoolProperties.getUserName(), nebulaPoolProperties.getPassword(), nebulaPoolProperties.isReconnect());
            return pool;
        } catch (Exception e) {
            logger.error("初始化nebula pool 异常", e);
            throw new RuntimeException(e);
        } finally {
            logger.info("初始化nebula pool 完成");
            Optional.ofNullable(session).ifPresent(Session::release);
        }
    }

    /**
     * 注入模块
     * @return
     */
    @Bean
    public NebulaModule nebulaModule() {
        return new NebulaModule();
    }

    @Override
    public Plugin plugin() {
        return new Plugin("Nebula");
    }


    public NebulaPoolConfig buildNebulaPoolConfig(NebulaPoolProperties nebulaPoolProperties) {

        NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
        //将nebulaPoolProperties的同名属性赋值到nebulaPoolConfig
        nebulaPoolConfig.setMinConnSize(nebulaPoolProperties.getMinConnsSize());
        nebulaPoolConfig.setSslParam(nebulaPoolProperties.getSslParam());
        nebulaPoolConfig.setWaitTime(nebulaPoolProperties.getWaitTime());
        nebulaPoolConfig.setTimeout(nebulaPoolProperties.getTimeout());
        nebulaPoolConfig.setMaxConnSize(nebulaPoolProperties.getMaxConnsSize());
        nebulaPoolConfig.setIntervalIdle(nebulaPoolProperties.getIntervalIdle());
        nebulaPoolConfig.setMinClusterHealthRate(nebulaPoolProperties.getMinClusterHealthRate());
        nebulaPoolConfig.setIdleTime(nebulaPoolProperties.getIdleTime());
        nebulaPoolConfig.setEnableSsl(nebulaPoolProperties.isEnableSsl());
        return nebulaPoolConfig;
    }
}
