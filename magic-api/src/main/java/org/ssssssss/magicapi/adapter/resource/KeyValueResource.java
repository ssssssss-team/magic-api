package org.ssssssss.magicapi.adapter.resource;

import org.ssssssss.magicapi.adapter.Resource;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class KeyValueResource implements Resource {

	protected String separator;

	protected String path;

	protected KeyValueResource parent;

	protected boolean readonly = false;

	public KeyValueResource(String separator, String path, KeyValueResource parent) {
		this.separator = separator;
		this.path = path;
		this.parent = parent;
	}

	public KeyValueResource(String separator, String path, boolean readonly, KeyValueResource parent) {
		this.separator = separator;
		this.path = path;
		this.parent = parent;
		this.readonly = readonly;
	}

	@Override
	public String separator() {
		return this.separator;
	}

	@Override
	public boolean isDirectory() {
		return this.path.endsWith(separator);
	}

	@Override
	public boolean readonly() {
		return this.readonly;
	}

	@Override
	public final boolean renameTo(Resource resource) {
		if (readonly()) {
			return false;
		}
		if (resource.getAbsolutePath().equalsIgnoreCase(this.getAbsolutePath())) {
			return true;
		}
		if (!(resource instanceof KeyValueResource)) {
			throw new IllegalArgumentException("无法将" + this.getAbsolutePath() + "重命名为:" + resource.getAbsolutePath());
		}
		KeyValueResource targetResource = (KeyValueResource) resource;
		// 判断移动的是否是文件夹
		if (resource.isDirectory()) {
			Set<String> oldKeys = this.keys();
			Map<String, String> mappings = new HashMap<>(oldKeys.size());
			int keyLen = this.path.length();
			oldKeys.forEach(oldKey -> mappings.put(oldKey, targetResource.path + oldKey.substring(keyLen)));
			return renameTo(mappings);
		} else {
			return renameTo(Collections.singletonMap(this.path, targetResource.path));
		}
	}

	@Override
	public boolean delete() {
		if (readonly()) {
			return false;
		}
		if (isDirectory()) {
			return this.keys().stream().allMatch(this::deleteByKey);
		}
		return deleteByKey(getAbsolutePath());
	}

	protected boolean deleteByKey(String key) {
		return false;
	}

	/**
	 * 需要做修改的key，原key: 新key
	 */
	protected abstract boolean renameTo(Map<String, String> renameKeys);

	@Override
	public String name() {
		String name = this.path;
		if (isDirectory()) {
			name = this.path.substring(0, name.length() - 1);
		}
		int index = name.lastIndexOf(separator);
		return index > -1 ? name.substring(index + 1) : name;
	}

	@Override
	public Resource getResource(String name) {
		name = (isDirectory() ? this.path : this.path + separator) + name;
		return mappedFunction().apply(name);
	}

	@Override
	public Resource getDirectory(String name) {
		return getResource(name + separator);
	}

	@Override
	public boolean mkdir() {
		if (!isDirectory()) {
			this.path += separator;
		}
		return write("this is directory");
	}

	@Override
	public Resource parent() {
		return this.parent;
	}

	@Override
	public boolean write(byte[] bytes) {
		return !readonly() && write(new String(bytes, StandardCharsets.UTF_8));
	}

	@Override
	public List<Resource> resources() {
		return keys().stream().map(mappedFunction()).collect(Collectors.toList());
	}

	protected abstract Function<String, Resource> mappedFunction();

	protected abstract Set<String> keys();

	@Override
	public List<Resource> dirs() {
		return resources().stream().filter(Resource::isDirectory).collect(Collectors.toList());
	}

	@Override
	public List<Resource> files(String suffix) {
		return resources().stream().filter(it -> it.name().endsWith(suffix)).collect(Collectors.toList());
	}

	@Override
	public String getAbsolutePath() {
		return this.path;
	}

	@Override
	public String getFilePath() {
		Resource parent = parent();
		while (parent.parent() != null){
			parent = parent.parent();
		}
		String path = this.getAbsolutePath()
				.replace(parent.getAbsolutePath(), "")
				.replace("\\","/")
				.replace(this.separator, "/");
		return path.startsWith("/") ? path.substring(1) : path;
	}
}
