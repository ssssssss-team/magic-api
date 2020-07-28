package org.ssssssss.magicapi.cache;

import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.magicapi.functions.DatabaseQuery;

import java.util.Arrays;

/**
 * SQL缓存接口
 */
public interface SqlCache {

    /**
     * 计算key
     */
    default String buildSqlCacheKey(DatabaseQuery.BoundSql boundSql) {
        return MD5Utils.encrypt(boundSql.getSql() + ":" + Arrays.toString(boundSql.getParameters()));
    }

    /**
     * 存入缓存
     * @param name 名字
     * @param key   key
     * @param value 值
     * @param ttl 有效期
     */
    void put(String name, String key, Object value, long ttl);

    /**
     * 获取缓存
     * @param name  名字
     * @param key   key
     * @return
     */
    <T> T get(String name, String key);

    /**
     * 删除缓存
     * @param name  名字
     */
    void delete(String name);

}
