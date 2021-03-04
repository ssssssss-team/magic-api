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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class RedisResource extends KeyValueResource {

	private final StringRedisTemplate redisTemplate;

	private static final Logger logger = LoggerFactory.getLogger(RedisResource.class);

	public RedisResource(StringRedisTemplate redisTemplate, String path, String separator, boolean readonly, RedisResource parent) {
		super(separator, path, readonly, parent);
		this.redisTemplate = redisTemplate;
	}

	public RedisResource(StringRedisTemplate redisTemplate, String path, String separator, boolean readonly) {
		this(redisTemplate,path,separator,readonly,null);
	}

	@Override
	public byte[] read() {
		String value = redisTemplate.opsForValue().get(path);
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public boolean write(String content) {
		this.redisTemplate.opsForValue().set(this.path, content);
		return true;
	}

	@Override
	protected boolean renameTo(Map<String, String> renameKeys) {
		renameKeys.forEach(this.redisTemplate::rename);
		return true;
	}

	@Override
	public boolean exists() {
		return Boolean.TRUE.equals(this.redisTemplate.hasKey(this.path));
	}


	@Override
	protected boolean deleteByKey(String key) {
		return Boolean.TRUE.equals(this.redisTemplate.delete(key));
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
