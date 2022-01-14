package org.ssssssss.magicapi.modules.db.cache;

import org.ssssssss.magicapi.utils.MD5Utils;

import java.util.Arrays;

/**
 * SQL缓存接口
 *
 * @author mxd
 */
public interface SqlCache {

	/**
	 * 计算key
	 */
	default String buildSqlCacheKey(String sql, Object[] params) {
		return MD5Utils.encrypt(sql + ":" + Arrays.toString(params));
	}


	/**
	 * 存入缓存
	 *
	 * @param name  名字
	 * @param key   key
	 * @param value 值
	 */
	void put(String name, String key, Object value);

	/**
	 * 存入缓存
	 *
	 * @param name  名字
	 * @param key   key
	 * @param value 值
	 * @param ttl   有效期
	 */
	void put(String name, String key, Object value, long ttl);

	/**
	 * 获取缓存
	 *
	 * @param name 名字
	 * @param key  key
	 */
	<T> T get(String name, String key);

	/**
	 * 删除缓存
	 *
	 * @param name 名字
	 */
	void delete(String name);

}
