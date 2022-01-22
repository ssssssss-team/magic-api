package org.ssssssss.magicapi.core.config;

/**
 * 缓存配置
 *
 * @author mxd
 */
public class Cache {

	/**
	 * 是否启用缓存
	 */
	private boolean enable = false;

	/**
	 * 默认缓存容量
	 */
	private int capacity = 10000;

	/**
	 * 默认过期时间,单位为毫秒，-1为不过期
	 */
	private long ttl = -1;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
