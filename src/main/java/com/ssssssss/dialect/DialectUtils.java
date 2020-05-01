package com.ssssssss.dialect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DialectUtils {

    private static Map<String, Dialect> dialectMap = new ConcurrentHashMap<>();

    public static Dialect getDialectFromUrl(String fromUrl) {
        Dialect dialect = dialectMap.get(fromUrl);
        if (dialect == null) {
            if (fromUrl.startsWith("jdbc:mysql:") || fromUrl.startsWith("jdbc:cobar:") || fromUrl.startsWith("jdbc:log4jdbc:mysql:")) {
                dialect = new MySqlDialect();
            }
            dialectMap.put(fromUrl, dialect);
        }
        return dialect;
    }
}
