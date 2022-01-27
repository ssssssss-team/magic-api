package org.ssssssss.magicapi.core.model;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

/**
 * 属性信息
 *
 * @param <T>
 * @author mxd
 */
public class Attributes<T> {

	protected Map<String, T> properties = new HashMap<>();

	/**
	 * 设置属性
	 *
	 * @param key   key
	 * @param value value
	 */
	public void setAttribute(String key, T value) {
		properties.put(key, value);
	}

	/**
	 * 获取属性
	 *
	 * @param key key
	 */
	public Object getAttribute(String key) {
		return properties.get(key);
	}

	public Map<String, T> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, T> properties) {
		this.properties = properties;
	}
}
