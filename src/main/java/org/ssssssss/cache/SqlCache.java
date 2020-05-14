package org.ssssssss.cache;

import org.ssssssss.utils.MD5Utils;

import java.util.Arrays;

/**
 * SQL缓存接口
 */
public interface SqlCache {

    /**
     * 计算key
     * @param sql   sql
     * @param parameters sql参数
     */
    default String buildSqlCacheKey(String sql, Object[] parameters) {
        return MD5Utils.encrypt(sql + ":" + Arrays.toString(parameters));
    }

    /**
     * 存入缓存
     * @param name 名字
     * @param key   key
     * @param value 值
     */
    void put(String name, String key, Object value);

    /**
     * 获取缓存
     * @param name  名字
     * @param key   key
     * @return
     */
    Object get(String name,String key);

    /**
     * 删除缓存
     * @param name  名字
     */
    void delete(String name);

}
