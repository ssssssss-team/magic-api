package org.ssssssss.magicapi.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.functions.DynamicMethod;

import java.util.*;

/**
 * redis模块
 *
 * @author mxd
 */
@MagicModule("redis")
public class RedisModule implements DynamicMethod {

	private final StringRedisTemplate redisTemplate;

	public RedisModule(RedisConnectionFactory connectionFactory) {
		this.redisTemplate = new StringRedisTemplate(connectionFactory);
	}

	/**
	 * 序列化
	 */
	private byte[] serializer(Object value) {
		if (value == null || value instanceof String) {
			return redisTemplate.getStringSerializer().serialize((String) value);
		}
		return serializer(value.toString());
	}

	/**
	 * 反序列化
	 */
	@SuppressWarnings("unchecked")
	private Object deserialize(Object value) {
		if (value != null) {
			if (value instanceof byte[]) {
				return this.redisTemplate.getStringSerializer().deserialize((byte[]) value);
			}
			if (value instanceof List) {
				List<Object> valueList = (List<Object>) value;
				List<Object> resultList = new ArrayList<>(valueList.size());
				for (Object val : valueList) {
					resultList.add(deserialize(val));
				}
				return resultList;
			}
			if(value instanceof Map){
				Map<Object, Object> map = (Map<Object, Object>) value;
				LinkedHashMap<Object, Object> newMap = new LinkedHashMap<>(map.size());
				map.forEach((key, val) -> newMap.put(deserialize(key), deserialize(val)));
				return newMap;
			}
		}
		return value;
	}

	/**
	 * 执行命令
	 *
	 * @param methodName 命令名称
	 * @param parameters 命令参数
	 */
	@Override
	public Object execute(String methodName, List<Object> parameters) {
		return this.redisTemplate.execute((RedisCallback<Object>) connection -> {
			byte[][] params = new byte[parameters.size()][];
			for (int i = 0; i < params.length; i++) {
				params[i] = serializer(parameters.get(i));
			}
			return deserialize(connection.execute(methodName, params));
		});
	}
}
