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
            if (fromUrl.startsWith("jdbc:mysql:") || fromUrl.startsWith("jdbc:cobar:") || fromUrl.startsWith("jdbc:log4jdbc:mysql:") || fromUrl.startsWith("jdbc:mariadb:")) {
                dialect = new MySQLDialect();
            } else if (fromUrl.startsWith("jdbc:oracle:") || fromUrl.startsWith("jdbc:log4jdbc:oracle:")) {
                dialect = new OracleDialect();
            } else if (fromUrl.startsWith("jdbc:sqlserver2012:")) {
                dialect = new SQLServerDialect();
            } else if (fromUrl.startsWith("jdbc:postgresql:") || fromUrl.startsWith("jdbc:log4jdbc:postgresql:")) {
                dialect = new PostgreSQLDialect();
            } else if (fromUrl.startsWith("jdbc:db2:")) {
                dialect = new DB2Dialect();
            } else {
                logger.warn(String.format("ssssssss在%s中无法获取dialect", fromUrl));
            }
            dialectMap.put(fromUrl, dialect);
        }
        return dialect;
    }
}
