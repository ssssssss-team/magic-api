package org.ssssssss.dialect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DialectUtils {

    /**
     * 缓存已解析的方言
     */
    private static Map<String, Dialect> dialectMap = new ConcurrentHashMap<>();

    /**
     * 获取数据库方言
     */
    public static Dialect getDialectFromUrl(String fromUrl) {
        Dialect dialect = dialectMap.get(fromUrl);
        if (dialect == null) {
            //判断mysql
            if (fromUrl.startsWith("jdbc:mysql:") || fromUrl.startsWith("jdbc:cobar:") || fromUrl.startsWith("jdbc:log4jdbc:mysql:")) {
                dialect = new MySqlDialect();
            }
            dialectMap.put(fromUrl, dialect);
        }
        return dialect;
    }
}
