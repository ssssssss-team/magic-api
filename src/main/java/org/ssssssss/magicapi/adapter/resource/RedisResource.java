package org.ssssssss.magicapi.adapter.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.ssssssss.magicapi.adapter.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RedisResource extends KeyValueResource {

	private final StringRedisTemplate redisTemplate;

	private static final Logger logger = LoggerFactory.getLogger(RedisResource.class);

	private final Map<String,String> cachedContent = new ConcurrentHashMap<>();

	public RedisResource(StringRedisTemplate redisTemplate, String path, String separator, boolean readonly, RedisResource parent) {
		super(separator, path, readonly, parent);
		this.redisTemplate = redisTemplate;
	}

	public RedisResource(StringRedisTemplate redisTemplate, String path, String separator, boolean readonly) {
		this(redisTemplate,path,separator,readonly,null);
	}

	@Override
	public void readAll() {
		this.cachedContent.clear();
		List<String> keys = new ArrayList<>(keys());
		List<String> values = redisTemplate.opsForValue().multiGet(keys);
		if (values != null) {
			for (int i = 0, size = keys.size(); i < size; i++) {
				this.cachedContent.put(keys.get(i), values.get(i));
			}
		}
	}

	@Override
	public byte[] read() {
		String value = this.cachedContent.get(path);
		if(value == null){
			value = redisTemplate.opsForValue().get(path);
		}
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public boolean write(String content) {
		this.redisTemplate.opsForValue().set(this.path, content);
		this.cachedContent.put(this.path, content);
		return true;
	}

	@Override
	protected boolean renameTo(Map<String, String> renameKeys) {
		renameKeys.forEach(this.redisTemplate::rename);
		renameKeys.forEach((oldKey, newKey) -> this.cachedContent.put(newKey, this.cachedContent.remove(oldKey)));
		return true;
	}

	@Override
	public boolean exists() {
		if(this.cachedContent.containsKey(this.path)){
			return true;
		}
		return Boolean.TRUE.equals(this.redisTemplate.hasKey(this.path));
	}


	@Override
	protected boolean deleteByKey(String key) {
		if (Boolean.TRUE.equals(this.redisTemplate.delete(key))) {
			this.cachedContent.remove(key);
			return true;
		}
		return false;
	}

	@Override
	protected Function<String, Resource> mappedFunction() {
		return (it) -> new RedisResource(this.redisTemplate, it, this.separator, readonly, this);
	}

	@Override
	protected Set<String> keys() {
		Set<String> keys = this.redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
			ScanOptions options = new ScanOptions.ScanOptionsBuilder()
					.count(Long.MAX_VALUE)
					.match((isDirectory() ? this.path : (this.path + separator)) + "*")
					.build();
			Set<String> returnKeys = new HashSet<>();
			try (Cursor<byte[]> cursor = connection.scan(options)) {
				while (cursor.hasNext()) {
					returnKeys.add(new String(cursor.next()));
				}
			} catch (IOException e) {
				logger.error("扫描key出错", e);
			}
			return returnKeys;
		});
		return keys == null ? Collections.emptySet() : keys;
	}

	@Override
	public String toString() {
		return String.format("redis://%s", getAbsolutePath());
	}
}
