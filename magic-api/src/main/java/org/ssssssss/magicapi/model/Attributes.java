package org.ssssssss.magicapi.model;

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

	public void setAttribute(String key, T value) {
		properties.put(key, value);
	}

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
