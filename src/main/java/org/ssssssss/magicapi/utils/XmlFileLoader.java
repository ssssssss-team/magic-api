package org.ssssssss.magicapi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.ssssssss.magicapi.session.Configuration;
import org.ssssssss.magicapi.session.XMLStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * XML文件加载器
 */
public class XmlFileLoader implements Runnable {

    /**
     * 路径表达式
     */
    private String[] patterns;

    private Configuration configuration;

    private static Logger logger = LoggerFactory.getLogger(XmlFileLoader.class);

    /**
     * 缓存xml文件修改时间
     */
    private Map<String, Long> fileMap = new HashMap<>();

    private ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    public XmlFileLoader(String[] patterns, Configuration configuration) {
        this.patterns = patterns;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try {
            for (String pattern : this.patterns) {
                // 提取所有符合表达式的XML文件
                Resource[] resources = resourceResolver.getResources(pattern);
                for (Resource resource : resources) {
                    // 获取修改时间
                    long modifiedTime = resource.lastModified();
                    // 获取上次修改时间
                    Long lastModified = fileMap.get(resource.getDescription());
                    // 修改缓存
                    fileMap.put(resource.getDescription(), modifiedTime);
                    // 判断是否更新
                    if (lastModified == null || lastModified < modifiedTime) {
                        XMLStatement xmlStatement = MagicAPIXMLFileParser.parse(resource);
                        if (xmlStatement != null) {
                            // 注册HTTP接口
                            xmlStatement.getStatements().forEach(configuration::addStatement);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("加载XML失败", e);
        }
    }
}
