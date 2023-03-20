package org.ssssssss.magicapi.redis;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPipelineException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ReflectionUtils;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.functions.DynamicMethod;
import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * redis模块
 *
 * @author mxd
 */
@MagicModule("redis")
public class RedisModule implements DynamicMethod {

	private final StringRedisTemplate redisTemplate;

	private final boolean isRedisson;

	public RedisModule(RedisConnectionFactory connectionFactory) {
		this.redisTemplate = new StringRedisTemplate(connectionFactory);
		this.isRedisson = Objects.equals("org.redisson.spring.data.connection.RedissonConnectionFactory", this.redisTemplate.getConnectionFactory().getClass().getName());
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

	private Object serializerForRedisson(Object value){
		if(value == null || JavaReflection.isPrimitiveAssignableFrom(value.getClass(), value.getClass())){
			return value;
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
			if (value instanceof Map) {
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
		return this.redisTemplate.execute(connection -> {
			Object result;
			if(isRedisson){
				result = executeForRedisson(((DefaultStringRedisConnection) connection).getDelegate(), methodName, parameters);
			} else {
				byte[][] params = new byte[parameters.size()][];
				for (int i = 0; i < params.length; i++) {
					params[i] = serializer(parameters.get(i));
				}
				result = connection.execute(methodName, params);
			}
			return deserialize(result);
		}, isRedisson || this.redisTemplate.isExposeConnection());
	}

	private Object executeForRedisson(RedisConnection connection, String command, List<Object> parameters) {
		Method[] methods = connection.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equalsIgnoreCase(command) && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == parameters.size()) {
				try {
					Object ret = this.execute(connection, method, parameters);
					if (ret instanceof String) {
						return ((String) ret).getBytes();
					}
					return ret;
				} catch (IllegalArgumentException e) {
					if (connection.isPipelined()) {
						throw new RedisPipelineException(e);
					}

					throw new InvalidDataAccessApiUsageException(e.getMessage(), e);
				}
			}
		}
		throw new UnsupportedOperationException();
	}
	private Object execute(RedisConnection connection,Method method, List<Object> parameters){
		if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0] == byte[][].class) {
			return ReflectionUtils.invokeMethod(method, connection, parameters.stream().map(this::serializer).toArray(byte[][]::new));
		} else if (parameters.size() == 0){
			return ReflectionUtils.invokeMethod(method, connection);
		}
		return ReflectionUtils.invokeMethod(method, connection, parameters.stream().map(this::serializerForRedisson).toArray());
	}

}
