package org.ssssssss.magicapi.dialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DialectUtils {

    private static Logger logger = LoggerFactory.getLogger(DialectUtils.class);

    /**
     * 缓存已解析的方言
     */
    private static Map<String, Dialect> dialectMap = new ConcurrentHashMap<>();

    /**
     * 获取数据库方言
     */
    public static Dialect getDialectFromUrl(String fromUrl) {
        Dialect dialect = dialectMap.get(fromUrl);
        if (dialect == null && !dialectMap.containsKey(fromUrl)) {
            if (fromUrl.contains(":mysql:") || fromUrl.contains(":cobar:") || fromUrl.contains("jdbc:mariadb:") || fromUrl.contains(":clickhouse:")) {
                dialect = new MySQLDialect();
            } else if (fromUrl.contains(":oracle:")) {
                dialect = new OracleDialect();
            } else if (fromUrl.contains(":sqlserver:")) {
                dialect = new SQLServer2005Dialect();
            } else if (fromUrl.contains(":sqlserver2012:")) {
                dialect = new SQLServerDialect();
            } else if (fromUrl.contains(":postgresql:")) {
                dialect = new PostgreSQLDialect();
            } else if (fromUrl.contains(":db2:")) {
                dialect = new DB2Dialect();
            } else {
                logger.warn(String.format("magic-api在%s中无法获取dialect", fromUrl));
            }
            dialectMap.put(fromUrl, dialect);
        }
        return dialect;
    }
}
