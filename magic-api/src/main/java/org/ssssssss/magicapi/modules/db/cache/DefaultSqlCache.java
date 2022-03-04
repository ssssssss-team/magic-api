package org.ssssssss.magicapi.modules.db.cache;

/**
 * 默认SQL缓存实现
 *
 * @author mxd
 */
public class DefaultSqlCache implements SqlCache {

	private final LRUCache cache;

	public DefaultSqlCache(int capacity, long expire) {
		this.cache = new LRUCache(capacity, expire);
	}

	@Override
	public void put(String name, String key, Object value) {
		cache.put(name, key, value);
	}

	@Override
	public void put(String name, String key, Object value, long ttl) {
		cache.put(name, key, value, ttl);
	}

	@Override
	public Object get(String name, String key) {
		return cache.get(name, key);
	}

	@Override
	public void delete(String name) {
		cache.delete(name);
	}


}
