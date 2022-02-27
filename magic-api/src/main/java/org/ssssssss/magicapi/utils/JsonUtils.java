package org.ssssssss.magicapi.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JSON工具包
 *
 * @author mxd
 */
public class JsonUtils {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

	static {
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Logger.class, new JsonSerializer<Logger>() {
			@Override
			public void serialize(Logger value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
				gen.writeString(value.toString());
			}
		});
		MAPPER.registerModule(simpleModule);
	}

	public static String toJsonString(Object target) {
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(target);
		} catch (JsonProcessingException e) {
			logger.error("json序列化失败", e);
			return null;
		}
	}

	public static String toJsonStringWithoutPretty(Object target) {
		try {
			return MAPPER.writeValueAsString(target);
		} catch (JsonProcessingException e) {
			logger.error("json序列化失败", e);
			return null;
		}
	}

	public static String toJsonStringWithoutLog(Object target) {
		try {
			return MAPPER.writeValueAsString(target);
		} catch (Exception e) {
			return target == null ? null : target.toString();
		}
	}

	public static <T> T readValue(String json, TypeReference<T> typeReference) {
		try {
			return MAPPER.readValue(json, typeReference);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", json, e);
			return null;
		}
	}

	public static <T> T readValue(String json, Class<T> clazz) {
		try {
			return MAPPER.readValue(json, clazz);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", json, e);
			return null;
		}
	}

	public static <T> T readValue(byte[] bytes, Class<T> clazz) {
		try {
			return MAPPER.readValue(bytes, clazz);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", new String(bytes), e);
			return null;
		}
	}

	public static <T> T readValue(byte[] bytes, JavaType javaType) {
		try {
			return MAPPER.readValue(bytes, javaType);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", new String(bytes), e);
			return null;
		}
	}

	public static byte[] toJsonBytes(Object target) {
		String json = toJsonString(target);
		return json == null ? new byte[0] : json.getBytes(StandardCharsets.UTF_8);
	}

}
