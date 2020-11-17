package org.ssssssss.magicapi.functions;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.script.functions.DynamicMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * redis模块
 */
public class RedisFunctions implements MagicModule, DynamicMethod {

	@Override
	public String getModuleName() {
		return "redis";
	}

	private StringRedisTemplate redisTemplate;

	public RedisFunctions(RedisConnectionFactory connectionFactory) {
		this.redisTemplate = new StringRedisTemplate(connectionFactory);
	}

	/**
	 * 序列化
	 */
	private byte[] serializer(Object value) {
		if(value == null || value instanceof String){
			return redisTemplate.getStringSerializer().serialize((String) value);
		}
		return serializer(value.toString());
	}

	/**
	 * 反序列化
	 */
	private Object deserialize(Object value) {
		if (value != null) {
			if (value instanceof byte[]) {
				return this.redisTemplate.getStringSerializer().deserialize((byte[]) value);
			}
			if (value instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> valueList = (List<Object>) value;
				List<Object> resultList = new ArrayList<>(valueList.size());
				for (Object val : valueList) {
					resultList.add(deserialize(val));
				}
				return resultList;
			}
		}
		return value;
	}

	/**
	 * 执行命令
	 * @param methodName	命令名称
	 * @param parameters	命令参数
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
