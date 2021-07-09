package org.ssssssss.magicapi.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DataSourceInfo extends Attributes<String> implements Map<String, String> {

	public String getId() {
		return get("id");
	}

	@Override
	public int size() {
		return properties.size();
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return properties.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return properties.containsValue(value);
	}

	@Override
	public String get(Object key) {
		return properties.get(key);
	}

	@Override
	public String put(String key, String value) {
		return properties.put(key, value);
	}

	@Override
	public String remove(Object key) {
		return properties.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		properties.putAll(m);
	}

	@Override
	public void clear() {
		properties.clear();
	}

	@Override
	public Set<String> keySet() {
		return properties.keySet();
	}

	@Override
	public Collection<String> values() {
		return properties.values();
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return properties.entrySet();
	}
}
