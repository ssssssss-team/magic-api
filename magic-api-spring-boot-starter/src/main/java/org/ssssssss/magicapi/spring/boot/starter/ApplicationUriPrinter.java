package org.ssssssss.magicapi.spring.boot.starter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 输出服务访问地址
 * @author 冰点
 * @date 2020-6-17 11:44:04
 * @since 1.1.1
 */
@Component
@ConditionalOnProperty(name = "spring.ex.config.access.url.show", havingValue = "true", matchIfMissing = true)
@Order
public class ApplicationUriPrinter implements CommandLineRunner {
    @Resource
    private ConfigurableEnvironment springEnv;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("****************************************************当前服务相关地址start****************************************************");
        String ip = "IP";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("当前服务地址获取失败");
        }
        String port = springEnv.getProperty("server.port","port");
        String path =springEnv.getProperty("server.servlet.context-path","");
        String magicWebPath =springEnv.getProperty("magic-api.web","/magic/web");
        System.out.println(
                "服务启动成功，magic-api已内置启动! Access URLs:\n\t" +
                        "接口本地地址: \t\thttp://localhost:" + port + path + "/\n\t" +
                        "接口外部访问地址: \thttp://" + ip + ":" + port + path + "/\n\t" +
                        "接口配置平台: \thttp://" + ip + ":" + port + magicWebPath + "/index.html\n"
        );
        System.out.println("****************************************************当前服务相关地址end 可通过配置关闭输出spring.ex.config.access.url.show=false****************************************************");

    }
}
