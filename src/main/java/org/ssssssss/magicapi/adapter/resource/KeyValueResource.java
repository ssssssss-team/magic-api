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

	public KeyValueResource(String separator, String path, KeyValueResource parent) {
		this.separator = separator;
		this.path = path;
		this.parent = parent;
	}

	@Override
	public boolean isDirectory() {
		return this.path.endsWith(separator);
	}

	@Override
	public final boolean renameTo(Resource resource) {
		if (!(resource instanceof KeyValueResource)) {
			throw new IllegalArgumentException("无法将" + this.getAbsolutePath() + "重命名为:" + resource.getAbsolutePath());
		}
		KeyValueResource targetResource = (KeyValueResource) resource;
		// 判断移动的是否是文件夹
		if (resource.isDirectory()) {
			Set<String> oldKeys = this.keys();
			Map<String,String> mappings = new HashMap<>(oldKeys.size());
			int keyLen = this.path.length();
			oldKeys.forEach(oldKey -> mappings.put(oldKey,targetResource.path + oldKey.substring(keyLen)));
			return renameTo(mappings);
		} else {
			return renameTo(Collections.singletonMap(this.path, targetResource.path));
		}
	}

	@Override
	public boolean delete() {
		return this.keys().stream().allMatch(this::deleteByKey);
	}

	protected boolean deleteByKey(String key){
		return false;
	}

	/**
	 * 需要做修改的key，原key: 新key
	 */
	protected abstract boolean renameTo(Map<String, String> renameKeys);

	@Override
	public String name() {
		String name = this.path;
		if(isDirectory()){
			name = this.path.substring(0,name.length() - 1);
		}
		int index = name.lastIndexOf(separator);
		return index > -1 ? name.substring(index + 1) : name;
	}

	@Override
	public Resource getDirectory(String name) {
		return getResource(name + separator);
	}

	@Override
	public boolean mkdir() {
		if(!isDirectory()){
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
		return write(new String(bytes, StandardCharsets.UTF_8));
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
}
